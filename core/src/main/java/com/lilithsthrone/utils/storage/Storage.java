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
}
