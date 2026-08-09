package com.lilithsthrone.game.settings;

/**
 * Platform-neutral replacement for {@code javafx.scene.input.KeyCode}, used so that
 * {@code core} does not depend on JavaFX. Constant names intentionally match their
 * JavaFX counterparts so that existing saved hotkey bindings (serialised via
 * {@link #name()}/{@link #valueOf(String)}) keep working unchanged.
 *
 * Full keyboard-hotkey parity (e.g. desktop's key-event handling actually driving this
 * type) is out of scope for the Android milestone this type was introduced for; this
 * enum exists so that {@code core} compiles, not to be a complete key-code mapping.
 */
public enum LTKeyCode {

	A, C, D, E, I, J, M, P, Q, S, W, Z,

	DIGIT1, DIGIT2, DIGIT3, DIGIT4, DIGIT5,
	NUMPAD0, NUMPAD1, NUMPAD2, NUMPAD3, NUMPAD4, NUMPAD5, NUMPAD6, NUMPAD7, NUMPAD8, NUMPAD9,

	F5, F9, F11,

	UP, DOWN, LEFT, RIGHT,
	KP_UP, KP_DOWN, KP_LEFT, KP_RIGHT,

	ADD("+"), SUBTRACT("-"), DIVIDE("/"), PLUS("+"), MINUS("-"),
	ALT("Alt"), CONTROL("Ctrl"), SHIFT("Shift"), CAPS("Caps"),
	AMPERSAND("&"), ASTERISK("*"), BACK_QUOTE("\""), BACK_SLASH("\\"), BACK_SPACE("Back space"),
	BRACELEFT("{"), BRACERIGHT("}"), CLOSE_BRACKET("]"), OPEN_BRACKET("["),
	COLON(":"), COMMA(","), DELETE("Delete"), DOLLAR("$"),
	END("End"), ENTER("Enter"), EQUALS("="), ESCAPE("Esc"),
	EURO_SIGN("&euro;"), EXCLAMATION_MARK("!"), GREATER(">"), LESS("<"),
	LEFT_PARENTHESIS("("), RIGHT_PARENTHESIS(")"),
	PAGE_DOWN("Page down"), PAGE_UP("Page up"), PERIOD("."), POUND("#"), POWER("Power"),
	QUOTE("'"), SEMICOLON(";"), SLASH("/"), SPACE("Space"), TAB("Tab");

	private final String displayName;

	LTKeyCode() {
		this.displayName = null;
	}

	LTKeyCode(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Best-effort human-readable name, mirroring what {@code javafx.scene.input.KeyCode#getName()}
	 * previously provided for the desktop UI. Falls back to the enum constant's name.
	 */
	public String getName() {
		return displayName != null ? displayName : name();
	}
}
