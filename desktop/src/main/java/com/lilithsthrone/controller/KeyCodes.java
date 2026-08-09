package com.lilithsthrone.controller;

import com.lilithsthrone.game.settings.KeyCodeWithModifiers;
import com.lilithsthrone.game.settings.LTKeyCode;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Converts JavaFX key events into the platform-neutral {@link LTKeyCode} that {@code core}
 * stores hotkey bindings against. Lives on the desktop side because it is the JavaFX
 * boundary; {@code LTKeyCode}'s constant names deliberately mirror JavaFX's so the mapping
 * is a straight name lookup.
 */
public class KeyCodes {

	/** @return the matching {@link LTKeyCode}, or null if core has no constant for this key. */
	public static LTKeyCode toLT(KeyCode code) {
		if (code == null) {
			return null;
		}
		try {
			return LTKeyCode.valueOf(code.name());
		} catch (IllegalArgumentException e) {
			// LTKeyCode only covers the subset of keys the game actually binds.
			return null;
		}
	}

	/** Bridges a JavaFX key event to {@link KeyCodeWithModifiers#matches}. */
	public static boolean matches(KeyCodeWithModifiers binding, KeyEvent event) {
		LTKeyCode code = toLT(event.getCode());
		return code != null && binding.matches(code, event.isControlDown(), event.isShiftDown());
	}
}
