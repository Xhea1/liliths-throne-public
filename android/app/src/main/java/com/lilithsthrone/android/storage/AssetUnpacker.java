package com.lilithsthrone.android.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * Copies the bundled {@code assets/lt-res/} tree into private app storage.
 *
 * Needed because {@code java.io.File} cannot read directly out of an APK's compressed assets,
 * and {@code core} loads its runtime data (dialogue XML, patch notes, ...) through {@code File}.
 * Idempotent: it re-unpacks only when the app has been reinstalled or updated.
 */
public class AssetUnpacker {

	private static final String TAG = "LTAssets";
	private static final String PREFS = "lt_assets";
	private static final String KEY_STAMP = "unpacked_stamp";
	private static final String ASSET_ROOT = "lt-res";

	private final Context context;

	public AssetUnpacker(Context context) {
		this.context = context.getApplicationContext();
	}

	/**
	 * @param installStamp changes whenever the APK is reinstalled or updated (versionCode does not:
	 *        it is never bumped during development), so updated game data is always re-unpacked.
	 * @return false if unpacking failed; the stamp is then not recorded, so the next launch retries.
	 */
	public boolean unpackIfNeeded(long installStamp, File destRoot) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		if (prefs.getLong(KEY_STAMP, -1) == installStamp && destRoot.exists()) {
			return true;
		}
		try {
			destRoot.mkdirs();
			copyDir(ASSET_ROOT, destRoot);
			prefs.edit().putLong(KEY_STAMP, installStamp).apply();
			Log.i(TAG, "Unpacked assets to " + destRoot);
			return true;
		} catch (IOException e) {
			Log.e(TAG, "Failed unpacking assets", e);
			return false;
		}
	}

	private void copyDir(String assetPath, File dest) throws IOException {
		AssetManager assets = context.getAssets();
		String[] children = assets.list(assetPath);
		if (children == null || children.length == 0) {
			copyFile(assetPath, dest);
			return;
		}
		dest.mkdirs();
		for (String child : children) {
			copyDir(assetPath + "/" + child, new File(dest, child));
		}
	}

	private void copyFile(String assetPath, File dest) throws IOException {
		File parent = dest.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		try (InputStream in = context.getAssets().open(assetPath);
			 OutputStream out = new FileOutputStream(dest)) {
			byte[] buf = new byte[16 * 1024];
			int read;
			while ((read = in.read(buf)) != -1) {
				out.write(buf, 0, read);
			}
		}
	}
}
