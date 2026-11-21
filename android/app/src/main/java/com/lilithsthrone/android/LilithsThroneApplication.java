package com.lilithsthrone.android;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.lilithsthrone.android.storage.AndroidGameStorage;
import com.lilithsthrone.android.storage.AssetUnpacker;
import com.lilithsthrone.main.GameState;
import com.lilithsthrone.utils.storage.Storage;

/**
 * Plugs the Android implementations into {@code core}'s platform hooks before any game code runs,
 * and unpacks bundled game data out of the APK's assets.
 */
public class LilithsThroneApplication extends Application {

	private static final String TAG = "LTApp";
	private static final String PREFS = "lt_settings";
	private static final String KEY_DATA_TREE = "data_tree_uri";
	/**
	 * Set once the user has answered the first-run storage question, including when they choose
	 * app-private storage (which stores no tree URI). Without this, declining a folder would
	 * re-prompt on every launch.
	 */
	private static final String KEY_SETUP_DONE = "storage_setup_done";

	/** Serialises every touch of the engine: HTTP request threads and LTTask completions alike. */
	public static final Object ENGINE_LOCK = new Object();

	/** Released once game data has been unpacked (or has failed to); the engine must not boot before. */
	public static final java.util.concurrent.CountDownLatch ASSETS_READY = new java.util.concurrent.CountDownLatch(1);
	private static volatile boolean assetsOk;

	private static AndroidGameStorage storage;

	@Override
	public void onCreate() {
		super.onCreate();

		GameState.setPlatform(new AndroidPlatform(this));

		storage = new AndroidGameStorage(this);
		Storage.set(storage);
		storage.ensureDirectoriesExist();

		// Re-attach a previously granted save folder, if the user has picked one.
		Uri granted = getGrantedDataTree(this);
		if (granted != null) {
			storage.setDataTree(granted);
			if (storage.hasDataTree()) {
				Log.i(TAG, "Reusing granted save folder: " + granted);
			} else {
				// Deleted, moved or revoked: without this the engine fails on every start with no
				// way back to the picker.
				Log.w(TAG, "Granted save folder is no longer usable; asking again: " + granted);
				storage.setDataTree(null);
				getSharedPreferences(PREFS, MODE_PRIVATE).edit()
						.remove(KEY_DATA_TREE).remove(KEY_SETUP_DONE).apply();
			}
		}

		long installStamp = 1;
		try {
			installStamp = getPackageManager().getPackageInfo(getPackageName(), 0).lastUpdateTime;
		} catch (Exception e) {
			Log.w(TAG, "Could not read install time", e);
		}
		// ~48MB of small files: off the main thread, or first launch trips the ANR watchdog.
		final long stamp = installStamp;
		new Thread(() -> {
			try {
				assetsOk = new AssetUnpacker(this).unpackIfNeeded(stamp, storage.resDir());
			} finally {
				ASSETS_READY.countDown();
			}
		}, "lt-asset-unpack").start();
	}

	public static boolean assetsOk() {
		return assetsOk;
	}

	public static AndroidGameStorage storage() {
		return storage;
	}

	public static Uri getGrantedDataTree(android.content.Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS, MODE_PRIVATE);
		String uri = prefs.getString(KEY_DATA_TREE, null);
		return uri == null ? null : Uri.parse(uri);
	}

	public static boolean isStorageSetupDone(android.content.Context context) {
		return context.getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_SETUP_DONE, false);
	}

	public static void setStorageSetupDone(android.content.Context context) {
		context.getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_SETUP_DONE, true).apply();
	}

	/** Persists the folder the user picked so later launches don't re-prompt. */
	public static void setGrantedDataTree(android.content.Context context, Uri treeUri) {
		context.getSharedPreferences(PREFS, MODE_PRIVATE)
				.edit()
				.putString(KEY_DATA_TREE, treeUri == null ? null : treeUri.toString())
				.putBoolean(KEY_SETUP_DONE, true)
				.apply();
		if (storage != null) {
			storage.setDataTree(treeUri);
		}
	}
}
