package com.lilithsthrone.controller.eventListeners;

import com.lilithsthrone.game.dialogue.utils.EnchantmentDialogue;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

import com.lilithsthrone.controller.TooltipUpdateThread;
import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.dialogue.DialogueFlagValue;
import com.lilithsthrone.game.dialogue.DialogueNodeType;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.utils.InventoryDialogue;
import com.lilithsthrone.game.inventory.InventorySlot;
import com.lilithsthrone.game.inventory.clothing.AbstractClothing;
import com.lilithsthrone.game.inventory.item.AbstractItem;
import com.lilithsthrone.game.inventory.weapon.AbstractWeapon;
import com.lilithsthrone.main.GameState;
import com.lilithsthrone.main.Main;
import com.lilithsthrone.rendering.RenderingEngine;

/**
 * @since 0.1.0
 * @version 0.3.4.5
 * @author Innoxia
 */
public class InventorySelectedItemEventListener implements EventListener {
	private AbstractItem item;
	
	private AbstractClothing clothing;
	private AbstractClothing clothingEquipped;
	
	private AbstractWeapon weapon;
	private AbstractWeapon weaponEquipped;
	
	private InventorySlot weaponSlot;
	
	private GameCharacter owner;
	
	private int buyBackIndex;

	@Override
	public void handleEvent(Event event) {

//		if (GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.CHARACTERS_PRESENT
//				|| GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.PHONE
//				|| GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.OCCUPANT_MANAGEMENT) {
		if(GameState.game.getCurrentDialogueNode()==EnchantmentDialogue.ENCHANTMENT_MENU
			|| (GameState.game.getCurrentDialogueNode().getDialogueNodeType()!=DialogueNodeType.INVENTORY
				&& (!GameState.game.isInSex() || GameState.game.getCurrentDialogueNode().isInventoryDisabled())
				&& !GameState.game.isInCombat()
				&& (clothingEquipped!=null || weaponEquipped!=null))) {
			return;
		}
		
		TooltipUpdateThread.cancelThreads=true;
		
		if (item != null) {
			if(GameState.game.getCurrentDialogueNode().getDialogueNodeType()!=DialogueNodeType.INVENTORY) {
				Main.mainController.openInventory();
			}
			if(GameState.game.getDialogueFlags().values.contains(DialogueFlagValue.quickTrade)) {
				switch(InventoryDialogue.getNPCInventoryInteraction()) { //TODO
					case COMBAT:
						break;
					case FULL_MANAGEMENT:
						break;
					case SEX:
						break;
					case TRADING:
						break;
					case CHARACTER_CREATION:
						break;
				}
				
				
			} else {
				InventoryDialogue.setOwner(owner);
				InventoryDialogue.setItem(item);
				InventoryDialogue.setBuyBackIndex(buyBackIndex);
				GameState.game.setResponseTab(1);
				RenderingEngine.setPage(owner, item);
				GameState.game.setContent(new Response("", "", InventoryDialogue.ITEM_INVENTORY));
			}
			
		} else if (clothing != null) {
			if(GameState.game.getCurrentDialogueNode().getDialogueNodeType()!=DialogueNodeType.INVENTORY) {
				Main.mainController.openInventory();
			}
			InventoryDialogue.setOwner(owner);
			InventoryDialogue.setClothing(clothing);
			InventoryDialogue.setBuyBackIndex(buyBackIndex);
			GameState.game.setResponseTab(1);
			RenderingEngine.setPage(owner, clothing);
			GameState.game.setContent(new Response("", "", InventoryDialogue.CLOTHING_INVENTORY));
			
		} else if (weapon != null) {
			if(GameState.game.getCurrentDialogueNode().getDialogueNodeType()!=DialogueNodeType.INVENTORY) {
				Main.mainController.openInventory();
			}
			InventoryDialogue.setOwner(owner);
			InventoryDialogue.setWeapon(weaponSlot, weapon);
			InventoryDialogue.setBuyBackIndex(buyBackIndex);
			GameState.game.setResponseTab(1);
			RenderingEngine.setPage(owner, weapon);
			GameState.game.setContent(new Response("", "", InventoryDialogue.WEAPON_INVENTORY));

		} else if (clothingEquipped != null) {
			if(GameState.game.getCurrentDialogueNode().getDialogueNodeType()!=DialogueNodeType.INVENTORY) {
				Main.mainController.openInventory();
			}
			InventoryDialogue.setOwner(owner);
			InventoryDialogue.setClothing(clothingEquipped);
			InventoryDialogue.setBuyBackIndex(buyBackIndex);
			GameState.game.setResponseTab(1);
			GameState.game.setContent(new Response("", "", InventoryDialogue.CLOTHING_EQUIPPED));

		} else if (weaponEquipped != null) {
			if(GameState.game.getCurrentDialogueNode().getDialogueNodeType()!=DialogueNodeType.INVENTORY) {
				Main.mainController.openInventory();
			}
			InventoryDialogue.setOwner(owner);
			InventoryDialogue.setWeapon(weaponSlot, weaponEquipped);
			InventoryDialogue.setBuyBackIndex(buyBackIndex);
			GameState.game.setResponseTab(1);
			GameState.game.setContent(new Response("", "", InventoryDialogue.WEAPON_EQUIPPED));
		}

		Main.mainController.getTooltip().hide();
	}

	public InventorySelectedItemEventListener setItemInventory(AbstractItem item, GameCharacter owner) {
		return setItemInventory(item, owner, 0);
	}
	public InventorySelectedItemEventListener setItemInventory(AbstractItem item, GameCharacter owner, int buyBackIndex) {
		resetVariables();
		this.item = item;
		this.buyBackIndex=buyBackIndex;
		setOwner(owner);
		return this;
	}

	public InventorySelectedItemEventListener setClothingInventory(AbstractClothing clothing, GameCharacter owner) {
		return setClothingInventory(clothing, owner, 0);
	}
	public InventorySelectedItemEventListener setClothingInventory(AbstractClothing clothing, GameCharacter owner, int buyBackIndex) {
		resetVariables();
		this.clothing = clothing;
		this.buyBackIndex=buyBackIndex;
		setOwner(owner);
		return this;
	}

	public InventorySelectedItemEventListener setWeaponInventory(AbstractWeapon weapon, GameCharacter owner) {
		return setWeaponInventory(weapon, owner, 0);
	}
	public InventorySelectedItemEventListener setWeaponInventory(AbstractWeapon weapon, GameCharacter owner, int buyBackIndex) {
		resetVariables();
		this.weapon = weapon;
		this.buyBackIndex=buyBackIndex;
		setOwner(owner);
		return this;
	}
	
	public InventorySelectedItemEventListener setWeaponEquipped(GameCharacter owner, InventorySlot invSlot) {
		resetVariables();
		setOwner(owner);
		weaponSlot = invSlot;
		
		if (owner != null) {
			if (invSlot == InventorySlot.WEAPON_MAIN_1) {
				weaponEquipped = owner.getMainWeapon(0);
			} else if (invSlot == InventorySlot.WEAPON_MAIN_2) {
				weaponEquipped = owner.getMainWeapon(1);
			} else if (invSlot == InventorySlot.WEAPON_MAIN_3) {
				weaponEquipped = owner.getMainWeapon(2);
			} else if (invSlot == InventorySlot.WEAPON_OFFHAND_1) {
				weaponEquipped = owner.getOffhandWeapon(0);
			} else if (invSlot == InventorySlot.WEAPON_OFFHAND_2) {
				weaponEquipped = owner.getOffhandWeapon(1);
			} else if (invSlot == InventorySlot.WEAPON_OFFHAND_3) {
				weaponEquipped = owner.getOffhandWeapon(2);
			}
		} else {
			weaponEquipped = null;
		}

		return this;
	}

	public InventorySelectedItemEventListener setClothingEquipped(GameCharacter owner, InventorySlot invSlot) {
		resetVariables();
		
		setOwner(owner);

		if (owner != null) {
			clothingEquipped = owner.getClothingInSlot(invSlot);
		} else {
			clothingEquipped = null;
		}
		
		return this;
	}
	
	private void setOwner(GameCharacter owner) {
		this.owner = owner;
	}

	private void resetVariables() {
		item = null;
		clothing = null;
		clothingEquipped = null;
		weapon = null;
		weaponEquipped = null;
		weaponSlot = null;
		owner = null;
		buyBackIndex = 0;
	}
}