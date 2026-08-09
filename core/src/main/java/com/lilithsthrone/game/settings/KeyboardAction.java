package com.lilithsthrone.game.settings;

/**
 * @since 0.1.61
 * @version 0.3.1
 * @author Innoxia
 */
public enum KeyboardAction {

	MENU("Menu", new KeyCodeWithModifiers(LTKeyCode.ESCAPE), null),

	FULL_SCREEN("Fullscreen Toggle", new KeyCodeWithModifiers(LTKeyCode.F11), null),
	
	MOVE_NORTH("Move North", new KeyCodeWithModifiers(LTKeyCode.W), new KeyCodeWithModifiers(LTKeyCode.UP)),
	MOVE_WEST("Move West", new KeyCodeWithModifiers(LTKeyCode.A), new KeyCodeWithModifiers(LTKeyCode.LEFT)),
	MOVE_SOUTH("Move South", new KeyCodeWithModifiers(LTKeyCode.S), new KeyCodeWithModifiers(LTKeyCode.DOWN)),
	MOVE_EAST("Move East", new KeyCodeWithModifiers(LTKeyCode.D), new KeyCodeWithModifiers(LTKeyCode.RIGHT)),
	
	MOVE_RESPONSE_CURSOR_NORTH("Response Cursor Up", new KeyCodeWithModifiers(LTKeyCode.W, false, true), new KeyCodeWithModifiers(LTKeyCode.UP, false, true)),
	MOVE_RESPONSE_CURSOR_WEST("Response Cursor Left", new KeyCodeWithModifiers(LTKeyCode.A, false, true), new KeyCodeWithModifiers(LTKeyCode.LEFT, false, true)),
	MOVE_RESPONSE_CURSOR_SOUTH("Response Cursor Down", new KeyCodeWithModifiers(LTKeyCode.S, false, true), new KeyCodeWithModifiers(LTKeyCode.DOWN, false, true)),
	MOVE_RESPONSE_CURSOR_EAST("Response Cursor Right", new KeyCodeWithModifiers(LTKeyCode.D, false, true), new KeyCodeWithModifiers(LTKeyCode.RIGHT, false, true)),

	QUICKSAVE("Quicksave", new KeyCodeWithModifiers(LTKeyCode.F5), null),
	QUICKLOAD("Quickload", new KeyCodeWithModifiers(LTKeyCode.F9), null),

	MENU_SELECT("Select", new KeyCodeWithModifiers(LTKeyCode.SPACE), new KeyCodeWithModifiers(LTKeyCode.ENTER)),
	INVENTORY("Inventory", new KeyCodeWithModifiers(LTKeyCode.I), new KeyCodeWithModifiers(LTKeyCode.TAB)),
	JOURNAL("Phone", new KeyCodeWithModifiers(LTKeyCode.P), new KeyCodeWithModifiers(LTKeyCode.J)),
	MAP("Map", new KeyCodeWithModifiers(LTKeyCode.M), null),
	CHARACTERS("Characters", new KeyCodeWithModifiers(LTKeyCode.C), null),
	ZOOM("Zoom", new KeyCodeWithModifiers(LTKeyCode.Z), null),

	SCROLL_UP("Scroll Up", new KeyCodeWithModifiers(LTKeyCode.PAGE_UP), null),
	SCROLL_DOWN("Scroll Down", new KeyCodeWithModifiers(LTKeyCode.PAGE_DOWN), null),

	RESPOND_1("Response 1", new KeyCodeWithModifiers(LTKeyCode.DIGIT1), null),
	RESPOND_2("Response 2", new KeyCodeWithModifiers(LTKeyCode.DIGIT2), null),
	RESPOND_3("Response 3", new KeyCodeWithModifiers(LTKeyCode.DIGIT3), null),
	RESPOND_4("Response 4", new KeyCodeWithModifiers(LTKeyCode.DIGIT4), null),
	RESPOND_5("Response 5", new KeyCodeWithModifiers(LTKeyCode.DIGIT5), null),
	RESPOND_6("Response 6", new KeyCodeWithModifiers(LTKeyCode.DIGIT1, false, true), null),
	RESPOND_7("Response 7", new KeyCodeWithModifiers(LTKeyCode.DIGIT2, false, true), null),
	RESPOND_8("Response 8", new KeyCodeWithModifiers(LTKeyCode.DIGIT3, false, true), null),
	RESPOND_9("Response 9", new KeyCodeWithModifiers(LTKeyCode.DIGIT4, false, true), null),
	RESPOND_10("Response 10", new KeyCodeWithModifiers(LTKeyCode.DIGIT5, false, true), null),
	RESPOND_11("Response 11", new KeyCodeWithModifiers(LTKeyCode.DIGIT1, true, false), null),
	RESPOND_12("Response 12", new KeyCodeWithModifiers(LTKeyCode.DIGIT2, true, false), null),
	RESPOND_13("Response 13", new KeyCodeWithModifiers(LTKeyCode.DIGIT3, true, false), null),
	RESPOND_14("Response 14", new KeyCodeWithModifiers(LTKeyCode.DIGIT4, true, false), null),
	RESPOND_0("Response 15", new KeyCodeWithModifiers(LTKeyCode.DIGIT5, true, false), new KeyCodeWithModifiers(LTKeyCode.BACK_SPACE)),

	RESPOND_NEXT_TAB("Next Response Tab", new KeyCodeWithModifiers(LTKeyCode.E, false, true), null),
	RESPOND_PREVIOUS_TAB("Previous Response Tab", new KeyCodeWithModifiers(LTKeyCode.Q, false, true), null),
	
	RESPOND_NEXT_PAGE("Next Response Page", new KeyCodeWithModifiers(LTKeyCode.E), null),
	RESPOND_PREVIOUS_PAGE("Previous Response Page", new KeyCodeWithModifiers(LTKeyCode.Q), null);

	private String name;
	private KeyCodeWithModifiers primaryDefault, secondaryDefault;

	private KeyboardAction(String name, KeyCodeWithModifiers primaryDefault, KeyCodeWithModifiers secondaryDefault) {
		this.name = name;
		this.primaryDefault = primaryDefault;
		this.secondaryDefault = secondaryDefault;
	}

	public String getName() {
		return name;
	}

	public KeyCodeWithModifiers getPrimaryDefault() {
		return primaryDefault;
	}

	public KeyCodeWithModifiers getSecondaryDefault() {
		return secondaryDefault;
	}
}