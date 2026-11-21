package com.lilithsthrone.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebResourceRequest;
import android.webkit.WebViewClient;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.lilithsthrone.android.LilithsThroneApplication;
import com.lilithsthrone.android.service.GameHttpServer;
import com.lilithsthrone.android.service.GameService;

/**
 * Hosts the WebView the game renders into, and starts the engine service behind it.
 *
 * On first launch it asks the user to pick a folder for save data via the Storage Access
 * Framework. Picking a folder that a sync tool (e.g. Syncthing) mirrors is what lets saves move
 * between this device and the desktop version -- both sides run the same core save/load code, so
 * no conversion is involved.
 */
public class MainActivity extends AppCompatActivity {

	private static final String TAG = "LTActivity";

	private WebView webView;
	private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
	/** Probe budget while the engine service starts (40 attempts, up to ~750ms each: <= ~30s). */
	private static final int MAX_LOAD_ATTEMPTS = 40;

	private final ActivityResultLauncher<Uri> pickSaveFolder =
			registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), uri -> {
				if (uri == null) {
					Log.i(TAG, "No save folder picked; falling back to private app storage");
					LilithsThroneApplication.setStorageSetupDone(this);
					startEngineAndLoad();
					return;
				}
				// Persist the grant so subsequent launches don't re-prompt.
				getContentResolver().takePersistableUriPermission(uri,
						Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				LilithsThroneApplication.setGrantedDataTree(this, uri);
				Log.i(TAG, "Save folder granted: " + uri);
				startEngineAndLoad();
			});

	/** Result is ignored: the engine runs either way, the notification is only a courtesy. */
	private final ActivityResultLauncher<String> askNotifications =
			registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
			askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS);
		}

		webView = new WebView(this);
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true); // needed by the click relay; no Java bridge is exposed
		settings.setDomStorageEnabled(true);
		// Let the player pinch-zoom to their preferred text size; the served page scales a
		// desktop-proportioned layout down to fit, which won't suit every screen or eyesight.
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				Log.i(TAG, "page loaded: " + url);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				if ("lt".equals(request.getUrl().getScheme())) { // the retry link on the error page
					startEngineAndLoad();
					return true;
				}
				return false;
			}
		});
		setContentView(webView);

		if (LilithsThroneApplication.isStorageSetupDone(this)) {
			startEngineAndLoad();
		} else {
			promptForSaveFolder();
		}
	}

	private void promptForSaveFolder() {
		new AlertDialog.Builder(this)
				.setTitle("Choose a save folder")
				.setMessage("Pick a folder for your saves and settings.\n\n"
						+ "Choosing a folder that you sync (for example with Syncthing) lets you share "
						+ "saves with the desktop version of the game.")
				.setPositiveButton("Choose folder", (d, w) -> pickSaveFolder.launch(null))
				.setNegativeButton("Use app storage", (d, w) -> {
					LilithsThroneApplication.setStorageSetupDone(this);
					startEngineAndLoad();
				})
				.setCancelable(false)
				.show();
	}

	private void startEngineAndLoad() {
		startForegroundService(new Intent(this, GameService.class));
		showLoading();
		waitForServerThenLoad();
	}

	/**
	 * The engine service starts asynchronously, so the port isn't accepting connections yet when
	 * we get here. Probe it on a background thread and only point the WebView at it once it
	 * answers -- loading eagerly just produces an ERR_CONNECTION_REFUSED error page.
	 */
	private void waitForServerThenLoad() {
		new Thread(() -> {
			try {
				LilithsThroneApplication.ASSETS_READY.await();
			} catch (InterruptedException interrupted) {
				Thread.currentThread().interrupt();
				return;
			}
			if (!LilithsThroneApplication.assetsOk()) {
				handler.post(() -> showError("Could not unpack the game data (is the device out of space?)."));
				return;
			}
			for (int attempt = 1; attempt <= MAX_LOAD_ATTEMPTS; attempt++) {
				try (java.net.Socket probe = new java.net.Socket()) {
					probe.connect(new java.net.InetSocketAddress("127.0.0.1", GameHttpServer.PORT), 500);
					Log.i(TAG, "engine reachable after " + attempt + " probe(s)");
					handler.post(this::attemptLoad);
					return;
				} catch (Exception notUpYet) {
					try {
						Thread.sleep(250);
					} catch (InterruptedException interrupted) {
						Thread.currentThread().interrupt();
						return;
					}
				}
			}
			Log.e(TAG, "Engine never became reachable after " + MAX_LOAD_ATTEMPTS + " probes");
			handler.post(() -> showError("The game engine did not start. Another app may be using port "
					+ GameHttpServer.PORT + "."));
		}, "lt-server-probe").start();
	}

	/**
	 * The engine takes a few seconds to boot, and the WebView's own error page would otherwise
	 * flash "ERR_CONNECTION_REFUSED" while we retry.
	 */
	private void showLoading() {
		webView.loadDataWithBaseURL(null,
				"<html><body style='margin:0;background:#1a1a1a;color:#bbb;font-family:sans-serif;"
				+ "display:flex;align-items:center;justify-content:center;height:100vh'>"
				+ "<div>Starting Lilith's Throne\u2026</div></body></html>",
				"text/html", "utf-8", null);
	}

	private void showError(String message) {
		webView.loadDataWithBaseURL(null,
				"<html><body style='margin:0;background:#1a1a1a;color:#bbb;font-family:sans-serif;"
				+ "display:flex;align-items:center;justify-content:center;height:100vh;text-align:center'>"
				+ "<div>" + message + "<p><a style='color:#8cf' href='lt://retry'>Retry</a></p></div></body></html>",
				"text/html", "utf-8", null);
	}

	private void attemptLoad() {
		webView.loadUrl("http://127.0.0.1:" + GameHttpServer.PORT + "/");
	}

	/**
	 * Maps the system back gesture onto the game's own back action.
	 *
	 * The engine renders its "Back"/"Resume" choice in the conventional {@code option_0} slot, so
	 * this clicks that element -- going through the same relay a tap would, rather than a parallel
	 * code path. If the screen has no usable back action, the press falls through to the system
	 * (which backgrounds the app) instead of being silently swallowed.
	 */
	@Override
	public void onBackPressed() {
		webView.evaluateJavascript(
				"(function(){var b=document.getElementById('option_0');"
				+ "if(b && !b.classList.contains('disabled')){b.click(); return 'handled';}"
				+ "return 'none';})()",
				result -> {
					if (result == null || result.contains("none")) {
						Log.d(TAG, "no in-game back action on this screen; deferring to system");
						moveTaskToBack(true);
					}
				});
	}
}
