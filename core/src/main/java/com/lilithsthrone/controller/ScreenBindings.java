package com.lilithsthrone.controller;

import com.lilithsthrone.game.dialogue.DialogueNode;
import com.lilithsthrone.game.dialogue.utils.OptionsDialogue;
import com.lilithsthrone.main.GameState;

/**
 * Registers the {@link Ui} bindings a screen needs, based on which dialogue node is showing.
 *
 * Desktop had this as a long if/else chain inside its own controller, which meant only the JavaFX
 * front-end knew which screen wanted which handlers. Screens whose controllers have moved into
 * core are dispatched here so every front-end gets them.
 *
 * Screens whose controllers still live in the desktop module are not listed yet -- desktop keeps
 * its own branches for those, and they move here as each controller is migrated. Nothing may be
 * registered in both places: desktop attaches a DOM listener per binding, so a screen handled
 * twice would fire its actions twice.
 */
public class ScreenBindings {

	private ScreenBindings() {
	}

	/** @return true if this node's bindings are owned by core (so a front-end should not re-register). */
	public static boolean registerFor(DialogueNode node) {
		if (node == null) {
			return false;
		}

		if (node.equals(OptionsDialogue.AGE_PREFERENCE)) {
			OptionsController.initAgeListeners();
		} else if (node.equals(OptionsDialogue.BODIES)) {
			OptionsController.initBodiesListeners();
		} else if (node.equals(OptionsDialogue.GAMEPLAY)) {
			OptionsController.initGameplayListeners();
		} else if (node.equals(OptionsDialogue.GENDER_PREFERENCE)) {
			OptionsController.initGenderListeners();
		} else if (node.equals(OptionsDialogue.FETISH_PREFERENCE)) {
			OptionsController.initFetishListeners();
		} else if (node.equals(OptionsDialogue.FURRY_PREFERENCE)) {
			OptionsController.initFurryListeners();
		} else if (node.equals(OptionsDialogue.KEYBINDS)) {
			OptionsController.initKeybindListeners();
		} else if (node.equals(OptionsDialogue.MISCELLANEOUS)) {
			OptionsController.initMiscellaneousListeners();
		} else if (node.equals(OptionsDialogue.ORIENTATION_PREFERENCE)) {
			OptionsController.initOrientationListeners();
		} else if (node.equals(OptionsDialogue.SEX)) {
			OptionsController.initSexListeners();
		} else if (node.equals(OptionsDialogue.UNIT_PREFERENCE)) {
			OptionsController.initUnitListeners();
		} else {
			return false;
		}

		GameState.getRenderSink().bindingsChanged(Panel.MAIN);
		return true;
	}
}
