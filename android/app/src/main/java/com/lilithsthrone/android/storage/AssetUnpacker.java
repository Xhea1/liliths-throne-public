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
 * Idempotent: it re-unpacks only when the app's version code changes.
 */
public class AssetUnpacker {

	private static final String TAG = "LTAssets";
	private static final String PREFS = "lt_assets";
	private static final String KEY_VERSION = "unpacked_version";
	private static final String ASSET_ROOT = "lt-res";

	private final Context context;

	public AssetUnpacker(Context context) {
		this.context = context.getApplicationContext();
	}

	/** @param versionCode bump-triggered re-unpack key; pass the app's versionCode. */
	public void unpackIfNeeded(int versionCode, File destRoot) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		if (prefs.getInt(KEY_VERSION, -1) == versionCode && destRoot.exists()) {
			return;
		}
		try {
			destRoot.mkdirs();
			copyDir(ASSET_ROOT, destRoot);
			prefs.edit().putInt(KEY_VERSION, versionCode).apply();
			Log.i(TAG, "Unpacked assets to " + destRoot);
		} catch (IOException e) {
			Log.e(TAG, "Failed unpacking assets", e);
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
