package com.lilithsthrone.controller;

import com.lilithsthrone.game.PropertyValue;
import com.lilithsthrone.game.settings.KeyboardAction;
import com.lilithsthrone.main.GameState;
import com.lilithsthrone.utils.Units;

/**
 * State for the "press a key to rebind it" flow on the keybinds screen: which action the player is
 * currently rebinding, and whether they picked the primary or secondary slot.
 *
 * Lives in core because the *screen* is shared -- the buttons that arm a rebind are ordinary
 * elements handled by {@link OptionsController}. Only the part that observes an actual keypress is
 * front-end specific, and that stays on the desktop side; a front-end with no keyboard simply
 * never completes the flow.
 */
public class Keybinds {

	private static KeyboardAction actionToBind;
	private static boolean primaryBinding = true;

	private Keybinds() {
	}

	public static KeyboardAction getActionToBind() {
		return actionToBind;
	}

	public static boolean isPrimaryBinding() {
		return primaryBinding;
	}

	/** Arms the rebind: the next key the front-end observes is assigned to this action. */
	public static void arm(KeyboardAction action, boolean primary) {
		actionToBind = action;
		primaryBinding = primary;
	}

	public static void clear() {
		actionToBind = null;
		primaryBinding = true;
	}

	/**
	 * Changing a unit preference by hand means the player no longer wants units chosen from the
	 * system locale, so stop overriding their choice on the next format.
	 */
	public static void overrideAutoLocale() {
		if (GameState.getProperties().hasValue(PropertyValue.autoLocale)) {
			GameState.getProperties().setValue(PropertyValue.autoLocale, false);
			Units.FORMATTER.updateNumberFormat(false);
		}
	}
}
