package com.lilithsthrone.controller;

import com.lilithsthrone.game.settings.KeyCodeWithModifiers;
import com.lilithsthrone.game.settings.KeyboardAction;
import com.lilithsthrone.main.GameState;
import com.lilithsthrone.rendering.InformationTooltip;

/**
 * The persistent button strips either side of the response box: the five screen buttons on the
 * left (menu, inventory, phone, characters present, map zoom) and the four file/utility buttons on
 * the right (quick save, quick load, export character, copy dialogue).
 *
 * Registered through {@link Ui} so every front-end gets the same behaviour. Previously this lived
 * in desktop's {@code MainController.manageButtonLeftListeners}/{@code manageButtonRightListeners}
 * as direct DOM listener attachment, which meant the buttons simply did nothing on any other
 * front-end.
 */
public class ChromeButtons {

	private ChromeButtons() {
	}

	/** Appends the primary hotkey to a tooltip title, matching the desktop wording. */
	private static String withHotkey(String label, KeyboardAction action) {
		KeyCodeWithModifiers hotKey = GameState.getProperties().hotkeyMapPrimary.get(action);
		return label + (hotKey == null ? "" : " (" + hotKey.getFullName() + ")");
	}

	public static void registerLeft() {
		Ui.clear(Panel.BUTTONS_LEFT);

		Ui.bind(Panel.BUTTONS_LEFT, "mainMenu", Screens::openOptions, true);
		Ui.bindTooltip(Panel.BUTTONS_LEFT, "mainMenu",
				new InformationTooltip().setInformation(withHotkey("Main Menu", KeyboardAction.MENU), ""));

		Ui.bind(Panel.BUTTONS_LEFT, "inventory", Screens::openInventory, true);
		Ui.bindTooltip(Panel.BUTTONS_LEFT, "inventory",
				new InformationTooltip().setInformation(withHotkey("Inventory", KeyboardAction.INVENTORY), ""));

		Ui.bind(Panel.BUTTONS_LEFT, "journal", Screens::openPhone, true);
		Ui.bindTooltip(Panel.BUTTONS_LEFT, "journal",
				new InformationTooltip().setInformation(withHotkey("Phone", KeyboardAction.JOURNAL), ""));

		Ui.bind(Panel.BUTTONS_LEFT, "charactersPresent", Screens::openCharactersPresent, true);
		Ui.bindTooltip(Panel.BUTTONS_LEFT, "charactersPresent",
				new InformationTooltip().setInformation(withHotkey("Characters Present", KeyboardAction.CHARACTERS), ""));

		Ui.bind(Panel.BUTTONS_LEFT, "mapZoom", Screens::zoomMap, true);
		Ui.bindTooltip(Panel.BUTTONS_LEFT, "mapZoom",
				new InformationTooltip().setInformation(withHotkey("Minimap Zoom", KeyboardAction.ZOOM), ""));

		GameState.getRenderSink().bindingsChanged(Panel.BUTTONS_LEFT);
	}

	public static void registerRight() {
		Ui.clear(Panel.BUTTONS_RIGHT);

		boolean quickSaveAvailable = GameState.isQuickSaveAvailable() && GameState.game.isInNewWorld();
		boolean quickLoadAvailable = GameState.isLoadGameAvailable(GameState.getQuickSaveName()) && GameState.game.isInNewWorld();

		Ui.bind(Panel.BUTTONS_RIGHT, "quickSave", GameState::quickSaveGame);
		Ui.bindTooltip(Panel.BUTTONS_RIGHT, "quickSave", new InformationTooltip().setInformation(
				(quickSaveAvailable ? "[style.colourGood(" : "[style.colourBad(")
						+ withHotkey("Quick Save", KeyboardAction.QUICKSAVE) + ")]",
				"Either creates or " + (quickLoadAvailable ? "[style.italicsBad(overrides)]" : "")
						+ " the save file with the name '" + GameState.getQuickSaveName() + "'."));

		Ui.bind(Panel.BUTTONS_RIGHT, "quickLoad", GameState::quickLoadGame);
		Ui.bindTooltip(Panel.BUTTONS_RIGHT, "quickLoad", new InformationTooltip().setInformation(
				(quickLoadAvailable ? "[style.colourGood(" : "[style.colourBad(")
						+ withHotkey("Quick Load", KeyboardAction.QUICKLOAD) + ")]",
				"Loads the save file with the name '" + GameState.getQuickSaveName() + "'."));

		GameState.getRenderSink().bindingsChanged(Panel.BUTTONS_RIGHT);
	}
}
