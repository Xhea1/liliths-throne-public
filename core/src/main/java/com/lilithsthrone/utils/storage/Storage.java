package com.lilithsthrone.utils.storage;

/**
 * Static holder for the process's active {@link GameStorage}. Each platform's bootstrap sets
 * this once before any game code runs (desktop: {@code new DefaultGameStorage()}; Android:
 * its SAF-backed implementation). Defaults to the desktop behaviour so that {@code core}
 * remains usable standalone without explicit setup.
 */
public class Storage {

	private static GameStorage instance = new DefaultGameStorage();

	public static GameStorage get() {
		return instance;
	}

	public static void set(GameStorage storage) {
		instance = storage;
	}

	/**
	 * Resolves a path against the game data directory.
	 *
	 * Accepts the legacy working-directory-relative form the codebase is written in, with or
	 * without the leading {@code "data/"} -- so {@code data("saves/x.xml")} and
	 * {@code data("data/saves/x.xml")} are equivalent. That keeps the many existing call sites
	 * a mechanical substitution rather than a rewrite.
	 */
	public static java.io.File data(String relativePath) {
		return new java.io.File(get().dataDir(), strip(relativePath, "data"));
	}

	/** As {@link #data}, but against the bundled resource directory and a leading {@code "res/"}. */
	public static java.io.File res(String relativePath) {
		return new java.io.File(get().resDir(), strip(relativePath, "res"));
	}

	private static String strip(String path, String prefix) {
		String p = path.replace('\\', '/');
		while (p.startsWith("./")) {
			p = p.substring(2);
		}
		if (p.equals(prefix)) {
			return "";
		}
		if (p.startsWith(prefix + "/")) {
			return p.substring(prefix.length() + 1);
		}
		return p;
	}
}
