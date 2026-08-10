package com.lilithsthrone.controller;

import com.lilithsthrone.main.GameState;

/**
 * Reads the current value of a text field on the rendered screen.
 *
 * Screens that ask the player to type something (character names, save names, outfit and tattoo
 * names, enchantment output names) emit an {@code <input>} or {@code <textarea>} and later need
 * its value. Desktop used to do that by injecting JavaScript to copy the value into a hidden
 * element and then reading that element back out of the live DOM -- which only works if the
 * front-end *has* a live DOM it can script.
 *
 * Asking the front-end for the value directly works everywhere: desktop reads it from its
 * WebEngine, and an HTTP front-end reads it from the field values posted alongside the click.
 */
public class UiInput {

	private UiInput() {
	}

	/**
	 * @return the field's current text, or an empty string if the front-end can't supply it (no
	 *         such field on screen, or no front-end attached). Never null, since every caller
	 *         immediately does length/regex checks on the result.
	 */
	public static String get(String id) {
		if (GameState.getRenderSink() == null) {
			return "";
		}
		String value = GameState.getRenderSink().getInputValue(id);
		return value == null ? "" : value;
	}
}
