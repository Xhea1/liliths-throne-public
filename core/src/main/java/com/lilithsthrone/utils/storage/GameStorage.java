package com.lilithsthrone.utils.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Abstracts where the game reads/writes its data, so that {@code core} doesn't assume a
 * writable "data/"/"res/" folder next to a jar (true on desktop, meaningless on Android).
 *
 * Two families of methods:
 * <ul>
 *   <li>{@code File}-returning methods ({@link #dataDir()}, {@link #resDir()}, etc.) -- trivial
 *       on desktop (today's relative paths, unchanged), used for bundled/read-mostly resources
 *       on Android too (unpacked from assets into private app storage, a real filesystem path).</li>
 *   <li>Stream-oriented methods ({@link #openDataFile}, {@link #createDataFile}, etc.) for the
 *       actual save/character/properties I/O -- desktop implements these trivially via
 *       {@code FileInputStream}/{@code FileOutputStream} against {@link #dataDir()}; Android's
 *       implementation (introduced separately, alongside its Storage-Access-Framework-backed
 *       synced folder) goes through {@code DocumentFile}/{@code ContentResolver} instead, since
 *       SAF trees don't reliably map to {@code java.io.File} paths. Desktop code may keep using
 *       {@code new File(storage.dataDir(), "saves/" + name + ".xml")} directly where that's more
 *       convenient -- these methods exist for the paths that need to also work on Android.</li>
 * </ul>
 */
public interface GameStorage {

	/** Save games, character exports, properties.xml, error.log -- user-generated/mutable state. */
	File dataDir();

	File savesDir();

	File charactersDir();

	/** Bundled read-mostly game data (patch notes, mods, root-level "res/" data folder). */
	File resDir();

	File patchNotesDir();

	File modsDir();

	File propertiesFile();

	File errorLogFile();

	boolean ensureDirectoriesExist();

	InputStream openDataFile(String relativePath) throws IOException;

	OutputStream createDataFile(String relativePath) throws IOException;

	boolean dataFileExists(String relativePath);

	boolean deleteDataFile(String relativePath);

	/** File names (not full paths) of files directly inside a data subdirectory, e.g. "saves". */
	List<String> listDataFiles(String subdirRelativePath);
}
