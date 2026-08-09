package com.lilithsthrone.android.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.lilithsthrone.utils.storage.GameStorage;

/**
 * Android storage layout:
 *
 * <ul>
 *   <li><b>Bundled game data</b> ({@link #resDir()}) lives in private app storage, unpacked from
 *       APK assets by {@link AssetUnpacker}. It's read-only from the user's perspective and does
 *       not need to be shared, so plain {@code File} access is fine.</li>
 *   <li><b>User save data</b> (saves, exported characters, properties.xml) goes through the
 *       Storage Access Framework: the user picks one folder once, and the app keeps a persisted
 *       permission grant for it. This is what makes the folder syncable with the desktop version
 *       via e.g. Syncthing, without requesting broad storage permissions.</li>
 * </ul>
 *
 * Because SAF trees do not reliably map to {@code java.io.File} paths, the save-data methods are
 * the stream-oriented ones on {@link GameStorage}; {@link #dataDir()} and friends fall back to
 * private storage and exist only so that code paths still expecting a {@code File} degrade
 * safely rather than crash.
 */
public class AndroidGameStorage implements GameStorage {

	private static final String TAG = "LTStorage";

	private final Context context;
	/** SAF tree the user granted, or null until they pick one. */
	private DocumentFile dataTree;

	public AndroidGameStorage(Context context) {
		this.context = context.getApplicationContext();
	}

	/** @param treeUri a persisted ACTION_OPEN_DOCUMENT_TREE grant. */
	public void setDataTree(Uri treeUri) {
		this.dataTree = treeUri == null ? null : DocumentFile.fromTreeUri(context, treeUri);
	}

	public boolean hasDataTree() {
		return dataTree != null && dataTree.canWrite();
	}

	// ---- File-based: bundled resources (private app storage) ----

	@Override
	public File resDir() {
		return new File(context.getFilesDir(), "res");
	}

	@Override
	public File patchNotesDir() {
		return new File(resDir(), "patchNotes");
	}

	@Override
	public File modsDir() {
		return new File(resDir(), "mods");
	}

	/**
	 * Fallback only. Real save I/O goes through the stream methods below, against the
	 * user-selected SAF folder; this private-storage directory is used when no folder has been
	 * granted yet so that nothing crashes before first-run setup completes.
	 */
	@Override
	public File dataDir() {
		return new File(context.getFilesDir(), "data");
	}

	@Override
	public File savesDir() {
		return new File(dataDir(), "saves");
	}

	@Override
	public File charactersDir() {
		return new File(dataDir(), "characters");
	}

	@Override
	public File propertiesFile() {
		return new File(dataDir(), "properties.xml");
	}

	@Override
	public File errorLogFile() {
		return new File(dataDir(), "error.log");
	}

	@Override
	public boolean ensureDirectoriesExist() {
		dataDir().mkdirs();
		savesDir().mkdirs();
		charactersDir().mkdirs();
		resDir().mkdirs();
		return dataDir().exists();
	}

	// ---- Stream-based: user save data (SAF when available) ----

	@Override
	public InputStream openDataFile(String relativePath) throws IOException {
		if (dataTree == null) {
			return new FileInputStream(new File(dataDir(), relativePath));
		}
		DocumentFile file = resolve(relativePath, false);
		if (file == null) {
			throw new IOException("Not found in granted folder: " + relativePath);
		}
		InputStream in = context.getContentResolver().openInputStream(file.getUri());
		if (in == null) {
			throw new IOException("Could not open: " + relativePath);
		}
		return in;
	}

	@Override
	public OutputStream createDataFile(String relativePath) throws IOException {
		if (dataTree == null) {
			File target = new File(dataDir(), relativePath);
			File parent = target.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}
			return new FileOutputStream(target);
		}
		DocumentFile existing = resolve(relativePath, false);
		if (existing != null) {
			existing.delete(); // overwrite semantics, matching FileOutputStream
		}
		DocumentFile parent = resolveParent(relativePath, true);
		if (parent == null) {
			throw new IOException("Could not create parent folder for " + relativePath);
		}
		DocumentFile created = parent.createFile("text/xml", fileName(relativePath));
		if (created == null) {
			throw new IOException("Could not create " + relativePath);
		}
		OutputStream out = context.getContentResolver().openOutputStream(created.getUri());
		if (out == null) {
			throw new IOException("Could not open for writing: " + relativePath);
		}
		return out;
	}

	@Override
	public boolean dataFileExists(String relativePath) {
		if (dataTree == null) {
			return new File(dataDir(), relativePath).exists();
		}
		return resolve(relativePath, false) != null;
	}

	@Override
	public boolean deleteDataFile(String relativePath) {
		if (dataTree == null) {
			return new File(dataDir(), relativePath).delete();
		}
		DocumentFile file = resolve(relativePath, false);
		return file != null && file.delete();
	}

	@Override
	public List<String> listDataFiles(String subdirRelativePath) {
		if (dataTree == null) {
			String[] names = new File(dataDir(), subdirRelativePath).list();
			return names == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(names));
		}
		DocumentFile dir = resolveDir(subdirRelativePath, false);
		List<String> names = new ArrayList<>();
		if (dir != null) {
			for (DocumentFile f : dir.listFiles()) {
				if (f.getName() != null) {
					names.add(f.getName());
				}
			}
		}
		return names;
	}

	// ---- SAF path helpers ----

	private static String fileName(String relativePath) {
		int slash = relativePath.lastIndexOf('/');
		return slash < 0 ? relativePath : relativePath.substring(slash + 1);
	}

	private DocumentFile resolve(String relativePath, boolean createDirs) {
		DocumentFile parent = resolveParent(relativePath, createDirs);
		return parent == null ? null : parent.findFile(fileName(relativePath));
	}

	private DocumentFile resolveParent(String relativePath, boolean createDirs) {
		int slash = relativePath.lastIndexOf('/');
		if (slash < 0) {
			return dataTree;
		}
		return resolveDir(relativePath.substring(0, slash), createDirs);
	}

	/** Walks (optionally creating) a '/'-separated subdirectory chain under the granted tree. */
	private DocumentFile resolveDir(String relativeDir, boolean create) {
		DocumentFile current = dataTree;
		if (current == null || relativeDir.isEmpty()) {
			return current;
		}
		for (String segment : relativeDir.split("/")) {
			if (segment.isEmpty()) {
				continue;
			}
			DocumentFile next = current.findFile(segment);
			if (next == null || !next.isDirectory()) {
				if (!create) {
					return null;
				}
				next = current.createDirectory(segment);
				if (next == null) {
					Log.w(TAG, "Could not create folder: " + segment);
					return null;
				}
			}
			current = next;
		}
		return current;
	}
}
