package com.lilithsthrone.controller;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.npc.NPC;
import com.lilithsthrone.game.dialogue.DialogueFlagValue;
import com.lilithsthrone.game.dialogue.DialogueNode;
import com.lilithsthrone.game.dialogue.DialogueNodeType;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.companions.CompanionManagement;
import com.lilithsthrone.game.dialogue.companions.OccupantManagementDialogue;
import com.lilithsthrone.game.dialogue.utils.CharactersPresentDialogue;
import com.lilithsthrone.game.dialogue.utils.InventoryDialogue;
import com.lilithsthrone.game.dialogue.utils.InventoryInteraction;
import com.lilithsthrone.game.dialogue.utils.OptionsDialogue;
import com.lilithsthrone.game.dialogue.utils.PhoneDialogue;
import com.lilithsthrone.game.sex.sexActions.baseActionsMisc.PositioningMenu;
import com.lilithsthrone.main.GameState;
import com.lilithsthrone.rendering.RenderingEngine;
import com.lilithsthrone.utils.Pathing;

/**
 * Opening and closing the game's overlay screens -- main menu, phone, inventory, characters
 * present.
 *
 * These were methods on desktop's {@code MainController}, but none of them did any JavaFX work:
 * each just records the current dialogue node and swaps in another one, which is engine state, not
 * presentation. Moving them here lets every front-end use the same logic instead of the desktop
 * controller being the only thing that knows how to open the inventory.
 */
public class Screens {

	private Screens() {
	}

	public static void openOptions() {
		if(!GameState.game.isStarted()) {
			return;
		}

		if (GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.OPTIONS) {
			GameState.game.restoreSavedContent(false);

		} else {
			DialogueNodeType currentDialogueNodeType = GameState.game.getCurrentDialogueNode().getDialogueNodeType();
			if (currentDialogueNodeType == DialogueNodeType.NORMAL
					|| currentDialogueNodeType == DialogueNodeType.OCCUPANT_MANAGEMENT // This was commented out at some point, which was causing issues with the opening/closing of the main menu screen.
					|| (!GameState.game.isInNewWorld() && currentDialogueNodeType != DialogueNodeType.CHARACTERS_PRESENT)) {
				GameState.game.saveDialogueNode();
			}

			GameState.game.setContent(new Response("", "", OptionsDialogue.MENU));
		}
	}

	public static boolean isPhoneDisabled() {
		return !GameState.game.isStarted() || !GameState.game.isInNewWorld() || GameState.game.getCurrentDialogueNode().isPhoneDisabled();
	}

	public static void openPhone() {
		openPhone(PhoneDialogue.MENU);
	}

	public static void openPhone(DialogueNode toDialogue) {
		if(isPhoneDisabled() && toDialogue!=PositioningMenu.POSITIONING_MENU && GameState.game.getCurrentDialogueNode()!=PositioningMenu.POSITIONING_MENU) {
			return;
		}

		if (GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.PHONE) {
			GameState.game.restoreSavedContent(false);

		} else {
			if (GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.NORMAL) {
				GameState.game.saveDialogueNode();
			}

			Pathing.initPathingVariables();
			if(toDialogue.equals(PhoneDialogue.MAP)) {
				PhoneDialogue.worldTypeMap = GameState.game.getPlayer().getWorldLocation();
			}
			GameState.game.setContent(new Response("", "", toDialogue));
		}
	}

	public static boolean isInventoryDisabled() {
		if(GameState.game.isBadEnd()) {
			return false;
		}

		if(!GameState.game.isInNewWorld() && !GameState.game.isInSex()) {
			return true;
		}
		if(GameState.game.isInSex() && GameState.game.getCurrentDialogueNode().isInventoryForcedDisabledInSex()) {
			return true;
		}

		if (GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.INVENTORY
				|| GameState.game.isInCombat()) {
			return false;

		} else if (GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.OPTIONS
				|| GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.PHONE) {
			if(GameState.game.getSavedDialogueNode()==null) {
				return true;
			}
			return GameState.game.getSavedDialogueNode().isInventoryDisabled();

		} else {
			return GameState.game.getCurrentDialogueNode().isInventoryDisabled();
		}
	}

	public static void openInventory() {
		if(!GameState.game.isInNewWorld() && !GameState.game.isInSex()) {
			// Character creation handles its own inventory entry point.

		} else if(GameState.game.isInCombat()) {
			if(GameState.combat.getTargetedCombatant().isPlayer()) {
				openInventory((NPC) GameState.combat.getEnemies(GameState.game.getPlayer()).get(0), InventoryInteraction.COMBAT);
			} else {
				openInventory((NPC) GameState.combat.getTargetedCombatant(), InventoryInteraction.COMBAT);
			}

		} else if(GameState.game.isInSex()) {
			if(isInventoryDisabled()) {
				return;
			}
			openInventory(
					GameState.sex.isMasturbation()
						?null
						:(NPC) GameState.sex.getTargetedPartner(GameState.game.getPlayer()),
					InventoryInteraction.SEX);

		} else if(GameState.game.getDialogueFlags().getManagementCompanion() != null) {
			openInventory(GameState.game.getDialogueFlags().getManagementCompanion(), InventoryInteraction.FULL_MANAGEMENT);

		} else {
			openInventory(null, InventoryInteraction.FULL_MANAGEMENT);
		}
	}

	public static void openInventory(NPC npc, InventoryInteraction interaction) {
		if(!GameState.game.isStarted()) {
			return;
		}

		RenderingEngine.setPageLeft(0);
		RenderingEngine.setPageRight(0);

		InventoryDialogue.setBuyback(false);
		InventoryDialogue.setInventoryNPC(npc);
		InventoryDialogue.setNPCInventoryInteraction(interaction);

		if (GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.INVENTORY) {
			if(GameState.game.getDialogueFlags().getManagementCompanion() != null) {
				GameState.game.setContent(new Response("", "", CompanionManagement.getCoreNode()) {
					@Override
					public void effects() {
						GameState.game.setResponseTab(CompanionManagement.getDefaultResponseTab());
						if(CompanionManagement.getCoreNode()==OccupantManagementDialogue.SLAVE_LIST) {
							CompanionManagement.initManagement(null, 0, null);
						}
					}
				});

			} else {
				GameState.game.restoreSavedContent(GameState.game.getDialogueFlags().getManagementCompanion() != null);
			}

		} else if(!isInventoryDisabled() || npc!=null) {
			if (GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.NORMAL) {
				GameState.game.saveDialogueNode();
			}

			if(npc!=null) {
				npc.sortInventory();
			} else {
				GameState.game.getPlayerCell().getInventory().sortInventory();
			}

			GameState.game.setContent(new Response("", "", InventoryDialogue.INVENTORY_MENU));
		}
	}

	/** Toggles the minimap between zoom levels and refreshes the panels that show it. */
	public static void zoomMap() {
		if (!GameState.game.getCurrentDialogueNode().isTravelDisabled()) {
			RenderingEngine.setZoomedIn(!RenderingEngine.isZoomedIn());

			GameState.getRenderSink().updateUILeftPanel();
			RenderingEngine.ENGINE.renderButtonsLeft();
			RenderingEngine.ENGINE.renderButtonsRight();
		}
	}

	public static void openCharactersPresent() {
		openCharactersPresent(null);
	}

	public static void openCharactersPresent(GameCharacter characterViewed) {
		if(!GameState.game.isStarted()) {
			return;
		}
		GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.coveringChangeListenersRequired, true);
		if(characterViewed!=null && characterViewed != CharactersPresentDialogue.characterViewed) {
			if (GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.NORMAL) {
				GameState.game.saveDialogueNode();
			}

			CharactersPresentDialogue.resetContent(characterViewed);
			GameState.game.setContent(new Response("", "", CharactersPresentDialogue.MENU));

		} else {
			if (GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.CHARACTERS_PRESENT) {
				GameState.game.restoreSavedContent(false);

			} else if (!GameState.game.getCharactersPresent().isEmpty()) {
				if (GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.NORMAL) {
					GameState.game.saveDialogueNode();
				}

				CharactersPresentDialogue.resetContent(characterViewed);
				GameState.game.setContent(new Response("", "", CharactersPresentDialogue.MENU));
			}
		}
	}
}
