package com.lilithsthrone.utils.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The original desktop behaviour: "data/" and "res/" resolved relative to the process's
 * working directory, i.e. sitting next to the jar. Kept in {@code core} (rather than the
 * desktop module) so that {@code core} is runnable/testable standalone without a UI.
 */
public class DefaultGameStorage implements GameStorage {

	private final File dataDir;
	private final File resDir;

	public DefaultGameStorage() {
		this(new File("data"), new File("res"));
	}

	public DefaultGameStorage(File dataDir, File resDir) {
		this.dataDir = dataDir;
		this.resDir = resDir;
	}

	@Override
	public File dataDir() {
		return dataDir;
	}

	@Override
	public File savesDir() {
		return new File(dataDir, "saves");
	}

	@Override
	public File charactersDir() {
		return new File(dataDir, "characters");
	}

	@Override
	public File resDir() {
		return resDir;
	}

	@Override
	public File patchNotesDir() {
		return new File(resDir, "patchNotes");
	}

	@Override
	public File modsDir() {
		return new File(resDir, "mods");
	}

	@Override
	public File propertiesFile() {
		return new File(dataDir, "properties.xml");
	}

	@Override
	public File errorLogFile() {
		return new File(dataDir, "error.log");
	}

	@Override
	public boolean ensureDirectoriesExist() {
		return dataDir.mkdirs() | savesDir().mkdirs() | charactersDir().mkdirs() || dataDir.exists();
	}

	@Override
	public InputStream openDataFile(String relativePath) throws IOException {
		return new FileInputStream(new File(dataDir, relativePath));
	}

	@Override
	public OutputStream createDataFile(String relativePath) throws IOException {
		File target = new File(dataDir, relativePath);
		File parent = target.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		return new FileOutputStream(target);
	}

	@Override
	public boolean dataFileExists(String relativePath) {
		return new File(dataDir, relativePath).exists();
	}

	@Override
	public boolean deleteDataFile(String relativePath) {
		return new File(dataDir, relativePath).delete();
	}

	@Override
	public List<String> listDataFiles(String subdirRelativePath) {
		File dir = new File(dataDir, subdirRelativePath);
		String[] names = dir.list();
		return names == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(names));
	}
}
