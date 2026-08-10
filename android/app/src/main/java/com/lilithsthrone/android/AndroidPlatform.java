package com.lilithsthrone.android;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import com.lilithsthrone.main.Platform;

/**
 * Android implementation of the platform hooks {@code core} needs. Counterpart to the desktop
 * implementation on {@code Main}.
 */
public class AndroidPlatform implements Platform {

	private static final String TAG = "LTPlatform";

	private final Context context;

	public AndroidPlatform(Context context) {
		this.context = context.getApplicationContext();
	}

	@Override
	public int getScreenHeightPx() {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return metrics.heightPixels;
	}

	@Override
	public int getWindowWidthPx() {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	/**
	 * False: on Android the user neither picks a JVM nor manages a window, so advice about
	 * which Java version to launch with (or using the .exe build, or pressing Windows+Up to
	 * maximise) would be nonsense here.
	 */
	@Override
	public boolean showsDesktopLauncherHints() {
		return false;
	}

	@Override
	public boolean isRunningFromTempDir() {
		return false;
	}

	@Override
	public void openLink(String url) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} catch (Exception e) {
			Log.w(TAG, "Could not open link: " + url, e);
		}
	}

	@Override
	public void exitApplication() {
		// Stopping the engine service is handled by the Activity's lifecycle; there is no
		// desktop-style "quit the process" concept worth forcing on Android.
		Log.i(TAG, "exitApplication() requested");
	}

	@Override
	public void refreshTitle() {
		// No window title on Android.
	}

	/**
	 * Runs inline: the engine owns its own thread and the WebView pulls rendered HTML over HTTP,
	 * so there is no UI thread that game callbacks must be marshalled onto.
	 */
	@Override
	public void runOnUiThread(Runnable task) {
		task.run();
	}

	@Override
	public void copyToClipboard(String text) {
		android.content.ClipboardManager clipboard =
				(android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		if (clipboard != null) {
			clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Lilith's Throne", text));
		}
	}

	@Override
	public LoadedImage loadImageAsDataUri(File file, int maxWidth, int maxHeight) {
		try {
			BitmapFactory.Options bounds = new BitmapFactory.Options();
			bounds.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
			int originalWidth = bounds.outWidth;
			int originalHeight = bounds.outHeight;
			if (originalWidth <= 0 || originalHeight <= 0) {
				return null;
			}

			// Decode at a power-of-two downsample first (cheap), then scale exactly.
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = Math.max(1, Math.min(originalWidth / maxWidth, originalHeight / maxHeight));
			Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
			if (bitmap == null) {
				return null;
			}

			float scale = Math.min((float) maxWidth / bitmap.getWidth(), (float) maxHeight / bitmap.getHeight());
			if (scale < 1) {
				Bitmap scaled = Bitmap.createScaledBitmap(bitmap,
						Math.round(bitmap.getWidth() * scale), Math.round(bitmap.getHeight() * scale), true);
				if (scaled != bitmap) {
					bitmap.recycle();
				}
				bitmap = scaled;
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			bitmap.recycle();

			String dataUri = "data:image/png;base64," + Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
			return new LoadedImage(dataUri, w, h, originalWidth, originalHeight);
		} catch (Exception e) {
			Log.w(TAG, "Could not load image: " + file, e);
			return null;
		}
	}
}
