package com.lilithsthrone.game.dialogue.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.body.CoverableArea;
import com.lilithsthrone.game.character.body.types.LegType;
import com.lilithsthrone.game.character.effects.Perk;
import com.lilithsthrone.game.character.fetishes.Fetish;
import com.lilithsthrone.game.character.npc.NPC;
import com.lilithsthrone.game.character.quests.QuestLine;
import com.lilithsthrone.game.combat.DamageType;
import com.lilithsthrone.game.combat.moves.CombatMove;
import com.lilithsthrone.game.combat.spells.SpellSchool;
import com.lilithsthrone.game.dialogue.DialogueFlagValue;
import com.lilithsthrone.game.dialogue.DialogueNode;
import com.lilithsthrone.game.dialogue.DialogueNodeType;
import com.lilithsthrone.game.dialogue.companions.SlaveDialogue;
import com.lilithsthrone.game.dialogue.eventLog.EventLogEntry;
import com.lilithsthrone.game.dialogue.eventLog.EventLogEntryEncyclopediaUnlock;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.responses.ResponseEffectsOnly;
import com.lilithsthrone.game.dialogue.story.CharacterCreation;
import com.lilithsthrone.game.inventory.AbstractCoreItem;
import com.lilithsthrone.game.inventory.ColourReplacement;
import com.lilithsthrone.game.inventory.InventorySlot;
import com.lilithsthrone.game.inventory.ItemTag;
import com.lilithsthrone.game.inventory.Rarity;
import com.lilithsthrone.game.inventory.ShopTransaction;
import com.lilithsthrone.game.inventory.clothing.AbstractClothing;
import com.lilithsthrone.game.inventory.clothing.BlockedParts;
import com.lilithsthrone.game.inventory.clothing.DisplacementType;
import com.lilithsthrone.game.inventory.clothing.Sticker;
import com.lilithsthrone.game.inventory.clothing.StickerCategory;
import com.lilithsthrone.game.inventory.enchanting.ItemEffect;
import com.lilithsthrone.game.inventory.enchanting.ItemEffectType;
import com.lilithsthrone.game.inventory.enchanting.TFModifier;
import com.lilithsthrone.game.inventory.enchanting.TFPotency;
import com.lilithsthrone.game.inventory.item.AbstractItem;
import com.lilithsthrone.game.inventory.item.ItemType;
import com.lilithsthrone.game.inventory.weapon.AbstractWeapon;
import com.lilithsthrone.game.sex.sexActions.SexActionUtility;
import com.lilithsthrone.main.GameState;
import com.lilithsthrone.rendering.Pattern;
import com.lilithsthrone.rendering.RenderingEngine;
import com.lilithsthrone.utils.Util;
import com.lilithsthrone.utils.Util.Value;
import com.lilithsthrone.utils.colours.Colour;
import com.lilithsthrone.utils.colours.PresetColour;
import com.lilithsthrone.utils.comparators.ClothingZLayerComparator;

/**
 * @since 0.1.0
 * @version 0.4.10.10
 * @author Innoxia
 */
public class InventoryDialogue {
	
	private static final int IDENTIFICATION_PRICE = 1000;
	private static final int IDENTIFICATION_ESSENCE_PRICE = 15;
	
	private static AbstractItem item;
	private static AbstractClothing clothing;
	private static AbstractWeapon weapon;
	private static InventorySlot weaponSlot;
	private static GameCharacter owner;
	
	private static NPC inventoryNPC;
	private static InventoryInteraction interactionType;

	private static StringBuilder inventorySB = new StringBuilder();

	private static boolean buyback;

	private static int buyBackPrice;
	private static int buyBackIndex;

	public static DamageType damageTypePreview;
	
	public static List<Colour> dyePreviews;
	public static String dyePreviewPattern;
	public static List<Colour> dyePreviewPatternColours;
	
	public static Map<StickerCategory, Sticker> dyePreviewStickers;

	public static Map<String, String> getDyePreviewStickersAsStrings() {
		Map<String, String> stickerIds = new HashMap<>();
		for(Entry<StickerCategory, Sticker> entry : dyePreviewStickers.entrySet()) {
			stickerIds.put(entry.getKey().getId(), entry.getValue().getId());
		}
		return stickerIds;
	}
	
	private static void resetClothingDyeColours() {
		dyePreviews = new ArrayList<>();
		dyePreviews.addAll(clothing.getColours());
		
		dyePreviewPattern = clothing.getPattern();

		dyePreviewPatternColours = new ArrayList<>();
		dyePreviewPatternColours.addAll(clothing.getPatternColours());
		
		dyePreviewStickers = new HashMap<>(clothing.getStickersAsObjects());
	}

	private static void resetWeaponDyeColours() {
		dyePreviews = new ArrayList<>();
		dyePreviews.addAll(weapon.getColours());
		
		damageTypePreview = weapon.getDamageType();
	}
	
	private static String inventoryView() {
		inventorySB = new StringBuilder();
		
		inventorySB.append(RenderingEngine.ENGINE.getInventoryPanel(inventoryNPC, buyback));
		
		return inventorySB.toString();
	}

	private static void equipAll(GameCharacter character) {
		List<AbstractClothing> zlayerClothing = new ArrayList<>(character.getAllClothingInInventory().keySet());
		zlayerClothing.removeIf((c) -> c.isEnchantmentKnown() && c.isSealed());
		zlayerClothing.sort(new ClothingZLayerComparator().reversed());
		Set<InventorySlot> slotsTaken = new HashSet<>();

		for(AbstractClothing c : character.getClothingCurrentlyEquipped()) {
			slotsTaken.add(c.getSlotEquippedTo());
		}

		for(AbstractClothing c : zlayerClothing) {
			if(!slotsTaken.contains(c.getClothingType().getEquipSlots().get(0))) {
				GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"+character.equipClothingFromInventory(c, true, character, character)+"</p>");
				slotsTaken.add(c.getClothingType().getEquipSlots().get(0));
			}
		}
	}

	private static String unequipAll(GameCharacter character) {
		StringBuilder sb = new StringBuilder();
		
//		for(int i=0; i<character.getArmRows(); i++) {
//			sb.append(character.unequipMainWeapon(i, false, character.isPlayer()));
//			sb.append(character.unequipOffhandWeapon(i, false, character.isPlayer()));
//		}
		
		List<AbstractClothing> zlayerClothing = new ArrayList<>(character.getClothingCurrentlyEquipped());
		zlayerClothing.sort(new ClothingZLayerComparator());
		
		for(AbstractClothing c : zlayerClothing) { 
			if((!GameState.game.isInSex()
					|| (!c.getSlotEquippedTo().isJewellery()
							&& !c.isCondom()
							&& (!character.hasPerkAnywhereInTree(Perk.SPECIAL_SHORT_SIGHTED) || !c.getItemTags().contains(ItemTag.PRESCRIPTION_GLASSES))))
					&& !c.isMilkingEquipment()) {
				if (c.isDiscardedOnUnequip(null)) {
					character.unequipClothingIntoVoid(c, true, GameState.game.getPlayer());
				} else {
					if(GameState.game.isInNewWorld()) {
						character.unequipClothingIntoInventory(c, true, GameState.game.getPlayer());
					} else {
						character.unequipClothingOntoFloor(c, true, GameState.game.getPlayer());
					}
				}
				sb.append("<p style='text-align:center;'>"+character.getUnequipDescription()+"</p>");
			}
		}
		
		return sb.toString();
	}
	
	private static String getEnchantmentNotDiscoveredText(String item) {
		StringBuilder sb = new StringBuilder();
		sb.append("You have not discovered how to enchant ");
		sb.append(item);
		sb.append(" yet...");
		sb.append("<br/>");
		if(GameState.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)) {
			sb.append("[style.italicsArcane(You should go and talk to Lilaya about essences...)]");
		} else {
			sb.append("[style.italicsArcane(You need to absorb an essence after combat or sex, or via purchasing one, to discover more about enchanting...)]");
		}
		return sb.toString();
	}
	
	private static String getClothingBlockingRemovalText(GameCharacter equipTarget, String equipVerb) {
		StringBuilder sb = new StringBuilder();
		AbstractClothing blockingClothing = equipTarget.getBlockingClothing();

		sb.append("You can't ");
		sb.append(equipVerb);
		sb.append(" the ");
		sb.append(clothing.getName());
		sb.append(", as ");
		
		if(blockingClothing.equals(clothing) && clothing.isSealed()) {
			sb.append((blockingClothing.getClothingType().isPlural()?" they are":" it is"));
			sb.append(" sealed!");
			
		} else {
			sb.append(UtilText.parse(equipTarget, "[npc.namePos] "));
			sb.append(blockingClothing.getName());
			sb.append((blockingClothing.getClothingType().isPlural()?" are":" is"));
			sb.append(" blocking you from doing so!");
		}
		
		return sb.toString();
	}
	
	private static boolean isWeaponDyeReforgeActionAvailable() {
		return GameState.game.getPlayer().hasItemType(ItemType.DYE_BRUSH)
				|| GameState.game.getPlayer().hasItemType(ItemType.REFORGE_HAMMER)
				|| GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
				|| GameState.game.isDebugMode();
	}
	
	private static boolean isClothingDyeActionAvailable() {
		return GameState.game.getPlayer().hasItemType(ItemType.DYE_BRUSH)
				|| GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
				|| GameState.game.isDebugMode();
	}
	
	
	/**
	 * The main DialogueNode. From here, the player can gain access to all parts
	 * of their inventory.
	 */
	public static final DialogueNode INVENTORY_MENU = new DialogueNode("Inventory", "Return to inventory menu.", true) {
		@Override
		public String getLabel() {
			if(!GameState.game.isInNewWorld()) {
				return "Evening's Attire";
			}
			
			if (GameState.game.getDialogueFlags().values.contains(DialogueFlagValue.quickTrade) && !GameState.game.isInSex() && !GameState.game.isInCombat()) {
				return "Inventory (Quick-Manage is <b style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>)";
			} else {
				return "Inventory";
			}
		}

		@Override
		public String getHeaderContent() {
			return inventoryView();
		}

		@Override
		public String getContent() {
			UtilText.nodeContentSB.setLength(0);
			
			if(inventoryNPC!=null && interactionType==InventoryInteraction.TRADING) {
				if(!GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.removeTraderDescription)) {
					UtilText.nodeContentSB.append(inventoryNPC.getTraderDescription());
				}
				
			} else if(interactionType==InventoryInteraction.CHARACTER_CREATION) {
				return CharacterCreation.getCheckingClothingDescription();
			}
			
			return UtilText.nodeContentSB.toString();
		}

		public String getResponseTabTitle(int index) {
			return getGeneralResponseTabTitle(index);
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return getCloseInventoryResponse();
			}
			
			if(GameState.game.isBadEnd()) {
				if(index==1) {
					return new Response("Unavailable", "You can't manipulate your inventory during a bad end...", null);
				}
				return null;
			}
			
			if(responseTab==1) {
				if(item!=null) {
					return ITEM_INVENTORY.getResponse(responseTab, index);
					
				} else if(clothing!=null) {
					if(GameState.game.getPlayer().getClothingCurrentlyEquipped().contains(clothing) || (inventoryNPC!=null && inventoryNPC.getClothingCurrentlyEquipped().contains(clothing))) {
						return CLOTHING_EQUIPPED.getResponse(responseTab, index);
					} else {
						return CLOTHING_INVENTORY.getResponse(responseTab, index);
					}
					
				} else if(weapon!=null) {
					if(GameState.game.getPlayer().hasWeaponEquipped(weapon)
							|| (inventoryNPC!=null && inventoryNPC.hasWeaponEquipped(weapon))) {
						return WEAPON_EQUIPPED.getResponse(responseTab, index);
					} else {
						return WEAPON_INVENTORY.getResponse(responseTab, index);
					}
					
				} else {
					return null;
				}
			}

			StringBuilder responseSB = new StringBuilder();
			switch(interactionType) {
				case COMBAT:
					if(index == 1) {
						return new Response("Take all", "You can't do this during combat!", null);

					} else if (index == 2) {
						return new Response("Displace all", "You cannot do this while fighting someone!", null);

					} else if (index == 3) {
						return new Response("Replace all", "You cannot do this while fighting someone!", null);
						
					} else if (index == 4) {
						return new Response("Unequip all", "You cannot do this while fighting someone!", null);

					} else if (index == 5) {
						return new Response("Equip all", "You cannot do this while fighting someone!", null);

					} else if(index == 6) {
						if(GameState.game.getPlayer().getLocationPlace().isItemsDisappear()) {
							return new Response("Store all", "You can't do this during combat!", null);
						}
						return new Response("Drop all", "You can't do this during combat!", null);

					} else if(index==11) {
						if(GameState.game.getPlayer().getUnlockKeyMap().isEmpty()) {
							return new Response("Keys", "You do not currently own any keys which are used for unlocking certain items of clothing.", null);
							
						} else if(GameState.game.getCurrentDialogueNode()==INVENTORY_MENU_KEYS) {
							return new Response("Keys", "You are already viewing a list of all the keys that you own which are used for unlocking certain items of clothing.", null);
							
						} else {
							return new Response("Keys", "View a list of all the keys that you currently own which are used for unlocking certain items of clothing.", INVENTORY_MENU_KEYS);
						}
						
					} else {
						return null;
					}
					
				case FULL_MANAGEMENT:
					if (index == 1) {
						if(inventoryNPC == null ) {
							if((GameState.game.getPlayerCell().getInventory().getInventorySlotsTaken()==0 && !GameState.game.getPlayerCell().getInventory().isAnyQuestItemPresent())
									|| GameState.game.isInCombat()
									|| GameState.game.isInSex()) {
								return new Response("Take all", "Pick up everything on the ground.", null);

							} else {
								return new Response("Take all", "Pick up everything on the ground.", INVENTORY_MENU){
									@Override
									public void effects(){
										for(Entry<AbstractItem, Integer> entry : new HashMap<>(GameState.game.getPlayerCell().getInventory().getAllItemsInInventory()).entrySet()) {
											GameState.game.getPlayer().addItem(entry.getKey(), entry.getValue(), true, true);
										}
										for(Entry<AbstractWeapon, Integer> entry : new HashMap<>(GameState.game.getPlayerCell().getInventory().getAllWeaponsInInventory()).entrySet()) {
											GameState.game.getPlayer().addWeapon(entry.getKey(), entry.getValue(), true, true);
										}
										for(Entry<AbstractClothing, Integer> entry : new HashMap<>(GameState.game.getPlayerCell().getInventory().getAllClothingInInventory()).entrySet()) {
											GameState.game.getPlayer().addClothing(entry.getKey(), entry.getValue(), true, true);
										}
									}
								};
							}

						} else {
							if(inventoryNPC.getInventorySlotsTaken()==0 || GameState.game.isInCombat() || GameState.game.isInSex()) {
								return new Response("Take all", UtilText.parse(inventoryNPC, "Take everything from [npc.namePos] inventory."), null);

							} else {
								return new Response("Take all", UtilText.parse(inventoryNPC, "Take everything from [npc.namePos] inventory."), INVENTORY_MENU){
									@Override
									public void effects(){
										for(Entry<AbstractItem, Integer> entry : new HashMap<>(inventoryNPC.getAllItemsInInventory()).entrySet()) {
											if(!GameState.game.getPlayer().isInventoryFull()
													|| GameState.game.getPlayer().hasItem(entry.getKey())
													|| entry.getKey().getRarity()==Rarity.QUEST) {
												inventoryNPC.removeItem(entry.getKey(), entry.getValue());
												GameState.game.getPlayer().addItem(entry.getKey(), entry.getValue(), true, true);
											}
										}
										for(Entry<AbstractClothing, Integer> entry : new HashMap<>(inventoryNPC.getAllClothingInInventory()).entrySet()) {
											if(!GameState.game.getPlayer().isInventoryFull()
													|| GameState.game.getPlayer().hasClothing(entry.getKey())
													|| entry.getKey().getRarity()==Rarity.QUEST) {
												inventoryNPC.removeClothing(entry.getKey(), entry.getValue());
												GameState.game.getPlayer().addClothing(entry.getKey(), entry.getValue(), true, true);
											}
										}
										for(Entry<AbstractWeapon, Integer> entry : new HashMap<>(inventoryNPC.getAllWeaponsInInventory()).entrySet()) {
											if(!GameState.game.getPlayer().isInventoryFull()
													|| GameState.game.getPlayer().hasWeapon(entry.getKey())
													|| entry.getKey().getRarity()==Rarity.QUEST) {
												inventoryNPC.removeWeapon(entry.getKey(), entry.getValue());
												GameState.game.getPlayer().addWeapon(entry.getKey(), entry.getValue(), true, true);
											}
										}
									}
								};
							}
						}

					} else if (index == 2) {
						if(GameState.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Displace all", "You aren't wearing any clothing, so there's nothing to displace!", null);

						} else {
							return new Response("Displace all", "Displace as much of your clothing as possible.", INVENTORY_MENU){
								@Override
								public void effects(){
									for(AbstractClothing c : GameState.game.getPlayer().getClothingCurrentlyEquipped()) {
										for(BlockedParts bp : c.getBlockedPartsMap(GameState.game.getPlayer(), c.getSlotEquippedTo())) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												GameState.game.getPlayer().isAbleToBeDisplaced(c, bp.displacementType, true, true, GameState.game.getPlayer());
												GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"+GameState.game.getPlayer().getDisplaceDescription()+"</p>");
											}
										}
									}
								}
							};
						}

					} else if (index == 3) {
						if(GameState.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Replace all", "You aren't wearing any clothing, so there's nothing to replace!", null);

						} else {
							return new Response("Replace all", "Replace as much of your clothing as possible.", INVENTORY_MENU){
								@Override
								public void effects(){

									List<AbstractClothing> zlayerClothing = new ArrayList<>(GameState.game.getPlayer().getClothingCurrentlyEquipped());
									zlayerClothing.sort(new ClothingZLayerComparator().reversed());

									for(AbstractClothing c : zlayerClothing) {
										for(BlockedParts bp : c.getBlockedPartsMap(GameState.game.getPlayer(), c.getSlotEquippedTo())) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												GameState.game.getPlayer().isAbleToBeReplaced(c, bp.displacementType, true, true, GameState.game.getPlayer());
												GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"+GameState.game.getPlayer().getReplaceDescription()+"</p>");
											}
										}
									}
								}
							};
						}

					} else if (index == 4) {
						if(GameState.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Unequip all", "You aren't wearing any clothing, so there's nothing to remove!", null);

						} else {
							return new Response("Unequip all", "Remove as much of your clothing as possible.", INVENTORY_MENU){
								@Override
								public void effects(){
									GameState.game.getTextEndStringBuilder().append(unequipAll(GameState.game.getPlayer()));
								}
							};
						}

					} else if (index == 5) {
						if(GameState.game.getPlayer().getAllClothingInInventory().isEmpty()) {
							return new Response("Equip all", "You don't have any clothing, so there's nothing to equip!", null);

						} else {
							return new Response("Equip all", "Equip as much of the clothing in your inventory as possible.", INVENTORY_MENU){
								@Override
								public void effects(){
									equipAll(GameState.game.getPlayer());
								}
							};
						}

					} else if(index == 6) {
						if(GameState.game.getPlayer().getInventorySlotsTaken()==0) {
							return new Response(
									!GameState.game.getPlayer().getLocationPlace().isItemsDisappear()
										?"Store all"
										:"Drop all",
									!GameState.game.getPlayer().getLocationPlace().isItemsDisappear()
										?"You have nothing in your inventory to store..."
										:"You have nothing in your inventory to drop...",
											null);
						}
						return new Response(
								!GameState.game.getPlayer().getLocationPlace().isItemsDisappear()
									?"Store all"
									:"Drop all",
								!GameState.game.getPlayer().getLocationPlace().isItemsDisappear()
									?"Store everything from your inventory in this location."
									:"Drop everything from your inventory onto the ground.",
										INVENTORY_MENU) {
							@Override
							public void effects() {
								for(Entry<AbstractItem, Integer> i : new HashSet<>(GameState.game.getPlayer().getAllItemsInInventory().entrySet())) {
									if(i.getKey().getItemType().isAbleToBeDropped()) {
										dropItems(GameState.game.getPlayer(), i.getKey(), i.getValue());
									}
								}
								for(Entry<AbstractWeapon, Integer> w : new HashSet<>(GameState.game.getPlayer().getAllWeaponsInInventory().entrySet())) {
									if(w.getKey().getWeaponType().isAbleToBeDropped()) {
										dropWeapons(GameState.game.getPlayer(), w.getKey(), w.getValue());
									}
								}
								for(Entry<AbstractClothing, Integer> c : new HashSet<>(GameState.game.getPlayer().getAllClothingInInventory().entrySet())) {
									if(c.getKey().getClothingType().isAbleToBeDropped()) {
										dropClothing(GameState.game.getPlayer(), c.getKey(), c.getValue());
									}
								}
							}
						};

					} else if (index == 7 && inventoryNPC != null) {
						if(inventoryNPC.getClothingCurrentlyEquipped().isEmpty()) {
							return new Response(UtilText.parse(inventoryNPC,"Displace all ([npc.HerHim])"),
									UtilText.parse(inventoryNPC, "[npc.Name] isn't wearing any clothing, so there's nothing to displace!"),
									null);

						} else {
							return new Response(UtilText.parse(inventoryNPC,"Displace all ([npc.HerHim])"),
									UtilText.parse(inventoryNPC, "Displace as much of [npc.namePos] clothing as possible."),
									INVENTORY_MENU){
								@Override
								public void effects(){
									for(AbstractClothing c : inventoryNPC.getClothingCurrentlyEquipped()) {
										for(BlockedParts bp : c.getBlockedPartsMap(inventoryNPC, c.getSlotEquippedTo())) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												inventoryNPC.isAbleToBeDisplaced(c, bp.displacementType, true, true, GameState.game.getPlayer());
												GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"+inventoryNPC.getDisplaceDescription()+"</p>");
											}
										}
									}
								}
							};
						}

					} else if (index == 8 && inventoryNPC != null) {
						if(inventoryNPC.getClothingCurrentlyEquipped().isEmpty()) {
							return new Response(UtilText.parse(inventoryNPC,"Replace all ([npc.HerHim])"),
									UtilText.parse(inventoryNPC, "[npc.Name] isn't wearing any clothing, so there's nothing to replace!"),
									null);

						} else {
							return new Response(UtilText.parse(inventoryNPC,"Replace all ([npc.HerHim])"),
									UtilText.parse(inventoryNPC, "Replace as much of [npc.namePos] clothing as possible."),
									INVENTORY_MENU){
								@Override
								public void effects(){

									List<AbstractClothing> zlayerClothing = new ArrayList<>(inventoryNPC.getClothingCurrentlyEquipped());
									zlayerClothing.sort(new ClothingZLayerComparator().reversed());

									for(AbstractClothing c : zlayerClothing) {
										for(BlockedParts bp : c.getBlockedPartsMap(inventoryNPC, c.getSlotEquippedTo())) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												inventoryNPC.isAbleToBeReplaced(c, bp.displacementType, true, true, GameState.game.getPlayer());
												GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"+inventoryNPC.getReplaceDescription()+"</p>");
											}
										}
									}
								}
							};
						}

					} else if (index == 9 && inventoryNPC != null) {
						if(inventoryNPC.getClothingCurrentlyEquipped().isEmpty()) {
							return new Response(UtilText.parse(inventoryNPC, "Unequip all ([npc.HerHim])"),
									UtilText.parse(inventoryNPC, "[npc.Name] isn't wearing any clothing, so there's nothing to remove!"),
									null);

						} else {
							return new Response(UtilText.parse(inventoryNPC, "Unequip all ([npc.HerHim])"),
									UtilText.parse(inventoryNPC, "Remove as much of [npc.namePos] clothing as possible."),
									INVENTORY_MENU){
								@Override
								public void effects(){
									GameState.game.getTextEndStringBuilder().append(unequipAll(inventoryNPC));
								}
							};
						}

					} else if (index == 10 && !GameState.game.isInSex() && !GameState.game.isInCombat()) {
						return getQuickTradeResponse();

					} else if(index==11) {
						if(GameState.game.getPlayer().getUnlockKeyMap().isEmpty()) {
							return new Response("Keys", "You do not currently own any keys which are used for unlocking certain items of clothing.", null);
							
						} else if(GameState.game.getCurrentDialogueNode()==INVENTORY_MENU_KEYS) {
							return new Response("Keys", "You are already viewing a list of all the keys that you own which are used for unlocking certain items of clothing.", null);
							
						} else {
							return new Response("Keys", "View a list of all the keys that you currently own which are used for unlocking certain items of clothing.", INVENTORY_MENU_KEYS);
						}
						
					} else {
						return null;
					}
				case CHARACTER_CREATION:
					if (index == 1) {
						if(GameState.game.getPlayer().isCoverableAreaVisible(CoverableArea.NIPPLES)
								|| GameState.game.getPlayer().isCoverableAreaVisible(CoverableArea.ANUS)
								|| GameState.game.getPlayer().isCoverableAreaVisible(CoverableArea.PENIS)
								|| GameState.game.getPlayer().isCoverableAreaVisible(CoverableArea.VAGINA)
								|| (GameState.game.getPlayer().getClothingInSlot(InventorySlot.FOOT)==null && GameState.game.getPlayer().getLegType().equals(LegType.HUMAN))) {
							return new Response("To the stage", "You need to be wearing clothing that covers your body, as well as a pair of shoes.", null);
							
						} else {
							return new Response("To the stage", "You're ready to approach the stage now.", CharacterCreation.CHOOSE_BACKGROUND) {
								@Override
								public int getSecondsPassed() {
									return CharacterCreation.TIME_TO_BACKGROUND;
								}
								@Override
								public void effects() {
									CharacterCreation.moveNPCIntoPlayerTile();
								}
							};
						}
						
					} else if(index == 2){
						if(GameState.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()){
							return new Response("Unequip all", "You're currently naked, there's nothing to be unequipped.", null);
						}
						else{
							return new Response("Unequip all", "Remove as much of your clothing as possible.", INVENTORY_MENU){
								@Override
								public void effects(){
									GameState.game.getTextEndStringBuilder().append(unequipAll(GameState.game.getPlayer()));
								}
							};
						}

					} else {
						return null;
					}
					
				case TRADING:
					if (index == 1) {
						if(inventoryNPC != null ||GameState.game.getPlayerCell().getInventory().getInventorySlotsTaken()==0 || GameState.game.isInCombat() || GameState.game.isInSex()) {
							return new Response("Take all", "Pick up everything on the ground.", null);

						} else {
							return new Response("Take all", "Pick up everything on the ground.", INVENTORY_MENU){
								@Override
								public void effects(){
									for(Entry<AbstractItem, Integer> entry : new HashMap<>(GameState.game.getPlayerCell().getInventory().getAllItemsInInventory()).entrySet()) {
										GameState.game.getPlayer().addItem(entry.getKey(), entry.getValue(), true, true);
									}
									for(Entry<AbstractWeapon, Integer> entry : new HashMap<>(GameState.game.getPlayerCell().getInventory().getAllWeaponsInInventory()).entrySet()) {
										GameState.game.getPlayer().addWeapon(entry.getKey(), entry.getValue(), true, true);
									}
									for(Entry<AbstractClothing, Integer> entry : new HashMap<>(GameState.game.getPlayerCell().getInventory().getAllClothingInInventory()).entrySet()) {
										GameState.game.getPlayer().addClothing(entry.getKey(), entry.getValue(), true, true);
									}
								}
							};
						}

					} else if (index == 2) {
						if(GameState.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Displace all", "You aren't wearing any clothing, so there's nothing to displace!", null);

						} else {
							return new Response("Displace all", "Displace as much of your clothing as possible.", INVENTORY_MENU){
								@Override
								public void effects(){
									for(AbstractClothing c : GameState.game.getPlayer().getClothingCurrentlyEquipped()) {
										for(BlockedParts bp : c.getBlockedPartsMap(GameState.game.getPlayer(), c.getSlotEquippedTo())) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												GameState.game.getPlayer().isAbleToBeDisplaced(c, bp.displacementType, true, true, GameState.game.getPlayer());
												GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"+GameState.game.getPlayer().getDisplaceDescription()+"</p>");
											}
										}
									}
								}
							};
						}

					} else if (index == 3) {
						if(GameState.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Replace all", "You aren't wearing any clothing, so there's nothing to replace!", null);

						} else {
							return new Response("Replace all", "Replace as much of your clothing as possible.", INVENTORY_MENU){
								@Override
								public void effects(){

									List<AbstractClothing> zlayerClothing = new ArrayList<>(GameState.game.getPlayer().getClothingCurrentlyEquipped());
									zlayerClothing.sort(new ClothingZLayerComparator().reversed());

									for(AbstractClothing c : zlayerClothing) {
										for(BlockedParts bp : c.getBlockedPartsMap(GameState.game.getPlayer(), c.getSlotEquippedTo())) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												GameState.game.getPlayer().isAbleToBeReplaced(c, bp.displacementType, true, true, GameState.game.getPlayer());
												GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"+GameState.game.getPlayer().getReplaceDescription()+"</p>");
											}
										}
									}
								}
							};
						}

					} else if (index == 4) {
						if(GameState.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Unequip all", "You aren't wearing any clothing, so there's nothing to remove!", null);

						} else {
							return new Response("Unequip all", "Remove as much of your clothing as possible.", INVENTORY_MENU){
								@Override
								public void effects(){
									GameState.game.getTextEndStringBuilder().append(unequipAll(GameState.game.getPlayer()));
								}
							};
						}

					} else if (index == 5) {
						if(GameState.game.getPlayer().getAllClothingInInventory().isEmpty()) {
							return new Response("Equip all", "You don't have any clothing, so there's nothing to equip!", null);

						} else {
							return new Response("Equip all", "Equip as much of the clothing in your inventory as possible.", INVENTORY_MENU){
								@Override
								public void effects(){
									equipAll(GameState.game.getPlayer());
								}
							};
						}

					} else if(index == 6) {
						if(GameState.game.getPlayer().getLocationPlace().isItemsDisappear()) {
							return new Response("Store all", "You can't do this while trading with someone...", null);
						}
						return new Response("Drop all", "You can't do this while trading with someone...", null);

					} else if (index == 9 && inventoryNPC!=null) {
						return getBuybackResponse();

					} else if (index == 10 && !GameState.game.isInSex() && !GameState.game.isInCombat()) {
						return getQuickTradeResponse();

					} else if(index==11) {
						if(GameState.game.getPlayer().getUnlockKeyMap().isEmpty()) {
							return new Response("Keys", "You do not currently own any keys which are used for unlocking certain items of clothing.", null);
							
						} else if(GameState.game.getCurrentDialogueNode()==INVENTORY_MENU_KEYS) {
							return new Response("Keys", "You are already viewing a list of all the keys that you own which are used for unlocking certain items of clothing.", null);
							
						} else {
							return new Response("Keys", "View a list of all the keys that you currently own which are used for unlocking certain items of clothing.", INVENTORY_MENU_KEYS);
						}
						
					} else {
						return null;
					}
				case SEX:
					if(index == 1) {
						return new Response("Take all", "Pick up everything on the ground.", null);

					} else if (index == 2) {
						if(GameState.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Displace all", "You aren't wearing any clothing, so there's nothing to displace!", null);

						} else {
							return new Response("Displace all", "Displace as much of your clothing as possible.", GameState.sex.SEX_DIALOGUE){
								@Override
								public void effects(){
									responseSB.setLength(0);

									for(AbstractClothing c : GameState.game.getPlayer().getClothingCurrentlyEquipped()) {
										for(BlockedParts bp : c.getBlockedPartsMap(GameState.game.getPlayer(), c.getSlotEquippedTo())) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												GameState.game.getPlayer().isAbleToBeDisplaced(c, bp.displacementType, true, true, GameState.game.getPlayer());
												responseSB.append("<p style='text-align:center;'>"+GameState.game.getPlayer().getDisplaceDescription()+"</p>");
											}
										}
									}

									GameState.sex.setUnequipClothingText(null, responseSB.toString());
									GameState.getRenderSink().openInventory();
									GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
									GameState.sex.setSexStarted(true);
								}
							};
						}

					} else if (index == 3) {
						return new Response("Replace all", "You can't replace clothing in sex!", null);

					} else if (index == 4) {
						if(GameState.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Unequip all", "You aren't wearing any clothing, so there's nothing to remove!", null);

						} else {
							return new Response("Unequip all", "Remove as much of your clothing as possible.", GameState.sex.SEX_DIALOGUE){
								@Override
								public void effects(){
									responseSB.setLength(0);
									
									responseSB.append(unequipAll(GameState.game.getPlayer()));
									
									GameState.sex.setUnequipClothingText(null, responseSB.toString());
									GameState.getRenderSink().openInventory();
									GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
									GameState.sex.setSexStarted(true);
								}
							};
						}

					} else if (index == 5) {
						return new Response("Equip all", "You can't equip clothing in sex!", null);

					} else if(index == 6) {
						if(GameState.game.getPlayer().getLocationPlace().isItemsDisappear()) {
							return new Response("Store all", "You can't do this during sex...", null);
						}
						return new Response("Drop all", "You can't do this during sex...", null);

					} else if (index == 7 && inventoryNPC != null) {
						if(GameState.sex.getInitialSexManager().isHidden(GameState.game.getPlayer())) {
							return new Response(UtilText.parse(inventoryNPC, "Displace all ([npc.HerHim])"), UtilText.parse(inventoryNPC, "As you're hiding, you can't displace [npc.namePos] clothing!"), null);
							
						} else if(!GameState.sex.isCanRemoveOthersClothing(GameState.game.getPlayer(), null)) {
							return new Response(UtilText.parse(inventoryNPC, "Displace all ([npc.HerHim])"), UtilText.parse(inventoryNPC, "You can't displace [npc.namePos] clothing in this sex scene!"), null);

						} else if(inventoryNPC.getClothingCurrentlyEquipped().isEmpty()) {
							return new Response(UtilText.parse(inventoryNPC, "Displace all ([npc.HerHim])"), UtilText.parse(inventoryNPC, "[npc.Name] isn't wearing any clothing, so there's nothing to displace!"), null);

						} else {
							return new Response(UtilText.parse(inventoryNPC, "Displace all ([npc.HerHim])"), UtilText.parse(inventoryNPC, "Displace as much of [npc.namePos] clothing as possible."), GameState.sex.SEX_DIALOGUE){
								@Override
								public void effects(){
									responseSB.setLength(0);

									for(AbstractClothing c : inventoryNPC.getClothingCurrentlyEquipped()) {
										for(BlockedParts bp : c.getBlockedPartsMap(inventoryNPC, c.getSlotEquippedTo())) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												inventoryNPC.isAbleToBeDisplaced(c, bp.displacementType, true, true, GameState.game.getPlayer());
												responseSB.append("<p style='text-align:center;'>"+inventoryNPC.getDisplaceDescription()+"</p>");
											}
										}
									}

									GameState.sex.setUnequipClothingText(null, responseSB.toString());
									GameState.getRenderSink().openInventory();
									GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
									GameState.sex.setSexStarted(true);
								}
							};
						}

					} else if (index == 8 && inventoryNPC != null) {
						return new Response(UtilText.parse(inventoryNPC, "Replace all ([npc.HerHim])"), UtilText.parse(inventoryNPC, "You can't replace [npc.namePos] clothing in sex!"), null);

					} else if (index == 9 && inventoryNPC != null) {
						if(GameState.sex.getInitialSexManager().isHidden(GameState.game.getPlayer())) {
							return new Response(UtilText.parse(inventoryNPC, "Unequip all ([npc.HerHim])"), UtilText.parse(inventoryNPC, "As you're hiding, you can't unequip [npc.namePos] clothing!"), null);
							
						} else if(!GameState.sex.isCanRemoveOthersClothing(GameState.game.getPlayer(), null)) {
							return new Response(UtilText.parse(inventoryNPC, "Unequip all ([npc.HerHim])"), UtilText.parse(inventoryNPC, "You can't unequip [npc.namePos] clothing in this sex scene!"), null);

						} else if(inventoryNPC.getClothingCurrentlyEquipped().isEmpty()) {
							return new Response(UtilText.parse(inventoryNPC, "Unequip all ([npc.HerHim])"), UtilText.parse(inventoryNPC, "[npc.Name] isn't wearing any clothing, so there's nothing to remove!"), null);

						} else {
							return new Response(UtilText.parse(inventoryNPC, "Unequip all ([npc.HerHim])"), UtilText.parse(inventoryNPC, "Remove as much of [npc.namePos] clothing as possible."), GameState.sex.SEX_DIALOGUE){
								@Override
								public void effects(){
									responseSB.setLength(0);

									responseSB.append(unequipAll(inventoryNPC));

									GameState.sex.setUnequipClothingText(null, responseSB.toString());
									GameState.getRenderSink().openInventory();
									GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
									GameState.sex.setSexStarted(true);
								}
							};
						}

					} else if(index==11) {
						if(GameState.game.getPlayer().getUnlockKeyMap().isEmpty()) {
							return new Response("Keys", "You do not currently own any keys which are used for unlocking certain items of clothing.", null);
							
						} else if(GameState.game.getCurrentDialogueNode()==INVENTORY_MENU_KEYS) {
							return new Response("Keys", "You are already viewing a list of all the keys that you own which are used for unlocking certain items of clothing.", null);
							
						} else {
							return new Response("Keys", "View a list of all the keys that you currently own which are used for unlocking certain items of clothing.", INVENTORY_MENU_KEYS);
						}
						
					} else {
						return null;
					}
			}
			
			return null;
		}

		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.INVENTORY;
		}
	};
	
	public static final DialogueNode INVENTORY_MENU_KEYS = new DialogueNode("Inventory", "Return to inventory menu.", true) {
		@Override
		public String getLabel() {
			if(!GameState.game.isInNewWorld()) {
				return "Evening's Attire";
			}
			
			if (GameState.game.getDialogueFlags().values.contains(DialogueFlagValue.quickTrade) && !GameState.game.isInSex() && !GameState.game.isInCombat()) {
				return "Inventory (Quick-Manage is <b style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>)";
			} else {
				return "Inventory";
			}
		}

		@Override
		public String getHeaderContent() {
			return inventoryView();
		}

		@Override
		public String getContent() {
			UtilText.nodeContentSB.setLength(0);
			
			UtilText.nodeContentSB.append("<p>");
			UtilText.nodeContentSB.append("[style.boldMinorGood(Keys owned:)]");
			if(GameState.game.getPlayer().getUnlockKeyMap().isEmpty()) {
				UtilText.nodeContentSB.append("<br/>[style.italicsDisabled(None...)]");
				
			} else {
				for(Entry<String, List<InventorySlot>> entry : GameState.game.getPlayer().getUnlockKeyMap().entrySet()) {
					try {
						GameCharacter npc = GameState.game.getNPCById(entry.getKey());
						List<String> slots = new ArrayList<>();
						for(InventorySlot slot : entry.getValue()) {
							AbstractClothing slotClothing = npc.getClothingInSlot(slot);
							if(slotClothing!=null) {
								slots.add(Util.capitaliseSentence(slotClothing.getName())+" ('"+slot.getName()+"' slot)");
							}
						}
						if(!slots.isEmpty()) {
							UtilText.nodeContentSB.append(UtilText.parse(npc, "<br/><b style='color:"+npc.getFemininity().getColour().toWebHexString()+";'>[npc.Name]</b> ("+slots.size()+"): "));
							int i=0;
							for(String s : slots) {
								UtilText.nodeContentSB.append((i>0?", ":"")+s);
								i++;
							}
						}
					} catch (Exception e) {
					}
				}
			}
			UtilText.nodeContentSB.append("</p>");
			
			return UtilText.nodeContentSB.toString();
		}

		public String getResponseTabTitle(int index) {
			return getGeneralResponseTabTitle(index);
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			return INVENTORY_MENU.getResponse(responseTab, index);
		}

		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.INVENTORY;
		}
	};
	
	public static final DialogueNode ITEM_INVENTORY = new DialogueNode("Item", "", true) {

		@Override
		public String getLabel() {
			if (GameState.game.getDialogueFlags().values.contains(DialogueFlagValue.quickTrade) && !GameState.game.isInSex() && !GameState.game.isInCombat()) {
				return "Inventory (Quick-Manage is <b style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>)";
			} else {
				return "Inventory";
			}
		}

		@Override
		public String getHeaderContent() {
			return inventoryView();
		}
		
		@Override
		public String getContent() {
			return getItemDisplayPanel(item,
					item.getSVGString(),
					item.getDisplayName(true),
					item.getDescription(owner)
						+ item.getExtraDescription(owner, owner)
						+ (owner!=null && owner.isPlayer()
								? (inventoryNPC != null && interactionType == InventoryInteraction.TRADING
										? "<p>"
											+(inventoryNPC.willBuy(item) && item.getItemType().isAbleToBeSold()
												?inventoryNPC.getName("The") + " will buy it for " + UtilText.formatAsMoney(item.getPrice(inventoryNPC.getBuyModifier())) + "."
												:inventoryNPC.getName("The") + " doesn't want to buy this.")
											+"</p>"
										: "")
								:(inventoryNPC != null && interactionType == InventoryInteraction.TRADING
									? "<p>"
											+ inventoryNPC.getName("The") + " will sell it for " + UtilText.formatAsMoney(item.getPrice(inventoryNPC.getSellModifier(item))) + "."
										+ "</p>" 
									: "")));
		}

		public String getResponseTabTitle(int index) {
			return getGeneralResponseTabTitle(index);
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return getCloseInventoryResponse();
			}

			if(GameState.game.isBadEnd()) {
				if(index==1) {
					return new Response("Unavailable", "You can't manipulate your inventory during a bad end...", null);
				}
				return null;
			}
			
			if(responseTab==0) {
				return INVENTORY_MENU.getResponse(responseTab, index);
			}
			// ****************************** ITEM BELONGS TO THE PLAYER ******************************
			if(owner != null && owner.isPlayer()) {
				
				// ****************************** Interacting with the ground ******************************
				if(inventoryNPC == null) {
					boolean areaFull = GameState.game.isPlayerTileFull() && !GameState.game.getPlayerCell().getInventory().hasItem(item);
					
					switch(interactionType) {
						case SEX:
							String dropTitle = owner.getLocationPlace().isItemsDisappear()?"Drop ":"Store";
							if(index == 1) {
								return new Response(dropTitle+"(1)", "You can't drop items while masturbating.", null);
								
							} else if(index == 2) {
								return new Response(dropTitle+"(5)", "You can't drop items while masturbating.", null);
								
							} else if(index == 3) {
								return new Response(dropTitle+"(All)", "You can't drop items while masturbating.", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant items while masturbating.", null);
								
							} else if(index == 6) {
								if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", "You can't use this as you are currently [style.colourTerrible(immobilised)]!", null);
								}
								if(!GameState.sex.isItemUseAvailable()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", "Items cannot be used during this sex scene!", null);
									
								} else if (!item.isAbleToBeUsedInSex()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", "You cannot use this during sex!", null);
									
								} else if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(GameState.game.getPlayer(), GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), GameState.game.getPlayer()), null);
									
								} else {
									if(item.isBreakOutOfInventory()) {
										return new ResponseEffectsOnly(
												Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
												item.getItemType().getUseTooltipDescription(owner, owner)){
											@Override
											public void effects(){
												GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), false);
												resetPostAction();
											}
										};
									} else {
										return new Response(
												Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
												item.getItemType().getUseTooltipDescription(owner, owner),
												GameState.sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												GameState.sex.setUsingItemText(GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), false));
												resetPostAction();
												GameState.getRenderSink().openInventory();
												GameState.sex.endSexTurn(SexActionUtility.PLAYER_USE_ITEM);
												GameState.sex.setSexStarted(true);
											}
										};
									}
								}
								
							} else if(index == 7) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", "You can only use one item at a time during sex!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else {
								return null;
							}
						
						default:
							if(index == 1) {
								if(owner.getLocationPlace().isItemsDisappear()) {
									if(!item.getItemType().isAbleToBeDropped()) {
										return new Response("Drop (1)", "You cannot drop the " + item.getName() + "!", null);
									} else if(areaFull) {
										return new Response("Drop (1)", "This area is full, so you can't drop your " + item.getName() + " here!", null);
									} else {
										return new Response("Drop (1)", "Drop your " + item.getName() + ".", INVENTORY_MENU){
											@Override
											public void effects(){
												dropItems(owner, item, 1);
											}
										};
									}
								} else {
									if(!item.getItemType().isAbleToBeDropped()) {
										return new Response("Store (1)", "You cannot store the " + item.getName() + "!", null);
									} else if(areaFull) {
										return new Response("Store (1)", "This area is full, so you can't store your " + item.getName() + " here!", null);
									} else {
										return new Response("Store (1)", "Store the " + item.getName() + " in this area.", INVENTORY_MENU){
											@Override
											public void effects(){
												dropItems(owner, item, 1);
											}
										};
									}
								}
								
							} else if(index == 2) {
								if(owner.getLocationPlace().isItemsDisappear()) {
									if(owner.getItemCount(item) < 5) {
										return new Response("Drop (5)", "You don't have five " + item.getNamePlural() + " to give!", null);
										
									} else if(!item.getItemType().isAbleToBeDropped()) {
										return new Response("Drop (5)", "You cannot drop the " + item.getName() + "!", null);
										
									} else if(areaFull) {
										return new Response("Drop (5)", "This area is full, so you can't drop your " + item.getNamePlural() + " here!", null);
										
									} else {
										return new Response("Drop (5)", "Drop five of your " + item.getNamePlural() + ".", INVENTORY_MENU){
											@Override
											public void effects(){
												dropItems(owner, item, 5);
											}
										};
									}
								} else {
									if(owner.getItemCount(item) < 5) {
										return new Response("Store (5)", "You don't have five " + item.getNamePlural() + " to give!", null);
										
									} else if(!item.getItemType().isAbleToBeDropped()) {
										return new Response("Store (5)", "You cannot store the " + item.getName() + "!", null);
										
									} else if(areaFull) {
										return new Response("Store (5)", "This area is full, so you can't store your " + item.getNamePlural() + " here!", null);
										
									} else {
										return new Response("Store (5)", "Store five of your " + item.getNamePlural() + " in this area.", INVENTORY_MENU){
											@Override
											public void effects(){
												dropItems(owner, item, 5);
											}
										};
									}
								}
								
							} else if(index == 3) {
								if(owner.getLocationPlace().isItemsDisappear()) {
									if(!item.getItemType().isAbleToBeDropped()) {
										return new Response("Drop (All)", "You cannot drop the " + item.getName() + "!", null);
									} else if(areaFull) {
										return new Response("Drop (All)", "This area is full, so you can't drop your " + item.getNamePlural() + " here!", null);
									} else {
										return new Response("Drop (All)", "Drop all of your " + item.getNamePlural() + ".", INVENTORY_MENU){
											@Override
											public void effects(){
												dropItems(owner, item, owner.getItemCount(item));
											}
										};
									}
								} else {
									if(!item.getItemType().isAbleToBeDropped()) {
										return new Response("Store (All)", "You cannot store the " + item.getName() + "!", null);
									} else if(areaFull) {
										return new Response("Store (All)", "This area is full, so you can't store your " + item.getNamePlural() + " here!", null);
									} else {
										return new Response("Store (All)", "Store all of your " + item.getNamePlural() + " in this area.", INVENTORY_MENU){
											@Override
											public void effects(){
												dropItems(owner, item, owner.getItemCount(item));
											}
										};
									}
								}
								
							} else if(index == 5) {
								if(item.getEnchantmentItemType(null)==null || item.getItemTags().contains(ItemTag.UNENCHANTABLE)) {
									return new Response("Enchant", "This item cannot be enchanted!", null);
									
								} else if(GameState.game.isDebugMode()
										|| (GameState.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY) && GameState.game.getPlayer().isQuestCompleted(QuestLine.SIDE_ENCHANTMENT_DISCOVERY))) {
									return new Response("Enchant", "Enchant this item.", EnchantmentDialogue.ENCHANTMENT_MENU) {
										@Override
										public DialogueNode getNextDialogue() {
											return EnchantmentDialogue.getEnchantmentMenu(item);
										}
									};
									
								} else {
									return new Response("Enchant", getEnchantmentNotDiscoveredText("items"), null);
								}
								
							} else if(index == 6) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(GameState.game.getPlayer(), GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), GameState.game.getPlayer()), null);
								} else {
									if(item.isBreakOutOfInventory()) {
										return new ResponseEffectsOnly(
												Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
												item.getItemType().getUseTooltipDescription(owner, owner)){
											@Override
											public void effects(){
												GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), false);
												resetPostAction();
											}
										};
									}
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
											item.getItemType().getUseTooltipDescription(owner, owner),
											INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), false) + "</p>");
											resetPostAction();
										}
									};
								}
								
							} else if(index == 7) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if(!item.isAbleToBeUsed(GameState.game.getPlayer(), GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), GameState.game.getPlayer()), null);
									
								} else {
									if(item.isBreakOutOfInventory()) {
										return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", "As this item has special effects, you can only use one at a time!", null);
									}
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)",
											item.getItemType().getUseTooltipDescription(owner, owner)
												+"<br/>[style.italicsMinorGood(Repeat this for all of the " + item.getNamePlural() + " which are in your inventory.)]",
											INVENTORY_MENU){
										@Override
										public void effects(){
											int itemCount = GameState.game.getPlayer().getItemCount(item);
											for(int i=0;i<itemCount;i++) {
												GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), false) + "</p>");
											}
											resetPostAction();
										}
									};
								}
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else {
								return null;
							}
					}
					
				// ****************************** Interacting with an NPC ******************************
				} else {
					switch(interactionType) {
						case COMBAT:
							if(index == 1) {
								return new Response("Give (1)", "You can't give someone items while fighting them!", null);
								
							} else if(index == 2) {
								return new Response("Give (5)", "You can't give someone items while fighting them!", null);
								
							} else if(index == 3) {
								return new Response("Give (All)", "You can't give someone items while fighting them!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant items while fighting someone!", null);
								
							} else if(index == 6) {
								if(GameState.game.getPlayer().isStunned()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", "You cannot use any items while you're stunned!", null);
									
								} else if(GameState.combat.isCombatantDefeated(GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", "You cannot use any items while you're defeated!", null);
									
								} else if(GameState.game.getPlayer().getRemainingAP()<CombatMove.ITEM_USAGE.getAPcost(GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", "You need at least "+CombatMove.ITEM_USAGE.getAPcost(GameState.game.getPlayer())+" AP to use this actions!", null);
									
								} else if (!item.isAbleToBeUsedInCombatAllies()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", "You cannot use this during combat!", null);
									
								} else if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(GameState.game.getPlayer(), GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), GameState.game.getPlayer()), null);
									
								} else {
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
											item.getItemType().getUseTooltipDescription(owner, owner),
											GameState.combat.ENEMY_ATTACK){
										@Override
										public void effects(){
											GameState.combat.addItemToBeUsed(owner, owner, item);
											resetPostAction();
											GameState.getRenderSink().openInventory();
										}
									};
								}
								
							} else if(index == 7) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", "You can only use one item at a time during combat!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {//TODO on ally though???
								if(GameState.game.getPlayer().isStunned()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Opponent)", "You cannot use any items while you're stunned!", null);
									
								} else if(GameState.combat.isCombatantDefeated(GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Opponent)", "You cannot use any items while you're defeated!", null);
									
								} else if(GameState.game.getPlayer().getRemainingAP()<CombatMove.ITEM_USAGE.getAPcost(GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Opponent)", "You need at least "+CombatMove.ITEM_USAGE.getAPcost(GameState.game.getPlayer())+" AP to use this actions!", null);
									
								} else if (!item.isAbleToBeUsedInCombatEnemies()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Opponent)", "You cannot use this during combat!", null);
									
								} else if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Opponent)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(GameState.game.getPlayer(), inventoryNPC)) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Opponent)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), inventoryNPC), null);

								} else if(item.getItemType().isFetishGiving()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Opponent)",
											item.getItemType().getUseTooltipDescription(owner, inventoryNPC),
											GameState.combat.ENEMY_ATTACK,
											Util.newArrayListOfValues(Fetish.FETISH_KINK_GIVING),
											Fetish.FETISH_KINK_GIVING.getAssociatedCorruptionLevel(),
											null,
											null,
											null){
										@Override
										public void effects(){
											GameState.combat.addItemToBeUsed(owner, inventoryNPC, item);
											resetPostAction();
											GameState.getRenderSink().openInventory();
										}
									};
								} else if(item.getItemType().isTransformative()) {
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName()) +" (Opponent)",
											item.getItemType().getUseTooltipDescription(owner, inventoryNPC),
											GameState.combat.ENEMY_ATTACK,
											Util.newArrayListOfValues(Fetish.FETISH_TRANSFORMATION_GIVING),
											Fetish.FETISH_TRANSFORMATION_GIVING.getAssociatedCorruptionLevel(),
											null,
											null,
											null){
										@Override
										public void effects(){
											GameState.combat.addItemToBeUsed(owner, inventoryNPC, item);
											resetPostAction();
											GameState.getRenderSink().openInventory();
										}
									};
									
								} else {
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName()) +" (Opponent)",
											item.getItemType().getUseTooltipDescription(owner, inventoryNPC),
											GameState.combat.ENEMY_ATTACK){
										@Override
										public void effects(){
											GameState.combat.addItemToBeUsed(owner, inventoryNPC, item);
											resetPostAction();
											GameState.getRenderSink().openInventory();
										}
									};
								}
								
							} else if(index == 12) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Opponent)", "You can only use one item at a time during combat!", null);
								
							} else {
								return null;
							}
							
						case FULL_MANAGEMENT:  case CHARACTER_CREATION:
							boolean inventoryFull = inventoryNPC.isInventoryFull() && !inventoryNPC.hasItem(item);
							
							if(index == 1) {
								if(!item.getItemType().isAbleToBeDropped()) {
									return new Response("Give (1)", "You cannot give away the " + item.getName() + "!", null);
								} else if(inventoryFull) {
									return new Response("Give (1)", UtilText.parse(inventoryNPC, "[npc.NamePos] inventory is already full!"), null);
								}
								return new Response("Give (1)", UtilText.parse(inventoryNPC, "Give [npc.name] one " + item.getName() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferItems(GameState.game.getPlayer(), inventoryNPC, item, 1);
									}
								};
								
							} else if(index == 2) {
								if(!item.getItemType().isAbleToBeDropped()) {
									return new Response("Give (5)", "You cannot give away the " + item.getName() + "!", null);
								} else if(inventoryFull) {
									return new Response("Give (5)", UtilText.parse(inventoryNPC, "[npc.NamePos] inventory is already full!"), null);
								}
								if(GameState.game.getPlayer().getItemCount(item) >= 5) {
									return new Response("Give (5)", UtilText.parse(inventoryNPC, "Give [npc.name] five of your " + item.getNamePlural() + "."), INVENTORY_MENU){
										@Override
										public void effects(){
											transferItems(GameState.game.getPlayer(), inventoryNPC, item, 5);
										}
									};
								} else {
									return new Response("Give (5)", "You don't have five " + item.getNamePlural() + " to give!", null);
								}
								
							} else if(index == 3) {
								if(!item.getItemType().isAbleToBeDropped()) {
									return new Response("Give (All)", "You cannot give away the " + item.getName() + "!", null);
								} else if(inventoryFull) {
									return new Response("Give (All)", UtilText.parse(inventoryNPC, "[npc.NamePos] inventory is already full!"), null);
								}
								return new Response("Give (All)", UtilText.parse(inventoryNPC, "Give [npc.name] all of your " + item.getNamePlural() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferItems(GameState.game.getPlayer(), inventoryNPC, item, GameState.game.getPlayer().getItemCount(item));
									}
								};
								
							} else if(index == 5) {
								if(item.getEnchantmentItemType(null)==null || item.getItemTags().contains(ItemTag.UNENCHANTABLE)) {
									return new Response("Enchant", "This item cannot be enchanted!", null);
									
								} else if(GameState.game.isDebugMode()
											|| (GameState.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY) && GameState.game.getPlayer().isQuestCompleted(QuestLine.SIDE_ENCHANTMENT_DISCOVERY))) {
										return new Response("Enchant", "Enchant this item.", EnchantmentDialogue.ENCHANTMENT_MENU) {
											@Override
											public DialogueNode getNextDialogue() {
												return EnchantmentDialogue.getEnchantmentMenu(item);
											}
										};
								
								} else {
									return new Response("Enchant", getEnchantmentNotDiscoveredText("items"), null);
								}
							
							} else if(index == 6) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(GameState.game.getPlayer(), GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), GameState.game.getPlayer()), null);
									
								} else {
									if(item.isBreakOutOfInventory()) {
										return new ResponseEffectsOnly(
												Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
												item.getItemType().getUseTooltipDescription(owner, owner)){
											@Override
											public void effects(){
												GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), false);
												resetPostAction();
											}
										};
									}
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
											item.getItemType().getUseTooltipDescription(owner, owner),
											INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), false) + "</p>");
											resetPostAction();
										}
									};
								}
								
							} else if(index == 7) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if(!item.isAbleToBeUsed(GameState.game.getPlayer(), GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), GameState.game.getPlayer()), null);
									
								} else {
									if(item.isBreakOutOfInventory()) {
										return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", "As this item has special effects, you can only use one at a time!", null);
									}
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)",
											item.getItemType().getUseTooltipDescription(owner, owner)
												+"<br/>[style.italicsMinorGood(Repeat this for all of the " + item.getNamePlural() + " which are in your inventory.)]",
											INVENTORY_MENU){
										@Override
										public void effects(){
											int itemCount = GameState.game.getPlayer().getItemCount(item);
											for(int i=0;i<itemCount;i++) {
												GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), false) + "</p>");
											}
											resetPostAction();
										}
									};
								}
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " ([npc.HerHim])"), item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(GameState.game.getPlayer(), inventoryNPC)) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " ([npc.HerHim])"), item.getUnableToBeUsedDescription(GameState.game.getPlayer(), inventoryNPC), null);
									
								} else if(item.isBreakOutOfInventory()) {
									return new ResponseEffectsOnly(
											Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " ([npc.HerHim])"),
											item.getItemType().getUseTooltipDescription(owner, owner)){
										@Override
										public void effects(){
											GameState.game.getPlayer().useItem(item, inventoryNPC, false);
											resetPostAction();
										}
									};
									
								} else if(item.getItemType().isFetishGiving()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " ([npc.HerHim])"),
											item.getItemType().getUseTooltipDescription(owner, inventoryNPC),
											INVENTORY_MENU,
											Util.newArrayListOfValues(Fetish.FETISH_KINK_GIVING),
											Fetish.FETISH_KINK_GIVING.getAssociatedCorruptionLevel(),
											null,
											null,
											null){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append(inventoryNPC.getItemUseEffects(item, owner, GameState.game.getPlayer(), inventoryNPC).getValue());
											resetPostAction();
										}
									};
								} else if(item.getItemType().isTransformative()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " ([npc.HerHim])"),
											item.getItemType().getUseTooltipDescription(owner, inventoryNPC),
											INVENTORY_MENU,
											Util.newArrayListOfValues(Fetish.FETISH_TRANSFORMATION_GIVING),
											Fetish.FETISH_TRANSFORMATION_GIVING.getAssociatedCorruptionLevel(),
											null,
											null,
											null){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append(inventoryNPC.getItemUseEffects(item, owner, GameState.game.getPlayer(), inventoryNPC).getValue());
											resetPostAction();
										}
									};
									
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " ([npc.HerHim])"),
											item.getItemType().getUseTooltipDescription(owner, inventoryNPC),
											INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append(inventoryNPC.getItemUseEffects(item, owner, GameState.game.getPlayer(), inventoryNPC).getValue());
											resetPostAction();
										}
									};
								}
							} else if(index == 12) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " all ([npc.HerHim])"), item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if(!item.isAbleToBeUsed(GameState.game.getPlayer(), inventoryNPC)) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " all ([npc.HerHim])"), item.getUnableToBeUsedDescription(GameState.game.getPlayer(), inventoryNPC), null);
									
								} else if(item.isBreakOutOfInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " all ([npc.HerHim])"), "As this item has special effects, you can only use one at a time!", null);
									
								} else if(item.getItemType().isFetishGiving()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " all ([npc.HerHim])"),
											item.getItemType().getUseTooltipDescription(owner, inventoryNPC)
												+"<br/>[style.italicsMinorGood(Repeat this for all of the " + item.getNamePlural() + " which are in your inventory.)]",
											INVENTORY_MENU,
											Util.newArrayListOfValues(Fetish.FETISH_KINK_GIVING),
											Fetish.FETISH_KINK_GIVING.getAssociatedCorruptionLevel(),
											null,
											null,
											null){
										@Override
										public void effects(){
											int itemCount = GameState.game.getPlayer().getItemCount(item);
											for(int i=0;i<itemCount;i++) {
												GameState.game.getTextEndStringBuilder().append(inventoryNPC.getItemUseEffects(item, owner, GameState.game.getPlayer(), inventoryNPC).getValue());
											}
											resetPostAction();
										}
									};
								} else if(item.getItemType().isTransformative()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " all ([npc.HerHim])"),
											item.getItemType().getUseTooltipDescription(owner, inventoryNPC)
												+"<br/>[style.italicsMinorGood(Repeat this for all of the " + item.getNamePlural() + " which are in your inventory.)]",
											INVENTORY_MENU,
											Util.newArrayListOfValues(Fetish.FETISH_TRANSFORMATION_GIVING),
											Fetish.FETISH_TRANSFORMATION_GIVING.getAssociatedCorruptionLevel(),
											null,
											null,
											null){
										@Override
										public void effects(){
											int itemCount = GameState.game.getPlayer().getItemCount(item);
											for(int i=0;i<itemCount;i++) {
												GameState.game.getTextEndStringBuilder().append(inventoryNPC.getItemUseEffects(item, owner, GameState.game.getPlayer(), inventoryNPC).getValue());
											}
											resetPostAction();
										}
									};
									
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " all ([npc.HerHim])"),
											item.getItemType().getUseTooltipDescription(owner, inventoryNPC)
												+"<br/>[style.italicsMinorGood(Repeat this for all of the " + item.getNamePlural() + " which are in your inventory.)]",
											INVENTORY_MENU){
										@Override
										public void effects(){
											int itemCount = GameState.game.getPlayer().getItemCount(item);
											for(int i=0;i<itemCount;i++) {
												GameState.game.getTextEndStringBuilder().append(inventoryNPC.getItemUseEffects(item, owner, GameState.game.getPlayer(), inventoryNPC).getValue());
											}
											resetPostAction();
										}
									};
								}
								
							} else {
								return null;
							}
							
						case SEX:
							if(index == 1) {
								return new Response("Give (1)", "You can't give someone items while having sex with them!", null);
								
							} else if(index == 2) {
								return new Response("Give (5)", "You can't give someone items while having sex with them!", null);
								
							} else if(index == 3) {
								return new Response("Give (All)", "You can't give someone items while having sex with them!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant items while having sex with someone!", null);
								
							} else if(index == 6) {
								if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", "You can't use this as you are currently [style.colourTerrible(immobilised)]!", null);
								}
								if(!GameState.sex.isItemUseAvailable()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", "Items cannot be used during this sex scene!", null);
									
								} else if (!item.isAbleToBeUsedInSex()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", "You cannot use this during sex!", null);
									
								} else if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(GameState.game.getPlayer(), GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), GameState.game.getPlayer()), null);
									
								} else {
									if(item.isBreakOutOfInventory()) {
										return new ResponseEffectsOnly(
												Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
												item.getItemType().getUseTooltipDescription(owner, owner)){
											@Override
											public void effects(){
												GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), false);
												resetPostAction();
											}
										};
									} else {
										return new Response(
												Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
												item.getItemType().getUseTooltipDescription(owner, owner),
												GameState.sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												GameState.sex.setUsingItemText(((NPC)GameState.sex.getTargetedPartner(GameState.game.getPlayer())).getItemUseEffects(item, owner, GameState.game.getPlayer(), GameState.game.getPlayer()).getValue());
												resetPostAction();
												GameState.getRenderSink().openInventory();
												GameState.sex.endSexTurn(SexActionUtility.PLAYER_USE_ITEM);
												GameState.sex.setSexStarted(true);
											}
										};
									}
								}
								
							} else if(index == 7) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", "You can only use one item at a time during sex!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Partner)", "You can't use this as you are currently [style.colourTerrible(immobilised)]!", null);
								}
								if(GameState.sex.getInitialSexManager().isHidden(GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Partner)", UtilText.parse(inventoryNPC, "As you're hiding, you can't use items on [npc.name]!"), null);
									
								} else if(!GameState.sex.isItemUseAvailable()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Partner)", "Items cannot be used during this sex scene!", null);
									
								} else if (!item.isAbleToBeUsedInSex()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Partner)", "You cannot use this during sex!", null);
									
								} else if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Partner)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(GameState.game.getPlayer(), inventoryNPC)) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Partner)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), inventoryNPC), null);
									
								} else if(inventoryNPC.isAsleep()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Partner)", UtilText.parse(inventoryNPC, "You cannot use this while [npc.nameIsFull] asleep!"), null);
									
								} else if(item.getItemType().isFetishGiving()) {
									if(item.isBreakOutOfInventory()) {
										return new ResponseEffectsOnly(
												Util.capitaliseSentence(item.getItemType().getUseName()) +" (Partner)",
												item.getItemType().getUseTooltipDescription(owner, inventoryNPC),
												Util.newArrayListOfValues(Fetish.FETISH_KINK_GIVING),
												Fetish.FETISH_KINK_GIVING.getAssociatedCorruptionLevel(),
												null,
												null,
												null){
											@Override
											public void effects(){
												GameState.game.getPlayer().useItem(item, inventoryNPC, false);
												resetPostAction();
											}
										};
									} else {
										return new Response(
												Util.capitaliseSentence(item.getItemType().getUseName()) +" (Partner)",
												item.getItemType().getUseTooltipDescription(owner, inventoryNPC),
												GameState.sex.SEX_DIALOGUE,
												Util.newArrayListOfValues(Fetish.FETISH_KINK_GIVING),
												Fetish.FETISH_KINK_GIVING.getAssociatedCorruptionLevel(),
												null,
												null,
												null){
											@Override
											public void effects(){
												GameState.sex.setUsingItemText(inventoryNPC.getItemUseEffects(item, owner, GameState.game.getPlayer(), inventoryNPC).getValue());
												resetPostAction();
												GameState.getRenderSink().openInventory();
												GameState.sex.endSexTurn(SexActionUtility.PLAYER_USE_ITEM);
												GameState.sex.setSexStarted(true);
											}
										};
									}
									
								} else if(item.getItemType().isTransformative()) {
									if(item.isBreakOutOfInventory()) {
										return new ResponseEffectsOnly(
												Util.capitaliseSentence(item.getItemType().getUseName()) +" (Partner)",
												item.getItemType().getUseTooltipDescription(owner, inventoryNPC),
												Util.newArrayListOfValues(Fetish.FETISH_TRANSFORMATION_GIVING),
												Fetish.FETISH_TRANSFORMATION_GIVING.getAssociatedCorruptionLevel(),
												null,
												null,
												null){
											@Override
											public void effects(){
												GameState.game.getPlayer().useItem(item, inventoryNPC, false);
												resetPostAction();
											}
										};
									} else {
										return new Response(
												Util.capitaliseSentence(item.getItemType().getUseName()) +" (Partner)",
												item.getItemType().getUseTooltipDescription(owner, inventoryNPC),
												GameState.sex.SEX_DIALOGUE,
												Util.newArrayListOfValues(Fetish.FETISH_TRANSFORMATION_GIVING),
												Fetish.FETISH_TRANSFORMATION_GIVING.getAssociatedCorruptionLevel(),
												null,
												null,
												null){
											@Override
											public void effects(){
												GameState.sex.setUsingItemText(inventoryNPC.getItemUseEffects(item, owner, GameState.game.getPlayer(), inventoryNPC).getValue());
												resetPostAction();
												GameState.getRenderSink().openInventory();
												GameState.sex.endSexTurn(SexActionUtility.PLAYER_USE_ITEM);
												GameState.sex.setSexStarted(true);
											}
										};
									}
									
								} else {
									if(item.isBreakOutOfInventory()) {
										return new ResponseEffectsOnly(
												Util.capitaliseSentence(item.getItemType().getUseName()) +" (Partner)",
												item.getItemType().getUseTooltipDescription(owner, inventoryNPC)){
											@Override
											public void effects(){
												GameState.game.getPlayer().useItem(item, inventoryNPC, false);
												resetPostAction();
											}
										};
									} else {
										return new Response(
												Util.capitaliseSentence(item.getItemType().getUseName()) +" (Partner)",
												item.getItemType().getUseTooltipDescription(owner, inventoryNPC),
												GameState.sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												GameState.sex.setUsingItemText(inventoryNPC.getItemUseEffects(item, owner, GameState.game.getPlayer(), inventoryNPC).getValue());
												resetPostAction();
												GameState.getRenderSink().openInventory();
												GameState.sex.endSexTurn(SexActionUtility.PLAYER_USE_ITEM);
												GameState.sex.setSexStarted(true);
											}
										};
									}
								}
								
							} else if(index == 12) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Partner)", "You can only use one item at a time during sex!", null);
								
							} else {
								return null;
							}
							
						case TRADING:
							if(index == 1) {
								if(!item.getItemType().isAbleToBeSold()) {
									return new Response("Sell (1)", "You cannot sell the " + item.getName() + "!", null);
									
								} else if (inventoryNPC.willBuy(item)) {
									int sellPrice = item.getPrice(inventoryNPC.getBuyModifier());
									return new Response("Sell (1) (" + UtilText.formatAsMoney(sellPrice, "span") + ")", "Sell the " + item.getName() + " for " + UtilText.formatAsMoney(sellPrice) + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											sellItems(GameState.game.getPlayer(), inventoryNPC, item, 1, sellPrice);
										}
									};
								} else {
									return new Response("Sell (1)", inventoryNPC.getName("The") + " doesn't want to buy this.", null);
								}
								
							} else if(index == 2) {
								if(GameState.game.getPlayer().getItemCount(item) >= 5) {
									if(!item.getItemType().isAbleToBeSold()) {
										return new Response("Sell (5)", "You cannot sell the " + item.getName() + "!", null);
										
									} else if (inventoryNPC.willBuy(item)) {
										int sellPrice = item.getPrice(inventoryNPC.getBuyModifier());
										return new Response("Sell (5) (" + UtilText.formatAsMoney(sellPrice*5, "span") + ")", "Sell five of your " + item.getNamePlural() + " for " + UtilText.formatAsMoney(sellPrice*5) + ".", INVENTORY_MENU){
											@Override
											public void effects(){
												sellItems(GameState.game.getPlayer(), inventoryNPC, item, 5, sellPrice);
											}
										};
									} else {
										return new Response("Sell (5)", inventoryNPC.getName("The") + " doesn't want to buy these.", null);
									}
									
								} else {
									return new Response("Sell (5)", "You don't have five " + item.getNamePlural() + " to sell!", null);
								}
								
							} else if(index == 3) {
								if(!item.getItemType().isAbleToBeSold()) {
									return new Response("Sell (All)", "You cannot sell the " + item.getName() + "!", null);
									
								} else if (inventoryNPC.willBuy(item)) {
									int sellPrice = item.getPrice(inventoryNPC.getBuyModifier());
									return new Response("Sell (All) (" + UtilText.formatAsMoney(sellPrice*GameState.game.getPlayer().getItemCount(item), "span") + ")",
											"Sell the " + item.getName() + " for " + UtilText.formatAsMoney(sellPrice*GameState.game.getPlayer().getItemCount(item)) + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											sellItems(GameState.game.getPlayer(), inventoryNPC, item, GameState.game.getPlayer().getItemCount(item), sellPrice);
										}
									};
								} else {
									return new Response("Sell (All)", inventoryNPC.getName("The") + " doesn't want to buy these.", null);
								}
								
							} else if(index == 5) {
								if(item.getEnchantmentItemType(null)==null || item.getItemTags().contains(ItemTag.UNENCHANTABLE)) {
									return new Response("Enchant", "This item cannot be enchanted!", null);
									
								} else if(GameState.game.isDebugMode()
										|| (GameState.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY) && GameState.game.getPlayer().isQuestCompleted(QuestLine.SIDE_ENCHANTMENT_DISCOVERY))) {
									return new Response("Enchant", "Enchant this item.", EnchantmentDialogue.ENCHANTMENT_MENU) {
										@Override
										public DialogueNode getNextDialogue() {
											return EnchantmentDialogue.getEnchantmentMenu(item);
										}
									};
									
								} else {
									return new Response("Enchant", getEnchantmentNotDiscoveredText("items"), null);
								}
								
							} else if(index == 6) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if(!item.isAbleToBeUsedWhileTrading()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", item.getUnableToBeUsedWhileTradingDescription(), null);
									
								} else if (!item.isAbleToBeUsed(GameState.game.getPlayer(), GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), GameState.game.getPlayer()), null);
									
								} else {
									if(item.isBreakOutOfInventory()) {
										return new ResponseEffectsOnly(
												Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
												item.getItemType().getUseTooltipDescription(owner, owner)){
											@Override
											public void effects(){
												GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), false);
												resetPostAction();
											}
										};
									}
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
											item.getItemType().getUseTooltipDescription(owner, owner),
											INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), false) + "</p>");
											resetPostAction();
										}
									};
								}
								
							} else if(index == 7) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if(!item.isAbleToBeUsedWhileTrading()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", item.getUnableToBeUsedWhileTradingDescription(), null);
									
								} else if(!item.isAbleToBeUsed(GameState.game.getPlayer(), GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), GameState.game.getPlayer()), null);
									
								} else {
									if(item.isBreakOutOfInventory()) {
										return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", "As this item has special effects, you can only use one at a time!", null);
									}
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)",
											item.getItemType().getUseTooltipDescription(owner, owner)
												+"<br/>[style.italicsMinorGood(Repeat this for all of the " + item.getNamePlural() + " which are in your inventory.)]",
											INVENTORY_MENU){
										@Override
										public void effects(){
											int itemCount = GameState.game.getPlayer().getItemCount(item);
											for(int i=0;i<itemCount;i++) {
												GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), false) + "</p>");
											}
											resetPostAction();
										}
									};
								}
								
							} else if (index == 9) {
								return getBuybackResponse();
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " ([npc.HerHim])"), UtilText.parse(inventoryNPC, "[npc.Name] doesn't want to use your items."), null);
								
							} else if(index == 12) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " all ([npc.HerHim])"), UtilText.parse(inventoryNPC, "[npc.Name] doesn't want to use your items."), null);
								
							} else {
								return null;
							}
					}
				}
				
			// ****************************** ITEM DOES NOT BELONG TO PLAYER ******************************
				
			} else {
				// ****************************** Interacting with the ground ******************************
				if(inventoryNPC == null) {
					boolean inventoryFull = GameState.game.getPlayer().isInventoryFull() && !GameState.game.getPlayer().hasItem(item) && item.getRarity()!=Rarity.QUEST;
					
					switch(interactionType) {
						case SEX:
							if(index == 1) {
								return new Response("Take (1)", "You can't pick up items while masturbating.", null);
								
							} else if(index == 2) {
								return new Response("Take (5)", "You can't pick up items while masturbating.", null);
								
							} else if(index == 3) {
								return new Response("Take (All)", "You can't pick up items while masturbating.", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant items while masturbating.", null);
								
							} else if(index == 6) {
								if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", "You can't use this as you are currently [style.colourTerrible(immobilised)]!", null);
								}
								if(!GameState.sex.isItemUseAvailable()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", "Items cannot be used during this sex scene!", null);
									
								} else if (!item.isAbleToBeUsedInSex()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", "You cannot use this during sex!", null);
									
								} else if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(GameState.game.getPlayer(), GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), GameState.game.getPlayer()), null);
									
								} else {
									if(item.isBreakOutOfInventory()) {
										return new ResponseEffectsOnly(
												Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
												item.getItemType().getUseTooltipDescription(GameState.game.getPlayer(), GameState.game.getPlayer())){
											@Override
											public void effects(){
												GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), true);
												resetPostAction();
											}
										};
									} else {
										return new Response(
												Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
												item.getItemType().getUseTooltipDescription(GameState.game.getPlayer(), GameState.game.getPlayer()),
												GameState.sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												GameState.sex.setUsingItemText(GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), true));
												resetPostAction();
												GameState.getRenderSink().openInventory();
												GameState.sex.endSexTurn(SexActionUtility.PLAYER_USE_ITEM);
												GameState.sex.setSexStarted(true);
											}
										};
									}
								}
								
							} else if(index == 7) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", "You can only use one item at a time during sex!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else {
								return null;
							}
					
						default:
							if(index == 1) {
								if(inventoryFull) {
									return new Response("Take (1)", "Your inventory is already full!", null);
								}
								return new Response("Take (1)", "Take one " + item.getName() + " from the ground.", INVENTORY_MENU){
									@Override
									public void effects(){
										pickUpItems(GameState.game.getPlayer(), item, 1);
									}
								};
								
							} else if(index == 2) {
								if(inventoryFull) {
									return new Response("Take (5)", "Your inventory is already full!", null);
								}
								if(GameState.game.getPlayerCell().getInventory().getItemCount(item) >= 5) {
									return new Response("Take (5)", "Take five of the " + item.getNamePlural() + " from the ground.", INVENTORY_MENU){
										@Override
										public void effects(){
											pickUpItems(GameState.game.getPlayer(), item, 5);
										}
									};
								} else {
									return new Response("Take (5)", "There aren't five " + item.getNamePlural() + " on the ground!", null);
								}
								
							} else if(index == 3) {
								if(inventoryFull) {
									return new Response("Take (All)", "Your inventory is already full!", null);
								}
								return new Response("Take (All)", "Take all of the " + item.getNamePlural() + " from the ground.", INVENTORY_MENU){
									@Override
									public void effects(){
										pickUpItems(GameState.game.getPlayer(), item, GameState.game.getPlayerCell().getInventory().getItemCount(item));
									}
								};
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant items on the ground!", null);
								
							} else if(index == 6) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(GameState.game.getPlayer(), GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), GameState.game.getPlayer()), null);
									
								} else {
									if(item.isBreakOutOfInventory()) {
										return new ResponseEffectsOnly(
												Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
												item.getItemType().getUseTooltipDescription(GameState.game.getPlayer(), GameState.game.getPlayer())){
											@Override
											public void effects(){
												GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), true);
												resetPostAction();
											}
										};
									}
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
											item.getItemType().getUseTooltipDescription(GameState.game.getPlayer(), GameState.game.getPlayer()),
											INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), true) + "</p>");
											resetPostAction();
										}
									};
								}
								
							} else if(index == 7) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if(!item.isAbleToBeUsed(GameState.game.getPlayer(), GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), GameState.game.getPlayer()), null);
									
								} else {
									if(item.isBreakOutOfInventory()) {
										return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" all (Self)", "As this item has special effects, you can only use one at a time!", null);
									}
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)",
											item.getItemType().getUseTooltipDescription(GameState.game.getPlayer(), GameState.game.getPlayer())
											+"<br/>[style.italicsMinorGood(Repeat this for all of the " + item.getNamePlural() + " which are in this area.)]",
											INVENTORY_MENU){
										@Override
										public void effects(){
											int itemCount = GameState.game.getPlayerCell().getInventory().getItemCount(item);
											for(int i=0;i<itemCount;i++) {
												GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), true) + "</p>");
											}
											resetPostAction();
										}
									};
								}
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else {
								return null;
							}
					}
					
				// ****************************** Interacting with an NPC ******************************
				} else {
					boolean inventoryFull = false;
					switch(interactionType) {
						case COMBAT:
							if(index == 1) {
								return new Response("Take (1)", "You can't take someone items while fighting them!", null);
								
							} else if(index == 2) {
								return new Response("Take (5)", "You can't take someone items while fighting them!", null);
								
							} else if(index == 3) {
								return new Response("Take (All)", "You can't take someone items while fighting them!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant someone else's items, especially not while fighting them!", null);
								
							} else if(index == 6) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)", "You can't use someone else's items while fighting them!", null);
								
							} else if(index == 7) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" all (Self)", "You can't use someone else's items while fighting them!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Opponent)", "You can't make someone use an item while fighting them!", null);
								
							} else if(index == 12) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" all (Opponent)", "You can't make someone use an item while fighting them!", null);
								
							} else {
								return null;
							}
							
						case FULL_MANAGEMENT:  case CHARACTER_CREATION:
							inventoryFull = GameState.game.getPlayer().isInventoryFull() && !GameState.game.getPlayer().hasItem(item) && item.getRarity()!=Rarity.QUEST;
						
							if(index == 1) {
								if(inventoryFull) {
									return new Response("Take (1)", "Your inventory is already full!", null);
								}
								return new Response("Take (1)", UtilText.parse(inventoryNPC, "Take " + item.getName() + " from [npc.name]."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferItems(inventoryNPC, GameState.game.getPlayer(), item, 1);
									}
								};
								
							} else if(index == 2) {
								if(inventoryFull) {
									return new Response("Take (5)", "Your inventory is already full!", null);
								}
								if(inventoryNPC.getItemCount(item) >= 5) {
									return new Response("Take (5)", UtilText.parse(inventoryNPC, "Take five of  [npc.namePos] " + item.getNamePlural() + "."), INVENTORY_MENU){
										@Override
										public void effects(){
											transferItems(inventoryNPC, GameState.game.getPlayer(), item, 5);
										}
									};
								} else {
									return new Response("Take (5)", UtilText.parse(inventoryNPC, "[npc.Name] doesn't have five " + item.getNamePlural() + "!"), null);
								}
								
							} else if(index == 3) {
								if(inventoryFull) {
									return new Response("Take (All)", "Your inventory is already full!", null);
								}
								return new Response("Take (All)", UtilText.parse(inventoryNPC, "Take all "+Util.intToString(inventoryNPC.getItemCount(item))+" of  [npc.namePos] " + item.getNamePlural() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferItems(inventoryNPC, GameState.game.getPlayer(), item, inventoryNPC.getItemCount(item));
									}
								};
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant items owned by someone else!", null);
								
							} else if(index == 6) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(GameState.game.getPlayer(), GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), GameState.game.getPlayer()), null);
									
								} else {
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
											item.getItemType().getUseTooltipDescription(GameState.game.getPlayer(), GameState.game.getPlayer()),
											INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), false, false, false) + "</p>");
											if (item.isConsumedOnUse()) {
												inventoryNPC.getInventory().removeItem(item);
											}
											resetPostAction();
										}
									};
								}
								
							} else if(index == 7) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if(!item.isAbleToBeUsed(GameState.game.getPlayer(), GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), GameState.game.getPlayer()), null);
									
								} else {
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)",
											item.getItemType().getUseTooltipDescription(GameState.game.getPlayer(), GameState.game.getPlayer())
												+"<br/>[style.italicsMinorGood(Repeat this for all of the " + item.getNamePlural() + " which are in [npc.namePos] inventory.)]",
											INVENTORY_MENU){
										@Override
										public void effects(){
											int itemCount = inventoryNPC.getItemCount(item);
											for(int i=0;i<itemCount;i++) {
												GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + GameState.game.getPlayer().useItem(item, GameState.game.getPlayer(), false, false, false) + "</p>");
											}
											if (item.isConsumedOnUse()) {
												inventoryNPC.getInventory().removeItem(item, itemCount);
											}
											resetPostAction();
										}
									};
								}
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " ([npc.HerHim])"), item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(GameState.game.getPlayer(), inventoryNPC)) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " ([npc.HerHim])"), item.getUnableToBeUsedDescription(GameState.game.getPlayer(), inventoryNPC), null);
									
								} else if(item.isBreakOutOfInventory()) {
									return new ResponseEffectsOnly(
											Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " ([npc.HerHim])"),
											item.getItemType().getUseTooltipDescription(owner, owner)){
										@Override
										public void effects(){
											GameState.game.getPlayer().useItem(item, inventoryNPC, false);
											resetPostAction();
										}
									};
									
								} else if(item.getItemType().isFetishGiving()) {
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " ([npc.HerHim])"),
											item.getItemType().getUseTooltipDescription(GameState.game.getPlayer(), inventoryNPC),
											INVENTORY_MENU,
											Util.newArrayListOfValues(Fetish.FETISH_KINK_GIVING),
											Fetish.FETISH_KINK_GIVING.getAssociatedCorruptionLevel(),
											null,
											null,
											null){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append(inventoryNPC.getItemUseEffects(item, owner, GameState.game.getPlayer(), inventoryNPC).getValue());
											resetPostAction();
										}
									};
								} else if(item.getItemType().isTransformative()) {
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " ([npc.HerHim])"),
											item.getItemType().getUseTooltipDescription(GameState.game.getPlayer(), inventoryNPC),
											INVENTORY_MENU,
											Util.newArrayListOfValues(Fetish.FETISH_TRANSFORMATION_GIVING),
											Fetish.FETISH_TRANSFORMATION_GIVING.getAssociatedCorruptionLevel(),
											null,
											null,
											null){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append(inventoryNPC.getItemUseEffects(item, owner, GameState.game.getPlayer(), inventoryNPC).getValue());
											resetPostAction();
										}
									};
									
								} else {
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " ([npc.HerHim])"),
											item.getItemType().getUseTooltipDescription(GameState.game.getPlayer(), inventoryNPC),
											INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append(inventoryNPC.getItemUseEffects(item, owner, GameState.game.getPlayer(), inventoryNPC).getValue());
											resetPostAction();
										}
									};
								}
								
							} else if(index == 12) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " all ([npc.HerHim])"), item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if(!item.isAbleToBeUsed(GameState.game.getPlayer(), inventoryNPC)) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " all ([npc.HerHim])"), item.getUnableToBeUsedDescription(GameState.game.getPlayer(), inventoryNPC), null);
									
								} else if(item.isBreakOutOfInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " all ([npc.HerHim])"), "As this item has special effects, you can only use one at a time!", null);
									
								} else if(item.getItemType().isFetishGiving()) {
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " all ([npc.HerHim])"),
											item.getItemType().getUseTooltipDescription(GameState.game.getPlayer(), inventoryNPC)
												+"<br/>[style.italicsMinorGood(Repeat this for all of the " + item.getNamePlural() + " which are in [npc.namePos] inventory.)]",
											INVENTORY_MENU,
											Util.newArrayListOfValues(Fetish.FETISH_KINK_GIVING),
											Fetish.FETISH_KINK_GIVING.getAssociatedCorruptionLevel(),
											null,
											null,
											null){
										@Override
										public void effects(){
											int itemCount = inventoryNPC.getItemCount(item);
											for(int i=0;i<itemCount;i++) {
												GameState.game.getTextEndStringBuilder().append(inventoryNPC.getItemUseEffects(item, owner, GameState.game.getPlayer(), inventoryNPC).getValue());
											}
											resetPostAction();
										}
									};
									
								} else if(item.getItemType().isTransformative()) {
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " all ([npc.HerHim])"),
											item.getItemType().getUseTooltipDescription(GameState.game.getPlayer(), inventoryNPC)
												+"<br/>[style.italicsMinorGood(Repeat this for all of the " + item.getNamePlural() + " which are in [npc.namePos] inventory.)]",
											INVENTORY_MENU,
											Util.newArrayListOfValues(Fetish.FETISH_TRANSFORMATION_GIVING),
											Fetish.FETISH_TRANSFORMATION_GIVING.getAssociatedCorruptionLevel(),
											null,
											null,
											null){
										@Override
										public void effects(){
											int itemCount = inventoryNPC.getItemCount(item);
											for(int i=0;i<itemCount;i++) {
												GameState.game.getTextEndStringBuilder().append(inventoryNPC.getItemUseEffects(item, owner, GameState.game.getPlayer(), inventoryNPC).getValue());
											}
											resetPostAction();
										}
									};
									
								} else {
									return new Response(
											Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " all ([npc.HerHim])"),
											item.getItemType().getUseTooltipDescription(GameState.game.getPlayer(), inventoryNPC)
												+"<br/>[style.italicsMinorGood(Repeat this for all of the " + item.getNamePlural() + " which are in [npc.namePos] inventory.)]",
											INVENTORY_MENU){
										@Override
										public void effects(){
											int itemCount = inventoryNPC.getItemCount(item);
											for(int i=0;i<itemCount;i++) {
												GameState.game.getTextEndStringBuilder().append(inventoryNPC.getItemUseEffects(item, owner, GameState.game.getPlayer(), inventoryNPC).getValue());
											}
											resetPostAction();
										}
									};
								}
								
							} else {
								return null;
							}
							
						case SEX:
							if(index == 1) {
								return new Response("Take (1)", "You can't take someone's items while having sex with them!", null);
								
							} else if(index == 2) {
								return new Response("Take (5)", "You can't take someone's items while having sex with them!", null);
								
							} else if(index == 3) {
								return new Response("Take (All)", "You can't take someone's items while having sex with them!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant someone else's items, especially not while having sex with them!", null);
								
							} else if(index == 6) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", "You can't use your partner's items during sex!", null);
								//TODO
//								if (!item.isAbleToBeUsedInSex()) {
//									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", "This cannot be used during sex!", null);
//									
//								} else if (!item.isAbleToBeUsedFromInventory()) {
//									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Self)", item.getUnableToBeUsedFromInventoryDescription(), null);
//									
//								} else if (!item.isAbleToBeUsed(GameState.game.getPlayer(), GameState.game.getPlayer())) {
//									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), GameState.game.getPlayer()), null);
//									
//								} else {
//									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)",
//											Util.capitaliseSentence(item.getItemType().getUseName()) + " the " + item.getName() + ".", GameState.sex.SEX_DIALOGUE){
//										@Override
//										public void effects(){
//											GameState.sex.setUsingItemText(GameState.sex.getPartner().getItemUseEffects(item, owner, inventoryNPC, GameState.game.getPlayer()));
//											resetPostAction();
//											GameState.getRenderSink().openInventory();
//											GameState.sex.endSexTurn(SexActionUtility.PLAYER_USE_ITEM);
//											GameState.sex.setSexStarted(true);
//										}
//									};
//								}
								
							} else if(index == 7) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Self)", "You can only use one item at a time during sex!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Partner)", "You can't use your partner's items during sex!", null);
								//TODO
//								if (!item.isAbleToBeUsedInSex()) {
//									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Partner)", "This cannot be used during sex!", null);
//									
//								} else if (!item.isAbleToBeUsedFromInventory()) {
//									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (Partner)", item.getUnableToBeUsedFromInventoryDescription(), null);
//									
//								} else if (!item.isAbleToBeUsed(GameState.game.getPlayer(), inventoryNPC)) {
//									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Partner)", item.getUnableToBeUsedDescription(GameState.game.getPlayer(), inventoryNPC), null);
//									
//								} else if(item.getItemType().isFetishGiving()) {
//									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Partner)",
//											"Get "+inventoryNPC.getName("the")+" to "+ item.getItemType().getUseName() + " the " + item.getName() + ".",
//											GameState.sex.SEX_DIALOGUE,
//											Util.newArrayListOfValues(Fetish.FETISH_KINK_GIVING),
//											Fetish.v.getAssociatedCorruptionLevel(),
//											null,
//											null,
//											null){
//										@Override
//										public void effects(){
//											GameState.sex.setUsingItemText(GameState.sex.getPartner().getItemUseEffects(item, owner, inventoryNPC, inventoryNPC));
//											resetPostAction();
//											GameState.getRenderSink().openInventory();
//											GameState.sex.endSexTurn(SexActionUtility.PLAYER_USE_ITEM);
//											GameState.sex.setSexStarted(true);
//										}
//									};
//								} else if(item.getItemType().isTransformative()) {
//									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Partner)",
//											"Get "+inventoryNPC.getName("the")+" to "+ item.getItemType().getUseName() + " the " + item.getName() + ".",
//											GameState.sex.SEX_DIALOGUE,
//											Util.newArrayListOfValues(Fetish.FETISH_TRANSFORMATION_GIVING),
//											Fetish.FETISH_TRANSFORMATION_GIVING.getAssociatedCorruptionLevel(),
//											null,
//											null,
//											null){
//										@Override
//										public void effects(){
//											GameState.sex.setUsingItemText(GameState.sex.getPartner().getItemUseEffects(item, owner, inventoryNPC, inventoryNPC));
//											resetPostAction();
//											GameState.getRenderSink().openInventory();
//											GameState.sex.endSexTurn(SexActionUtility.PLAYER_USE_ITEM);
//											GameState.sex.setSexStarted(true);
//										}
//									};
//									
//								} else {
//									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Partner)",
//											"Get "+inventoryNPC.getName("the")+" to "+ item.getItemType().getUseName() + " the " + item.getName() + ".", GameState.sex.SEX_DIALOGUE){
//										@Override
//										public void effects(){
//											GameState.sex.setUsingItemText(GameState.sex.getPartner().getItemUseEffects(item, owner, inventoryNPC, inventoryNPC));
//											resetPostAction();
//											GameState.getRenderSink().openInventory();
//											GameState.sex.endSexTurn(SexActionUtility.PLAYER_USE_ITEM);
//											GameState.sex.setSexStarted(true);
//										}
//									};
//								}
								
							} else if(index == 12) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (Partner)", "You can only use one item at a time during sex!", null);
								
							} else {
								return null;
							}
							
						case TRADING:
							inventoryFull = GameState.game.getPlayer().isInventoryFull() && !GameState.game.getPlayer().hasItem(item)  && item.getRarity()!=Rarity.QUEST;
							
							if(index == 1) {
								int sellPrice = buyback?GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():item.getPrice(inventoryNPC.getSellModifier(item));
								if(inventoryFull) {
									return new Response("Buy (1) ("+UtilText.formatAsMoneyUncoloured(sellPrice, "span")+")", "Your inventory is already full!", null);
								}
								if(GameState.game.getPlayer().getMoney() < sellPrice) {
									return new Response("Buy (1) ("+UtilText.formatAsMoneyUncoloured(sellPrice, "span")+")", "You can't afford to buy this!", null);
								}
								return new Response("Buy (1) (" + UtilText.formatAsMoney(sellPrice, "span") + ")", "Buy the " + item.getName() + " for " + UtilText.formatAsMoney(sellPrice) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellItems(inventoryNPC, GameState.game.getPlayer(), item, 1, sellPrice);
									}
								};
								
							} else if(index == 2) {
								int sellPrice = buyback?GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():item.getPrice(inventoryNPC.getSellModifier(item));
								if((buyback && GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getCount()<5)
										|| (!buyback && inventoryNPC.getItemCount(item) < 5)) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", UtilText.parse(inventoryNPC, "[npc.Name] doesn't have five "+item.getNamePlural()+"."), null);
								}
								if(inventoryFull) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", "Your inventory is already full!", null);
								}
								if(GameState.game.getPlayer().getMoney() < sellPrice*5) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", "You can't afford to buy this!", null);
								}
								return new Response("Buy (5) (" + UtilText.formatAsMoney(sellPrice*5, "span") + ")", "Buy the " + item.getName() + " for " + UtilText.formatAsMoney(sellPrice*5) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellItems(inventoryNPC, GameState.game.getPlayer(), item, 5, sellPrice);
									}
								};
								
							} else if(index == 3) {
								int sellPrice = buyback?GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():item.getPrice(inventoryNPC.getSellModifier(item));
								int count = buyback?GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getCount():inventoryNPC.getItemCount(item);
								if(inventoryFull) {
									return new Response("Buy (All) ("+UtilText.formatAsMoneyUncoloured(sellPrice*count, "span")+")", "Your inventory is already full!", null);
								}
								if(GameState.game.getPlayer().getMoney() < sellPrice*count) {
									int affordableCount = (int)(GameState.game.getPlayer().getMoney() / sellPrice);
									if(affordableCount > 0) {
										return new Response("Buy (Max " + affordableCount + ") (" + UtilText.formatAsMoney(sellPrice * affordableCount, "span") + ")",
												"Buy the " + item.getName() + " for " + UtilText.formatAsMoney(sellPrice * affordableCount) + ".", INVENTORY_MENU) {
											@Override
											public void effects() {
												sellItems(inventoryNPC, GameState.game.getPlayer(), item, affordableCount, sellPrice);
											}
										};
									} else {
										return new Response("Buy (All) ("+UtilText.formatAsMoneyUncoloured(sellPrice*count, "span")+")", "You can't afford to buy this!", null);
									}
								}
								return new Response("Buy (All) (" + UtilText.formatAsMoney(sellPrice*count, "span") + ")",
										"Buy the " + item.getName() + " for " + UtilText.formatAsMoney(sellPrice*count) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellItems(inventoryNPC, GameState.game.getPlayer(), item, count, sellPrice);
									}
								};
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant someone else's item!", null);
								
							} else if(index == 6) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (Self)", UtilText.parse(inventoryNPC, "[npc.Name] isn't going to let you use [npc.her] items without buying them first."), null);
								
							} else if(index == 7) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" all (Self)", UtilText.parse(inventoryNPC, "[npc.Name] isn't going to let you use [npc.her] items without buying them first."), null);
								
							} else if (index == 9) {
								return getBuybackResponse();
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " ([npc.HerHim])"),
										UtilText.parse(inventoryNPC, "[npc.Name] isn't going to use the items that [npc.sheIs] trying to sell!"),
										null);
								
							} else if(index == 12) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+UtilText.parse(inventoryNPC, " all ([npc.HerHim])"),
										UtilText.parse(inventoryNPC, "[npc.Name] isn't going to use the items that [npc.sheIs] trying to sell!"),
										null);
								
							} else {
								return null;
							}
					}
				}
			}
			return null;
		}

		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.INVENTORY;
		}
	};
	
	public static final DialogueNode WEAPON_INVENTORY = new DialogueNode("Weapon", "", true) {
		
		@Override
		public String getLabel() {
			if (GameState.game.getDialogueFlags().values.contains(DialogueFlagValue.quickTrade) && !GameState.game.isInSex() && !GameState.game.isInCombat()) {
				return "Inventory (Quick-Manage is <b style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>)";
			} else {
				return "Inventory";
			}
		}
		
		@Override
		public String getHeaderContent() {
			return inventoryView();
		}
		
		@Override
		public String getContent() {
			StringBuilder sb = new StringBuilder();
			List<String> extraDescriptions = weapon.getExtraDescriptions(owner);
			if(!extraDescriptions.isEmpty()) {
				sb.append("<p>");
					for(int i=0 ; i<extraDescriptions.size() ; i++) {
						sb.append(extraDescriptions.get(i));
						if(i<extraDescriptions.size()-1) {
							sb.append("<br/>");
						}
					}
				sb.append("</p>");
			}
			return getItemDisplayPanel(weapon,
					weapon.getSVGString(),
					Util.capitaliseSentence(weapon.getDisplayName(true)),
					weapon.getDescription(owner)
						+ sb.toString()
						+ (owner!=null && owner.isPlayer()
								? (inventoryNPC != null && interactionType == InventoryInteraction.TRADING
										? "<p>" 
											+(inventoryNPC.willBuy(weapon)
												?inventoryNPC.getName("The") + " will buy it for " + UtilText.formatAsMoney(weapon.getPrice(inventoryNPC.getBuyModifier())) + "."
												:inventoryNPC.getName("The") + " doesn't want to buy this.")
											+"</p>"
										: "")
								:(inventoryNPC != null && interactionType == InventoryInteraction.TRADING
									? "<p>"
											+ inventoryNPC.getName("The") + " will sell it for " + UtilText.formatAsMoney(weapon.getPrice(inventoryNPC.getSellModifier(weapon))) + "."
										+ "</p>" 
									: "")));
		}


		public String getResponseTabTitle(int index) {
			return getGeneralResponseTabTitle(index);
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return getCloseInventoryResponse();
			}

			if(GameState.game.isBadEnd()) {
				if(index==1) {
					return new Response("Unavailable", "You can't manipulate your inventory during a bad end...", null);
				}
				return null;
			}
			
			if(responseTab==0) {
				return INVENTORY_MENU.getResponse(responseTab, index);
			}
			
			// ****************************** ITEM BELONGS TO THE PLAYER ******************************
			if(owner != null && owner.isPlayer()) {
				
				// ****************************** Interacting with the ground ******************************
				if(inventoryNPC == null) {
					boolean areaFull = GameState.game.isPlayerTileFull() && !GameState.game.getPlayerCell().getInventory().hasWeapon(weapon);
					
					switch(interactionType) {
						case SEX:
							String dropTitle = owner.getLocationPlace().isItemsDisappear()?"Drop ":"Store";
							if(index == 1) {
								return new Response(dropTitle+"(1)", "You can't drop your weapons while masturbating.", null);
								
							} else if(index == 2) {
								return new Response(dropTitle+"(5)", "You can't drop your weapons while masturbating.", null);
								
							} else if(index == 3) {
								return new Response(dropTitle+"(All)", "You can't drop your weapons while masturbating.", null);
								
							} else if(index == 4) {
								return new Response("Dye/Reforge", "You can't dye or reforge your weapons while masturbating.", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant weapons while masturbating.", null);
								
							} else if(index == 6) {
								return new Response("Equip Main (Self)", "You can't equip weapons while masturbating.", null);
								
							} else if(index == 7) {
								return new Response("Equip Offhand (Self)", "You can't equip weapons while masturbating.", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else {
								return null;
							}
							
						default:
							if(index == 1) {
								if(owner.getLocationPlace().isItemsDisappear()) {
									if(!weapon.getWeaponType().isAbleToBeDropped()) {
										return new Response("Drop (1)", "You cannot drop the " + weapon.getName() + "!", null);
										
									} else if(areaFull) {
										return new Response("Drop (1)", "This area is full, so you can't drop your " + weapon.getName() + " here!", null);
										
									} else {
										return new Response("Drop (1)", "Drop your " + weapon.getName() + ".", INVENTORY_MENU){
											@Override
											public void effects(){
												dropWeapons(owner, weapon, 1);
											}
										};
									}
								} else {
									if(!weapon.getWeaponType().isAbleToBeDropped()) {
										return new Response("Store (1)", "You cannot drop the " + weapon.getName() + "!", null);
										
									} else if(areaFull) {
										return new Response("Store (1)", "This area is full, so you can't store your " + weapon.getName() + " here!", null);
									} else {
										return new Response("Store (1)", "Store the " + weapon.getName() + " in this area.", INVENTORY_MENU){
											@Override
											public void effects(){
												dropWeapons(owner, weapon, 1);
											}
										};
									}
								}
								
							} else if(index == 2) {
								if(owner.getLocationPlace().isItemsDisappear()) {
									if(!weapon.getWeaponType().isAbleToBeDropped()) {
										return new Response("Drop (5)", "You cannot drop the " + weapon.getName() + "!", null);
										
									} else if(owner.getWeaponCount(weapon) < 5) {
										return new Response("Drop (5)", "You don't have five " + weapon.getNamePlural() + " to drop!", null);
										
									} else if(areaFull) {
										return new Response("Drop (5)", "This area is full, so you can't drop your " + weapon.getNamePlural() + " here!", null);
										
									} else {
										return new Response("Drop (5)", "Drop five of your " + weapon.getNamePlural() + ".", INVENTORY_MENU){
											@Override
											public void effects(){
												dropWeapons(owner, weapon, 5);
											}
										};
									}
								} else {
									if(!weapon.getWeaponType().isAbleToBeDropped()) {
										return new Response("Store (5)", "You cannot drop the " + weapon.getName() + "!", null);
										
									} else if(owner.getWeaponCount(weapon) < 5) {
										return new Response("Store (5)", "You don't have five " + weapon.getNamePlural() + " to drop!", null);
										
									} else if(areaFull) {
										return new Response("Store (5)", "This area is full, so you can't store your " + weapon.getNamePlural() + " here!", null);
										
									} else {
										return new Response("Store (5)", "Store five of your " + weapon.getNamePlural() + " in this area.", INVENTORY_MENU){
											@Override
											public void effects(){
												dropWeapons(owner, weapon, 5);
											}
										};
									}
								}
								
							} else if(index == 3) {
								if(!weapon.getWeaponType().isAbleToBeDropped()) {
									return new Response("Drop (All)", "You cannot drop the " + weapon.getName() + "!", null);
									
								} else if(owner.getLocationPlace().isItemsDisappear()) {
									if(areaFull) {
										return new Response("Drop (All)", "This area is full, so you can't drop your " + weapon.getNamePlural() + " here!", null);
									} else {
										return new Response("Drop (All)", "Drop all of your " + weapon.getNamePlural() + ".", INVENTORY_MENU){
											@Override
											public void effects(){
												dropWeapons(owner, weapon, owner.getWeaponCount(weapon));
											}
										};
									}
								} else {
									if(!weapon.getWeaponType().isAbleToBeDropped()) {
										return new Response("Store (All)", "You cannot drop the " + weapon.getName() + "!", null);
										
									} else if(areaFull) {
										return new Response("Store (All)", "This area is full, so you can't store your " + weapon.getNamePlural() + " here!", null);
									} else {
										return new Response("Store (All)", "Store all of your " + weapon.getNamePlural() + " in this area.", INVENTORY_MENU){
											@Override
											public void effects(){
												dropWeapons(owner, weapon, owner.getWeaponCount(weapon));
											}
										};
									}
								}
								
							} else if (index==4) {
								if (isWeaponDyeReforgeActionAvailable()) {
									boolean hasFullInventory = GameState.game.getPlayer().isInventoryFull() && weapon.getRarity()!=Rarity.QUEST;
									boolean isDyeingStackItem = GameState.game.getPlayer().getAllWeaponsInInventory().get(weapon) > 1;
									boolean canDye = !(isDyeingStackItem && hasFullInventory);
									if (canDye) {
										return new Response("Dye/Reforge",
												GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
													?"Use your proficiency with [style.colourEarth(Earth spells)] to dye or reforge this item."
													:"Use a dye-brush or reforge hammer to alter this weapon's properties.",
												DYE_WEAPON) {
											@Override
											public void effects() {
												resetWeaponDyeColours();
											}
										};
									} else {
										return new Response("Dye/Reforge", "Your inventory is full, so you can't alter this weapon's properties.", null);
									}
								} else {
									return new Response("Dye/Reforge", "You'll need to find a dye-brush or reforge hammer if you want to alter this weapon's properties.", null);
								}
								
							} else if(index == 5) {
								if(weapon.getEnchantmentItemType(null)==null || weapon.getItemTags().contains(ItemTag.UNENCHANTABLE)) {
									return new Response("Enchant", "This weapon cannot be enchanted!", null);
									
								} else if(GameState.game.isDebugMode()
										|| (GameState.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY) && GameState.game.getPlayer().isQuestCompleted(QuestLine.SIDE_ENCHANTMENT_DISCOVERY))) {
									return new Response("Enchant", "Enchant this weapon.", EnchantmentDialogue.ENCHANTMENT_MENU) {
										@Override
										public DialogueNode getNextDialogue() {
											return EnchantmentDialogue.getEnchantmentMenu(weapon);
										}
									};
									
								} else {
									return new Response("Enchant", getEnchantmentNotDiscoveredText("weapons"), null);
								}
								
							} else if(index == 6) {
								InventorySlot slot = InventorySlot.mainWeaponSlots[GameState.game.getPlayer().getMainWeaponIndexToEquipTo(weapon)];
								if(weapon.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									return new Response("Equip Main (Self)", "Equip the " + weapon.getName() + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append(
												"<p style='text-align:center;'>"
													+ GameState.game.getPlayer().equipMainWeaponFromInventory(weapon, GameState.game.getPlayer())
												+ "</p>");
											resetPostAction();
										}
									};
								} else {
									return new Response("Equip Main (Self)", weapon.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
								
							} else if(index == 7) {
								if(weapon.getWeaponType().isTwoHanded()) {
									return new Response("Equip Offhand (Self)",
											(weapon.getWeaponType().isPlural()
												?"As the " + weapon.getName() + " require two hands to wield, they can only be equipped in the main slot!"
												:"As the " + weapon.getName() + " is a two-handed weapon, it can only be equipped in the main slot!"),
											null);
								}

								InventorySlot slot = InventorySlot.offhandWeaponSlots[GameState.game.getPlayer().getOffhandWeaponIndexToEquipTo(weapon)];
								if(weapon.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									return new Response("Equip Offhand (Self)",
											"Equip the " + weapon.getName() + "."
												+(weapon.getWeaponType().isTwoHanded()
														?"<br/>[style.italicsGood(Although "+(weapon.getWeaponType().isPlural()?"":"")+")]"
														:""),
											INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append(
												"<p style='text-align:center;'>"
													+ GameState.game.getPlayer().equipOffhandWeaponFromInventory(weapon, GameState.game.getPlayer())
												+ "</p>");
											resetPostAction();
										}
									};
								} else {
									return new Response("Equip Offhand (Self)", weapon.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else {
								return null;
							}
					}
					
				// ****************************** Interacting with an NPC ******************************
				} else {
					switch(interactionType) {
						case COMBAT:
							if(index == 1) {
								return new Response("Give (1)", "You can't give someone weapons while fighting them!", null);
								
							} else if(index == 2) {
								return new Response("Give (5)", "You can't give someone weapons while fighting them!", null);
								
							} else if(index == 3) {
								return new Response("Give (All)", "You can't give someone weapons while fighting them!", null);
								
							} else if(index == 4) {
								return new Response("Dye", "You can't dye your weapons while fighting someone!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant weapons while fighting someone!", null);
								
							} else if(index == 6) {
								return new Response("Equip Main (Self)", "You can't change weapons while fighting someone!", null);
									
							} else if(index == 7) {
								return new Response("Equip Offhand (Self)", "You can't change weapons while fighting someone!", null);
									
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response("Equip (Opponent)", "You can't make your opponent equip a weapon!", null);
								
							} else {
								return null;
							}
							
						case FULL_MANAGEMENT:  case CHARACTER_CREATION:
							boolean inventoryFull = inventoryNPC.isInventoryFull() && !inventoryNPC.hasWeapon(weapon);
							
							if(index == 1) {
								if(!weapon.getWeaponType().isAbleToBeDropped()) {
									return new Response("Give (1)", "You cannot give away the " + weapon.getName() + "!", null);
									
								} else if(inventoryFull) {
									return new Response("Give (1)", UtilText.parse(inventoryNPC, "[npc.NamePos] inventory is already full!"), null);
								}
								return new Response("Give (1)", UtilText.parse(inventoryNPC, "Give [npc.name] one " + weapon.getName() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferWeapons(GameState.game.getPlayer(), inventoryNPC, weapon, 1);
									}
								};
								
							} else if(index == 2) {
								if(!weapon.getWeaponType().isAbleToBeDropped()) {
									return new Response("Give (5)", "You cannot give away the " + weapon.getName() + "!", null);
									
								} else if(inventoryFull) {
									return new Response("Give (5)", UtilText.parse(inventoryNPC, "[npc.NamePos] inventory is already full!"), null);
								}
								if(GameState.game.getPlayer().getWeaponCount(weapon) >= 5) {
									return new Response("Give (5)", UtilText.parse(inventoryNPC, "Give [npc.name] five of your " + weapon.getNamePlural() + "."), INVENTORY_MENU){
										@Override
										public void effects(){
											transferWeapons(GameState.game.getPlayer(), inventoryNPC, weapon, 5);
										}
									};
								} else {
									return new Response("Give (5)", "You don't have five " + weapon.getNamePlural() + " to give!", null);
								}
								
							} else if(index == 3) {
								if(!weapon.getWeaponType().isAbleToBeDropped()) {
									return new Response("Give (All)", "You cannot give away the " + weapon.getName() + "!", null);
									
								} else if(inventoryFull) {
									return new Response("Give (All)", UtilText.parse(inventoryNPC, "[npc.NamePos] inventory is already full!"), null);
								}
								return new Response("Give (All)", UtilText.parse(inventoryNPC, "Give [npc.name] all of your " + weapon.getNamePlural() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferWeapons(GameState.game.getPlayer(), inventoryNPC, weapon, GameState.game.getPlayer().getWeaponCount(weapon));
									}
								};
								
							} else if (index==4) {
								if (isWeaponDyeReforgeActionAvailable()) {
									boolean hasFullInventory = GameState.game.getPlayer().isInventoryFull() && weapon.getRarity()!=Rarity.QUEST;
									boolean isDyeingStackItem = GameState.game.getPlayer().getAllWeaponsInInventory().get(weapon) > 1;
									boolean canDye = !(isDyeingStackItem && hasFullInventory);
									if (canDye) {
										return new Response("Dye/Reforge",
												GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
													?"Use your proficiency with [style.colourEarth(Earth spells)] to dye or reforge this item."
													:"Use a dye-brush or reforge hammer to alter this weapon's properties.",
												DYE_WEAPON) {
											@Override
											public void effects() {
												resetWeaponDyeColours();
											}
										};
									} else {
										return new Response("Dye/Reforge", "Your inventory is full, so you can't alter this weapon's properties.", null);
									}
								} else {
									return new Response("Dye/Reforge", "You'll need to find a dye-brush or reforge hammer if you want to alter this weapon's properties.", null);
								}
								
							} else if(index == 5) {
								if(weapon.getEnchantmentItemType(null)==null || weapon.getItemTags().contains(ItemTag.UNENCHANTABLE)) {
									return new Response("Enchant", "This weapon cannot be enchanted!", null);
									
								} else if(GameState.game.isDebugMode()
										|| (GameState.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY) && GameState.game.getPlayer().isQuestCompleted(QuestLine.SIDE_ENCHANTMENT_DISCOVERY))) {
									return new Response("Enchant", "Enchant this weapon.", EnchantmentDialogue.ENCHANTMENT_MENU) {
										@Override
										public DialogueNode getNextDialogue() {
											return EnchantmentDialogue.getEnchantmentMenu(weapon);
										}
									};
									
								} else {
									return new Response("Enchant", getEnchantmentNotDiscoveredText("weapons"), null);
								}
								
							} else if(index == 6) {
								InventorySlot slot = InventorySlot.mainWeaponSlots[GameState.game.getPlayer().getMainWeaponIndexToEquipTo(weapon)];
								if(weapon.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									return new Response("Equip Main (Self)", "Equip the " + weapon.getName() + " as your main weapon.", INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"
													+ GameState.game.getPlayer().equipMainWeaponFromInventory(weapon, GameState.game.getPlayer())
												+ "</p>");
											resetPostAction();
										}
									};
								} else {
									return new Response("Equip Main (Self)", weapon.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
									
							} else if(index == 7) {
								if(weapon.getWeaponType().isTwoHanded()) {
									return new Response("Equip Offhand (Self)",
											(weapon.getWeaponType().isPlural()
												?"As the " + weapon.getName() + " require two hands to wield, they can only be equipped in the main slot!"
												:"As the " + weapon.getName() + " is a two-handed weapon, it can only be equipped in the main slot!"),
											null);
								}

								InventorySlot slot = InventorySlot.offhandWeaponSlots[GameState.game.getPlayer().getOffhandWeaponIndexToEquipTo(weapon)];
								if(weapon.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									return new Response("Equip Offhand (Self)", "Equip the " + weapon.getName() + " as your offhand weapon.", INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"
													+ GameState.game.getPlayer().equipOffhandWeaponFromInventory(weapon, GameState.game.getPlayer())
												+ "</p>");
											resetPostAction();
										}
									};
								} else {
									return new Response("Equip Offhand (Self)", weapon.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
//								if(!weapon.getWeaponType().isAbleToBeDropped()) {
//									return new Response(UtilText.parse(inventoryNPC, "Equip Main ([npc.HerHim])"), "You cannot give away the " + weapon.getName() + "!", null);
//								}

								InventorySlot slot = InventorySlot.mainWeaponSlots[inventoryNPC.getMainWeaponIndexToEquipTo(weapon)];
								if(weapon.isCanBeEquipped(inventoryNPC, slot)) {
									return new Response(UtilText.parse(inventoryNPC, "Equip Main ([npc.HerHim])"), UtilText.parse(inventoryNPC, "Make [npc.name] equip the "+weapon.getName()+" as [npc.her] main weapon."), INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"
												+ inventoryNPC.equipMainWeaponFromInventory(weapon, GameState.game.getPlayer())
												+ "</p>");
											resetPostAction();
										}
									};
								} else {
									return new Response(UtilText.parse(inventoryNPC, "Equip Main ([npc.HerHim])"), weapon.getCannotBeEquippedText(inventoryNPC, slot), null);
								}
								
							
							} else if(index == 12) {
//								if(!weapon.getWeaponType().isAbleToBeDropped()) {
//									return new Response(UtilText.parse(inventoryNPC, "Equip Main ([npc.HerHim])"), "You cannot give away the " + weapon.getName() + "!", null);
//								}
								if(weapon.getWeaponType().isTwoHanded()) {
									return new Response(UtilText.parse(inventoryNPC, "Equip Offhand ([npc.HerHim])"),
											(weapon.getWeaponType().isPlural()
												?"As the " + weapon.getName() + " require two hands to wield, they can only be equipped in the main slot!"
												:"As the " + weapon.getName() + " is a two-handed weapon, it can only be equipped in the main slot!"),
											null);
								}
								InventorySlot slot = InventorySlot.offhandWeaponSlots[inventoryNPC.getOffhandWeaponIndexToEquipTo(weapon)];
								if(weapon.isCanBeEquipped(inventoryNPC, slot)) {
									return new Response(UtilText.parse(inventoryNPC, "Equip Offhand ([npc.HerHim])"), UtilText.parse(inventoryNPC, "Make [npc.name] equip the "+weapon.getName()+" as [npc.her] offhand weapon."), INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"
												+ inventoryNPC.equipOffhandWeaponFromInventory(weapon, GameState.game.getPlayer())
												+ "</p>");
											resetPostAction();
										}
									};
								} else {
									return new Response(UtilText.parse(inventoryNPC, "Equip Offhand ([npc.HerHim])"), weapon.getCannotBeEquippedText(inventoryNPC, slot), null);
								}
							
							} else {
								return null;
							}
							
						case SEX:
							if(index == 1) {
								return new Response("Give (1)", "You can't give someone weapons while having sex with them!", null);
								
							} else if(index == 2) {
								return new Response("Give (5)", "You can't give someone weapons while having sex with them!", null);
								
							} else if(index == 3) {
								return new Response("Give (All)", "You can't give someone weapons while having sex with them!", null);
								
							} else if(index == 4) {
								return new Response("Dye", "You can't dye your weapons while having sex with someone!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant weapons while having sex with someone!", null);
								
							} else if(index == 6) {
								return new Response("Equip Main (Self)", "You can't equip weapons while having sex with someone!", null);
								
							} else if(index == 7) {
								return new Response("Equip Offhand (Self)", "You can't equip weapons while having sex with someone!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.HerHim])"), "You can't equip weapons while having sex with someone!", null);
								
							} else {
								return null;
							}
							
						case TRADING:
							if(index == 1) {
								if(!weapon.getWeaponType().isAbleToBeSold()) {
									return new Response("Sell (1)", "You cannot sell the " + weapon.getName() + "!", null);
									
								} else if (inventoryNPC.willBuy(weapon)) {
									int sellPrice = weapon.getPrice(inventoryNPC.getBuyModifier());
									return new Response("Sell (1) (" + UtilText.formatAsMoney(sellPrice, "span") + ")", "Sell the " + weapon.getName() + " for " + UtilText.formatAsMoney(sellPrice) + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											sellWeapons(GameState.game.getPlayer(), inventoryNPC, weapon, 1, sellPrice);
										}
									};
								} else {
									return new Response("Sell (1)", inventoryNPC.getName("The") + " doesn't want to buy this.", null);
								}
								
							} else if(index == 2) {
								if(GameState.game.getPlayer().getWeaponCount(weapon) >= 5) {
									if(!weapon.getWeaponType().isAbleToBeSold()) {
										return new Response("Sell (5)", "You cannot sell the " + weapon.getName() + "!", null);
										
									} else if (inventoryNPC.willBuy(weapon)) {
										int sellPrice = weapon.getPrice(inventoryNPC.getBuyModifier());
										return new Response("Sell (5) (" + UtilText.formatAsMoney(sellPrice*5, "span") + ")", "Sell five of your " + weapon.getNamePlural() + " for " + UtilText.formatAsMoney(sellPrice*5) + ".", INVENTORY_MENU){
											@Override
											public void effects(){
												sellWeapons(GameState.game.getPlayer(), inventoryNPC, weapon, 5, sellPrice);
											}
										};
									} else {
										return new Response("Sell (5)", inventoryNPC.getName("The") + " doesn't want to buy these.", null);
									}
									
								} else {
									return new Response("Sell (5)", "You don't have five " + weapon.getNamePlural() + " to sell!", null);
								}
								
							} else if(index == 3) {
								if(!weapon.getWeaponType().isAbleToBeSold()) {
									return new Response("Sell (All)", "You cannot sell the " + weapon.getName() + "!", null);
									
								} else if (inventoryNPC.willBuy(weapon)) {
									int sellPrice = weapon.getPrice(inventoryNPC.getBuyModifier());
									return new Response("Sell (All) (" + UtilText.formatAsMoney(sellPrice*GameState.game.getPlayer().getWeaponCount(weapon), "span") + ")",
											"Sell the " + weapon.getName() + " for " + UtilText.formatAsMoney(sellPrice*GameState.game.getPlayer().getWeaponCount(weapon)) + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											sellWeapons(GameState.game.getPlayer(), inventoryNPC, weapon, GameState.game.getPlayer().getWeaponCount(weapon), sellPrice);
										}
									};
								} else {
									return new Response("Sell (All)", inventoryNPC.getName("The") + " doesn't want to buy these.", null);
								}
								
							} else if (index==4) {
								if (isWeaponDyeReforgeActionAvailable()) {
									boolean hasFullInventory = GameState.game.getPlayer().isInventoryFull() && weapon.getRarity()!=Rarity.QUEST;
									boolean isDyeingStackItem = GameState.game.getPlayer().getAllWeaponsInInventory().get(weapon) > 1;
									boolean canDye = !(isDyeingStackItem && hasFullInventory);
									if (canDye) {
										return new Response("Dye/Reforge",
												GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
													?"Use your proficiency with [style.colourEarth(Earth spells)] to dye or reforge this item."
													:"Use a dye-brush or reforge hammer to alter this weapon's properties.",
												DYE_WEAPON) {
											@Override
											public void effects() {
												resetWeaponDyeColours();
											}
										};
									} else {
										return new Response("Dye/Reforge", "Your inventory is full, so you can't alter this weapon's properties.", null);
									}
								} else {
									return new Response("Dye/Reforge", "You'll need to find a dye-brush or reforge hammer if you want to alter this weapon's properties.", null);
								}
								
							} else if(index == 5) {
								if(weapon.getEnchantmentItemType(null)==null || weapon.getItemTags().contains(ItemTag.UNENCHANTABLE)) {
									return new Response("Enchant", "This weapon cannot be enchanted!", null);
									
								} else if(GameState.game.isDebugMode()
										|| (GameState.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY) && GameState.game.getPlayer().isQuestCompleted(QuestLine.SIDE_ENCHANTMENT_DISCOVERY))) {
									return new Response("Enchant", "Enchant this weapon.", EnchantmentDialogue.ENCHANTMENT_MENU) {
										@Override
										public DialogueNode getNextDialogue() {
											return EnchantmentDialogue.getEnchantmentMenu(weapon);
										}
									};
									
								} else {
									return new Response("Enchant", getEnchantmentNotDiscoveredText("weapons"), null);
								}
								
							} else if(index == 6) {
								InventorySlot slot = InventorySlot.mainWeaponSlots[GameState.game.getPlayer().getMainWeaponIndexToEquipTo(weapon)];
								if(weapon.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									return new Response("Equip Main (Self)", "Equip the " + weapon.getName() + " as your main weapon.", INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append(
												"<p style='text-align:center;'>"
													+ GameState.game.getPlayer().equipMainWeaponFromInventory(weapon, GameState.game.getPlayer())
												+ "</p>");
											resetPostAction();
										}
									};
								} else {
									return new Response("Equip Main (Self)", weapon.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
									
							} else if(index == 7) {
								if(weapon.getWeaponType().isTwoHanded()) {
									return new Response("Equip Offhand (Self)",
											(weapon.getWeaponType().isPlural()
												?"As the " + weapon.getName() + " require two hands to wield, they can only be equipped in the main slot!"
												:"As the " + weapon.getName() + " is a two-handed weapon, it can only be equipped in the main slot!"),
											null);
								}
								
								InventorySlot slot = InventorySlot.offhandWeaponSlots[GameState.game.getPlayer().getOffhandWeaponIndexToEquipTo(weapon)];
								if(weapon.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									return new Response("Equip Offhand (Self)", "Equip the " + weapon.getName() + " as your offhand weapon.", INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append(
												"<p style='text-align:center;'>"
													+ GameState.game.getPlayer().equipOffhandWeaponFromInventory(weapon, GameState.game.getPlayer())
												+ "</p>");
											resetPostAction();
										}
									};
								} else {
									return new Response("Equip Offhand (Self)", weapon.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
								
							} else if (index == 9) {
								return getBuybackResponse();
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(UtilText.parse(inventoryNPC, "Equip Main ([npc.HerHim])"), UtilText.parse(inventoryNPC, "[npc.Name] doesn't want to use your weapons."), null);
								
							} else if(index == 12) {
								return new Response(UtilText.parse(inventoryNPC, "Equip Offhand ([npc.HerHim])"), UtilText.parse(inventoryNPC, "[npc.Name] doesn't want to use your weapons."), null);
								
							} else {
								return null;
							}
					}
				}
				
			// ****************************** ITEM DOES NOT BELONG TO PLAYER ******************************
				
			} else {
				// ****************************** Interacting with the ground ******************************
				if(inventoryNPC == null) {
					boolean inventoryFull = GameState.game.getPlayer().isInventoryFull() && !GameState.game.getPlayer().hasWeapon(weapon) && weapon.getRarity()!=Rarity.QUEST;

					switch(interactionType) {
						case SEX:
							if(index == 1) {
								return new Response("Take (1)", "You can't pick up weapons while masturbating.", null);
								
							} else if(index == 2) {
								return new Response("Take (5)", "You can't pick up weapons while masturbating.", null);
								
							} else if(index == 3) {
								return new Response("Take (All)", "You can't pick up weapons while masturbating.", null);
								
							} else if(index == 4) {
								return new Response("Dye", "You can't dye weapons while masturbating.", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant weapons while masturbating.", null);
								
							} else if(index == 6) {
								return new Response("Equip Main (Self)", "You can't equip weapons while masturbating.", null);
								
							} else if(index == 7) {
								return new Response("Equip Offhand (Self)", "You can't equip weapons while masturbating.", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else {
								return null;
							}
							
						default:
							if(index == 1) {
								if(inventoryFull) {
									return new Response("Take (1)", "Your inventory is already full!", null);
								}
								return new Response("Take (1)", "Take one " + weapon.getName() + " from the ground.", INVENTORY_MENU){
									@Override
									public void effects(){
										pickUpWeapons(GameState.game.getPlayer(), weapon, 1);
									}
								};
								
							} else if(index == 2) {
								if(inventoryFull) {
									return new Response("Take (5)", "Your inventory is already full!", null);
								}
								if(GameState.game.getPlayerCell().getInventory().getWeaponCount(weapon) >= 5) {
									return new Response("Take (5)", "Take five of the " + weapon.getNamePlural() + " from the ground.", INVENTORY_MENU){
										@Override
										public void effects(){
											pickUpWeapons(GameState.game.getPlayer(), weapon, 5);
										}
									};
								} else {
									return new Response("Take (5)", "There aren't five " + weapon.getNamePlural() + " on the ground!", null);
								}
								
							} else if(index == 3) {
								if(inventoryFull) {
									return new Response("Take (All)", "Your inventory is already full!", null);
								}
								return new Response("Take (All)", "Take all of the " + weapon.getNamePlural() + " from the ground.", INVENTORY_MENU){
									@Override
									public void effects(){
										pickUpWeapons(GameState.game.getPlayer(), weapon, GameState.game.getPlayerCell().getInventory().getWeaponCount(weapon));
									}
								};
								
							} else if (index==4) {
								if (isWeaponDyeReforgeActionAvailable()) {
									boolean hasFullInventory = GameState.game.getPlayerCell().getInventory().isInventoryFull() && weapon.getRarity()!=Rarity.QUEST;
									boolean isDyeingStackItem = GameState.game.getPlayerCell().getInventory().getAllWeaponsInInventory().get(weapon) > 1;
									boolean canDye = !(isDyeingStackItem && hasFullInventory);
									if (canDye) {
										return new Response("Dye/Reforge",
												GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
													?"Use your proficiency with [style.colourEarth(Earth spells)] to dye or reforge this item."
													:"Use a dye-brush or reforge hammer to alter this weapon's properties.",
												DYE_WEAPON) {
											@Override
											public void effects() {
												resetWeaponDyeColours();
											}
										};
									} else {
										return new Response("Dye/Reforge", "Your inventory is full, so you can't alter this weapon's properties.", null);
									}
								} else {
									return new Response("Dye/Reforge", "You'll need to find a dye-brush or reforge hammer if you want to alter this weapon's properties.", null);
								}
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant weapons on the ground!", null);
								
							} else if(index == 6) {
								InventorySlot slot = InventorySlot.mainWeaponSlots[GameState.game.getPlayer().getMainWeaponIndexToEquipTo(weapon)];
								if(weapon.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									return new Response("Equip Main (Self)", "Equip the " + weapon.getName() + " as your main weapon.", INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"
												+ GameState.game.getPlayer().equipMainWeaponFromFloor(weapon)
												+ "</p>");
											resetPostAction();
										}
									};
								} else {
									return new Response("Equip Main (Self)", weapon.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
								
							} else if(index == 7) {
								InventorySlot slot = InventorySlot.mainWeaponSlots[GameState.game.getPlayer().getMainWeaponIndexToEquipTo(weapon)];
								if(!weapon.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									return new Response("Equip Offhand (Self)", weapon.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
								
								if(weapon.getWeaponType().isTwoHanded()) {
									return new Response("Equip Offhand (Self)",
											(weapon.getWeaponType().isPlural()
												?"As the " + weapon.getName() + " require two hands to wield, they can only be equipped in the main slot!"
												:"As the " + weapon.getName() + " is a two-handed weapon, it can only be equipped in the main slot!"),
											null);
								}
								return new Response("Equip Offhand (Self)", "Equip the " + weapon.getName() + " as your offhand weapon.", INVENTORY_MENU){
									@Override
									public void effects(){
										GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"
											+ GameState.game.getPlayer().equipOffhandWeaponFromFloor(weapon)
											+ "</p>");
										resetPostAction();
									}
								};
									
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else {
								return null;
							}
					}
					
				// ****************************** Interacting with an NPC ******************************
				} else {
					boolean inventoryFull = false;
					switch(interactionType) {
						case COMBAT:
							if(index == 1) {
								return new Response("Take (1)", "You can't take someone weapons while fighting them!", null);
								
							} else if(index == 2) {
								return new Response("Take (5)", "You can't take someone weapons while fighting them!", null);
								
							} else if(index == 3) {
								return new Response("Take (All)", "You can't take someone weapons while fighting them!", null);
								
							} else if(index == 4) {
								return new Response("Dye", "You can't dye someone's weapons while fighting them!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant someone else's weapons, especially not while fighting them!", null);
								
							} else if(index == 6) {
								return new Response("Equip Main (Self)", "You can't use someone else's weapons while fighting them!", null);
								
							} else if(index == 7) {
								return new Response("Equip Offhand (Self)", "You can't use someone else's weapons while fighting them!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response("Equip Main (Opponent)", "You can't make someone use a weapon while fighting them!", null);
								
							} else if(index == 12) {
								return new Response("Equip Offhand (Opponent)", "You can't make someone use a weapon while fighting them!", null);
								
							} else {
								return null;
							}
							
						case FULL_MANAGEMENT:  case CHARACTER_CREATION:
							inventoryFull = GameState.game.getPlayer().isInventoryFull() && !GameState.game.getPlayer().hasWeapon(weapon) && weapon.getRarity()!=Rarity.QUEST;
						
							if(index == 1) {
								if(inventoryFull) {
									return new Response("Take (1)", "Your inventory is already full!", null);
								}
								return new Response("Take (1)", UtilText.parse(inventoryNPC, "Take one " + weapon.getName() + " from [npc.name]."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferWeapons(inventoryNPC, GameState.game.getPlayer(), weapon, 1);
									}
								};
								
							} else if(index == 2) {
								if(inventoryFull) {
									return new Response("Take (5)", "Your inventory is already full!", null);
								}
								if(inventoryNPC.getWeaponCount(weapon) >= 5) {
									return new Response("Take (5)", UtilText.parse(inventoryNPC, "Take five of  [npc.namePos] " + weapon.getNamePlural() + "."), INVENTORY_MENU){
										@Override
										public void effects(){
											transferWeapons(inventoryNPC, GameState.game.getPlayer(), weapon, 5);
										}
									};
								} else {
									return new Response("Take (5)", UtilText.parse(inventoryNPC, "[npc.Name] doesn't have five " + weapon.getNamePlural() + "!"), null);
								}
								
							} else if(index == 3) {
								if(inventoryFull) {
									return new Response("Take (All)", "Your inventory is already full!", null);
								}
								return new Response("Take (All)", UtilText.parse(inventoryNPC, "Take all "+Util.intToString(inventoryNPC.getWeaponCount(weapon))+" of  [npc.namePos] " + weapon.getNamePlural() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferWeapons(inventoryNPC, GameState.game.getPlayer(), weapon, inventoryNPC.getWeaponCount(weapon));
									}
								};
								
							} else if (index==4) {
								if (isWeaponDyeReforgeActionAvailable()) {
									boolean hasFullInventory = inventoryNPC.isInventoryFull() && weapon.getRarity()!=Rarity.QUEST;
									boolean isDyeingStackItem = inventoryNPC.getAllWeaponsInInventory().get(weapon) > 1;
									boolean canDye = !(isDyeingStackItem && hasFullInventory);
									if (canDye) {
										return new Response("Dye/Reforge",
												GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
													?"Use your proficiency with [style.colourEarth(Earth spells)] to dye or reforge this item."
													:"Use a dye-brush or reforge hammer to alter this weapon's properties.",
												DYE_WEAPON) {
											@Override
											public void effects() {
												resetWeaponDyeColours();
											}
										};
									} else {
										return new Response("Dye/Reforge", UtilText.parse(inventoryNPC, "[npc.NamePos] inventory is full, so you can't alter this weapon's properties."), null);
									}
								} else {
									return new Response("Dye/Reforge", UtilText.parse(inventoryNPC, "You'll need to find a dye-brush or reforge hammer if you want to alter the properties of [npc.namePos] weapons."), null);
								}
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant weapons owned by someone else!", null);
								
							} else if(index == 6) {
								InventorySlot slot = InventorySlot.mainWeaponSlots[GameState.game.getPlayer().getMainWeaponIndexToEquipTo(weapon)];
								if(weapon.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									return new Response("Equip Main (Self)", "Equip the " + weapon.getName() + " as your main weapon.", INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"
												+ GameState.game.getPlayer().equipMainWeaponFromInventory(weapon, inventoryNPC)
												+ "</p>");
											resetPostAction();
										}
									};
								} else {
									return new Response("Equip Main (Self)", weapon.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
								
							} else if(index == 7) {
								if(weapon.getWeaponType().isTwoHanded()) {
									return new Response("Equip Offhand (Self)",
											(weapon.getWeaponType().isPlural()
												?"As the " + weapon.getName() + " require two hands to wield, they can only be equipped in the main slot!"
												:"As the " + weapon.getName() + " is a two-handed weapon, it can only be equipped in the main slot!"),
											null);
								}
								
								InventorySlot slot = InventorySlot.offhandWeaponSlots[GameState.game.getPlayer().getOffhandWeaponIndexToEquipTo(weapon)];
								if(weapon.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									return new Response("Equip Offhand (Self)", "Equip the " + weapon.getName() + " as your offhand weapon.", INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"
												+ GameState.game.getPlayer().equipOffhandWeaponFromInventory(weapon, inventoryNPC)
												+ "</p>");
											resetPostAction();
										}
									};
								} else {
									return new Response("Equip Offhand (Self)", weapon.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								InventorySlot slot = InventorySlot.mainWeaponSlots[inventoryNPC.getMainWeaponIndexToEquipTo(weapon)];
								if(weapon.isCanBeEquipped(inventoryNPC, slot)) {
									return new Response(UtilText.parse(inventoryNPC, "Equip Main ([npc.HerHim])"), UtilText.parse(inventoryNPC, "Get [npc.name] to equip the " + weapon.getName() + " as [npc.her] main weapon."), INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"
												+ inventoryNPC.equipMainWeaponFromInventory(weapon, inventoryNPC)
												+ "</p>");
											resetPostAction();
										}
									};
								} else {
									return new Response(UtilText.parse(inventoryNPC, "Equip Main ([npc.HerHim])"), weapon.getCannotBeEquippedText(inventoryNPC, slot), null);
								}
								
							} else if(index == 12) {
								if(weapon.getWeaponType().isTwoHanded()) {
									return new Response(UtilText.parse(inventoryNPC, "Equip Offhand ([npc.HerHim])"),
											(weapon.getWeaponType().isPlural()
												?"As the " + weapon.getName() + " require two hands to wield, they can only be equipped in the main slot!"
												:"As the " + weapon.getName() + " is a two-handed weapon, it can only be equipped in the main slot!"),
											null);
								}
								
								InventorySlot slot = InventorySlot.offhandWeaponSlots[GameState.game.getPlayer().getOffhandWeaponIndexToEquipTo(weapon)];
								if(weapon.isCanBeEquipped(inventoryNPC, slot)) {
									return new Response(UtilText.parse(inventoryNPC, "Equip Offhand ([npc.HerHim])"), UtilText.parse(inventoryNPC, "Get [npc.name] to equip the " + weapon.getName() + " as [npc.her] offhand weapon."), INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>"
												+ inventoryNPC.equipOffhandWeaponFromInventory(weapon, inventoryNPC)
												+ "</p>");
											resetPostAction();
										}
									};
								} else {
									return new Response(UtilText.parse(inventoryNPC, "Equip Offhand ([npc.HerHim])"), weapon.getCannotBeEquippedText(inventoryNPC, slot), null);
								}
								
							} else {
								return null;
							}
							
						case SEX:
							if(index == 1) {
								return new Response("Take (1)", "You can't take someone's weapons while having sex with them!", null);
								
							} else if(index == 2) {
								return new Response("Take (5)", "You can't take someone's weapons while having sex with them!", null);
								
							} else if(index == 3) {
								return new Response("Take (All)", "You can't take someone's weapons while having sex with them!", null);
								
							} else if(index == 4) {
								return new Response("Dye", "You can't dye someone's weapons while having sex with them!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant someone else's weapons, especially not while having sex with them!", null);
								
							} else if(index == 6) {
								return new Response("Equip Main (Self)", "You can't use someone else's weapons while having sex with them!", null);
								
							} else if(index == 7) {
								return new Response("Equip Offhand (Self)", "You can't use someone else's weapons while having sex with them!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response("Equip Main (Opponent)", "You can't make someone use a weapon while having sex with them!", null);
								
							} else if(index == 12) {
								return new Response("Equip Offhand (Opponent)", "You can't make someone use a weapon while having sex with them!", null);
								
							} else {
								return null;
							}
							
						case TRADING:
							inventoryFull = GameState.game.getPlayer().isInventoryFull() && !GameState.game.getPlayer().hasWeapon(weapon) && weapon.getRarity()!=Rarity.QUEST;
							
							if(index == 1) {
								int sellPrice = buyback?GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():weapon.getPrice(inventoryNPC.getSellModifier(weapon));
								if(inventoryFull) {
									return new Response("Buy (1) ("+UtilText.formatAsMoneyUncoloured(sellPrice, "span")+")", "Your inventory is already full!", null);
								}
								if(GameState.game.getPlayer().getMoney() < sellPrice) {
									return new Response("Buy (1) ("+UtilText.formatAsMoneyUncoloured(sellPrice, "span")+")", "You can't afford to buy this!", null);
								}
								return new Response("Buy (1) (" + UtilText.formatAsMoney(sellPrice, "span") + ")", "Buy the " + weapon.getName() + " for " + UtilText.formatAsMoney(sellPrice) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellWeapons(inventoryNPC, GameState.game.getPlayer(), weapon, 1, sellPrice);
									}
								};
								
							} else if(index == 2) {
								int sellPrice = buyback?GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():weapon.getPrice(inventoryNPC.getSellModifier(weapon));
								if((buyback && GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getCount()<5)
										|| (!buyback && inventoryNPC.getWeaponCount(weapon) < 5)) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", UtilText.parse(inventoryNPC, "[npc.Name] doesn't have five "+weapon.getNamePlural()+"."), null);
								}
								if(inventoryFull) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", "Your inventory is already full!", null);
								}
								if(GameState.game.getPlayer().getMoney() < sellPrice*5) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", "You can't afford to buy this!", null);
								}
								return new Response("Buy (5) (" + UtilText.formatAsMoney(sellPrice*5, "span") + ")", "Buy the " + weapon.getName() + " for " + UtilText.formatAsMoney(sellPrice*5) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellWeapons(inventoryNPC, GameState.game.getPlayer(), weapon, 5, sellPrice);
									}
								};
								
							} else if(index == 3) {
								int sellPrice = buyback?GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():weapon.getPrice(inventoryNPC.getSellModifier(weapon));
								int count = buyback?GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getCount():inventoryNPC.getWeaponCount(weapon);
								if(inventoryFull) {
									return new Response("Buy (All) ("+UtilText.formatAsMoneyUncoloured(sellPrice*count, "span")+")", "Your inventory is already full!", null);
								}
								if(GameState.game.getPlayer().getMoney() < sellPrice*count) {
									int affordableCount = (int)(GameState.game.getPlayer().getMoney() / sellPrice);
									if(affordableCount > 0) {
										return new Response("Buy (Max " + affordableCount + ") (" + UtilText.formatAsMoney(sellPrice * affordableCount, "span") + ")",
												"Buy the " + weapon.getName() + " for " + UtilText.formatAsMoney(sellPrice * affordableCount) + ".", INVENTORY_MENU) {
											@Override
											public void effects() {
												sellWeapons(inventoryNPC, GameState.game.getPlayer(), weapon, affordableCount, sellPrice);
											}
										};
									} else {
										return new Response("Buy (All) ("+UtilText.formatAsMoneyUncoloured(sellPrice*count, "span")+")", "You can't afford to buy this!", null);
									}
								}
								return new Response("Buy (All) (" + UtilText.formatAsMoney(sellPrice*count, "span") + ")",
										"Buy the " + weapon.getName() + " for " + UtilText.formatAsMoney(sellPrice*count) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellWeapons(inventoryNPC, GameState.game.getPlayer(), weapon, count, sellPrice);
									}
								};
								
							} else if(index == 4) {
								return new Response("Dye", UtilText.parse(inventoryNPC, "[npc.Name] isn't going to let you dye the weapon that [npc.sheIs] trying to sell!"), null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant someone else's weapon!", null);
								
							} else if(index == 6) {
								return new Response("Equip Main (Self)", UtilText.parse(inventoryNPC, "[npc.Name] isn't going to let you equip [npc.her] weapons without buying them first."), null);
								
							} else if(index == 7) {
								return new Response("Equip Offhand (Self)", UtilText.parse(inventoryNPC, "[npc.Name] isn't going to let you equip [npc.her] weapons without buying them first."), null);
								
							} else if (index == 9) {
								return getBuybackResponse();
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(UtilText.parse(inventoryNPC, "Equip Main ([npc.HerHim])"), UtilText.parse(inventoryNPC, "[npc.Name] isn't going to equip the weapons that [npc.sheIs] trying to sell!"), null);
								
							} else if(index == 12) {
								return new Response(UtilText.parse(inventoryNPC, "Equip Offhand ([npc.HerHim])"), UtilText.parse(inventoryNPC, "[npc.Name] isn't going to equip the weapons that [npc.sheIs] trying to sell!"), null);
								
							} else {
								return null;
							}
					}
				}
			}
			return null;
		}
		
		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.INVENTORY;
		}
	};
	
	public static final DialogueNode CLOTHING_INVENTORY = new DialogueNode("Clothing", "", true) {

		@Override
		public String getLabel() {
			if(!GameState.game.isInNewWorld()) {
				return "Evening's Attire";
			}
			
			if (GameState.game.getDialogueFlags().values.contains(DialogueFlagValue.quickTrade) && !GameState.game.isInSex() && !GameState.game.isInCombat()) {
				return "Inventory (Quick-Manage is <b style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>)";
			} else {
				return "Inventory";
			}
		}
		
		@Override
		public String getHeaderContent() {
			return inventoryView();
		}

		@Override
		public String getContent() {
			StringBuilder sb = new StringBuilder();
			sb.append(clothing.getDescription(owner));
			sb.append("<p>");
				for(String s : clothing.getExtraDescriptions(null, null, true)) {
					sb.append(s+"<br/>");
				}
				for(InventorySlot is : clothing.getClothingType().getEquipSlots()) {
					List<String> descriptions = clothing.getExtraDescriptions(null, is, true);
					if(!descriptions.isEmpty()) {
						sb.append("<i>When equipped into the '"+is.getName()+"' slot:</i><br/>");
						for(String s : clothing.getExtraDescriptions(null, is, true)) {
							sb.append(s+"<br/>");
						}
					}
				}
			sb.append("</p>");
			sb.append((owner!=null && owner.isPlayer()
							? (inventoryNPC != null && interactionType == InventoryInteraction.TRADING
							? "<p>"
								+(inventoryNPC.willBuy(clothing)
									? inventoryNPC.getName("The") + " will buy it for " + UtilText.formatAsMoney(clothing.getPrice(inventoryNPC.getBuyModifier())) + "."
									: inventoryNPC.getName("The") + " doesn't want to buy this.")
								+"</p>"
							: "")
					:(inventoryNPC != null && interactionType == InventoryInteraction.TRADING
						? "<p>"
								+ inventoryNPC.getName("The") + " will sell it for " + UtilText.formatAsMoney(clothing.getPrice(inventoryNPC.getSellModifier(clothing))) + "."
							+ "</p>" 
						: "")));
			
			
			return getItemDisplayPanel(clothing,
						clothing.getSVGString(),
						clothing.getDisplayName(true),
						sb.toString())
					+(interactionType==InventoryInteraction.CHARACTER_CREATION
						?CharacterCreation.getCheckingClothingDescription()
						:"");
		}

		public String getResponseTabTitle(int index) {
			return getGeneralResponseTabTitle(index);
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return getCloseInventoryResponse();
			}

			if(GameState.game.isBadEnd()) {
				if(index==1) {
					return new Response("Unavailable", "You can't manipulate your inventory during a bad end...", null);
				}
				return null;
			}
			
			if(responseTab==0) {
				return INVENTORY_MENU.getResponse(responseTab, index);
			}
			
			// ****************************** ITEM BELONGS TO THE PLAYER ******************************
			if(owner != null && owner.isPlayer()) {
				
				// ****************************** Interacting with the ground ******************************
				if(inventoryNPC == null) {
					boolean areaFull = GameState.game.isPlayerTileFull() && !GameState.game.getPlayerCell().getInventory().hasClothing(clothing);

					switch(interactionType) {
						case SEX:
							String dropTitle = owner.getLocationPlace().isItemsDisappear()?"Drop ":"Store ";
							if(index == 1) {
								return new Response(dropTitle+"(1)", "You can't drop clothing while masturbating.", null);
								
							} else if(index == 2) {
								return new Response(dropTitle+"(5)", "You can't drop clothing while masturbating.", null);
								
							} else if(index == 3) {
								return new Response(dropTitle+"(All)", "You can't drop clothing while masturbating.", null);
								
							} else if(index == 4) {
								return new Response("Dye", "You can't dye your clothing while masturbating.", null);
								
							} else if(index == 5) {
								if(clothing.isCondom()) {
									if(clothing.getCondomEffect().getPotency().isNegative()) {
										return new Response("Repair (<i>1 Essence</i>)", "You can't repair the condom while masturbating.", null);
									}
									return new Response("Sabotage", "You can't sabotage the condom while masturbating.", null);
								}
								return new Response("Enchant", "You can't enchant clothing while masturbating.", null);

							} else if(index >= 6 && index <= 9 && index-6<clothing.getClothingType().getEquipSlots().size()) {
								InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-6);
								if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "You can't equip this as you are currently [style.colourTerrible(immobilised)]!", null);
								}
								if(clothing.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									if(clothing.isAbleToBeEquippedDuringSex(slot).getKey()) {
										if(!GameState.sex.getInitialSexManager().isAbleToEquipSexClothing(GameState.game.getPlayer(), GameState.game.getPlayer(), clothing)) {
											return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "As this is a special sex scene, you cannot equip clothing during it!", null);
										}
										if(!GameState.sex.isClothingEquipAvailable(GameState.game.getPlayer(), slot, clothing)) {
											return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "This slot is involved with an ongoing sex action, so you cannot equip clothing into it!", null);
										}
										if(GameState.game.getPlayer().isAbleToEquip(clothing, slot, false, GameState.game.getPlayer())) {
											return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "Equip the " + clothing.getName() + ".", GameState.sex.SEX_DIALOGUE){
												@Override
												public void effects(){
													AbstractClothing c = clothing;
													equipClothingFromInventory(GameState.game.getPlayer(), slot, GameState.game.getPlayer(), clothing);
													GameState.sex.setEquipClothingText(c, GameState.game.getPlayer().getUnequipDescription());
													GameState.getRenderSink().openInventory();
													GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
													GameState.sex.setSexStarted(true);
												}
											};
										} else {
											return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), getClothingBlockingRemovalText(GameState.game.getPlayer(), "equip"), null);
										}
										
									} else {
										return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), clothing.isAbleToBeEquippedDuringSex(slot).getValue(), null);
									}
									
								} else {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), clothing.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
								
							} else if (index == 10) {
								return getQuickTradeResponse();

							} else {
								return null;
							}
					default:
							if(index == 1) {
								if(owner.getLocationPlace().isItemsDisappear()) {
									if(!clothing.getClothingType().isAbleToBeDropped()) {
										return new Response("Drop (1)", "You cannot drop the " + clothing.getName() + "!", null);
										
									} else if(areaFull) {
										return new Response("Drop (1)", "This area is full, so you can't drop your " + clothing.getName() + " here!", null);
									} else {
										return new Response("Drop (1)", "Drop your " + clothing.getName() + ".", INVENTORY_MENU){
											@Override
											public void effects(){
												dropClothing(owner, clothing, 1);
											}
										};
									}
								} else {
									if(!clothing.getClothingType().isAbleToBeDropped()) {
										return new Response("Store (1)", "You cannot drop the " + clothing.getName() + "!", null);
										
									} else if(areaFull) {
										return new Response("Store (1)", "This area is full, so you can't store your " + clothing.getName() + " here!", null);
									} else {
										return new Response("Store (1)", "Store the " + clothing.getName() + " in this area.", INVENTORY_MENU){
											@Override
											public void effects(){
												dropClothing(owner, clothing, 1);
											}
										};
									}
								}
								
							} else if(index == 2) {
								if(owner.getLocationPlace().isItemsDisappear()) {
									if(!clothing.getClothingType().isAbleToBeDropped()) {
										return new Response("Drop (5)", "You cannot drop the " + clothing.getName() + "!", null);
										
									} else if(owner.getClothingCount(clothing) < 5) {
										return new Response("Drop (5)", "You don't have five " + clothing.getNamePlural() + " to give!", null);
										
									} else if(areaFull) {
										return new Response("Drop (5)", "This area is full, so you can't drop your " + clothing.getNamePlural() + " here!", null);
										
									} else {
										return new Response("Drop (5)", "Drop five of your " + clothing.getNamePlural() + ".", INVENTORY_MENU){
											@Override
											public void effects(){
												dropClothing(owner, clothing, 5);
											}
										};
									}
								} else {
									if(!clothing.getClothingType().isAbleToBeDropped()) {
										return new Response("Store (5)", "You cannot drop the " + clothing.getName() + "!", null);
										
									} else if(owner.getClothingCount(clothing) < 5) {
										return new Response("Store (5)", "You don't have five " + clothing.getNamePlural() + " to give!", null);
										
									} else if(areaFull) {
										return new Response("Store (5)", "This area is full, so you can't store your " + clothing.getNamePlural() + " here!", null);
										
									} else {
										return new Response("Store (5)", "Store five of your " + clothing.getNamePlural() + " in this area.", INVENTORY_MENU){
											@Override
											public void effects(){
												dropClothing(owner, clothing, 5);
											}
										};
									}
								}
								
							} else if(index == 3) {
								if(owner.getLocationPlace().isItemsDisappear()) {
									if(!clothing.getClothingType().isAbleToBeDropped()) {
										return new Response("Drop (All)", "You cannot drop the " + clothing.getName() + "!", null);
										
									} else if(areaFull) {
										return new Response("Drop (All)", "This area is full, so you can't drop your " + clothing.getNamePlural() + " here!", null);
									} else {
										return new Response("Drop (All)", "Drop all of your " + clothing.getNamePlural() + ".", INVENTORY_MENU){
											@Override
											public void effects(){
												dropClothing(owner, clothing, owner.getClothingCount(clothing));
											}
										};
									}
								} else {
									if(!clothing.getClothingType().isAbleToBeDropped()) {
										return new Response("Store (All)", "You cannot drop the " + clothing.getName() + "!", null);
										
									} else if(areaFull) {
										return new Response("Store (All)", "This area is full, so you can't store your " + clothing.getNamePlural() + " here!", null);
									} else {
										return new Response("Store (All)", "Store all of your " + clothing.getNamePlural() + " in this area.", INVENTORY_MENU){
											@Override
											public void effects(){
												dropClothing(owner, clothing, owner.getClothingCount(clothing));
											}
										};
									}
								}
								
							} else if (index==4) {
								if (isClothingDyeActionAvailable()) {
									boolean hasFullInventory = GameState.game.getPlayer().isInventoryFull() && clothing.getRarity()!=Rarity.QUEST;
									boolean isDyeingStackItem = GameState.game.getPlayer().getAllClothingInInventory().get(clothing) > 1;
									boolean canDye = !(isDyeingStackItem && hasFullInventory);
									if (canDye) {
										return new Response("Dye",
												GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
													?"Use your proficiency with [style.colourEarth(Earth spells)] to dye this item of clothing."
													:"Use a dye-brush to dye this item of clothing.",
												DYE_CLOTHING) {
											@Override
											public void effects() {
												resetClothingDyeColours();
											}
										};
									} else {
										return new Response("Dye", "Your inventory is full, so you can't dye this item of clothing.", null);
									}
								} else {
									return new Response("Dye", "You'll need to find a dye-brush if you want to dye your clothes.", null);
								}
								
							} else if(index == 5) {
								if(clothing.isCondom()) {
									return getCondomSabotageResponse(clothing);
								}
								if(GameState.game.isDebugMode()
										|| (GameState.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY) && GameState.game.getPlayer().isQuestCompleted(QuestLine.SIDE_ENCHANTMENT_DISCOVERY))) {
									if(clothing.getEnchantmentItemType(null)==null || clothing.getItemTags().contains(ItemTag.UNENCHANTABLE)) {
										return new Response("Enchant", "This clothing cannot be enchanted!", null);
										
									} else if(!clothing.isEnchantmentKnown()) {
										if(GameState.game.getPlayer().getEssenceCount() >= IDENTIFICATION_ESSENCE_PRICE) {
											return new Response("Identify ([style.italicsArcane("+IDENTIFICATION_ESSENCE_PRICE+" Essences)])",
													"To identify the "+clothing.getName()+", you can either spend "+IDENTIFICATION_ESSENCE_PRICE+" arcane essences to do it yourself,"
															+ " or go to a vendor and pay "+IDENTIFICATION_PRICE+" flames to have them do it for you.",
													CLOTHING_INVENTORY) {
												@Override
												public void effects() {
													GameState.game.getPlayer().incrementEssenceCount(-IDENTIFICATION_ESSENCE_PRICE, false);
													
													String enchantmentRemovedString = clothing.setEnchantmentKnown(owner, true);
													
													clothing = AbstractClothing.enchantmentRemovedClothing;
													
													GameState.game.getTextEndStringBuilder().append(
															"<p>"
																+ "You channel the power of "+Util.intToString(IDENTIFICATION_ESSENCE_PRICE)+" of your arcane essences into the "+clothing.getName()
																	+", and as it emits a faint purple glow, you find yourself able to detect what sort of enchantment it has!"
															+ "</p>"
															+ enchantmentRemovedString
															+ "<p style='text-align:center;'>"
																+ "Identifying the "+clothing.getName()+" has cost you [style.boldBad("+Util.intToString(IDENTIFICATION_ESSENCE_PRICE)+")] [style.boldArcane(Arcane Essences)]!"
															+ "</p>");
													RenderingEngine.setPage(GameState.game.getPlayer(), clothing);
												}
											};
										} else {
											return new Response("Identify (<i>"+IDENTIFICATION_ESSENCE_PRICE+" Essences</i>)",
													"To identify the "+clothing.getName()+", you can either spend "+IDENTIFICATION_ESSENCE_PRICE+" arcane essences to do it yourself ([style.italicsBad(which you don't have)]),"
															+ " or go to a vendor and pay "+IDENTIFICATION_PRICE+" flames to have them do it for you.", null);
										}
									}
									return new Response("Enchant", "Enchant this clothing.", EnchantmentDialogue.ENCHANTMENT_MENU) {
										@Override
										public DialogueNode getNextDialogue() {
											return EnchantmentDialogue.getEnchantmentMenu(clothing);
										}
									};
									
								} else {
									return new Response("Enchant", getEnchantmentNotDiscoveredText("clothing"), null);
								}
								
							} else if(index >= 6 && index <= 9 && index-6<clothing.getClothingType().getEquipSlots().size()) {
								InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-6);
								if(clothing.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "Equip the " + clothing.getName() + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + equipClothingFromInventory(GameState.game.getPlayer(), slot, GameState.game.getPlayer(), clothing) + "</p>");
										}
									};
								} else {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), clothing.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else {
								return null;
							}
					}
					
				// ****************************** Interacting with an NPC ******************************
				} else {
					switch(interactionType) {
						case COMBAT:
							if(index == 1) {
								return new Response("Give (1)", "You can't give someone clothing while fighting them!", null);
								
							} else if(index == 2) {
								return new Response("Give (5)", "You can't give someone clothing while fighting them!", null);
								
							} else if(index == 3) {
								return new Response("Give (All)", "You can't give someone clothing while fighting them!", null);
								
							} else if(index == 4) {
								return new Response("Dye", "You can't dye your clothing while fighting someone!", null);
								
							} else if(index == 5) {
								if(clothing.isCondom()) {
									if(clothing.getCondomEffect().getPotency().isNegative()) {
										return new Response("Repair (<i>1 Essence</i>)", "You can't repair the condom while fighting someone!", null);
									}
									return new Response("Sabotage", "You can't sabotage the condom while fighting someone!", null);
								}
								return new Response("Enchant", "You can't enchant clothing while fighting someone!", null);
								
							} else if(index >= 6 && index <= 9 && index-6<clothing.getClothingType().getEquipSlots().size()) {
								InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-6);
								return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "You cannot change your clothes while fighting someone!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();

							} else if(index >= 11 && index <= 14 && index-11<clothing.getClothingType().getEquipSlots().size()) {
								InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-11);
								return new Response("Equip: "+Util.capitaliseSentence(slot.getName())+" (Opponent)", "You can't make your opponent equip clothing while fighting them!", null);
								
							} else {
								return null;
							}
							
						case FULL_MANAGEMENT: case CHARACTER_CREATION:
							boolean inventoryFull = inventoryNPC.isInventoryFull() && !inventoryNPC.hasClothing(clothing);
							
							if(index == 1) {
								if(!clothing.getClothingType().isAbleToBeDropped()) {
									return new Response("Give (1)", "You cannot give away the " + clothing.getName() + "!", null);
									
								} else if(inventoryFull) {
									return new Response("Give (1)", UtilText.parse(inventoryNPC, "[npc.NamePos] inventory is already full!"), null);
								}
								return new Response("Give (1)", UtilText.parse(inventoryNPC, "Give [npc.name] the " + clothing.getName() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferClothing(GameState.game.getPlayer(), inventoryNPC, clothing, 1);
									}
								};
								
							} else if(index == 2) {
								if(!clothing.getClothingType().isAbleToBeDropped()) {
									return new Response("Give (5)", "You cannot give away the " + clothing.getName() + "!", null);
									
								} else if(inventoryFull) {
									return new Response("Give (5)", UtilText.parse(inventoryNPC, "[npc.NamePos] inventory is already full!"), null);
								}
								if(GameState.game.getPlayer().getClothingCount(clothing) >= 5) {
									return new Response("Give (5)", UtilText.parse(inventoryNPC, "Give [npc.name] five of your " + clothing.getNamePlural() + "."), INVENTORY_MENU){
										@Override
										public void effects(){
											transferClothing(GameState.game.getPlayer(), inventoryNPC, clothing, 5);
										}
									};
								} else {
									return new Response("Give (5)", "You don't have five " + clothing.getNamePlural() + " to give!", null);
								}
								
							} else if(index == 3) {
								if(!clothing.getClothingType().isAbleToBeDropped()) {
									return new Response("Give (All)", "You cannot give away the " + clothing.getName() + "!", null);
									
								} else if(inventoryFull) {
									return new Response("Give (All)", UtilText.parse(inventoryNPC, "[npc.NamePos] inventory is already full!"), null);
								}
								return new Response("Give (All)", UtilText.parse(inventoryNPC, "Give [npc.name] all of your " + clothing.getNamePlural() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferClothing(GameState.game.getPlayer(), inventoryNPC, clothing, GameState.game.getPlayer().getClothingCount(clothing));
									}
								};
								
							} else if (index==4) {
								if (isClothingDyeActionAvailable()) {
									boolean hasFullInventory = GameState.game.getPlayer().isInventoryFull();
									boolean isDyeingStackItem = GameState.game.getPlayer().getAllClothingInInventory().get(clothing) > 1;
									boolean canDye = !(isDyeingStackItem && hasFullInventory);
									if (canDye) {
										return new Response("Dye",
												GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
													?"Use your proficiency with [style.colourEarth(Earth spells)] to dye this item."
													:"Use a dye-brush to dye this item of clothing.",
												DYE_CLOTHING) {
											@Override
											public void effects() {
												resetClothingDyeColours();
											}
										};
									} else {
										return new Response("Dye", "Your inventory is full, so you can't dye this item of clothing.", null);
									}
								} else {
									return new Response("Dye", "You'll need to find a dye-brush if you want to dye your clothes.", null);
								}
								
							} else if(index == 5) {
								if(clothing.isCondom()) {
									return getCondomSabotageResponse(clothing);
								}
								if(GameState.game.isDebugMode()
										|| (GameState.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY) && GameState.game.getPlayer().isQuestCompleted(QuestLine.SIDE_ENCHANTMENT_DISCOVERY))) {
									if(clothing.getEnchantmentItemType(null)==null || clothing.getItemTags().contains(ItemTag.UNENCHANTABLE)) {
										return new Response("Enchant", "This clothing cannot be enchanted!", null);
										
									} else if(!clothing.isEnchantmentKnown()) {
										if(GameState.game.getPlayer().getEssenceCount() >= IDENTIFICATION_ESSENCE_PRICE) {
											return new Response("Identify ([style.italicsArcane("+IDENTIFICATION_ESSENCE_PRICE+" Essences)])",
													"To identify the "+clothing.getName()+", you can either spend "+IDENTIFICATION_ESSENCE_PRICE+" arcane essences to do it yourself,"
															+ " or go to a vendor and pay "+IDENTIFICATION_PRICE+" flames to have them do it for you.",
													CLOTHING_INVENTORY) {
												@Override
												public void effects() {
													GameState.game.getPlayer().incrementEssenceCount(-IDENTIFICATION_ESSENCE_PRICE, false);

													String enchantmentRemovedString = clothing.setEnchantmentKnown(owner, true);
													
													clothing = AbstractClothing.enchantmentRemovedClothing;
													
													GameState.game.getTextEndStringBuilder().append(
															"<p>"
																+ "You channel the power of "+Util.intToString(IDENTIFICATION_ESSENCE_PRICE)+" of your arcane essences into the "+clothing.getName()
																	+", and as it emits a faint purple glow, you find yourself able to detect what sort of enchantment it has!"
															+ "</p>"
															+ enchantmentRemovedString
															+ "<p style='text-align:center;'>"
																+ "Identifying the "+clothing.getName()+" has cost you [style.boldBad("+Util.intToString(IDENTIFICATION_ESSENCE_PRICE)+")] [style.boldArcane(Arcane Essences)]!"
															+ "</p>");
													RenderingEngine.setPage(GameState.game.getPlayer(), clothing);
												}
											};
										} else {
											return new Response("Identify (<i>"+IDENTIFICATION_ESSENCE_PRICE+" Essences</i>)",
													"To identify the "+clothing.getName()+", you can either spend "+IDENTIFICATION_ESSENCE_PRICE+" arcane essences to do it yourself ([style.italicsBad(which you don't have)]),"
															+ " or go to a vendor and pay "+IDENTIFICATION_PRICE+" flames to have them do it for you.", null);
										}
									}
									return new Response("Enchant", "Enchant this clothing.", EnchantmentDialogue.ENCHANTMENT_MENU) {
										@Override
										public DialogueNode getNextDialogue() {
											return EnchantmentDialogue.getEnchantmentMenu(clothing);
										}
									};
									
								} else {
									return new Response("Enchant", getEnchantmentNotDiscoveredText("clothing"), null);
								}
								
							} else if(index >= 6 && index <= 9 && index-6<clothing.getClothingType().getEquipSlots().size()) {
								InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-6);
								if(clothing.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "Equip the " + clothing.getName() + ".", INVENTORY_MENU) {
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + equipClothingFromInventory(GameState.game.getPlayer(), slot, GameState.game.getPlayer(), clothing) + "</p>");
										}
									};
								} else {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), clothing.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
									
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index >= 11 && index <= 14 && index-11<clothing.getClothingType().getEquipSlots().size()) {
								InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-11);
//								if(!clothing.getClothingType().isAbleToBeDropped()) {
//									return new Response(
//											UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
//											"You cannot give away the " + clothing.getName() + "!",
//											null);
//								}
								Value<Boolean, String> equipAllowed = inventoryNPC.isInventoryEquipAllowed(clothing, slot);
								if(!equipAllowed.getKey()) {
									return new Response(
											UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
											UtilText.parse(inventoryNPC, equipAllowed.getValue()),
											null);
								}
								if(clothing.isCanBeEquipped(inventoryNPC, slot)) {
									if(inventoryNPC.isAbleToEquip(clothing, slot, true, GameState.game.getPlayer()) && clothing.isEnslavementClothing() && (!inventoryNPC.isSlave() || !inventoryNPC.getOwner().isPlayer())) {
										boolean willEnslave = !inventoryNPC.isSlave() && inventoryNPC.isAbleToBeEnslaved() && GameState.game.getPlayer().isHasSlaverLicense();
										return new Response(
												UtilText.parse(inventoryNPC,
														!willEnslave
															?"[style.colourMinorBad(Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim]))]"
															:"[style.colourArcane(Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim]))]"),
												UtilText.parse(inventoryNPC,
														"Make [npc.name] equip the "+clothing.getName()+"."
														+(!willEnslave
															?"<br/><i>Despite the fact that the "+clothing.getName()+" "
																	+(clothing.getClothingType().isPlural()?"have an enslavement enchantment, they":"has an enslavement enchantment, it")
																	+" [style.colourMinorBad(will not enslave [npc.name])], as"
																	+ (GameState.game.getPlayer().isHasSlaverLicense()
																			?" [npc.sheIsFull] not a suitable target for enslavement"
																			:" you do not possess a slaver license")
																	+"!</i>"
															:"<br/><i>Thanks to the fact that the "+clothing.getName()+" "
																	+(clothing.getClothingType().isPlural()?"have an enslavement enchantment, they":"has an enslavement enchantment, it")
																	+" [style.colourArcane(will enslave [npc.name])], making you [npc.her] new owner!</i>")),
												INVENTORY_MENU){
											@Override
											public DialogueNode getNextDialogue() {
												if(inventoryNPC.getEnslavementDialogue(clothing)!=null) {
													return inventoryNPC.getEnslavementDialogue(clothing);
													
												} else {
													return INVENTORY_MENU;
												}
											}
											@Override
											public void effects() {
												List<NPC> enslavementTargets = new ArrayList<>(GameState.game.getNonCompanionCharactersPresent());
//												enslavementTargets.removeIf((npc) -> GameState.game.getPlayer().getFriendlyOccupants().contains(npc.getId()));
												enslavementTargets.removeIf((npc) -> !GameState.combat.getEnemies(GameState.game.getPlayer()).contains(npc));
												if(enslavementTargets.size()<=1) {
													SlaveDialogue.setFollowupEnslavementDialogue(GameState.game.getDefaultDialogue(false));
												} else {
													SlaveDialogue.setFollowupEnslavementDialogue(GameState.game.getSavedDialogueNode());
												}
												if(inventoryNPC.getEnslavementDialogue(clothing)==null) {
													GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + equipClothingFromInventory(inventoryNPC, slot, GameState.game.getPlayer(), clothing) + "</p>");
													
												} else {
													equipClothingFromInventory(inventoryNPC, slot, GameState.game.getPlayer(), clothing);
												}
											}
										};
										
									} else {
										return new Response(
												UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
												UtilText.parse(inventoryNPC, "Make [npc.name] equip the "+clothing.getName()+"!"),
												INVENTORY_MENU){
											@Override
											public void effects(){
												GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + equipClothingFromInventory(inventoryNPC, slot, GameState.game.getPlayer(), clothing) + "</p>");
											}
										};
									}
									
								} else {
									return new Response(
											UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
											clothing.getCannotBeEquippedText(inventoryNPC, slot),
											null);
								}
							
							} else {
								return null;
							}
							
						case SEX:
							if(index == 1) {
								return new Response("Give (1)", "You can't give someone clothing while having sex with them!", null);
								
							} else if(index == 2) {
								return new Response("Give (5)", "You can't give someone clothing while having sex with them!", null);
								
							} else if(index == 3) {
								return new Response("Give (All)", "You can't give someone clothing while having sex with them!", null);
								
							} else if(index == 4) {
								return new Response("Dye", "You can't dye your clothing while having sex with someone!", null);
								
							} else if(index == 5) {
								if(clothing.isCondom()) {
									if(clothing.getCondomEffect().getPotency().isNegative()) {
										return new Response("Repair (<i>1 Essence</i>)", "You can't repair the condom while having sex with someone!", null);
									}
									return new Response("Sabotage", "You can't sabotage the condom while having sex with someone!", null);
								}
								return new Response("Enchant", "You can't enchant clothing while having sex with someone!", null);

							} else if(index >= 6 && index <= 9 && index-6<clothing.getClothingType().getEquipSlots().size()) {
								InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-6);
								if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "You can't equip this as you are currently [style.colourTerrible(immobilised)]!", null);
								}
								if(clothing.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									if(clothing.isAbleToBeEquippedDuringSex(slot).getKey()) {
										if(!GameState.sex.getInitialSexManager().isAbleToEquipSexClothing(GameState.game.getPlayer(), GameState.game.getPlayer(), clothing)) {
											return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "As this is a special sex scene, you cannot equip clothing during it!", null);
										}
										if(!GameState.sex.isClothingEquipAvailable(GameState.game.getPlayer(), slot, clothing)) {
											return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "This slot is involved with an ongoing sex action, so you cannot equip clothing into it!", null);
										}
										if (GameState.game.getPlayer().isAbleToEquip(clothing, slot, false, GameState.game.getPlayer())) {
											return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "Equip the " + clothing.getName() + ".", GameState.sex.SEX_DIALOGUE){
												@Override
												public void effects(){
													AbstractClothing c = clothing;
													equipClothingFromInventory(GameState.game.getPlayer(), slot, GameState.game.getPlayer(), clothing);
													GameState.sex.setEquipClothingText(c, GameState.game.getPlayer().getUnequipDescription());
													GameState.getRenderSink().openInventory();
													GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
													GameState.sex.setSexStarted(true);
												}
											};
										} else {
											return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), getClothingBlockingRemovalText(GameState.game.getPlayer(), "equip"), null);
										}
										
									} else {
										return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), clothing.isAbleToBeEquippedDuringSex(slot).getValue(), null);
									}
									
								} else {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), clothing.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
								
							} else if (index == 10) {
								return getQuickTradeResponse();

							} else if(index >= 11 && index <= 14 && index-11<clothing.getClothingType().getEquipSlots().size()) {
								InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-11);
//								if(!clothing.getClothingType().isAbleToBeDropped()) {
//									return new Response(
//											UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
//											"You cannot give away the " + clothing.getName() + "!",
//											null);
//								}
								if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
									return new Response(
											UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
											UtilText.parse(inventoryNPC, "You can't get [npc.name] to equip the " + clothing.getName() + " as you are currently [style.colourTerrible(immobilised)]!"),
											null);
								}
								Value<Boolean, String> equipAllowed = inventoryNPC.isInventoryEquipAllowed(clothing, slot);
								if(!equipAllowed.getKey()) {
									return new Response(
											UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
											UtilText.parse(inventoryNPC, equipAllowed.getValue()),
											null);
								}
								if(clothing.isCanBeEquipped(inventoryNPC, slot)) {
									if(clothing.isAbleToBeEquippedDuringSex(slot).getKey()) {
										if(!GameState.sex.getInitialSexManager().isAbleToEquipSexClothing(GameState.game.getPlayer(), inventoryNPC, clothing)) {
											return new Response(
													UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
													"As this is a special sex scene, you cannot equip clothing during it!",
													null);
										}
										if(!GameState.sex.isClothingEquipAvailable(inventoryNPC, slot, clothing)) {
											return new Response(
													UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
													"This slot is involved with an ongoing sex action, so you cannot equip clothing into it!",
													null);
										}
										if(inventoryNPC.isAbleToEquip(clothing, slot, false, GameState.game.getPlayer())) {
											return new Response(
													UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
													UtilText.parse(inventoryNPC, "Get [npc.name] to equip the " + clothing.getName() + "."),
													GameState.sex.SEX_DIALOGUE){
												@Override
												public void effects(){
													AbstractClothing c = clothing;
													equipClothingFromInventory(inventoryNPC, slot, GameState.game.getPlayer(), clothing);
													GameState.sex.setEquipClothingText(c, inventoryNPC.getUnequipDescription());
													GameState.getRenderSink().openInventory();
													GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
													GameState.sex.setSexStarted(true);
												}
											};
										} else {
											return new Response(UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
													UtilText.parse(inventoryNPC, "[npc.Name] can't equip the " + clothing.getName() + ", as other clothing is blocking [npc.herHim] from doing so!"), null);
										}
										
									} else {
										return new Response(UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"), clothing.isAbleToBeEquippedDuringSex(slot).getValue(), null);
									}
									
								} else {
									return new Response(UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"), clothing.getCannotBeEquippedText(inventoryNPC, slot), null);
								}
								
							} else {
								return null;
							}
							
						case TRADING:
							if(index == 1) {
								if(!clothing.getClothingType().isAbleToBeSold()) {
									return new Response("Sell (1)", "You cannot sell the " + clothing.getName() + "!", null);
									
								} else if (inventoryNPC.willBuy(clothing)) {
									int sellPrice = clothing.getPrice(inventoryNPC.getBuyModifier());
									return new Response("Sell (1) (" + UtilText.formatAsMoney(sellPrice, "span") + ")", "Sell the " + clothing.getName() + " for " + UtilText.formatAsMoney(sellPrice) + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											sellClothing(GameState.game.getPlayer(), inventoryNPC, clothing, 1, sellPrice);
										}
									};
								} else {
									return new Response("Sell (1)", inventoryNPC.getName("The") + " doesn't want to buy this.", null);
								}
								
							} else if(index == 2) {
								if(!clothing.getClothingType().isAbleToBeSold()) {
									return new Response("Sell (5)", "You cannot sell the " + clothing.getName() + "!", null);
									
								} else if(GameState.game.getPlayer().getClothingCount(clothing) >= 5) {
									if (inventoryNPC.willBuy(clothing)) {
										int sellPrice = clothing.getPrice(inventoryNPC.getBuyModifier());
										return new Response("Sell (5) (" + UtilText.formatAsMoney(sellPrice*5, "span") + ")", "Sell five of your " + clothing.getNamePlural() + " for " + UtilText.formatAsMoney(sellPrice*5) + ".", INVENTORY_MENU){
											@Override
											public void effects(){
												sellClothing(GameState.game.getPlayer(), inventoryNPC, clothing, 5, sellPrice);
											}
										};
									} else {
										return new Response("Sell (5)", inventoryNPC.getName("The") + " doesn't want to buy these.", null);
									}
									
								} else {
									return new Response("Sell (5)", "You don't have five " + clothing.getNamePlural() + " to sell!", null);
								}
								
							} else if(index == 3) {
								if(!clothing.getClothingType().isAbleToBeSold()) {
									return new Response("Sell (All)", "You cannot sell the " + clothing.getName() + "!", null);
									
								} else if (inventoryNPC.willBuy(clothing)) {
									int sellPrice = clothing.getPrice(inventoryNPC.getBuyModifier());
									return new Response("Sell (All) (" + UtilText.formatAsMoney(sellPrice*GameState.game.getPlayer().getClothingCount(clothing), "span") + ")",
											"Sell the " + clothing.getName() + " for " + UtilText.formatAsMoney(sellPrice*GameState.game.getPlayer().getClothingCount(clothing)) + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											sellClothing(GameState.game.getPlayer(), inventoryNPC, clothing, GameState.game.getPlayer().getClothingCount(clothing), sellPrice);
										}
									};
								} else {
									return new Response("Sell (All)", inventoryNPC.getName("The") + " doesn't want to buy these.", null);
								}
								
							} else if (index==4) {
								if (isClothingDyeActionAvailable()) {
									boolean hasFullInventory = GameState.game.getPlayer().isInventoryFull() && clothing.getRarity()!=Rarity.QUEST;
									boolean isDyeingStackItem = GameState.game.getPlayer().getAllClothingInInventory().get(clothing) > 1;
									boolean canDye = !(isDyeingStackItem && hasFullInventory);
									if (canDye) {
										return new Response("Dye", 
												GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
													?"Use your proficiency with [style.colourEarth(Earth spells)] to dye this item."
													:"Use a dye-brush to dye this item of clothing.",
												DYE_CLOTHING) {
											@Override
											public void effects() {
												resetClothingDyeColours();
											}
										};
									} else {
										return new Response("Dye", "Your inventory is full, so you can't dye this item of clothing.", null);
									}
								} else {
									return new Response("Dye", "You'll need to find a dye-brush if you want to dye your clothes.", null);
								}
								
							} else if(index == 5) {
								if(clothing.isCondom()) {
									return getCondomSabotageResponse(clothing);
								}
								if(GameState.game.isDebugMode()
										|| (GameState.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY) && GameState.game.getPlayer().isQuestCompleted(QuestLine.SIDE_ENCHANTMENT_DISCOVERY))) {
									if(clothing.getEnchantmentItemType(null)==null || clothing.getItemTags().contains(ItemTag.UNENCHANTABLE)) {
										return new Response("Enchant", "This clothing cannot be enchanted!", null);
										
									} else if(!clothing.isEnchantmentKnown()) {
										if(GameState.game.getPlayer().getEssenceCount() >= IDENTIFICATION_ESSENCE_PRICE) {
											return new Response("Identify ([style.italicsArcane("+IDENTIFICATION_ESSENCE_PRICE+" Essences)])",
													"To identify the "+clothing.getName()+", you can either spend "+IDENTIFICATION_ESSENCE_PRICE+" arcane essences to do it yourself,"
															+ " or go to a vendor and pay "+IDENTIFICATION_PRICE+" flames to have them do it for you.",
													CLOTHING_INVENTORY) {
												@Override
												public void effects() {
													GameState.game.getPlayer().incrementEssenceCount(-IDENTIFICATION_ESSENCE_PRICE, false);

													String enchantmentRemovedString = clothing.setEnchantmentKnown(owner, true);
													
													clothing = AbstractClothing.enchantmentRemovedClothing;
													
													GameState.game.getTextEndStringBuilder().append(
															"<p>"
																+ "You channel the power of "+Util.intToString(IDENTIFICATION_ESSENCE_PRICE)+" of your arcane essences into the "+clothing.getName()
																	+", and as it emits a faint purple glow, you find yourself able to detect what sort of enchantment it has!"
															+ "</p>"
															+ enchantmentRemovedString
															+ "<p style='text-align:center;'>"
																+ "Identifying the "+clothing.getName()+" has cost you [style.boldBad("+Util.intToString(IDENTIFICATION_ESSENCE_PRICE)+")] [style.boldArcane(Arcane Essences)]!"
															+ "</p>");
													RenderingEngine.setPage(GameState.game.getPlayer(), clothing);
												}
											};
										} else {
											return new Response("Identify (<i>"+IDENTIFICATION_ESSENCE_PRICE+" Essences</i>)",
													"To identify the "+clothing.getName()+", you can either spend "+IDENTIFICATION_ESSENCE_PRICE+" arcane essences to do it yourself ([style.italicsBad(which you don't have)]),"
															+ " or go to a vendor and pay "+IDENTIFICATION_PRICE+" flames to have them do it for you.", null);
										}
									}
									return new Response("Enchant", "Enchant this clothing.", EnchantmentDialogue.ENCHANTMENT_MENU) {
										@Override
										public DialogueNode getNextDialogue() {
											return EnchantmentDialogue.getEnchantmentMenu(clothing);
										}
									};
									
								} else {
									return new Response("Enchant", getEnchantmentNotDiscoveredText("clothing"), null);
								}

							} else if(index >= 6 && index <= 9 && index-6<clothing.getClothingType().getEquipSlots().size()) {
								InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-6);
								if(clothing.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "Equip the " + clothing.getName() + ".", INVENTORY_MENU){
											@Override
											public void effects(){
												GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + equipClothingFromInventory(GameState.game.getPlayer(), slot, GameState.game.getPlayer(), clothing) + "</p>");
											}
										};
								} else {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), clothing.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
								
							} else if (index == 9) {
								return getBuybackResponse();
								
							}
//							else if (index == 10) {
//								return getQuickTradeResponse();
//
//							}
							else if(index >= 11 && index <= 14 && index-11<clothing.getClothingType().getEquipSlots().size()) {
								InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-11);
								return new Response(UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"), UtilText.parse(inventoryNPC, "[npc.Name] doesn't want to wear your clothing."), null);
								
							} else if (index == 10 && !clothing.isEnchantmentKnown()) {
								if(!inventoryNPC.willBuy(clothing)) {
									return new Response("Identify", inventoryNPC.getName("The") + " can't identify clothing!", null);
									
								} else if(GameState.game.getPlayer().getMoney() < IDENTIFICATION_PRICE){
									// don't format as money because we don't want to highlight non-selectable choices
									return new Response("Identify (" + UtilText.formatAsMoneyUncoloured(IDENTIFICATION_PRICE, "span") + ")", "You don't have enough money!", null);
									
								}else {
									return new Response("Identify (" + UtilText.formatAsMoney(IDENTIFICATION_PRICE, "span") + ")",
												"Have the " + clothing.getName() + " identified for " + UtilText.formatAsMoney(IDENTIFICATION_PRICE, "span") + ".", CLOTHING_INVENTORY){
										@Override
										public void effects(){
											GameState.game.getPlayer().incrementMoney(-IDENTIFICATION_PRICE);

											String enchantmentRemovedString = clothing.setEnchantmentKnown(owner, true);
											
											clothing = AbstractClothing.enchantmentRemovedClothing;
											
											GameState.game.getTextEndStringBuilder().append(
													"<p style='text-align:center;'>"
														+ UtilText.parse(inventoryNPC,
																"You hand over " + UtilText.formatAsMoney(IDENTIFICATION_PRICE) + " to [npc.name],"
																		+ " who promptly feeds several bottles of arcane essence into a specialist identification device, before using it to reveal the enchantment on your "+clothing.getName()+".")
													+ "</p>"
													+enchantmentRemovedString);
											
											RenderingEngine.setPage(GameState.game.getPlayer(), clothing);
										}
									};
								}
							} else {
								return null;
							}
					}
				}
				
			// ****************************** ITEM DOES NOT BELONG TO PLAYER ******************************
				
			} else {
				// ****************************** Interacting with the ground ******************************
				if(inventoryNPC == null) {

					boolean inventoryFull = GameState.game.getPlayer().isInventoryFull() && !GameState.game.getPlayer().hasClothing(clothing) && clothing.getRarity()!=Rarity.QUEST;
					switch(interactionType) {
						case CHARACTER_CREATION:
							if (index == 1) {
								if(GameState.game.getPlayer().isCoverableAreaVisible(CoverableArea.NIPPLES)
										|| GameState.game.getPlayer().isCoverableAreaVisible(CoverableArea.ANUS)
										|| GameState.game.getPlayer().isCoverableAreaVisible(CoverableArea.PENIS)
										|| GameState.game.getPlayer().isCoverableAreaVisible(CoverableArea.VAGINA)
										|| (GameState.game.getPlayer().getClothingInSlot(InventorySlot.FOOT)==null && GameState.game.getPlayer().getLegType().equals(LegType.HUMAN))) {
									return new Response("To the stage", "You need to be wearing clothing that covers your body, as well as a pair of shoes.", null);
									
								} else {
									return new Response("To the stage", "You're ready to approach the stage now.", CharacterCreation.CHOOSE_BACKGROUND) {
										@Override
										public void effects() {
											CharacterCreation.moveNPCIntoPlayerTile();
										}
									};
								}
								
							} else if(index == 4) {
								if(GameState.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()){
									return new Response("Unequip all", "You're currently naked, there's nothing to be unequipped.", null);
								}
								else{
									return new Response("Unequip all", "Remove as much of your clothing as possible.", INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append(unequipAll(GameState.game.getPlayer()));
										}
									};
								}
								
							} else if(index == 5) {
								return new Response("Change colour", "Change the colour of this item of clothing.", DYE_CLOTHING_CHARACTER_CREATION) {
									@Override
									public void effects() {
										resetClothingDyeColours();
									}
								};
								
							} else if(index >= 6 && index <= 9 && index-6<clothing.getClothingType().getEquipSlots().size()) {
								InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-6);
								if(clothing.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "Equip the " + clothing.getName() + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											equipClothingFromGround(GameState.game.getPlayer(), slot, GameState.game.getPlayer(), clothing);
										}
									};
									
								} else {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), clothing.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
							
							} else {
								return null;
							}
					
					case SEX:
						if(index == 1) {
							return new Response("Take (1)", "You can't pick up clothing while masturbating.", null);
							
						} else if(index == 2) {
							return new Response("Take (5)", "You can't pick up clothing while masturbating.", null);
							
						} else if(index == 3) {
							return new Response("Take (All)", "You can't pick up clothing while masturbating.", null);
							
						} else if(index == 4) {
							return new Response("Dye", "You can't dye your clothing while masturbating.", null);
							
						} else if(index == 5) {
							if(clothing.isCondom()) {
								if(clothing.getCondomEffect().getPotency().isNegative()) {
									return new Response("Repair (<i>1 Essence</i>)", "You can't repair condoms on the ground!", null);
								}
								return new Response("Sabotage", "You can't sabotage condoms on the ground!", null);
							}
							if(!clothing.isEnchantmentKnown()) {
								return new Response("Identify", "You can't identify clothing during sex!", null);
							}
							return new Response("Enchant", "You can't enchant clothing on the ground!", null);

						} else if(index >= 6 && index <= 9 && index-6<clothing.getClothingType().getEquipSlots().size()) {
							InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-6);
							if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
								return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "You can't equip this as you are currently [style.colourTerrible(immobilised)]!", null);
							}
							if(clothing.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
								if(clothing.isAbleToBeEquippedDuringSex(slot).getKey()) {
									if(!GameState.sex.getInitialSexManager().isAbleToEquipSexClothing(GameState.game.getPlayer(), GameState.game.getPlayer(), clothing)) {
										return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "As this is a special sex scene, you cannot equip clothing during it!", null);
									}
									if(!GameState.sex.isClothingEquipAvailable(GameState.game.getPlayer(), slot, clothing)) {
										return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "This slot is involved with an ongoing sex action, so you cannot equip clothing into it!", null);
									}
									if (GameState.game.getPlayer().isAbleToEquip(clothing, slot, false, GameState.game.getPlayer())) {
										return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "Equip the " + clothing.getName() + ".", GameState.sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												AbstractClothing c = clothing;
												equipClothingFromGround(GameState.game.getPlayer(), slot, GameState.game.getPlayer(), clothing);
												GameState.sex.setEquipClothingText(c, GameState.game.getPlayer().getUnequipDescription());
												GameState.getRenderSink().openInventory();
												GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
												GameState.sex.setSexStarted(true);
											}
										};
									} else {
										return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), getClothingBlockingRemovalText(GameState.game.getPlayer(), "equip"), null);
									}
								} else {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), clothing.isAbleToBeEquippedDuringSex(slot).getValue(), null);
								}
							} else {
								return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), clothing.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
							}
							
						} else if (index == 10) {
							return getQuickTradeResponse();

						} else {
							return null;
						}
					
					default:
						if(index == 1) {
							if(inventoryFull) {
								return new Response("Take (1)", "Your inventory is already full!", null);
							}
							return new Response("Take (1)", "Take one " + clothing.getName() + " from the ground.", INVENTORY_MENU){
								@Override
								public void effects(){
									pickUpClothing(GameState.game.getPlayer(), clothing, 1);
								}
							};
							
						} else if(index == 2) {
							if(inventoryFull) {
								return new Response("Take (5)", "Your inventory is already full!", null);
							}
							if(GameState.game.getPlayerCell().getInventory().getClothingCount(clothing) >= 5) {
								return new Response("Take (5)", "Take five of the " + clothing.getNamePlural() + " from the ground.", INVENTORY_MENU){
									@Override
									public void effects(){
										pickUpClothing(GameState.game.getPlayer(), clothing, 5);
									}
								};
							} else {
								return new Response("Take (5)", "There aren't five " + clothing.getNamePlural() + " on the ground!", null);
							}
							
						} else if(index == 3) {
							if(inventoryFull) {
								return new Response("Take (All)", "Your inventory is already full!", null);
							}
							return new Response("Take (All)", "Take all of the " + clothing.getNamePlural() + " from the ground.", INVENTORY_MENU){
								@Override
								public void effects(){
									pickUpClothing(GameState.game.getPlayer(), clothing, GameState.game.getPlayerCell().getInventory().getClothingCount(clothing));
								}
							};
							
						} else if (index==4) {
							if (isClothingDyeActionAvailable()) {
								boolean hasFullInventory = GameState.game.getPlayerCell().getInventory().isInventoryFull();
								boolean isDyeingStackItem = GameState.game.getPlayerCell().getInventory().getAllClothingInInventory().get(clothing) > 1;
								boolean canDye = !(isDyeingStackItem && hasFullInventory);
								if (canDye) {
									return new Response("Dye", 
											GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
												?"Use your proficiency with [style.colourEarth(Earth spells)] to dye this item."
												:"Use a dye-brush to dye this item of clothing.",
											DYE_CLOTHING) {
										@Override
										public void effects() {
											resetClothingDyeColours();
										}
									};
								} else {
									return new Response("Dye", "Your inventory is full, so you can't dye this item of clothing.", null);
								}
							} else {
								return new Response("Dye", "You'll need to find a dye-brush if you want to dye your clothes.", null);
							}
							
						} else if(index == 5) {
							if(clothing.isCondom()) {
								if(clothing.getCondomEffect().getPotency().isNegative()) {
									return new Response("Repair (<i>1 Essence</i>)", "You can't repair condoms on the ground!", null);
								}
								return new Response("Sabotage", "You can't sabotage condoms on the ground!", null);
							}
							if(!clothing.isEnchantmentKnown()) {
								if(GameState.game.getPlayer().getEssenceCount() >= IDENTIFICATION_ESSENCE_PRICE) {
									return new Response("Identify ([style.italicsArcane("+IDENTIFICATION_ESSENCE_PRICE+" Essences)])",
											"To identify the "+clothing.getName()+", you can either spend "+IDENTIFICATION_ESSENCE_PRICE+" arcane essences to do it yourself,"
													+ " or go to a vendor and pay "+IDENTIFICATION_PRICE+" flames to have them do it for you.",
											CLOTHING_INVENTORY) {
										@Override
										public void effects() {
											GameState.game.getPlayer().incrementEssenceCount(-IDENTIFICATION_ESSENCE_PRICE, false);
											
											GameState.game.getPlayerCell().getInventory().removeClothing(clothing);
											String enchantmentRemovedString = clothing.setEnchantmentKnown(owner, true);
											GameState.game.getPlayerCell().getInventory().addClothing(clothing);
											
//											clothing = AbstractClothing.enchantmentRemovedClothing;
											
											GameState.game.getTextEndStringBuilder().append(
													"<p>"
														+ "You channel the power of "+Util.intToString(IDENTIFICATION_ESSENCE_PRICE)+" of your arcane essences into the "+clothing.getName()
															+", and as it emits a faint purple glow, you find yourself able to detect what sort of enchantment it has!"
													+ "</p>"
													+ enchantmentRemovedString
													+ "<p style='text-align:center;'>"
														+ "Identifying the "+clothing.getName()+" has cost you [style.boldBad("+Util.intToString(IDENTIFICATION_ESSENCE_PRICE)+")] [style.boldArcane(Arcane Essences)]!"
													+ "</p>");
											RenderingEngine.setPage(GameState.game.getPlayer(), clothing);
										}
									};
								} else {
									return new Response("Identify (<i>"+IDENTIFICATION_ESSENCE_PRICE+" Essences</i>)",
											"To identify the "+clothing.getName()+", you can either spend "+IDENTIFICATION_ESSENCE_PRICE+" arcane essences to do it yourself ([style.italicsBad(which you don't have)]),"
													+ " or go to a vendor and pay "+IDENTIFICATION_PRICE+" flames to have them do it for you.", null);
								}
							}
							return new Response("Enchant", "You can't enchant clothing on the ground!", null);
							
						} else if(index >= 6 && index <= 9 && index-6<clothing.getClothingType().getEquipSlots().size()) {
							InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-6);
							if(clothing.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
								return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "Equip the " + clothing.getName() + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + equipClothingFromGround(GameState.game.getPlayer(), slot, GameState.game.getPlayer(), clothing) + "</p>");
									}
								};
							} else {
								return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), clothing.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
							}
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else {
							return null;
						}
					}
					
				// ****************************** Interacting with an NPC ******************************
				} else {
					boolean inventoryFull = false;
					switch(interactionType) {
						case COMBAT:
							if(index == 1) {
								return new Response("Take (1)", "You can't take someone clothing while fighting them!", null);
								
							} else if(index == 2) {
								return new Response("Take (5)", "You can't take someone clothing while fighting them!", null);
								
							} else if(index == 3) {
								return new Response("Take (All)", "You can't take someone clothing while fighting them!", null);
								
							} else if(index == 4) {
								return new Response("Dye", "You can't dye someone's clothing while fighting them!", null);
								
							} else if(index == 5) {
								if(clothing.isCondom()) {
									if(clothing.getCondomEffect().getPotency().isNegative()) {
										return new Response("Repair (<i>1 Essence</i>)", "You can't repair someone else's condom, especially not when fighting them!", null);
									}
									return new Response("Sabotage", "You can't sabotage someone else's condom, especially not while fighting them!", null);
								}
								return new Response("Enchant", "You can't enchant someone else's clothing, especially not while fighting them!", null);
								
							} else if(index == 6) {
								return new Response("Equip (Self)", "You can't use someone else's clothing while fighting them!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(UtilText.parse(inventoryNPC, "Equip: ([npc.HerHim])"), "You can't make someone wear clothing while fighting them!", null);
								
							} else {
								return null;
							}
							
						case FULL_MANAGEMENT: case CHARACTER_CREATION:
							inventoryFull = GameState.game.getPlayer().isInventoryFull() && !GameState.game.getPlayer().hasClothing(clothing) && clothing.getRarity()!=Rarity.QUEST;
						
							if(index == 1) {
								if(inventoryFull) {
									return new Response("Take (1)", "Your inventory is already full!", null);
								}
								return new Response("Take (1)", UtilText.parse(inventoryNPC, "Take the " + clothing.getName() + " from [npc.name]."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferClothing(inventoryNPC, GameState.game.getPlayer(), clothing, 1);
									}
								};
								
							} else if(index == 2) {
								if(inventoryFull) {
									return new Response("Take (5)", "Your inventory is already full!", null);
								}
								if(inventoryNPC.getClothingCount(clothing) >= 5) {
									return new Response("Take (5)", UtilText.parse(inventoryNPC, "Take five of  [npc.namePos] " + clothing.getNamePlural() + "."), INVENTORY_MENU){
										@Override
										public void effects(){
											transferClothing(inventoryNPC, GameState.game.getPlayer(), clothing, 5);
										}
									};
								} else {
									return new Response("Take (5)", UtilText.parse(inventoryNPC, "[npc.Name] doesn't have five " + clothing.getNamePlural() + "!"), null);
								}
								
							} else if(index == 3) {
								if(inventoryFull) {
									return new Response("Take (All)", "Your inventory is already full!", null);
								}
								return new Response("Take (All)", UtilText.parse(inventoryNPC, "Take all "+Util.intToString(inventoryNPC.getClothingCount(clothing))+" of  [npc.namePos] " + clothing.getNamePlural() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferClothing(inventoryNPC, GameState.game.getPlayer(), clothing, inventoryNPC.getClothingCount(clothing));
									}
								};
								
							} else if (index==4) {
								if (isClothingDyeActionAvailable()) {
									boolean hasFullInventory = inventoryNPC.isInventoryFull();
									boolean isDyeingStackItem = clothing!=null && inventoryNPC.getAllClothingInInventory().get(clothing) > 1;
									boolean canDye = !(isDyeingStackItem && hasFullInventory);
									if (canDye) {
										return new Response("Dye", 
												GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
													?"Use your proficiency with [style.colourEarth(Earth spells)] to dye this item."
													:"Use a dye-brush to dye this item of clothing.",
												DYE_CLOTHING) {
											@Override
											public void effects() {
												resetClothingDyeColours();
											}
										};
									} else {
										return new Response("Dye", UtilText.parse(inventoryNPC, "[npc.NamePos] inventory is full, so you can't dye this item of clothing."), null);
									}
								} else {
									return new Response("Dye", UtilText.parse(inventoryNPC, "You'll need to find another dye-brush if you want to dye [npc.namePos] clothes."), null);
								}
								
							} else if(index == 5) {
								if(clothing.isCondom()) {
									if(clothing.getCondomEffect().getPotency().isNegative()) {
										return new Response("Repair (<i>1 Essence</i>)", "You can't repair condoms owned by someone else!", null);
									}
									return new Response("Sabotage", "You can't sabotage condoms owned by someone else!", null);
								}
								return new Response("Enchant", "You can't enchant clothing owned by someone else!", null);

							} else if(index >= 6 && index <= 9 && index-6<clothing.getClothingType().getEquipSlots().size()) {
								InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-6);
								if(clothing.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "Equip the " + clothing.getName() + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + equipClothingFromInventory(GameState.game.getPlayer(), slot, GameState.game.getPlayer(), clothing) + "</p>");
										}
									};
								} else {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), clothing.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
								
							} else if (index == 10) {
								return getQuickTradeResponse();

							} else if(index >= 11 && index <= 14 && index-11<clothing.getClothingType().getEquipSlots().size()) {
								InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-11);
								Value<Boolean, String> equipAllowed = inventoryNPC.isInventoryEquipAllowed(clothing, slot);
								if(!equipAllowed.getKey()) {
									return new Response(
											UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
											UtilText.parse(inventoryNPC, equipAllowed.getValue()),
											null);
								}
								if(clothing.isCanBeEquipped(inventoryNPC, slot)) {
									if(inventoryNPC.isAbleToEquip(clothing, slot, true, GameState.game.getPlayer()) && clothing.isEnslavementClothing() && (!inventoryNPC.isSlave() || !inventoryNPC.getOwner().isPlayer())) {
										boolean willEnslave = !inventoryNPC.isSlave() && inventoryNPC.isAbleToBeEnslaved() && GameState.game.getPlayer().isHasSlaverLicense();
										return new Response(
												UtilText.parse(inventoryNPC,
														!willEnslave
															?"[style.colourMinorBad(Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim]))]"
															:"[style.colourArcane(Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim]))]"),
												UtilText.parse(inventoryNPC,
														"Make [npc.name] equip the "+clothing.getName()+"."
														+(!willEnslave
															?"<br/><i>Despite the fact that the "+clothing.getName()+" "
																	+(clothing.getClothingType().isPlural()?"have an enslavement enchantment, they":"has an enslavement enchantment, it")
																	+" [style.colourMinorBad(will not enslave [npc.name])], as"
																	+ (GameState.game.getPlayer().isHasSlaverLicense()
																			?" [npc.sheIsFull] not a suitable target for enslavement"
																			:" you do not possess a slaver license")
																	+"!</i>"
															:"<br/><i>Thanks to the fact that the "+clothing.getName()+" "
																	+(clothing.getClothingType().isPlural()?"have an enslavement enchantment, they":"has an enslavement enchantment, it")
																	+" [style.colourArcane(will enslave [npc.name])], making you [npc.her] new owner!</i>")),
												INVENTORY_MENU){
											@Override
											public DialogueNode getNextDialogue() {
												if(inventoryNPC.getEnslavementDialogue(clothing)!=null) {//inventoryNPC.isAbleToBeEnslaved() && !inventoryNPC.isSlave()) {
													return inventoryNPC.getEnslavementDialogue(clothing);
													
												} else {
													return INVENTORY_MENU;
												}
											}
											@Override
											public void effects() {
												List<NPC> enslavementTargets = new ArrayList<>(GameState.game.getNonCompanionCharactersPresent());
//												enslavementTargets.removeIf((npc) -> GameState.game.getPlayer().getFriendlyOccupants().contains(npc.getId()));
												enslavementTargets.removeIf((npc) -> !GameState.combat.getEnemies(GameState.game.getPlayer()).contains(npc));
												if(enslavementTargets.size()<=1) {
													SlaveDialogue.setFollowupEnslavementDialogue(GameState.game.getDefaultDialogue(false));
												} else {
													SlaveDialogue.setFollowupEnslavementDialogue(GameState.game.getSavedDialogueNode());
												}
												if(inventoryNPC.getEnslavementDialogue(clothing)==null) {
													GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + equipClothingFromInventory(inventoryNPC, slot, GameState.game.getPlayer(), clothing) + "</p>");
													
												} else {
													equipClothingFromInventory(inventoryNPC, slot, GameState.game.getPlayer(), clothing);
												}
											}
										};
										
									} else {
										return new Response(
												UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
												UtilText.parse(inventoryNPC, "Get [npc.name] to equip the " + clothing.getName() + "."),
												INVENTORY_MENU){
											@Override
											public void effects(){
												GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + equipClothingFromInventory(inventoryNPC, slot, GameState.game.getPlayer(), clothing) + "</p>");
											}
										};
									}
								} else {
									return new Response(
											UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
											clothing.getCannotBeEquippedText(inventoryNPC, slot),
											null);
								}
								
							} else {
								return null;
							}
							
						case SEX:
							if(index == 1) {
								return new Response("Take (1)", "You can't take someone's clothing while having sex with them!", null);
								
							} else if(index == 2) {
								return new Response("Take (5)", "You can't take someone's clothing while having sex with them!", null);
								
							} else if(index == 3) {
								return new Response("Take (All)", "You can't take someone's clothing while having sex with them!", null);
								
							} else if(index == 4) {
								return new Response("Dye", "You can't dye someone's clothing while having sex with them!", null);
								
							} else if(index == 5) {
								if(clothing.isCondom()) {
									if(clothing.getCondomEffect().getPotency().isNegative()) {
										return new Response("Repair (<i>1 Essence</i>)", "You can't repair someone else's condom, especially not while having sex with them!", null);
									}
									return new Response("Sabotage", "You can't sabotage someone else's condom, especially not while having sex with them!", null);
								}
								return new Response("Enchant", "You can't enchant someone else's clothing, especially not while having sex with them!", null);

							} else if(index >= 6 && index <= 9 && index-6<clothing.getClothingType().getEquipSlots().size()) { //TODO ???
								InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-6);
								if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "You can't equip this as you are currently [style.colourTerrible(immobilised)]!", null);
								}
								if(clothing.isCanBeEquipped(GameState.game.getPlayer(), slot)) {
									if(clothing.isAbleToBeEquippedDuringSex(slot).getKey() && !inventoryNPC.isTrader()) {
										if(!GameState.sex.getInitialSexManager().isAbleToEquipSexClothing(GameState.game.getPlayer(), GameState.game.getPlayer(), clothing)) {
											return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "As this is a special sex scene, you cannot equip clothing during it!", null);
										}
										if(!GameState.sex.isClothingEquipAvailable(GameState.game.getPlayer(), slot, clothing)) {
											return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "This slot is involved with an ongoing sex action, so you cannot equip clothing into it!", null);
										}
										if (GameState.game.getPlayer().isAbleToEquip(clothing, slot, false, GameState.game.getPlayer())) {
											return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), "Equip the " + clothing.getName() + ".", GameState.sex.SEX_DIALOGUE){
												@Override
												public void effects(){
													AbstractClothing c = clothing;
													equipClothingFromInventory(GameState.game.getPlayer(), slot, GameState.game.getPlayer(), clothing);
													GameState.sex.setEquipClothingText(c, GameState.game.getPlayer().getUnequipDescription());
													GameState.getRenderSink().openInventory();
													GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
													GameState.sex.setSexStarted(true);
												}
											};
										} else {
											return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), getClothingBlockingRemovalText(GameState.game.getPlayer(), "equip"), null);
										}
										
									} else {
										return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), clothing.isAbleToBeEquippedDuringSex(slot).getValue(), null);
									}
									
								} else {
									return new Response("Equip: "+Util.capitaliseSentence(slot.getName()), clothing.getCannotBeEquippedText(GameState.game.getPlayer(), slot), null);
								}
								
							} else if (index == 10) {
								return getQuickTradeResponse();

							} else if(index >= 11 && index <= 14 && index-11<clothing.getClothingType().getEquipSlots().size()) {
								InventorySlot slot = clothing.getClothingType().getEquipSlots().get(index-11);
								if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
									return new Response(
											UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
											UtilText.parse(inventoryNPC, "You can't get [npc.name] to equip the " + clothing.getName() + " as you are currently [style.colourTerrible(immobilised)]!"),
											null);
								}
								Value<Boolean, String> equipAllowed = inventoryNPC.isInventoryEquipAllowed(clothing, slot);
								if(!equipAllowed.getKey()) {
									return new Response(
											UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
											UtilText.parse(inventoryNPC, equipAllowed.getValue()),
											null);
								}
								if(clothing.isCanBeEquipped(inventoryNPC, slot)) {
									if(clothing.isAbleToBeEquippedDuringSex(slot).getKey() && !inventoryNPC.isTrader()) {
										if(!GameState.sex.getInitialSexManager().isAbleToEquipSexClothing(GameState.game.getPlayer(), inventoryNPC, clothing)) {
											return new Response(
													UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
													"As this is a special sex scene, you cannot equip clothing during it!",
													null);
										}
										if(!GameState.sex.isClothingEquipAvailable(inventoryNPC, slot, clothing)) {
											return new Response(
													UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"), 
													"This slot is involved with an ongoing sex action, so you cannot equip clothing into it!",
													null);
										}
										if (inventoryNPC.isAbleToEquip(clothing, slot, false, GameState.game.getPlayer())) {
											return new Response(
													UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
													UtilText.parse(inventoryNPC, "Get [npc.name] to equip the " + clothing.getName() + "."),
													GameState.sex.SEX_DIALOGUE){
												@Override
												public void effects(){
													AbstractClothing c = clothing;
													equipClothingFromInventory(inventoryNPC, slot, GameState.game.getPlayer(), clothing);
													GameState.sex.setEquipClothingText(c, inventoryNPC.getUnequipDescription());
													GameState.getRenderSink().openInventory();
													GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
													GameState.sex.setSexStarted(true);
												}
											};
										} else {
											return new Response(
													UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
													UtilText.parse(inventoryNPC, "[npc.Name] can't equip the " + clothing.getName() + ", as other clothing is blocking [npc.herHim] from doing so!"),
													null);
										}
										
									} else {
										return new Response(
												UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
												clothing.isAbleToBeEquippedDuringSex(slot).getValue(),
												null);
									}
									
								} else {
									return new Response(
											UtilText.parse(inventoryNPC, "Equip: "+Util.capitaliseSentence(slot.getName())+" ([npc.HerHim])"),
											clothing.getCannotBeEquippedText(inventoryNPC, slot),
											null);
								}
								
							} else {
								return null;
							}
							
						case TRADING:
							inventoryFull = GameState.game.getPlayer().isInventoryFull() && !GameState.game.getPlayer().hasClothing(clothing) && clothing.getRarity()!=Rarity.QUEST;
							
							if(index == 1) {
								int sellPrice = buyback?GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():clothing.getPrice(inventoryNPC.getSellModifier(clothing));
								if(inventoryFull) {
									return new Response("Buy (1) ("+UtilText.formatAsMoneyUncoloured(sellPrice, "span")+")", "Your inventory is already full!", null);
								}
								if(GameState.game.getPlayer().getMoney() < sellPrice) {
									return new Response("Buy (1) ("+UtilText.formatAsMoneyUncoloured(sellPrice, "span")+")", "You can't afford to buy this!", null);
								}
								return new Response("Buy (1) (" + UtilText.formatAsMoney(sellPrice, "span") + ")", "Buy the " + clothing.getName() + " for " + UtilText.formatAsMoney(sellPrice) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellClothing(inventoryNPC, GameState.game.getPlayer(), clothing, 1, sellPrice);
									}
								};
								
							} else if(index == 2) {
								int sellPrice = buyback?GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():clothing.getPrice(inventoryNPC.getSellModifier(clothing));
								if((buyback && GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getCount()<5)
										|| (!buyback && inventoryNPC.getClothingCount(clothing) < 5)) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", UtilText.parse(inventoryNPC, "[npc.Name] doesn't have five "+clothing.getNamePlural()+"."), null);
								}
								if(inventoryFull) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", "Your inventory is already full!", null);
								}
								if(GameState.game.getPlayer().getMoney() < sellPrice*5) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", "You can't afford to buy this!", null);
								}
								return new Response("Buy (5) (" + UtilText.formatAsMoney(sellPrice*5, "span") + ")", "Buy the " + clothing.getName() + " for " + UtilText.formatAsMoney(sellPrice*5) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellClothing(inventoryNPC, GameState.game.getPlayer(), clothing, 5, sellPrice);
									}
								};
								
							} else if(index == 3) {
								int sellPrice = buyback?GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():clothing.getPrice(inventoryNPC.getSellModifier(clothing));
								int count = buyback?GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getCount():inventoryNPC.getClothingCount(clothing);
								if(inventoryFull) {
									return new Response("Buy (All) ("+UtilText.formatAsMoneyUncoloured(sellPrice*count, "span")+")", "Your inventory is already full!", null);
								}
								if(GameState.game.getPlayer().getMoney() < sellPrice*count) {
									int affordableCount = (int)(GameState.game.getPlayer().getMoney() / sellPrice);
									if(affordableCount > 0) {
										return new Response("Buy (Max " + affordableCount + ") (" + UtilText.formatAsMoney(sellPrice * affordableCount, "span") + ")",
												"Buy the " + clothing.getName() + " for " + UtilText.formatAsMoney(sellPrice * affordableCount) + ".", INVENTORY_MENU) {
											@Override
											public void effects() {
												sellClothing(inventoryNPC, GameState.game.getPlayer(), clothing, affordableCount, sellPrice);
											}
										};
									} else {
										return new Response("Buy (All) ("+UtilText.formatAsMoneyUncoloured(sellPrice*count, "span")+")", "You can't afford to buy this!", null);
									}
								}
								return new Response("Buy (All) (" + UtilText.formatAsMoney(sellPrice*count, "span") + ")",
										"Buy the " + clothing.getName() + " for " + UtilText.formatAsMoney(sellPrice*count) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellClothing(inventoryNPC, GameState.game.getPlayer(), clothing, count, sellPrice);
									}
								};
								
							} else if(index == 4) {
								return new Response("Dye", UtilText.parse(inventoryNPC, "[npc.Name] isn't going to let you dye the clothing that [npc.sheIs] trying to sell!"), null);
								
							} else if(index == 5) {
								if(clothing.isCondom()) {
									if(clothing.getCondomEffect().getPotency().isNegative()) {
										return new Response("Repair (<i>1 Essence</i>)", "You can't repair someone else's condom!", null);
									}
									return new Response("Sabotage", "You can't sabotage someone else's condom!", null);
								}
								return new Response("Enchant", "You can't enchant someone else's clothing!", null);
								
							} else if(index == 6) {
								return new Response("Equip (Self)", UtilText.parse(inventoryNPC, "[npc.Name] isn't going to let you use [npc.her] clothing without buying them first."), null);
								
							} else if (index == 9) {
								return getBuybackResponse();
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.HerHim])"), UtilText.parse(inventoryNPC, "[npc.Name] isn't going to use the clothing that [npc.sheIs] trying to sell!"), null);
								
							} else {
								return null;
							}
					}
				}
			}
			return null;
			
		}

		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.INVENTORY;
		}
	};
	
	public static final DialogueNode WEAPON_EQUIPPED = new DialogueNode("Weapon equipped", "", true) {
		
		@Override
		public String getLabel() {
			if (GameState.game.getDialogueFlags().values.contains(DialogueFlagValue.quickTrade) && !GameState.game.isInSex() && !GameState.game.isInCombat()) {
				return "Inventory (Quick-Manage is <b style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>)";
			} else {
				return "Inventory";
			}
		}
		
		@Override
		public String getHeaderContent() {
			return inventoryView();
		}

		@Override
		public String getContent() {
			StringBuilder sb = new StringBuilder();
			List<String> extraDescriptions = weapon.getExtraDescriptions(owner);
			if(!extraDescriptions.isEmpty()) {
				sb.append("<p>");
					for(int i=0 ; i<extraDescriptions.size() ; i++) {
						sb.append(extraDescriptions.get(i));
						if(i<extraDescriptions.size()-1) {
							sb.append("<br/>");
						}
					}
				sb.append("</p>");
			}
			return getItemDisplayPanel(weapon,
					weapon.getSVGEquippedString(owner),
					Util.capitaliseSentence(weapon.getDisplayName(true)),
					weapon.getDescription(owner)
					 	+sb.toString());
		}

		public String getResponseTabTitle(int index) {
			return getGeneralResponseTabTitle(index);
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return getCloseInventoryResponse();
			}

			if(GameState.game.isBadEnd()) {
				if(index==1) {
					return new Response("Unavailable", "You can't manipulate your inventory during a bad end...", null);
				}
				return null;
			}
			
			if(responseTab==0) {
				return INVENTORY_MENU.getResponse(responseTab, index);
			}
			
			// ****************************** ITEM BELONGS TO THE PLAYER ******************************
			if(owner != null && owner.isPlayer()) {
				switch(interactionType) {
					case COMBAT:
						if (index == 1) {
							if(GameState.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								return new Response("Drop", "You can't change weapons while fighting someone!", null);
								
							} else {
								return new Response("Store", "You can't change weapons while fighting someone!", null);
							}
							
						} else if (index==4) {
							return new Response("Dye/Reforge", "You can't alter the properties of "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" weapons in combat!", null);
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant equipped weapons!", null);
							
						} if(index == 6) {
							return new Response("Unequip", "You can't change weapons while fighting someone!", null);
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else {
							return null;
						}
						
					case FULL_MANAGEMENT: case TRADING: case CHARACTER_CREATION:
						if (index == 1) {
							boolean areaFull = GameState.game.isPlayerTileFull() && !GameState.game.getPlayerCell().getInventory().hasWeapon(weapon);
							if(GameState.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(!weapon.getWeaponType().isAbleToBeDropped()) {
									return new Response("Drop", "You cannot drop the " + weapon.getName() + "!", null);
									
								} else if(areaFull) {
									return new Response("Drop", "This area is full, so you can't drop your " + weapon.getName() + " here!", null);
								} else {
									return new Response("Drop", "Drop your " + weapon.getName() + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append(
													"<p style='text-align:center;'>"
														+ (GameState.game.getPlayer().unequipWeapon(weaponSlot, weapon, true, true))
													+ "</p>");
											resetPostAction();
										}
									};
								}
								
							} else {
								if(!weapon.getWeaponType().isAbleToBeDropped()) {
									return new Response("Store", "You cannot drop the " + weapon.getName() + "!", null);
									
								} else if(areaFull) {
									return new Response("Store", "This area is full, so you can't store your " + weapon.getName() + " here!", null);
								} else {
									return new Response("Store", "Store your " + weapon.getName() + " in this area.", INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append(
													"<p style='text-align:center;'>"
														+ (GameState.game.getPlayer().unequipWeapon(weaponSlot, weapon, true, true))
													+ "</p>");
											resetPostAction();
										}
									};
								}
							}
							
						} else if (index==4) {
							if (isWeaponDyeReforgeActionAvailable()) {
								return new Response("Dye/Reforge", 
										GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
											?"Use your proficiency with [style.colourEarth(Earth spells)] to alter this weapon's properties."
											:"Use a dye-brush or reforge hammer to alter this weapon's properties.",
										DYE_EQUIPPED_WEAPON) {
									@Override
									public void effects() {
										resetWeaponDyeColours();
									}
								};
							} else {
								return new Response("Dye", "You need a dye-brush or reforge hammer in order to alter this weapon's properties.", null);
							}
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant equipped weapons!", null);
							
						} else if(index == 6) {
							return new Response("Unequip", "Unequip the " + weapon.getName() + ".", INVENTORY_MENU){
								@Override
								public void effects(){
									GameState.game.getTextEndStringBuilder().append(
											"<p style='text-align:center;'>"
												+ (GameState.game.getPlayer().unequipWeapon(weaponSlot, weapon, false, true))
											+ "</p>");
									resetPostAction();
								}
							};
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else {
							return null;
						}
						
					case SEX:
						if (index == 1) {
							boolean areaFull = GameState.game.isPlayerTileFull() && !GameState.game.getPlayerCell().getInventory().hasWeapon(weapon);
							if(GameState.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(!weapon.getWeaponType().isAbleToBeDropped()) {
									return new Response("Drop", "You cannot drop the " + weapon.getName() + "!", null);
									
								} else if(areaFull) {
									return new Response("Drop", "This area is full, so you can't drop your " + weapon.getName() + " here!", null);
								} else {
									return new Response("Drop", "Drop your " + weapon.getName() + ".", GameState.sex.SEX_DIALOGUE){
										@Override
										public void effects(){
											GameState.sex.setUnequipWeaponText(weapon,
													"<p style='text-align:center;'>"
														+ (GameState.game.getPlayer().unequipWeapon(weaponSlot, weapon, true, true))
													+ "</p>");
											resetPostAction();
											GameState.getRenderSink().openInventory();
											GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
											GameState.sex.setSexStarted(true);
										}
									};
								}
								
							} else {
								if(!weapon.getWeaponType().isAbleToBeDropped()) {
									return new Response("Store", "You cannot drop the " + weapon.getName() + "!", null);
									
								} else if(areaFull) {
									return new Response("Store", "This area is full, so you can't store your " + weapon.getName() + " here!", null);
								} else {
									return new Response("Store", "Store your " + weapon.getName() + " in this area.", GameState.sex.SEX_DIALOGUE){
										@Override
										public void effects(){
											GameState.sex.setUnequipWeaponText(weapon,
													"<p style='text-align:center;'>"
														+ (GameState.game.getPlayer().unequipWeapon(weaponSlot, weapon, true, true))
													+ "</p>");
											resetPostAction();
											GameState.getRenderSink().openInventory();
											GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
											GameState.sex.setSexStarted(true);
										}
									};
								}
							}
							
						} else if (index==4) {
							return new Response("Dye/Reforge", "You can't alter the properties of "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" weapons in sex!", null);
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant equipped weapons!", null);
							
						} else if(index == 6) {
							return new Response("Unequip", "Unequip the " + weapon.getName() + ".", GameState.sex.SEX_DIALOGUE){
								@Override
								public void effects(){
									GameState.sex.setUnequipWeaponText(weapon,
											"<p style='text-align:center;'>"
												+ (GameState.game.getPlayer().unequipWeapon(weaponSlot, weapon, false, true))
											+ "</p>");
									resetPostAction();
									GameState.getRenderSink().openInventory();
									GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
									GameState.sex.setSexStarted(true);
								}
							};
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else {
							return null;
						}
				}
				
			// ****************************** ITEM DOES NOT BELONG TO PLAYER ******************************
				
			} else {
				switch(interactionType) {
					case COMBAT:
						if(index == 1) {
							return new Response("Drop", "You can't make someone drop their weapon while fighting them!", null);
							
						} else if(index == 2) {
							return new Response("Take", "You can't take someone's weapon while fighting them!", null);
							
						} else if (index==4) {
							return new Response("Dye/Reforge", "You can't alter the properties of someone else's equipped weapons while you're fighting them!", null);
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant someone else's equipped weapon, especially not while fighting them!", null);
							
						} else if (index == 6) {
							return new Response("Unequip", "You can't unequip someone's weapon while fighting them!", null);
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else {
							return null;
						}
						
					case FULL_MANAGEMENT:  case CHARACTER_CREATION:
						boolean inventoryFull = GameState.game.getPlayer().isInventoryFull() && !GameState.game.getPlayer().hasWeapon(weapon) && weapon.getRarity()!=Rarity.QUEST;
						
						if (index == 1) {
							boolean areaFull = GameState.game.isPlayerTileFull() && !GameState.game.getPlayerCell().getInventory().hasWeapon(weapon);
							if(GameState.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(!weapon.getWeaponType().isAbleToBeDropped()) {
									return new Response("Drop", UtilText.parse(inventoryNPC, "[npc.Name] cannot drop [npc.her] " + weapon.getName() + "!"), null);
									
								} else if(areaFull) {
									return new Response("Drop", UtilText.parse(inventoryNPC, "This area is full, so [npc.name] can't drop [npc.her] " + weapon.getName() + " here!"), null);
								} else {
									return new Response("Drop", UtilText.parse(inventoryNPC, "Get [npc.name] to drop [npc.her] " + weapon.getName() + "."), INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append(
													"<p style='text-align:center;'>"
														+ (inventoryNPC.unequipWeapon(weaponSlot, weapon, true, false))
													+ "</p>");
											resetPostAction();
										}
									};
								}
								
							} else {
								if(!weapon.getWeaponType().isAbleToBeDropped()) {
									return new Response("Drop", UtilText.parse(inventoryNPC, "[npc.Name] cannot drop [npc.her] " + weapon.getName() + "!"), null);
									
								} else if(areaFull) {
									return new Response("Store", UtilText.parse(inventoryNPC, "This area is full, so [npc.name] can't store [npc.her] " + weapon.getName() + " here!"), null);
								} else {
									return new Response("Store", UtilText.parse(inventoryNPC, "Get [npc.name] to store [npc.her] " + weapon.getName() + " in this area."), INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append(
													"<p style='text-align:center;'>"
														+ (inventoryNPC.unequipWeapon(weaponSlot, weapon, true, false))
													+ "</p>");
											resetPostAction();
										}
									};
								}
							}
							
						} else if (index == 2) {
							if(inventoryFull) {
								return new Response("Take", "Your inventory is full, so you can't take this!", null);
								
							} else {
								return new Response("Take",
										UtilText.parse(inventoryNPC, "Take [npc.namePos] " + weapon.getName() + " and add it to your inventory."),
										INVENTORY_MENU){
									@Override
									public void effects(){
										inventoryNPC.unequipWeaponIntoVoid(weaponSlot, weapon, true);
										GameState.game.getTextEndStringBuilder().append(
												"<p style='text-align:center;'>"
													+ (GameState.game.getPlayer().addWeapon(weapon, false))
												+ "</p>");
										resetPostAction();
									}
								};
							}
							
						} else if (index==4) {
							if (isWeaponDyeReforgeActionAvailable()) {
								return new Response("Dye/Reforge", 
										GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
											?"Use your proficiency with [style.colourEarth(Earth spells)] to alter this weapon's properties."
											:"Use a dye-brush or reforge hammer to alter this weapon's properties.",
										DYE_EQUIPPED_WEAPON) {
									@Override
									public void effects() {
										resetWeaponDyeColours();
									}
								};
							} else {
								return new Response("Dye", UtilText.parse(inventoryNPC, "You'll need to find a dye-brush or reforge hammer if you want to alter the properties of [npc.namePos] weapons."), null);
							}
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant equipped weapons!", null);
							
						} else if(index == 6) {
							if(!weapon.getWeaponType().isAbleToBeDropped()) {
								return new Response("Unequip", UtilText.parse(inventoryNPC, "As it's a unique weapon, the " + weapon.getName() + " cannot be unequipped into [npc.namePos] inventory."), null);
							}
							return new Response("Unequip", UtilText.parse(inventoryNPC, "Get [npc.name] to unequip [npc.her] " + weapon.getName() + "."), INVENTORY_MENU){
								@Override
								public void effects(){
									GameState.game.getTextEndStringBuilder().append(
											"<p style='text-align:center;'>"
												+ (inventoryNPC.unequipWeapon(weaponSlot, weapon, false, false))
											+ "</p>");
									resetPostAction();
								}
							};
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else {
							return null;
						}
						
					case SEX:
						if(index == 1) {
							return new Response("Drop", "You can't unequip someone's weapon while having sex with them!", null);
							
						} else if (index == 2) {
							return new Response("Take", "You can't take someone's weapon while having sex with them!", null);
							
						} else if (index==4) {
							return new Response("Dye/Reforge", UtilText.parse(inventoryNPC, "You can't alter the properties of [npc.namePos] weapons in sex!"), null);
						
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant equipped weapons!", null);
							
						} else if(index == 6) {
							return new Response("Unequip", "You can't unequip someone's weapon while having sex with them!", null);
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else {
							return null;
						}
						
					case TRADING:
						if(index == 1) {
							return new Response("Drop", "You can't make someone drop their weapon!", null);
							
						} else if (index == 2) {
							return new Response("Take", "You can't take someone else's weapon!", null);
							
						} else if (index==4) {
							return new Response("Dye/Reforge", UtilText.parse(inventoryNPC, "You can't alter the properties of [npc.namePos] weapons!"), null);
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant someone else's equipped weapon!", null);
							
						} else if(index == 6) {
							return new Response("Unequip", "You can't unequip someone else's weapon!", null);
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else {
							return null;
						}
					}
				
				}
				return null;
		}

		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.INVENTORY;
		}
	};
	public static final DialogueNode CLOTHING_EQUIPPED = new DialogueNode("Clothing equipped", "", true) {

		@Override
		public String getLabel() {
			if(!GameState.game.isInNewWorld()) {
				return "Evening's Attire";
			}
			
			if (GameState.game.getDialogueFlags().values.contains(DialogueFlagValue.quickTrade) && !GameState.game.isInSex() && !GameState.game.isInCombat()) {
				return "Inventory (Quick-Manage is <b style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>)";
			} else {
				return "Inventory";
			}
		}
		
		@Override
		public String getHeaderContent() {
			return inventoryView();
		}

		@Override
		public String getContent() {
			StringBuilder sb = new StringBuilder();
			sb.append(clothing.getDescription(owner));
			sb.append("<p>");
				GameCharacter descriptionTarget = owner; //GameState.game.isInSex()?owner:GameState.game.getPlayer()
				for(String s : clothing.getExtraDescriptions(descriptionTarget, null, true)) {
					sb.append(s+"<br/>");
				}
				for(String s : clothing.getExtraDescriptions(descriptionTarget, clothing.getSlotEquippedTo(), true)) {
					sb.append(s+"<br/>");
				}
			sb.append("</p>");
			sb.append(GameState.game.isInSex()||GameState.game.isInCombat()?clothing.getDisplacementBlockingDescriptions(owner):"");
			
			return getItemDisplayPanel(clothing,
						clothing.getSVGEquippedString(owner),
						clothing.getDisplayName(true),
						sb.toString())
					+(interactionType==InventoryInteraction.CHARACTER_CREATION
						?CharacterCreation.getCheckingClothingDescription()
						:"");
		}

		public String getResponseTabTitle(int index) {
			return getGeneralResponseTabTitle(index);
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return getCloseInventoryResponse();
			}

			if(GameState.game.isBadEnd()) {
				if(index==1) {
					return new Response("Unavailable", "You can't manipulate your inventory during a bad end...", null);
				}
				return null;
			}
			
			if(responseTab==0) {
				return INVENTORY_MENU.getResponse(responseTab, index);
			}
			
			// ****************************** ITEM BELONGS TO THE PLAYER ******************************
			if(owner != null && owner.isPlayer()) {
				InventorySlot slotEquippedTo = clothing.getSlotEquippedTo();
				switch(interactionType) {
					case COMBAT:
						if (index == 1) {
							if(GameState.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								return new Response("Drop", "You cannot drop the " + clothing.getName() + " while fighting someone!", null);
								
							} else {
								return new Response("Store", "You cannot drop the " + clothing.getName() + " while fighting someone!", null);
							}
							
						} else if (index==4) {
							return new Response("Dye", "You can't dye "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" clothes in combat!", null);
							
						} else if(index == 5) {
							if(clothing.isSealed()) {
								return getJinxRemovalResponse(true);
									
							} else {
								if(clothing.isCondom()) {
									if(clothing.getCondomEffect().getPotency().isNegative()) {
										return new Response("Repair (<i>1 Essence</i>)", "You can't repair equipped condoms!", null);
									}
									return new Response("Sabotage", "You can't sabotage equipped condoms!", null);
								}
								return new Response("Enchant", "You can't enchant equipped clothing!", null);
							}
							
						} else if(index == 6 && !clothing.isDiscardedOnUnequip(slotEquippedTo)) {
							return new Response("Unequip", "You can't unequip the " + clothing.getName() + " during combat!", null);
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else if (index > 10 && index - 11 < clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).size()){
								return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index -11).getDescription()),
										"You can't "+clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index -11).getDescription()
										+ " the " + clothing.getName() + " during combat!", null);
								
							
						} else {
							
							return null;
						}
						
					case FULL_MANAGEMENT: case TRADING:
						if (index == 1) {
							boolean areaFull = GameState.game.isPlayerTileFull() && !GameState.game.getPlayerCell().getInventory().hasClothing(clothing);
							if(GameState.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(!clothing.getClothingType().isAbleToBeDropped()) {
									return new Response("Drop", "You cannot drop the " + clothing.getName() + "!", null);
									
								} else if(areaFull && !clothing.isDiscardedOnUnequip(slotEquippedTo)) {
									return new Response("Drop", "This area is full, so you can't drop "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" " + clothing.getName() + " here!", null);
								} else {
									return new Response((clothing.isDiscardedOnUnequip(slotEquippedTo)?"Discard":"Drop"),
											(clothing.isDiscardedOnUnequip(slotEquippedTo)
													?"Take off "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" " + clothing.getName() + " and throw it away."
													:"Drop "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" " + clothing.getName() + "."),
											INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + unequipClothingToFloor(GameState.game.getPlayer(), clothing) + "</p>");
										}
									};
								}
								
							} else {
								if(!clothing.getClothingType().isAbleToBeDropped()) {
									return new Response("Store", "You cannot drop the " + clothing.getName() + "!", null);
									
								} else if(areaFull && !clothing.isDiscardedOnUnequip(slotEquippedTo)) {
									return new Response("Store", "This area is full, so you can't store "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" " + clothing.getName() + " here!", null);
								} else {
									return new Response((clothing.isDiscardedOnUnequip(slotEquippedTo)?"Discard":"Store"),
											(clothing.isDiscardedOnUnequip(slotEquippedTo)
													?"Take off "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" " + clothing.getName() + " and throw it away."
													:"Store "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" " + clothing.getName() + " in this area."),
											INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + unequipClothingToFloor(GameState.game.getPlayer(), clothing) + "</p>");
										}
									};
								}
							}
							
						} else if (index==4) {
							if (isClothingDyeActionAvailable()) {
								return new Response("Dye", 
										GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
											?"Use your proficiency with [style.colourEarth(Earth spells)] to dye this item."
											:"Use a dye-brush to dye this item of clothing.",
										DYE_EQUIPPED_CLOTHING) {
									@Override
									public void effects() {
										resetClothingDyeColours();
									}
								};
							} else {
								return new Response("Dye", "You need a dye-brush in order to dye this item of clothing.", null);
							}
							
						} else if(index == 5) {
							if(clothing.isSealed()) {
								return getJinxRemovalResponse(true);
								
							} else {
								if(clothing.isCondom()) {
									if(clothing.getCondomEffect().getPotency().isNegative()) {
										return new Response("Repair (<i>1 Essence</i>)", "You can't repair equipped condoms!", null);
									}
									return new Response("Sabotage", "You can't sabotage equipped condoms!", null);
								}
								return new Response("Enchant", "You can't enchant equipped clothing!", null);
							}
							
						} else if(index == 6 && !clothing.isDiscardedOnUnequip(slotEquippedTo)) {
							if (owner.isAbleToUnequip(clothing, true, GameState.game.getPlayer())) {
								return new Response("Unequip", "Unequip the " + clothing.getName() + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + unequipClothingToInventory(GameState.game.getPlayer(), clothing) + "</p>");
									}
								};
								
							} else {
								return new Response("Unequip", getClothingBlockingRemovalText(owner, "unequip"), null);
							}
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else if (index > 10 && index - 11 < clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).size()){
							
							if (clothing.getDisplacedList().contains(clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index - 11))) {
								return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index - 11).getOppositeDescription()),
										Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index - 11).getOppositeDescription()) + " the " + clothing.getName() + ". "
												+ clothing.getClothingBlockingDescription(
														clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index - 11),
														GameState.game.getPlayer(),
														clothing.getSlotEquippedTo(),
														" <span style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>This will cover "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" ",
														".</span>"),
												CLOTHING_EQUIPPED){
									@Override
									public void effects(){
										GameState.game.getPlayer().isAbleToBeReplaced(clothing, clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index - 11), true, true, GameState.game.getPlayer());
									}
								};
							} else {
								return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index - 11).getDescription()),
										Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index - 11).getDescription()) + " the " + clothing.getName() + ". "
												+ clothing.getClothingBlockingDescription(
														clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index - 11),
														GameState.game.getPlayer(),
														clothing.getSlotEquippedTo(),
														" <span style='color:" + PresetColour.GENERIC_SEX.toWebHexString() + ";'>This will expose "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" ",
														".</span>"),
												CLOTHING_EQUIPPED){
									@Override
									public void effects(){
										GameState.game.getPlayer().isAbleToBeDisplaced(clothing, clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index - 11), true, true, GameState.game.getPlayer());
									}
								};
							}
							
						} else {
							return null;
						}
						
					case CHARACTER_CREATION:
						if (index == 1) {
							if(GameState.game.getPlayer().isCoverableAreaVisible(CoverableArea.NIPPLES)
									|| GameState.game.getPlayer().isCoverableAreaVisible(CoverableArea.ANUS)
									|| GameState.game.getPlayer().isCoverableAreaVisible(CoverableArea.PENIS)
									|| GameState.game.getPlayer().isCoverableAreaVisible(CoverableArea.VAGINA)
									|| (GameState.game.getPlayer().getClothingInSlot(InventorySlot.FOOT)==null && GameState.game.getPlayer().getLegType().equals(LegType.HUMAN))) {
								return new Response("To the stage", "You need to be wearing clothing that covers your body, as well as a pair of shoes.", null);
								
							} else {
								return new Response("To the stage", "You're ready to approach the stage now.", CharacterCreation.CHOOSE_BACKGROUND) {
									@Override
									public void effects() {
										CharacterCreation.moveNPCIntoPlayerTile();
									}
								};
							}
							
						} else if(index == 4){
							if(GameState.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()){
								return new Response("Unequip all", "You're currently naked, there's nothing to be unequipped.", null);
							}
							else{
								return new Response("Unequip all", "Remove as much of your clothing as possible.", INVENTORY_MENU){
									@Override
									public void effects(){
										GameState.game.getTextEndStringBuilder().append(unequipAll(GameState.game.getPlayer()));
									}
								};
							}
							
						} else if(index == 5) {
							return new Response("Change colour", "Change the colour of this item of clothing.", DYE_EQUIPPED_CLOTHING_CHARACTER_CREATION) {
								@Override
								public void effects() {
									resetClothingDyeColours();
								}
							};
							
						} else if(index == 6) {
							return new Response("Unequip", "Unequip the " + clothing.getName() + ".", INVENTORY_MENU){
								@Override
								public void effects(){
									unequipClothingToFloor(GameState.game.getPlayer(), clothing);
								}
							};
								
						} else {
							return null;
						}
						
					case SEX:
						if (index == 1) {
							String unequipTitle = "Drop";
							if(clothing.isDiscardedOnUnequip(slotEquippedTo)) {
								unequipTitle = "Discard";
							} else if(!GameState.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								unequipTitle = "Store";
							}
							if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
								return new Response(unequipTitle, "You can't unequip the " + clothing.getName() + " as you are currently [style.colourTerrible(immobilised)]!", null);
							}
							if(!GameState.sex.isCanRemoveSelfClothing(GameState.game.getPlayer())) {
								return new Response(unequipTitle, "You can't unequip the " + clothing.getName() + " in this sex scene!", null);
							}
							boolean areaFull = GameState.game.isPlayerTileFull() && !GameState.game.getPlayerCell().getInventory().hasClothing(clothing);
							if(GameState.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(!clothing.getClothingType().isAbleToBeDropped()) {
									return new Response("Drop", "You cannot drop the " + clothing.getName() + "!", null);
									
								} else if(areaFull && !clothing.isDiscardedOnUnequip(slotEquippedTo)) {
									return new Response("Drop", "This area is full, so you can't drop "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" " + clothing.getName() + " here!", null);
									
								} else {
									if (owner.isAbleToUnequip(clothing, false, GameState.game.getPlayer())) {
										return new Response((clothing.isDiscardedOnUnequip(slotEquippedTo)?"Discard":"Drop"),
												(clothing.isDiscardedOnUnequip(slotEquippedTo)
														?"Take off "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" " + clothing.getName() + " and throw it away."
														:"Drop "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" " + clothing.getName() + "."),
												GameState.sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												GameCharacter unequipOwner = owner;
												AbstractClothing c = clothing;
												unequipClothingToFloor(GameState.game.getPlayer(), clothing);
												GameState.sex.setUnequipClothingText(c, unequipOwner.getUnequipDescription());
												GameState.getRenderSink().openInventory();
												GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
												GameState.sex.setSexStarted(true);
											}
										};
									} else {
										return new Response("Drop", getClothingBlockingRemovalText(owner, "unequip"), null);
									}
								}
								
							} else {
								if(!clothing.getClothingType().isAbleToBeDropped()) {
									return new Response("Store", "You cannot drop the " + clothing.getName() + "!", null);
									
								} else if(areaFull && !clothing.isDiscardedOnUnequip(slotEquippedTo)) {
									return new Response("Store", "This area is full, so you can't store "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" " + clothing.getName() + " here!", null);
									
								} else {
									if (owner.isAbleToUnequip(clothing, false, GameState.game.getPlayer())) {
										return new Response((clothing.isDiscardedOnUnequip(slotEquippedTo)?"Discard":"Store"),
												(clothing.isDiscardedOnUnequip(slotEquippedTo)
														?"Take off "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" " + clothing.getName() + " and throw it away."
														:"Drop "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" " + clothing.getName() + "."),
												GameState.sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												GameCharacter unequipOwner = owner;
												AbstractClothing c = clothing;
												unequipClothingToFloor(GameState.game.getPlayer(), clothing);
												GameState.sex.setUnequipClothingText(c, unequipOwner.getUnequipDescription());
												GameState.getRenderSink().openInventory();
												GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
												GameState.sex.setSexStarted(true);
											}
										};
									} else {
										return new Response("Store", getClothingBlockingRemovalText(owner, "unequip"), null);
									}
								}
							}
							
						} else if (index==4) {
							return new Response("Dye", "You can't dye "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" clothes in sex!", null);
							
						} else if(index == 5) {
							if(clothing.isSealed()) {
								return getJinxRemovalResponse(true);
								
							} else {
								if(clothing.isCondom()) {
									if(clothing.getCondomEffect().getPotency().isNegative()) {
										return new Response("Repair (<i>1 Essence</i>)", "You can't repair equipped condoms!", null);
									}
									return new Response("Sabotage", "You can't sabotage equipped condoms!", null);
								}
								return new Response("Enchant", "You can't enchant equipped clothing!", null);
							}
							
						} else if(index == 6 && !clothing.isDiscardedOnUnequip(slotEquippedTo)) {
							if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
								return new Response("Unequip", "You can't unequip the " + clothing.getName() + " as you are currently [style.colourTerrible(immobilised)]!", null);
							}
							if(!GameState.sex.isCanRemoveSelfClothing(GameState.game.getPlayer())) {
								return new Response("Unequip", "You can't unequip the " + clothing.getName() + " in this sex scene!", null);
							}
							if (owner.isAbleToUnequip(clothing, false, GameState.game.getPlayer())) {
								return new Response("Unequip", "Unequip the " + clothing.getName() + ".", GameState.sex.SEX_DIALOGUE){
									@Override
									public void effects(){
										AbstractClothing c = clothing;
										unequipClothingToInventory(GameState.game.getPlayer(), clothing);
										GameState.sex.setUnequipClothingText(c, owner.getUnequipDescription());
										GameState.getRenderSink().openInventory();
										GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
										GameState.sex.setSexStarted(true);
									}
								};
							} else {
								return new Response("Unequip", getClothingBlockingRemovalText(owner, "unequip"), null);
							}
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else if (index > 10 && index - 11 < clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).size()) {
							if (clothing.getDisplacedList().contains(clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index - 11))) {
								return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index -11).getDescription()),
										"The "+ clothing.getName()+ " "
										+(clothing.getClothingType().isPlural()?"have":"has")+" already been "
												+ clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index -11).getDescriptionPast() + "!", null);
								
							} else {
								if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
									return new Response(
											Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index - 11).getDescription()),
											"You can't "+clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index -11).getDescription()
												+" the " + clothing.getName() + " as you are currently [style.colourTerrible(immobilised)]!",
											null);
								}
								if(!GameState.sex.isCanRemoveSelfClothing(GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index - 11).getDescription()),
											"You can't "+clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index -11).getDescription()
											+ " "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" " + clothing.getName() + " in this sex scene!", null);
								}
								
								if(owner.isAbleToBeDisplaced(clothing, clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index -11), false, false, GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index - 11).getDescription()),
											Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index - 11).getDescription()) + " the " + clothing.getName() + ". "
													+ clothing.getClothingBlockingDescription(
															clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index - 11),
															GameState.game.getPlayer(),
															clothing.getSlotEquippedTo(),
															" <span style='color:" + PresetColour.GENERIC_SEX.toWebHexString() + ";'>This will expose "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" ",
															".</span>"),
													GameState.sex.SEX_DIALOGUE){
										@Override
										public void effects(){
											GameState.game.getPlayer().isAbleToBeDisplaced(clothing, clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index - 11), true, true, GameState.game.getPlayer());
											GameState.sex.setDisplaceClothingText(clothing, owner.getDisplaceDescription());
											GameState.getRenderSink().openInventory();
											GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
											GameState.sex.setSexStarted(true);
										}
									};
								
								} else {
									return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index -11).getDescription()),
											"You can't "+clothing.getBlockedPartsKeysAsListWithoutNONE(owner, clothing.getSlotEquippedTo()).get(index -11).getDescription()
											+ " the " + clothing.getName() + ", as "+UtilText.parse(owner, "[npc.namePos] ")
											+owner.getBlockingClothing().getName()+" "+(owner.getBlockingClothing().getClothingType().isPlural()?"are":"is")+" in the way!", null);
								}
							}
							
						} else {
							return null;
						}
				}
				
			// ****************************** ITEM DOES NOT BELONG TO PLAYER ******************************
				
			} else {
				InventorySlot slotEquippedTo = clothing.getSlotEquippedTo();
				switch(interactionType) {
					case COMBAT:
						if (index == 1) {
							return new Response("Drop", "You can't make someone drop their clothing while fighting them!", null);
							
						} else if (index==4) {
							return new Response("Dye", "You can't dye someone else's equipped clothing while you're fighting them!", null);
							
						} else if(index == 5) {
							if(clothing.isSealed()) {
								return getJinxRemovalResponse(false);
								
							} else {
								if(clothing.isCondom()) {
									if(clothing.getCondomEffect().getPotency().isNegative()) {
										return new Response("Repair (<i>1 Essence</i>)", "You can't repair equipped condoms!", null);
									}
									return new Response("Sabotage", "You can't sabotage equipped condoms!", null);
								}
								return new Response("Enchant", "You can't enchant equipped clothing!", null);
							}
							
						} else if(index == 6) {
							return new Response("Unequip", "You can't unequip someone's clothing while fighting them!", null);
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else if (index > 10 && index - 11 < clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).size()){
							if (clothing.getDisplacedList().contains(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11))) {
								return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index -11).getDescription()),
										"The "+ clothing.getName()+ " "
										+(clothing.getClothingType().isPlural()?"have":"has")+" already been "
												+ clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index -11).getDescriptionPast() + "!", null);
								
							} else {
								return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index -11).getDescription()),
										"You can't "+clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index -11).getDescription() + " the " + clothing.getName() + " while in a fight!", null);
							}
							
						} else {
							return null;
						}
						
					case FULL_MANAGEMENT: case CHARACTER_CREATION:
						boolean inventoryFull = GameState.game.getPlayer().isInventoryFull() && !GameState.game.getPlayer().hasClothing(clothing) && clothing.getRarity()!=Rarity.QUEST;
						
						if (index == 1) {
							boolean areaFull = GameState.game.isPlayerTileFull() && !GameState.game.getPlayerCell().getInventory().hasClothing(clothing);
							if(GameState.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(!clothing.getClothingType().isAbleToBeDropped()) {
									return new Response("Drop", "You cannot drop the " + clothing.getName() + "!", null);
									
								} else if(areaFull && !clothing.isDiscardedOnUnequip(slotEquippedTo)) {
									return new Response("Drop", UtilText.parse(inventoryNPC, "This area is full, so you can't drop [npc.namePos] " + clothing.getName() + " here!"), null);
								} else {
									return new Response((clothing.isDiscardedOnUnequip(slotEquippedTo)?"Discard":"Drop"),
											(clothing.isDiscardedOnUnequip(slotEquippedTo)
													?UtilText.parse(inventoryNPC, "Take off [npc.namePos] " + clothing.getName() + " and throw it away.")
													:UtilText.parse(inventoryNPC, "Drop [npc.namePos] " + clothing.getName() + ".")),
											INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + unequipClothingToFloor(GameState.game.getPlayer(), clothing) + "</p>");
										}
									};
								}
								
							} else {
								if(!clothing.getClothingType().isAbleToBeDropped()) {
									return new Response("Store", "You cannot drop the " + clothing.getName() + "!", null);
									
								} else if(areaFull && !clothing.isDiscardedOnUnequip(slotEquippedTo)) {
									return new Response("Store", UtilText.parse(inventoryNPC, "This area is full, so you can't store [npc.namePos] " + clothing.getName() + " here!"), null);
								} else {
									return new Response((clothing.isDiscardedOnUnequip(slotEquippedTo)?"Discard":"Store"),
											(clothing.isDiscardedOnUnequip(slotEquippedTo)
													?UtilText.parse(inventoryNPC, "Take off [npc.namePos] " + clothing.getName() + " and throw it away.")
													:UtilText.parse(inventoryNPC, "Store [npc.namePos] " + clothing.getName() + " in this area.")),
											INVENTORY_MENU){
										@Override
										public void effects(){
											GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + unequipClothingToFloor(GameState.game.getPlayer(), clothing) + "</p>");
										}
									};
								}
							}
							
						} else if(index==2) {
							if(inventoryFull) {
								return new Response("Take", "Your inventory is full, so you can't take this!", null);
								
							} else if(clothing.isDiscardedOnUnequip(slotEquippedTo)) {
								return new Response("Take", "This piece of clothing is automatically discarded when unequipped, so you can't take it!", null);
								
							} else {
								return new Response("Take",
										UtilText.parse(inventoryNPC, "Take [npc.namePos] " + clothing.getName() + " and add it to your inventory."),
										INVENTORY_MENU){
									@Override
									public void effects(){
										GameState.game.getTextEndStringBuilder().append(
												"<p style='text-align:center;'>"
													+ unequipClothingToUnequippersInventory(GameState.game.getPlayer(), clothing)
												+ "</p>");
									}
								};
							}
							
						} else if (index==4) {
							if (isClothingDyeActionAvailable()) {
								return new Response("Dye", 
										GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
											?"Use your proficiency with [style.colourEarth(Earth spells)] to dye this item."
											:"Use a dye-brush to dye this item of clothing.",
										DYE_EQUIPPED_CLOTHING) {
									@Override
									public void effects() {
										resetClothingDyeColours();
									}
								};
								
							} else {
								return new Response("Dye", UtilText.parse(inventoryNPC, "You'll need to find a dye-brush if you want to dye [npc.namePos] clothes."), null);
							}
							
						} else if(index == 5) {
							if(clothing.isSealed()) {
								return getJinxRemovalResponse(false);
								
							} else {
								if(clothing.isCondom()) {
									if(clothing.getCondomEffect().getPotency().isNegative()) {
										return new Response("Repair (<i>1 Essence</i>)", "You can't repair equipped condoms!", null);
									}
									return new Response("Sabotage", "You can't sabotage equipped condoms!", null);
								}
								return new Response("Enchant", "You can't enchant equipped clothing!", null);
							}
							
						} else if(index == 6 && !clothing.isDiscardedOnUnequip(slotEquippedTo)) {
							if(!clothing.getClothingType().isAbleToBeDropped()) {
								return new Response("Unequip", UtilText.parse(inventoryNPC, "As it's a unique item, the " + clothing.getName() + " cannot be unequipped into [npc.namePos] inventory."), null);
							}
							return new Response("Unequip", "Unequip the " + clothing.getName() + ".", INVENTORY_MENU){
								@Override
								public void effects(){
									GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + unequipClothingToInventory(GameState.game.getPlayer(), clothing) + "</p>");
								}
							};
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else if (index > 10 && index - 11 < clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).size()){
							
							if (clothing.getDisplacedList().contains(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11))) {
								return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11).getOppositeDescription()),
										Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11).getOppositeDescription()) + " the " + clothing.getName() + ". "
												+ clothing.getClothingBlockingDescription(
														clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11),
														owner,
														clothing.getSlotEquippedTo(),
														" <span style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>This will cover "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" ",
														".</span>"),
												CLOTHING_EQUIPPED){
									@Override
									public void effects(){
										owner.isAbleToBeReplaced(clothing, clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11), true, true, GameState.game.getPlayer());
									}
								};
							} else {
								return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11).getDescription()),
										Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11).getDescription()) + " the " + clothing.getName() + ". "
												+ clothing.getClothingBlockingDescription(
														clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11),
														owner,
														clothing.getSlotEquippedTo(),
														" <span style='color:" + PresetColour.GENERIC_SEX.toWebHexString() + ";'>This will expose "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" ",
														".</span>"),
												CLOTHING_EQUIPPED){
									@Override
									public void effects(){
										owner.isAbleToBeDisplaced(clothing, clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11), true, true, GameState.game.getPlayer());
										GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + owner.getDisplaceDescription() + "</p>");
									}
								};
							}
							
						} else {
							return null;
						}
						
					case SEX:
						if (index == 1) {
							String unequipTitle = "Drop";
							if(clothing.isDiscardedOnUnequip(slotEquippedTo)) {
								unequipTitle = "Discard";
							} else if(!GameState.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								unequipTitle = "Store";
							}
							if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
								return new Response(unequipTitle,
									UtilText.parse(inventoryNPC, "You can't unequip [npc.namePos] clothing as you are currently [style.colourTerrible(immobilised)]!"),
									null);
							}
							if(GameState.sex.getInitialSexManager().isHidden(GameState.game.getPlayer())) {
								return new Response(unequipTitle,
										UtilText.parse(inventoryNPC, "As you're hiding, you can't unequip [npc.namePos] clothing!"),
										null);
								
							} else if(clothing.isDiscardedOnUnequip(slotEquippedTo) && !GameState.sex.isCanRemoveOthersClothing(GameState.game.getPlayer(), clothing)) {
								return new Response("Discard", "You can't unequip the " + clothing.getName() + " in this sex scene!", null);
							}

							if(!GameState.sex.isCanRemoveOthersClothing(GameState.game.getPlayer(), clothing)) {
								return new Response(unequipTitle, "You can't unequip the " + clothing.getName() + " in this sex scene!", null);
							}
							
							boolean areaFull = GameState.game.isPlayerTileFull() && !GameState.game.getPlayerCell().getInventory().hasClothing(clothing);
							if(GameState.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(!clothing.getClothingType().isAbleToBeDropped()) {
									return new Response("Drop", "You cannot drop the " + clothing.getName() + "!", null);
									
								} else if(areaFull && !clothing.isDiscardedOnUnequip(slotEquippedTo)) {
									return new Response("Drop", UtilText.parse(inventoryNPC, "This area is full, so you can't drop [npc.namePos] " + clothing.getName() + " here!"), null);
									
								} else {
									if (owner.isAbleToUnequip(clothing, false, GameState.game.getPlayer())) {
										return new Response((clothing.isDiscardedOnUnequip(slotEquippedTo)?"Discard":"Drop"),
											(clothing.isDiscardedOnUnequip(slotEquippedTo)
													?UtilText.parse(inventoryNPC, "Take off [npc.namePos] " + clothing.getName() + " and throw it away.")
													:UtilText.parse(inventoryNPC, "Drop [npc.namePos] " + clothing.getName() + ".")),
												GameState.sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												AbstractClothing c = clothing;
												GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + unequipClothingToFloor(GameState.game.getPlayer(), clothing) + "</p>");
												GameState.sex.setUnequipClothingText(c, inventoryNPC.getUnequipDescription());
												GameState.getRenderSink().openInventory();
												GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
												GameState.sex.setSexStarted(true);
											}
										};
									} else {
										return new Response("Drop", getClothingBlockingRemovalText(owner, "unequip"), null);
									}
								}
								
							} else {
								if(!clothing.getClothingType().isAbleToBeDropped()) {
									return new Response("Store", "You cannot drop the " + clothing.getName() + "!", null);
									
								} else if(areaFull && !clothing.isDiscardedOnUnequip(slotEquippedTo)) {
									return new Response("Store", UtilText.parse(inventoryNPC, "This area is full, so you can't store [npc.namePos] " + clothing.getName() + " here!"), null);
								} else {
									if (owner.isAbleToUnequip(clothing, false, GameState.game.getPlayer())) {
										return new Response((clothing.isDiscardedOnUnequip(slotEquippedTo)?"Discard":"Store"),
												(clothing.isDiscardedOnUnequip(slotEquippedTo)
														?UtilText.parse(inventoryNPC, "Take off [npc.namePos] " + clothing.getName() + " and throw it away.")
														:UtilText.parse(inventoryNPC, "Store [npc.namePos] " + clothing.getName() + " in this area.")),
												GameState.sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												AbstractClothing c = clothing;
												GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>" + unequipClothingToFloor(GameState.game.getPlayer(), clothing) + "</p>");
												GameState.sex.setUnequipClothingText(c, inventoryNPC.getUnequipDescription());
												GameState.getRenderSink().openInventory();
												GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
												GameState.sex.setSexStarted(true);
											}
										};
									} else {
										return new Response("Store", getClothingBlockingRemovalText(owner, "unequip"), null);
									}
								}
							}
							
						} else if (index==4) {
							return new Response("Dye", UtilText.parse(inventoryNPC, "You can't dye [npc.namePos] clothes in sex!"), null);
							
						} else if(index == 5) {
							if(clothing.isSealed()) {
								return getJinxRemovalResponse(false);
								
							} else {
								if(clothing.isCondom()) {
									if(clothing.getCondomEffect().getPotency().isNegative()) {
										return new Response("Repair (<i>1 Essence</i>)", "You can't repair equipped condoms!", null);
									}
									return new Response("Sabotage", "You can't sabotage equipped condoms!", null);
								}
								return new Response("Enchant", "You can't enchant equipped clothing!", null);
							}
							
						} else if(index == 6 && !clothing.isDiscardedOnUnequip(slotEquippedTo)) {
							if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
								return new Response("Unequip",
									UtilText.parse(inventoryNPC, "You can't unequip [npc.namePos] clothing as you are currently [style.colourTerrible(immobilised)]!"),
									null);
							}
							if(GameState.sex.getInitialSexManager().isHidden(GameState.game.getPlayer())) {
								return new Response("Unequip", UtilText.parse(inventoryNPC, "As you're hiding, you can't unequip [npc.namePos] clothing!"), null);
								
							} else if(!GameState.sex.isCanRemoveOthersClothing(GameState.game.getPlayer(), clothing)) {
								return new Response("Unequip", "You can't unequip the " + clothing.getName() + " in this sex scene!", null);
							}
							
							if (owner.isAbleToUnequip(clothing, false, GameState.game.getPlayer())) {
								return new Response("Unequip", "Unequip the " + clothing.getName() + ".", GameState.sex.SEX_DIALOGUE){
									@Override
									public void effects(){
										AbstractClothing c = clothing;
										unequipClothingToInventory(GameState.game.getPlayer(), clothing);
										GameState.sex.setUnequipClothingText(c, inventoryNPC.getUnequipDescription());
										GameState.getRenderSink().openInventory();
										GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
										GameState.sex.setSexStarted(true);
									}
								};
							} else {
								return new Response("Unequip", getClothingBlockingRemovalText(owner, "unequip"), null);
							}
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else if (index > 10 && index - 11 < clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).size()){
							if (clothing.getDisplacedList().contains(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11))) {
								return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index -11).getDescription()),
										"The "+ clothing.getName()+ " "
										+(clothing.getClothingType().isPlural()?"have":"has")+" already been "
												+ clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index -11).getDescriptionPast() + "!", null);
								
							} else {
								if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11).getDescription()),
										UtilText.parse(inventoryNPC, "You can't "+clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index -11).getDescription()
												+ " [npc.namePos] clothing as you are currently [style.colourTerrible(immobilised)]!"),
										null);
								}
								if(GameState.sex.getInitialSexManager().isHidden(GameState.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11).getDescription()),
											UtilText.parse(inventoryNPC,
													"As you're hiding, you can't "+clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index -11).getDescription() + " [npc.namePos] " + clothing.getName() + "!"),
											null);
								}
								if(owner.isAbleToBeDisplaced(clothing, clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index -11), false, false, GameState.game.getPlayer())){
									if(!GameState.sex.isCanRemoveOthersClothing(GameState.game.getPlayer(), clothing)) {
										return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11).getDescription()),
												"You can't "+clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index -11).getDescription() + " the " + clothing.getName() + " in this sex scene!", null);
									}
									
									return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11).getDescription()),
											Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11).getDescription()) + " the " + clothing.getName() + ". "
													+ clothing.getClothingBlockingDescription(
															clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11),
															owner,
															clothing.getSlotEquippedTo(),
															" <span style='color:" + PresetColour.GENERIC_SEX.toWebHexString() + ";'>This will expose "+(owner.isPlayer()?"your":owner.getName("")+"'s")+" ",
															".</span>"),
													GameState.sex.SEX_DIALOGUE){
										@Override
										public void effects(){
											owner.isAbleToBeDisplaced(clothing, clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11), true, true, GameState.game.getPlayer());
											GameState.sex.setDisplaceClothingText(clothing, owner.getDisplaceDescription());
											GameState.getRenderSink().openInventory();
											GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
											GameState.sex.setSexStarted(true);
										}
									};
								
								} else {
									return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index -11).getDescription()),
											"You can't "+clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index -11).getDescription()
											+ " the " + clothing.getName() + ", as "+UtilText.parse(owner, "[npc.namePos] ")
											+owner.getBlockingClothing().getName()+" "+(owner.getBlockingClothing().getClothingType().isPlural()?"are":"is")+" in the way!", null);
								}
							}
							
						} else {
							return null;
						}
						
						case TRADING:
							if (index == 1) {
								return new Response("Drop", UtilText.parse(inventoryNPC, "You can't make [npc.name] drop [npc.her] clothing!"), null);
								
							} else if (index==4) {
								return new Response("Dye", UtilText.parse(inventoryNPC, "You can't dye [npc.namePos] clothes!"), null);
								
							}  else if(index == 5) {
								if(clothing.isCondom()) {
									if(clothing.getCondomEffect().getPotency().isNegative()) {
										return new Response("Repair (<i>1 Essence</i>)", "You can't repair [npc.namePos] condom!", null);
									}
									return new Response("Sabotage", "You can't sabotage [npc.namePos] condom!", null);
								}
								return new Response("Enchant", UtilText.parse(inventoryNPC, "You can't enchant [npc.namePos] clothing!"), null);
								
							} else if(index == 6) {
								return new Response("Unequip", UtilText.parse(inventoryNPC, "You can't unequip [npc.namePos] clothing!"), null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if (index > 10 && index - 11 < clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).size()){
								if (clothing.getDisplacedList().contains(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index - 11))) {
									return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index -11).getDescription()),
											"The "+ clothing.getName()+ " "
													+(clothing.getClothingType().isPlural()?"have":"has")+" already been "
													+ clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index -11).getDescriptionPast() + "!", null);
									
								} else {
									return new Response(Util.capitaliseSentence(clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index -11).getDescription()),
											"You can't "+clothing.getBlockedPartsKeysAsListWithoutNONE(inventoryNPC, clothing.getSlotEquippedTo()).get(index -11).getDescription() + " the " + clothing.getName() + "!", null);
								}
								
							} else {
								return null;
							}
					}
				
				}
				return null;
		}

		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.INVENTORY;
		}
	};
	
	private static String getClothingDyeUI() {
		InventorySlot slotEquippedTo = clothing.getSlotEquippedTo();
		if(slotEquippedTo==null) {
			slotEquippedTo = clothing.getClothingType().getEquipSlots().get(0);
		}
		
		inventorySB = new StringBuilder(
				"<div class='container-full-width'>"
					+ "<div class='inventoryImage'>"
						+ "<div class='inventoryImage-content'>"
							+ clothing.getSVGString()
						+ "</div>"
					+ "</div>"
					+ "<h3 style='text-align:center;'><b>"+clothing.getDisplayName(true)+"</b></h3>"
					+ "<p>"
						+ "Select the desired colours from the coloured buttons below, and after using the preview to see how the new clothing will look, press the 'Dye' option at the bottom of the screen to apply your changes."
					+ "</p>"
				+ "</div>"
					
				+ "<br/>"
				
				+ "<div class='container-full-width'>"
					+ "<div class='inventoryImage'>"
						+ "<div class='inventoryImage-content'>"
							+ clothing.getClothingType().getSVGImage(slotEquippedTo,
									dyePreviews,
									dyePreviewPattern,
									dyePreviewPatternColours,
									getDyePreviewStickersAsStrings())
						+ "</div>"
					+ "</div>");
		
		inventorySB.append("<h3 style='text-align:center;'><b>Dye & Preview</b></h3>");
		
		if(!clothing.getClothingType().getStickers().isEmpty()) {
			StringBuilder stickerSB = new StringBuilder();
			boolean stickerFound = false;
			List<StickerCategory> orderedCategories = new ArrayList<>(clothing.getClothingType().getStickers().keySet());
			Collections.sort(orderedCategories, (s1, s2)->s1.getPriority()-s2.getPriority());
			
			for(StickerCategory cat : orderedCategories) {
				stickerSB.append("<div class='container-quarter-width' style='width:calc(75% - 16px); margin:0 8px; padding:0;'>");
					stickerSB.append("<div class='container-quarter-width' style='margin:0; padding-top:6px; width:20%;'>");
						stickerSB.append(Util.capitaliseSentence(cat.getName())+":"); // Category name
					stickerSB.append("</div>");
					
					stickerSB.append("<div class='container-quarter-width' style='margin:0; padding:0; width:80%;'>");
						List<Sticker> orderedStickers = new ArrayList<>(clothing.getClothingType().getStickers().get(cat));
						Collections.sort(orderedStickers, (s1, s2)->s1.getPriority()-s2.getPriority());
						for(Sticker sticker : orderedStickers) {
							String requirements = UtilText.parse(sticker.getUnavailabilityText()).trim();
							if(requirements.isEmpty() || sticker.isShowDisabledButton()) {
								boolean specialSticker = !sticker.getAvailabilityText().isEmpty() || !sticker.getTagsApplied().isEmpty() || !sticker.getTagsRemoved().isEmpty();
								stickerFound = true;
								String id = "ITEM_STICKER_"+cat.getId()+sticker.getId();
								if(!requirements.isEmpty()) {
									stickerSB.append(
											"<div id='"+id+"' class='cosmetics-button disabled'>"
													+ "<b style='color:" + PresetColour.TEXT_GREY.toWebHexString() + ";'>" + Util.capitaliseSentence(sticker.getName()) + (specialSticker?"*":"") + "</b>"
											+ "</div>");
									
								} else if(dyePreviewStickers.get(cat)==sticker) {
									stickerSB.append(
											"<div id='"+id+"' class='cosmetics-button active'>"
													+ "<b style='color:" + sticker.getColourSelected().toWebHexString() + ";'>" + Util.capitaliseSentence(sticker.getName()) + (specialSticker?"*":"") + "</b>"
											+ "</div>");
								} else {
									stickerSB.append(
											"<div id='"+id+"' class='cosmetics-button'>"
													+ "<span style='color:"+sticker.getColourDisabled().toWebHexString()+";'>" + Util.capitaliseSentence(sticker.getName()) + (specialSticker?"*":"") + "</span>"
											+ "</div>");
								}
							}
						}
					stickerSB.append("</div>");
				stickerSB.append("</div>");
				
				if(stickerFound) {
					stickerFound = false;
					inventorySB.append(stickerSB.toString());
					stickerSB = new StringBuilder();
				}
			}
		}
		
		for(int i=0; i<clothing.getClothingType().getColourReplacements().size(); i++) {
			ColourReplacement cr = clothing.getClothingType().getColourReplacement(i);
			if(!cr.getAllColours().isEmpty()) {
				inventorySB.append("<div class='container-quarter-width' style='width:calc(75% - 16px);'>"
							+ Util.capitaliseSentence(Util.intToPrimarySequence(i+1))+" Colour"+(cr.isRecolouringAllowed()?"":" ([style.italicsBad(cannot be changed)])")+":<br/>");
				
				for(Colour c : cr.getAllColours()) {
//					if(!c.isDesaturated()) {
						inventorySB.append("<div class='normal-button"+(dyePreviews.size()>i && dyePreviews.get(i)==c?" selected":"")+"' id='DYE_CLOTHING_"+i+"_"+c.getId()+"'"
											+ " style='width:auto; margin-right:4px; border-width:1px;"
												+(cr.getDefaultColours().contains(c)
													?"border-color:"+PresetColour.TEXT_GREY.toWebHexString()+";"
													:"")
												+(dyePreviews.size()>i && dyePreviews.get(i)==c
													?" background-color:"+PresetColour.BASE_GREEN.getShades()[4]+";"
													:"")+"'>"
										+ "<div class='phone-item-colour' style='"
											+ (c.isMetallic()
													?"background: repeating-linear-gradient(135deg, " + c.toWebHexString() + ", " + c.getShades()[4] + " 10px);"
													:"background-color:" + c.toWebHexString() + ";")
											+ "'></div>"
							+ "</div>");
//					}
				}
				inventorySB.append("</div>");
			}
		}
		
		if(clothing.getClothingType().isPatternAvailable()){
			inventorySB.append(
					"<br/>"
					+ "<div class='container-full-width'>"
					+ "Pattern:<br/>");
	 
			for (Pattern pattern : Pattern.getAllPatterns()) {
				if (dyePreviewPattern.equals(pattern.getId())) {
					inventorySB.append(
							"<div class='cosmetics-button active'>"
								+ "<b style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>" + Util.capitaliseSentence(pattern.getNiceName()) + "</b>"
							+ "</div>");
				} else {
					inventorySB.append(
							"<div id='ITEM_PATTERN_"+pattern.getId()+"' class='cosmetics-button'>"
							+ "<span style='color:"+PresetColour.TRANSFORMATION_GENERIC.getShades()[0]+";'>" + Util.capitaliseSentence(pattern.getNiceName()) + "</span>"
							+ "</div>");
				}
			}
			inventorySB.append("</div>");

			for(int i=0; i<clothing.getClothingType().getPatternColourReplacements().size(); i++) {
				ColourReplacement cr = clothing.getClothingType().getPatternColourReplacement(i);
				if(!cr.getAllColours().isEmpty() && Pattern.getPattern(dyePreviewPattern).isRecolourAvailable(cr)) {
					inventorySB.append("<div class='container-quarter-width' style='width:calc(75% - 16px);'>"
								+ "Pattern "+Util.capitaliseSentence(Util.intToPrimarySequence(i+1))+" Colour:<br/>");
					
					for (Colour c : cr.getAllColours()) {
						inventorySB.append("<div class='normal-button"+(dyePreviewPatternColours.size()>i && dyePreviewPatternColours.get(i)==c?" selected":"")+"' id='DYE_CLOTHING_PATTERN_"+i+"_"+c.getId()+"'"
											+ " style='width:auto; margin-right:4px;"+(dyePreviewPatternColours.size()>i && dyePreviewPatternColours.get(i)==c?" background-color:"+PresetColour.BASE_GREEN.getShades()[4]+";":"")+"'>"
										+ "<div class='phone-item-colour' style='"
											+ (c.isMetallic()
													?"background: repeating-linear-gradient(135deg, " + c.toWebHexString() + ", " + c.getShades()[4] + " 10px);"
													:"background-color:" + c.toWebHexString() + ";")
											+ "'></div>"
							+ "</div>");
					}
					inventorySB.append("</div>");
				}
			}
		}
		inventorySB.append("</div>");
		
		return inventorySB.toString();
	}
	
	private static String getWeaponDyeUI() {
		inventorySB = new StringBuilder(
				"<div class='container-full-width'>"
					+ "<div class='inventoryImage'>"
						+ "<div class='inventoryImage-content'>"
							+ weapon.getSVGString()
						+ "</div>"
					+ "</div>"
					+ "<h3 style='text-align:center;'><b>"+weapon.getDisplayName(true)+"</b></h3>"
					+ "<p>"
						+ "Select the desired colours from the coloured buttons below, and after using the preview to see how the new weapon will look, press the 'Dye' option at the bottom of the screen to apply your changes."
					+ "</p>"
				+ "</div>"
				+ "<br/>"
				+ "<div class='container-full-width'>"
					+ "<div class='container-full-width' style='text-align:center; width:calc(25% - 16px); float:right;'>"
						+ "<div class='inventoryImage' style='width:100%;'>"
							+ (weapon.getWeaponType().isEquippedSVGImageDifferent()
								?"Unequipped"
								:"")
							+ "<div class='inventoryImage-content'>"
								+ weapon.getWeaponType().getSVGImage(damageTypePreview, dyePreviews)
							+ "</div>"
						+ "</div>"
						+(weapon.getWeaponType().isEquippedSVGImageDifferent()
							?"<div class='inventoryImage' style='width:100%;'>"
								+ "Equipped"
									+ "<div class='inventoryImage-content'>"
										+ weapon.getWeaponType().getSVGEquippedImage(GameState.game.getPlayer(), damageTypePreview, dyePreviews)
									+ "</div>"
								+ "</div>"
							:"")
					+ "</div>"
					+ "<h3 style='text-align:center;'><b>Dye & Preview</b></h3>");
		
		
		inventorySB.append("<div class='container-quarter-width' style='text-align:center;width:calc(75% - 16px);'>"
				+ "<b>Damage type:</b><br/>");
		for(DamageType dt : weapon.getWeaponType().getAvailableDamageTypes()) {
			inventorySB.append("<div class='normal-button"+(damageTypePreview==dt?" selected":"")+"' id='DAMAGE_TYPE_"+dt.toString()+"'"
							+ "style='width:20%; margin:0 2.5%; color:"+(damageTypePreview==dt?dt.getColour().toWebHexString():dt.getColour().getShades(8)[0])+";'>"
						+ Util.capitaliseSentence(dt.getName())
					+ "</div>");
		}
		inventorySB.append("</div>");

		boolean colourOptions = false;

		for(int i=0; i<weapon.getWeaponType().getColourReplacements(false).size(); i++) {
			colourOptions = true;
			ColourReplacement cr = weapon.getWeaponType().getColourReplacement(false, i);
			if(!cr.getAllColours().isEmpty()) {
				inventorySB.append("<div class='container-quarter-width' style='width:calc(75% - 16px);'>"
						+ Util.capitaliseSentence(Util.intToPrimarySequence(i+1))+" Colour"+(cr.isRecolouringAllowed()?"":" ([style.italicsBad(cannot be changed)])")+":<br/>");
				
				for(Colour c : cr.getAllColours()) {
					inventorySB.append("<div class='normal-button"+(dyePreviews.size()>i && dyePreviews.get(i)==c?" selected":"")+"' id='DYE_WEAPON_"+i+"_"+c.getId()+"'"
										+ " style='width:auto; margin-right:4px; border-width:1px;"
											+(cr.getDefaultColours().contains(c)
												?"border-color:"+PresetColour.TEXT_GREY.toWebHexString()+";"
												:"")
											+(dyePreviews.size()>i && dyePreviews.get(i)==c
												?" background-color:"+PresetColour.BASE_GREEN.getShades()[4]+";"
												:"")
										+"'>"
									+ "<div class='phone-item-colour' style='"
										+ (c.isMetallic()
												?"background: repeating-linear-gradient(135deg, " + c.toWebHexString() + ", " + c.getShades()[4] + " 10px);"
												:"background-color:" + c.toWebHexString() + ";")
										+ "'></div>"
						+ "</div>");
				}
				inventorySB.append("</div>");
			}
		}
		
		if(!colourOptions) {
			inventorySB.append("<div class='container-half-width' style='text-align:center;'>"
					+ "[style.colourDisabled(No dye options available...)]"
					+ "</div>");
		}

		inventorySB.append("</div>");
		
		return inventorySB.toString();
	}
	
	public static final DialogueNode DYE_CLOTHING = new DialogueNode("Dye clothing", "", true) {

		@Override
		public String getContent() {
			return getClothingDyeUI();
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Return to the previous menu.", INVENTORY_MENU);

			} else if (index == 1) {
				if(dyePreviews.equals(clothing.getColours())
						&& dyePreviewPattern.equals(clothing.getPattern())
						&& dyePreviewPatternColours.equals(clothing.getPatternColours())
						&& dyePreviewStickers.equals(clothing.getStickersAsObjects())) {
					return new Response("Dye",
							"You need to choose different colours before being able to dye the " + clothing.getName() + "!",
							null); 
				}
				
				return new Response("Dye",
						"Dye the " + clothing.getName() + " in the colours you have chosen."
								+ (GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
										?" This action is permanent, but thanks to your proficiency with [style.boldEarth(Earth spells)], you can dye it a different colour at any time."
										:" This action is permanent, and you'll need another dye-brush if you want to change its colour again."),
						INVENTORY_MENU) {
					@Override
					public void effects(){
						if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
							GameState.game.getPlayer().useItem(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH), owner, false);
							GameState.game.getTextEndStringBuilder().append(
									"<p style='text-align:center;'>"
										+ getDyeBrushEffects(clothing, dyePreviews.get(0))
									+ "</p>"
									+ "<p>"
										+ "<b>The " + clothing.getName() + " " + (clothing.getClothingType().isPlural() ? "have been" : "has been") + " dyed</b>!"
									+ "</p>"
									+ "<p>"
										+ (isClothingDyeActionAvailable()
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH))
														+ "</b> dye-brush" + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH)) == 1 ? "" : "es") + " left!"
												:"You have <b>0</b> dye-brushes left!")
									+ "</p>");
							
						} else {
							GameState.game.getTextEndStringBuilder().append(
									"<p>"
											+ "Thanks to your proficiency with [style.boldEarth(Earth spells)], you are able to dye the " + clothing.getName() + " without needing to use a dye-brush!"
										+ "</p>");
						}
						
						if(owner!=null) {
							owner.removeClothing(clothing);
							AbstractClothing dyedClothing = new AbstractClothing(clothing) {};
							dyedClothing.setColours(dyePreviews);
							dyedClothing.setPattern(dyePreviewPattern);
							dyedClothing.setPatternColours(dyePreviewPatternColours);
							dyedClothing.setStickersAsObjects(dyePreviewStickers);
							owner.addClothing(dyedClothing, false);
							GameState.game.addEvent(new EventLogEntry("Dyed", dyedClothing.getDisplayName(true)), false);

						} else {
							GameState.game.getPlayerCell().getInventory().removeClothing(clothing);
							AbstractClothing dyedClothing = new AbstractClothing(clothing) {};
							dyedClothing.setColours(dyePreviews);
							dyedClothing.setPattern(dyePreviewPattern);
							dyedClothing.setPatternColours(dyePreviewPatternColours);
							dyedClothing.setStickersAsObjects(dyePreviewStickers);
							GameState.game.getPlayerCell().getInventory().addClothing(dyedClothing);
							GameState.game.addEvent(new EventLogEntry("Dyed", dyedClothing.getDisplayName(true)), false);
						}
						
					}
				};

			} else if (index == 6) {
				if(dyePreviews.equals(clothing.getColours())
						&& dyePreviewPattern.equals(clothing.getPattern())
						&& dyePreviewPatternColours.equals(clothing.getPatternColours())
						&& dyePreviewStickers.equals(clothing.getStickersAsObjects())) {
					return new Response("Dye all (stack)",
							"You need to choose different colours before being able to dye the " + clothing.getName() + "!",
							null); 
				}
				
				int stackCount = 0;
				if(owner!=null) {
					stackCount = owner.getClothingCount(clothing);
				} else {
					stackCount = GameState.game.getPlayerCell().getInventory().getClothingCount(clothing);
				}

				int finalCount = stackCount;
				
				if(stackCount==1) {
					return new Response("Dye all (stack)",
							"You only have one "+clothing.getName()+", so you should dye it using the individual action...",
							null); 
				}
				
				if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
					int dyeBrushCount = GameState.game.getPlayer().getItemCount(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH));
					
					if(dyeBrushCount<stackCount) {
						return new Response("Dye all (stack)",
								"You do not have enough dye brushes to dye all the " + clothing.getNamePlural() + "! You have "+dyeBrushCount+" dye brushes, but there are "+stackCount+" "+clothing.getNamePlural()+" in this stack...",
								null); 
					}
				}
				
				return new Response("Dye all (stack)",
						"Dye all " + clothing.getNamePlural() + " which are in this clothing stack ("+stackCount+" in total) in the colours you have chosen."
								+ (GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
										?" This action is permanent, but thanks to your proficiency with [style.boldEarth(Earth spells)], you can dye them a different colour at any time."
										:" This action is permanent, and you'll need another dye-brush if you want to change their colour again."),
						INVENTORY_MENU) {
					@Override
					public void effects(){
						if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
							GameState.game.getPlayer().removeItem(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH), finalCount);
							GameState.game.getTextEndStringBuilder().append(
									"<p style='text-align:center;'>"
										+ getDyeBrushEffects(clothing, dyePreviews.get(0))
									+ "</p>"
									+ "<p>"
										+ "<b>The "+clothing.getName()+(clothing.getClothingType().isPlural()?"have":"has")+" been dyed</b>!"
									+ "</p>");
							
							GameState.game.getTextEndStringBuilder().append("<p>You then repeat this for the other "+Util.intToString(finalCount-1)+" "+clothing.getNamePlural()+"...</p>");
							
							GameState.game.getTextEndStringBuilder().append("<p>"
										+ (isClothingDyeActionAvailable()
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH))
														+ "</b> dye-brush" + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH)) == 1 ? "" : "es") + " left!"
												:"You have <b>0</b> dye-brushes left!")
									+ "</p>");
							
						} else {
							GameState.game.getTextEndStringBuilder().append(
									"<p>"
											+ "Thanks to your proficiency with [style.boldEarth(Earth spells)], you are able to dye "
												+ (finalCount==2
													?"both"
													:"all "+Util.intToString(finalCount))
												+" of the " + clothing.getNamePlural() + " without needing to use a single dye-brush!"
										+ "</p>");
						}
						
						if(owner!=null) {
							owner.removeClothing(clothing, finalCount);
							AbstractClothing dyedClothing = new AbstractClothing(clothing) {};
							dyedClothing.setColours(dyePreviews);
							dyedClothing.setPattern(dyePreviewPattern);
							dyedClothing.setPatternColours(dyePreviewPatternColours);
							dyedClothing.setStickersAsObjects(dyePreviewStickers);
							owner.addClothing(dyedClothing, finalCount, false, false);
							GameState.game.addEvent(new EventLogEntry("Dyed", dyedClothing.getDisplayName(true)), false);

						} else {
							GameState.game.getPlayerCell().getInventory().removeClothing(clothing, finalCount);
							AbstractClothing dyedClothing = new AbstractClothing(clothing) {};
							dyedClothing.setColours(dyePreviews);
							dyedClothing.setPattern(dyePreviewPattern);
							dyedClothing.setPatternColours(dyePreviewPatternColours);
							dyedClothing.setStickersAsObjects(dyePreviewStickers);
							GameState.game.getPlayerCell().getInventory().addClothing(dyedClothing, finalCount);
							GameState.game.addEvent(new EventLogEntry("Dyed", dyedClothing.getDisplayName(true)), false);
						}
					}
				};

			} else if (index == 11) {
				if(dyePreviews.equals(clothing.getColours())
						&& dyePreviewPattern.equals(clothing.getPattern())
						&& dyePreviewPatternColours.equals(clothing.getPatternColours())
						&& dyePreviewStickers.equals(clothing.getStickersAsObjects())) {
					return new Response("Dye all",
							"You need to choose different colours before being able to dye the " + clothing.getName() + "!",
							null); 
				}
				
				List<AbstractClothing> clothingMatches = new ArrayList<>();
				int stackCount = 0;
				if(owner!=null) {
					for(Entry<AbstractClothing, Integer> entry : owner.getAllClothingInInventory().entrySet()) {
						if(entry.getKey().getClothingType().equals(clothing.getClothingType())) {
							clothingMatches.add(entry.getKey());
							stackCount += entry.getValue();
						}
					}
				} else {
					for(Entry<AbstractClothing, Integer> entry : GameState.game.getPlayerCell().getInventory().getAllClothingInInventory().entrySet()) {
						if(entry.getKey().getClothingType().equals(clothing.getClothingType())) {
							clothingMatches.add(entry.getKey());
							stackCount += entry.getValue();
						}
					}
				}
				
				int finalCount = stackCount;
				
				if(stackCount==1) {
					return new Response("Dye all",
							"You only have one "+clothing.getName()+", so you should dye it using the individual action...",
							null); 
				}
				
				if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
					int dyeBrushCount = GameState.game.getPlayer().getItemCount(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH));
					
					if(dyeBrushCount<stackCount) {
						return new Response("Dye all",
								"You do not have enough dye brushes to dye all the " + clothing.getNamePlural() + "! You have "+dyeBrushCount+" dye brushes, but there are "+stackCount+" "+clothing.getNamePlural()+" in total...",
								null); 
					}
				}
				
				return new Response("Dye all",
						"Dye all " + clothing.getNamePlural() + " which are in this clothing stack ("+stackCount+" in total) in the colours you have chosen."
								+ (GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
										?" This action is permanent, but thanks to your proficiency with [style.boldEarth(Earth spells)], you can dye them a different colour at any time."
										:" This action is permanent, and you'll need another dye-brush if you want to change their colour again."),
						INVENTORY_MENU) {
					@Override
					public void effects(){
						if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
							GameState.game.getPlayer().removeItem(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH), finalCount);
							GameState.game.getTextEndStringBuilder().append(
									"<p style='text-align:center;'>"
										+ getDyeBrushEffects(clothing, dyePreviews.get(0))
									+ "</p>"
									+ "<p>"
									+ "<b>The "+clothing.getName()+(clothing.getClothingType().isPlural()?"have":"has")+" been dyed</b>!"
									+ "</p>");
							
							GameState.game.getTextEndStringBuilder().append("<p>You then repeat this for the other "+Util.intToString(finalCount-1)+" "+clothing.getNamePlural()+"...</p>");
							
							GameState.game.getTextEndStringBuilder().append("<p>"
										+ (isClothingDyeActionAvailable()
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH))
														+ "</b> dye-brush" + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH)) == 1 ? "" : "es") + " left!"
												:"You have <b>0</b> dye-brushes left!")
									+ "</p>");
							
						} else {
							GameState.game.getTextEndStringBuilder().append(
									"<p>"
											+ "Thanks to your proficiency with [style.boldEarth(Earth spells)], you are able to dye "
												+ (finalCount==2
													?"both"
													:"all "+Util.intToString(finalCount))
												+" of the " + clothing.getNamePlural() + " without needing to use a single dye-brush!"
										+ "</p>");
						}
						
						if(owner!=null) {
							for(AbstractClothing c : clothingMatches) {
								int clothingCount = owner.getAllClothingInInventory().get(c);
								owner.removeClothing(c, clothingCount);
								AbstractClothing dyedClothing = new AbstractClothing(c) {};
								dyedClothing.setColours(dyePreviews);
								dyedClothing.setPattern(dyePreviewPattern);
								dyedClothing.setPatternColours(dyePreviewPatternColours);
								dyedClothing.setStickersAsObjects(dyePreviewStickers);
								owner.addClothing(dyedClothing, clothingCount, false, false);
								GameState.game.addEvent(new EventLogEntry("Dyed", dyedClothing.getDisplayName(true)), false);
							}
							
						} else {
							for(AbstractClothing c : clothingMatches) {
								int clothingCount = GameState.game.getPlayerCell().getInventory().getAllClothingInInventory().get(c);
								GameState.game.getPlayerCell().getInventory().removeClothing(c, clothingCount);
								AbstractClothing dyedClothing = new AbstractClothing(c) {};
								dyedClothing.setColours(dyePreviews);
								dyedClothing.setPattern(dyePreviewPattern);
								dyedClothing.setPatternColours(dyePreviewPatternColours);
								dyedClothing.setStickersAsObjects(dyePreviewStickers);
								GameState.game.getPlayerCell().getInventory().addClothing(dyedClothing, clothingCount);
								GameState.game.addEvent(new EventLogEntry("Dyed", dyedClothing.getDisplayName(true)), false);
							}
						}
					}
				};

			} else {
				return null;
			}
		}

		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.INVENTORY;
		}
	};

	public static final DialogueNode DYE_EQUIPPED_CLOTHING = new DialogueNode("Dye clothing", "", true) {

		@Override
		public String getContent() {
			return getClothingDyeUI();
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Return to the previous menu.", INVENTORY_MENU);

			} else if (index == 1) {
				if(dyePreviews.equals(clothing.getColours())
						&& dyePreviewPattern.equals(clothing.getPattern())
						&& dyePreviewPatternColours.equals(clothing.getPatternColours())
						&& dyePreviewStickers.equals(clothing.getStickersAsObjects())) {
					return new Response("Dye",
							"You need to choose different colours before being able to dye the " + clothing.getName() + "!",
							null); 
				}
				
				return new Response("Dye",
								"Dye the " + clothing.getName() + " in the colours you have chosen."
										+ (GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
												?" This action is permanent, but thanks to your proficiency with [style.boldEarth(Earth spells)], you can dye it a different colour at any time."
												:" This action is permanent, and you'll need another dye-brush if you want to change its colour again."),
								INVENTORY_MENU){
					@Override
					public void effects(){
						if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
							GameState.game.getPlayer().useItem(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH), owner, false);
							GameState.game.getTextEndStringBuilder().append(
									"<p style='text-align:center;'>"
										+ getDyeBrushEffects(clothing, dyePreviews.get(0))
									+ "</p>"
									+ "<p>"
										+ "<b>The " + clothing.getName() + " " + (clothing.getClothingType().isPlural() ? "have been" : "has been") + " dyed</b>!"
									+ "</p>"
									+ "<p>"
										+ (isClothingDyeActionAvailable()
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH))
														+ "</b> dye-brush" + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH)) == 1 ? "" : "es") + " left!"
												:"You have <b>0</b> dye-brushes left!")
									+ "</p>");
							
						} else {
							GameState.game.getTextEndStringBuilder().append(
									"<p>"
											+ "Thanks to your proficiency with [style.boldEarth(Earth spells)], you are able to dye the " + clothing.getName() + " without needing to use a dye-brush!"
										+ "</p>");
						}
						
						clothing.setColours(dyePreviews);
						clothing.setPattern(dyePreviewPattern);
						clothing.setPatternColours(dyePreviewPatternColours);
						clothing.setStickersAsObjects(dyePreviewStickers);
						
						GameState.game.addEvent(new EventLogEntry("Dyed", clothing.getDisplayName(true)), false);
					}
				};

			} else
				return null;
		}

		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.INVENTORY;
		}
	};
	
	
	public static final DialogueNode DYE_CLOTHING_CHARACTER_CREATION = new DialogueNode("Choose Colour", "", true) {

		@Override
		public String getContent() {
			return getClothingDyeUI();
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Return to the previous menu.", CLOTHING_INVENTORY);

			} else if (index == 1) {
				if(dyePreviews.equals(clothing.getColours())
						&& dyePreviewPattern.equals(clothing.getPattern())
						&& dyePreviewPatternColours.equals(clothing.getPatternColours())
						&& dyePreviewStickers.equals(clothing.getStickersAsObjects())) {
					return new Response("Dye",
							"You need to choose different colours before being able to dye the " + clothing.getName() + "!",
							null); 
				}
				
				return new Response("Dye",
						"Change the colour of the " + clothing.getName() + " to the colours you have chosen.",
						INVENTORY_MENU){
					@Override
					public void effects(){
						GameState.game.getPlayerCell().getInventory().removeClothing(clothing);
						AbstractClothing dyedClothing = new AbstractClothing(clothing) {};
						dyedClothing.setColours(dyePreviews);
						dyedClothing.setPattern(dyePreviewPattern);
						dyedClothing.setPatternColours(dyePreviewPatternColours);
						dyedClothing.setStickersAsObjects(dyePreviewStickers);
						clothing = dyedClothing;
						GameState.game.getPlayerCell().getInventory().addClothing(dyedClothing);
					}
				};

			} else
				return null;
		}

		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.INVENTORY;
		}
	};

	public static final DialogueNode DYE_EQUIPPED_CLOTHING_CHARACTER_CREATION = new DialogueNode("Choose Colour", "", true) {

		@Override
		public String getContent() {
			return getClothingDyeUI();
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Return to the previous menu.", CLOTHING_EQUIPPED);

			} else if (index  == 1) {
				if(dyePreviews.equals(clothing.getColours())
						&& dyePreviewPattern.equals(clothing.getPattern())
						&& dyePreviewPatternColours.equals(clothing.getPatternColours())
						&& dyePreviewStickers.equals(clothing.getStickersAsObjects())) {
					return new Response("Dye",
							"You need to choose different colours before being able to dye the " + clothing.getName() + "!",
							null); 
				}
				
				return new Response("Dye",
						"Change the colour of the " + clothing.getName() + " to the colours you have chosen.",
						CLOTHING_EQUIPPED){
					@Override
					public void effects(){
						clothing.setColours(dyePreviews);
						clothing.setPattern(dyePreviewPattern);
						clothing.setPatternColours(dyePreviewPatternColours);
						clothing.setStickersAsObjects(dyePreviewStickers);
					}
				};

			} else
				return null;
		}

		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.INVENTORY;
		}
	};

	public static final DialogueNode DYE_WEAPON = new DialogueNode("Dye Weapon", "", true) {

		@Override
		public String getContent() {
			return getWeaponDyeUI();
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Return to the previous menu.", INVENTORY_MENU);

			} else if (index == 1) {
				if (!GameState.game.getPlayer().hasItemType(ItemType.DYE_BRUSH)
						&& !GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
						&& !GameState.game.isDebugMode()) {
					return new Response("Dye",
							"You do not have a dye-brush, so cannot change the colours of the " + weapon.getName() + "...",
							null); 
				}
				
				if(dyePreviews.equals(weapon.getColours())) {
					return new Response("Dye",
							"You need to choose different colours before being able to dye the " + weapon.getName() + "!",
							null); 
				}
				
				return new Response("Dye",
						"Dye the " + weapon.getName() + " in the colours you have chosen."
								+ (GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
										?" This action is permanent, but thanks to your proficiency with [style.boldEarth(Earth spells)], you can dye it a different colour at any time."
										:" This action is permanent, and you'll need another dye-brush if you want to change its colour again."),
						INVENTORY_MENU){
					@Override
					public void effects(){
						if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
							GameState.game.getPlayer().useItem(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH), owner, false);
							GameState.game.getTextEndStringBuilder().append(
									"<p style='text-align:center;'>"
										+ getDyeBrushEffects(weapon, dyePreviews.get(0))
									+ "</p>"
									+ "<p>"
										+ "<b>The " + weapon.getName() + " " + (weapon.getWeaponType().isPlural() ? "have been" : "has been") + " dyed</b>!"
									+ "</p>"
									+ "<p>"
										+ (isClothingDyeActionAvailable()
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH))
														+ "</b> dye-brush" + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH)) == 1 ? "" : "es") + " left!"
												:"You have <b>0</b> dye-brushes left!")
									+ "</p>");
							
						} else {
							GameState.game.getTextEndStringBuilder().append(
									"<p>"
										+ "Thanks to your proficiency with [style.boldEarth(Earth spells)], you are able to dye the " + weapon.getName() + " without needing to use a dye-brush!"
									+ "</p>");
						}
						 
						if(owner!=null) {
							owner.removeWeapon(weapon);
							AbstractWeapon dyedWeapon = GameState.game.getItemGen().generateWeapon(weapon);
							dyedWeapon.setColours(dyePreviews);
							owner.addWeapon(dyedWeapon, false);
							GameState.game.addEvent(new EventLogEntry("Dyed", dyedWeapon.getDisplayName(true)), false);
							weapon = dyedWeapon;

						} else {
							GameState.game.getPlayerCell().getInventory().removeWeapon(weapon);
							AbstractWeapon dyedWeapon = GameState.game.getItemGen().generateWeapon(weapon);
							dyedWeapon.setColours(dyePreviews);
							GameState.game.getPlayerCell().getInventory().addWeapon(dyedWeapon);
							GameState.game.addEvent(new EventLogEntry("Dyed", dyedWeapon.getDisplayName(true)), false);
							weapon = dyedWeapon;
						}
					}
				};

			} else if (index == 2) {
				if (!GameState.game.getPlayer().hasItemType(ItemType.REFORGE_HAMMER)
						&& !GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
						&& !GameState.game.isDebugMode()) {
					return new Response("Reforge",
							"You do not have a reforging hammer, so cannot change the damage type of the " + weapon.getName() + "...",
							null); 
				}
				
				if(damageTypePreview == weapon.getDamageType()) {
					return new Response("Reforge",
							"You need to choose a different damage type before being able to reforge the " + weapon.getName() + "!",
							null); 
				}
				
				return new Response("Reforge",
						"Reforge the " + weapon.getName() + " into the damage type you have chosen."
								+ (GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
										?" This action is permanent, but thanks to your proficiency with [style.boldEarth(Earth spells)], you can reforge it at any time."
										:" This action is permanent, and you'll need another reforging hammer if you want to change its damage type again."),
						INVENTORY_MENU){
					@Override
					public void effects(){
						if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
							GameState.game.getPlayer().useItem(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER), owner, false);
							
							GameState.game.getTextEndStringBuilder().append(
									"<p style='text-align:center;'>"
										+ getReforgeHammerEffects(weapon, damageTypePreview)
									+ "</p>"
									+ "<p>"
										+ "<b>The " + weapon.getName() + " " + (weapon.getWeaponType().isPlural() ? "have been" : "has been") + " reforged</b>!"
									+ "</p>"
									+ "<p>"
										+ (GameState.game.getPlayer().hasItemType(ItemType.REFORGE_HAMMER) || GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER))
														+ "</b> reforging " + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER)) == 1 ? "hammer" : "hammers") + " left!"
												:"You have <b>0</b> reforging hammers left!")
									+ "</p>");
							
						} else {
							GameState.game.getTextEndStringBuilder().append(
									"<p>"
											+ "Thanks to your proficiency with [style.boldEarth(Earth spells)], you are able to reforge the " + weapon.getName() + " without needing to use a reforging hammer!"
										+ "</p>");
						}
						 
						if(owner!=null) {
							owner.removeWeapon(weapon);
							AbstractWeapon modifiedWeapon = GameState.game.getItemGen().generateWeapon(weapon);
							modifiedWeapon.setDamageType(damageTypePreview);
							// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
							owner.addWeapon(GameState.game.getItemGen().generateWeapon(modifiedWeapon), false);
							GameState.game.addEvent(new EventLogEntry("Reforged", modifiedWeapon.getDisplayName(true)), false);

						} else {
							GameState.game.getPlayerCell().getInventory().removeWeapon(weapon);
							AbstractWeapon modifiedWeapon = GameState.game.getItemGen().generateWeapon(weapon);
							modifiedWeapon.setDamageType(damageTypePreview);
							// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
							GameState.game.getPlayerCell().getInventory().addWeapon(GameState.game.getItemGen().generateWeapon(modifiedWeapon));
							GameState.game.addEvent(new EventLogEntry("Reforged", modifiedWeapon.getDisplayName(true)), false);
						}
					}
				};

			} else if (index == 3) {
				if ((!GameState.game.getPlayer().hasItemType(ItemType.REFORGE_HAMMER) || !GameState.game.getPlayer().hasItemType(ItemType.DYE_BRUSH))
						&& !GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
						&& !GameState.game.isDebugMode()) {
					return new Response("Dye & reforge",
							"You do not have both a dye brush and a reforging hammer, so cannot dye and reforge the " + weapon.getName() + "...",
							null); 
				}
				
				if(damageTypePreview == weapon.getDamageType()) {
					return new Response("Dye & reforge",
							"You need to choose a different damage type before being able to reforge the " + weapon.getName() + "!",
							null); 
				}
				
				if(dyePreviews.equals(weapon.getColours())) {
					return new Response("Dye & reforge",
							"You need to choose different colours before being able to dye the " + weapon.getName() + "!",
							null); 
				}
				
				return new Response("Dye & reforge",
						"Dye and reforge the " + weapon.getName() + " into the colours and damage type you have chosen."
								+ (GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
										?" This action is permanent, but thanks to your proficiency with [style.boldEarth(Earth spells)], you can dye and reforge it at any time."
										:" This action is permanent, and you'll need another dye-brush and another reforging hammer if you want to change its colours and damage type again."),
						INVENTORY_MENU){
					@Override
					public void effects(){
						if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
							GameState.game.getPlayer().useItem(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH), owner, false);
							GameState.game.getPlayer().useItem(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER), owner, false);
							GameState.game.getTextEndStringBuilder().append(
									"<p style='text-align:center;'>"
										+ getDyeBrushEffects(weapon, dyePreviews.get(0))
									+ "</p>"
									+ "<p style='text-align:center;'>"
										+ getReforgeHammerEffects(weapon, damageTypePreview)
									+ "</p>"
									+ "<p>"
										+ "<b>The " + weapon.getName() + " " + (weapon.getWeaponType().isPlural() ? "have been" : "has been") + " dyed and reforged</b>!"
									+ "</p>"
									+ "<p>"
										+ (isClothingDyeActionAvailable()
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH))
														+ "</b> dye-brush" + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH)) == 1 ? "" : "es") + " left!"
												:"You have <b>0</b> dye-brushes left!")
										+"<br/>"
										+ (GameState.game.getPlayer().hasItemType(ItemType.REFORGE_HAMMER) || GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER))
														+ "</b> reforging " + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER)) == 1 ? "hammer" : "hammers") + " left!"
												:"You have <b>0</b> reforging hammers left!")
									+ "</p>");
							
						} else {
							GameState.game.getTextEndStringBuilder().append(
									"<p>"
											+ "Thanks to your proficiency with [style.boldEarth(Earth spells)], you are able to dye and reforge the " + weapon.getName() + " without needing to use a dye-brush or reforging hammer!"
										+ "</p>");
						}
						
						if(owner!=null) {
							owner.removeWeapon(weapon);
							AbstractWeapon modifiedWeapon = GameState.game.getItemGen().generateWeapon(weapon);
							modifiedWeapon.setDamageType(damageTypePreview);
							modifiedWeapon.setColours(dyePreviews);
							// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
							owner.addWeapon(GameState.game.getItemGen().generateWeapon(modifiedWeapon), false);
							GameState.game.addEvent(new EventLogEntry("Dyed & Reforged", modifiedWeapon.getDisplayName(true)), false);

						} else {
							GameState.game.getPlayerCell().getInventory().removeWeapon(weapon);
							AbstractWeapon modifiedWeapon = GameState.game.getItemGen().generateWeapon(weapon);
							modifiedWeapon.setDamageType(damageTypePreview);
							modifiedWeapon.setColours(dyePreviews);
							// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
							GameState.game.getPlayerCell().getInventory().addWeapon(GameState.game.getItemGen().generateWeapon(modifiedWeapon));
							GameState.game.addEvent(new EventLogEntry("Dyed & Reforged", modifiedWeapon.getDisplayName(true)), false);
						}
					}
				};

			} else if (index == 6) {
				if(dyePreviews.equals(weapon.getColours())) {
					return new Response("Dye all (stack)",
							"You need to choose different colours before being able to dye the " + weapon.getName() + "!",
							null); 
				}
				
				int stackCount = 0;
				if(owner!=null) {
					stackCount = owner.getWeaponCount(weapon);
				} else {
					stackCount = GameState.game.getPlayerCell().getInventory().getWeaponCount(weapon);
				}

				int finalCount = stackCount;
				
				if(stackCount==1) {
					return new Response("Dye all (stack)",
							"You only have one "+weapon.getName()+", so you should dye it using the individual action...",
							null); 
				}
				
				if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
					int dyeBrushCount = GameState.game.getPlayer().getItemCount(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH));
					
					if(dyeBrushCount<stackCount) {
						return new Response("Dye all (stack)",
								"You do not have enough dye brushes to dye all the " + weapon.getNamePlural() + "! You have "+dyeBrushCount+" dye brushes, but there are "+stackCount+" "+weapon.getNamePlural()+" in this stack...",
								null); 
					}
				}
				
				return new Response("Dye all (stack)",
						"Dye all " + weapon.getNamePlural() + " which are in this weapon stack ("+stackCount+" in total) in the colours you have chosen."
								+ (GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
										?" This action is permanent, but thanks to your proficiency with [style.boldEarth(Earth spells)], you can dye them a different colour at any time."
										:" This action is permanent, and you'll need another dye-brush if you want to change their colour again."),
						INVENTORY_MENU) {
					@Override
					public void effects(){
						if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
							GameState.game.getPlayer().removeItem(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH), finalCount);
							GameState.game.getTextEndStringBuilder().append(
									"<p style='text-align:center;'>"
										+ getDyeBrushEffects(weapon, dyePreviews.get(0))
									+ "</p>"
									+ "<p>"
									+ "<b>The "+weapon.getName()+(weapon.getWeaponType().isPlural()?"have":"has")+" been dyed</b>!"
									+ "</p>");
							
							GameState.game.getTextEndStringBuilder().append("<p>You then repeat this for the other "+Util.intToString(finalCount-1)+" "+weapon.getNamePlural()+"...</p>");
							
							GameState.game.getTextEndStringBuilder().append("<p>"
										+ (isClothingDyeActionAvailable()
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH))
														+ "</b> dye-brush" + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH)) == 1 ? "" : "es") + " left!"
												:"You have <b>0</b> dye-brushes left!")
									+ "</p>");
							
						} else {
							GameState.game.getTextEndStringBuilder().append(
									"<p>"
											+ "Thanks to your proficiency with [style.boldEarth(Earth spells)], you are able to dye "
												+ (finalCount==2
													?"both"
													:"all "+Util.intToString(finalCount))
												+" of the " + weapon.getNamePlural() + " without needing to use a single dye-brush!"
										+ "</p>");
						}
						
						if(owner!=null) {
							owner.removeWeapon(weapon, finalCount);
							AbstractWeapon dyedWeapon = GameState.game.getItemGen().generateWeapon(weapon);
							dyedWeapon.setColours(dyePreviews);
							owner.addWeapon(dyedWeapon, finalCount, false, false);
							GameState.game.addEvent(new EventLogEntry("Dyed", dyedWeapon.getDisplayName(true)), false);

						} else {
							GameState.game.getPlayerCell().getInventory().removeWeapon(weapon, finalCount);
							AbstractWeapon dyedWeapon = GameState.game.getItemGen().generateWeapon(weapon);
							dyedWeapon.setColours(dyePreviews);
							GameState.game.getPlayerCell().getInventory().addWeapon(dyedWeapon, finalCount);
							GameState.game.addEvent(new EventLogEntry("Dyed", dyedWeapon.getDisplayName(true)), false);
						}
					}
				};

			} else if (index == 7) {
				if(damageTypePreview == weapon.getDamageType()) {
					return new Response("Reforge all (stack)",
							"You need to choose a different damage type before being able to reforge the " + weapon.getName() + "!",
							null); 
				}
				
				int stackCount = 0;
				if(owner!=null) {
					stackCount = owner.getWeaponCount(weapon);
				} else {
					stackCount = GameState.game.getPlayerCell().getInventory().getWeaponCount(weapon);
				}

				int finalCount = stackCount;
				
				if(stackCount==1) {
					return new Response("Reforge all (stack)",
							"You only have one "+weapon.getName()+", so you should reforge it using the individual action...",
							null); 
				}
				
				if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
					int reforgeHammerCount = GameState.game.getPlayer().getItemCount(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER));
					
					if(reforgeHammerCount<stackCount) {
						return new Response("Reforge all (stack)",
								"You do not have enough reforging hammers to reforge all of the " + weapon.getNamePlural() + "! You have "+reforgeHammerCount+" reforging hammers, but there are "+stackCount+" "+weapon.getNamePlural()+" in this stack...",
								null); 
					}
				}
				
				return new Response("Reforge all (stack)",
						"Reforge all " + weapon.getNamePlural() + " which are in this weapon stack ("+stackCount+" in total) into the damage type you have chosen."
								+ (GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
										?" This action is permanent, but thanks to your proficiency with [style.boldEarth(Earth spells)], you can reforge them at any time."
										:" This action is permanent, and you'll need another reforging hammer if you want to change their damage type again."),
						INVENTORY_MENU) {
					@Override
					public void effects(){
						if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
							GameState.game.getPlayer().removeItem(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER), finalCount);
							GameState.game.getTextEndStringBuilder().append(
									"<p style='text-align:center;'>"
										+ getReforgeHammerEffects(weapon, damageTypePreview)
									+ "</p>"
									+ "<p>"
									+ "<b>The "+weapon.getName()+(weapon.getWeaponType().isPlural()?"have":"has")+" been reforged</b>!"
									+ "</p>");
							
							GameState.game.getTextEndStringBuilder().append("<p>You then repeat this for the other "+Util.intToString(finalCount-1)+" "+weapon.getNamePlural()+"...</p>");
							
							GameState.game.getTextEndStringBuilder().append("<p>"
										+ (GameState.game.getPlayer().hasItemType(ItemType.REFORGE_HAMMER) || GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER))
														+ "</b> reforging hammer" + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER)) == 1 ? "" : "s") + " left!"
												:"You have <b>0</b> reforging hammers left!")
									+ "</p>");
							
						} else {
							GameState.game.getTextEndStringBuilder().append(
									"<p>"
											+ "Thanks to your proficiency with [style.boldEarth(Earth spells)], you are able to reforge "
												+ (finalCount==2
													?"both"
													:"all "+Util.intToString(finalCount))
												+" of the " + weapon.getNamePlural() + " without needing to use a single reforging hammer!"
										+ "</p>");
						}
						
						if(owner!=null) {
							owner.removeWeapon(weapon, finalCount);
							AbstractWeapon modifiedWeapon = GameState.game.getItemGen().generateWeapon(weapon);
							modifiedWeapon.setDamageType(damageTypePreview);
							// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
							owner.addWeapon(GameState.game.getItemGen().generateWeapon(modifiedWeapon), finalCount, false, false);
							GameState.game.addEvent(new EventLogEntry("Reforged", modifiedWeapon.getDisplayName(true)), false);

						} else {
							GameState.game.getPlayerCell().getInventory().removeWeapon(weapon, finalCount);
							AbstractWeapon modifiedWeapon = GameState.game.getItemGen().generateWeapon(weapon);
							modifiedWeapon.setDamageType(damageTypePreview);
							// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
							GameState.game.getPlayerCell().getInventory().addWeapon(GameState.game.getItemGen().generateWeapon(modifiedWeapon), finalCount);
							GameState.game.addEvent(new EventLogEntry("Reforged", modifiedWeapon.getDisplayName(true)), false);
						}
					}
				};

			} else if (index == 8) {
				if(damageTypePreview == weapon.getDamageType()) {
					return new Response("Dye & reforge all (stack)",
							"You need to choose a different damage type before being able to reforge the " + weapon.getName() + "!",
							null); 
				}
				
				if(dyePreviews.equals(weapon.getColours())) {
					return new Response("Dye & reforge all (stack)",
							"You need to choose different colours before being able to dye the " + weapon.getName() + "!",
							null); 
				}
				
				
				int stackCount = 0;
				if(owner!=null) {
					stackCount = owner.getWeaponCount(weapon);
				} else {
					stackCount = GameState.game.getPlayerCell().getInventory().getWeaponCount(weapon);
				}

				int finalCount = stackCount;
				
				if(stackCount==1) {
					return new Response("Dye & reforge all (stack)",
							"You only have one "+weapon.getName()+", so you should dye & reforge it using the individual action...",
							null); 
				}
				
				if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
					int dyeBrushCount = GameState.game.getPlayer().getItemCount(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH));
					int reforgeHammerCount = GameState.game.getPlayer().getItemCount(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER));
					
					if(dyeBrushCount<stackCount || reforgeHammerCount<stackCount) {
						return new Response("Dye & reforge all (stack)",
								"You do not have enough dye brushes or reforge hammers to dye & reforge all "+stackCount+" "+weapon.getNamePlural()+" in this stack...",
								null); 
					}
				}
				
				return new Response("Dye & reforge all (stack)",
						"Dye & reforge all " + weapon.getNamePlural() + " which are in this weapon stack ("+stackCount+" in total) in the colours and damage type you have chosen."
								+ (GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
										?" This action is permanent, but thanks to your proficiency with [style.boldEarth(Earth spells)], you can do this at any time."
										:" This action is permanent, and you'll need another dye-brush and reforging hammer if you want to do this again."),
						INVENTORY_MENU) {
					@Override
					public void effects(){
						if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
							GameState.game.getPlayer().removeItem(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH), finalCount);
							GameState.game.getPlayer().removeItem(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER), finalCount);
							GameState.game.getTextEndStringBuilder().append(
									"<p style='text-align:center;'>"
										+ getDyeBrushEffects(weapon, dyePreviews.get(0))
									+ "</p>"
									+ "<p style='text-align:center;'>"
										+ getReforgeHammerEffects(weapon, damageTypePreview)
									+ "</p>"
									+ "<p>"
									+ "<b>The "+weapon.getName()+(weapon.getWeaponType().isPlural()?"have":"has")+" been dyed and reforged</b>!"
									+ "</p>");
							
							GameState.game.getTextEndStringBuilder().append("<p>You then repeat this for the other "+Util.intToString(finalCount-1)+" "+weapon.getNamePlural()+"...</p>");
							
							GameState.game.getTextEndStringBuilder().append("<p>"
										+ (GameState.game.getPlayer().hasItemType(ItemType.DYE_BRUSH)
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH))
														+ "</b> dye-brush" + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH)) == 1 ? "" : "es") + " left!"
												:"You have <b>0</b> dye-brushes left!")
									+ "</p>");

							GameState.game.getTextEndStringBuilder().append("<p>"
										+ (GameState.game.getPlayer().hasItemType(ItemType.REFORGE_HAMMER)
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER))
														+ "</b> reforging hammer" + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER)) == 1 ? "" : "s") + " left!"
												:"You have <b>0</b> reforging hammers left!")
									+ "</p>");
							
						} else {
							GameState.game.getTextEndStringBuilder().append(
									"<p>"
											+ "Thanks to your proficiency with [style.boldEarth(Earth spells)], you are able to dye "
												+ (finalCount==2
													?"both"
													:"all "+Util.intToString(finalCount))
												+" of the " + weapon.getNamePlural() + " without needing to use a single dye-brush or reforging hammer!"
										+ "</p>");
						}
						
						if(owner!=null) {
							owner.removeWeapon(weapon, finalCount);
							AbstractWeapon modifiedWeapon = GameState.game.getItemGen().generateWeapon(weapon);
							modifiedWeapon.setDamageType(damageTypePreview);
							modifiedWeapon.setColours(dyePreviews);
							// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
							owner.addWeapon(GameState.game.getItemGen().generateWeapon(modifiedWeapon), finalCount, false, false);
							GameState.game.addEvent(new EventLogEntry("Dyed & Reforged", modifiedWeapon.getDisplayName(true)), false);

						} else {
							GameState.game.getPlayerCell().getInventory().removeWeapon(weapon, finalCount);
							AbstractWeapon modifiedWeapon = GameState.game.getItemGen().generateWeapon(weapon);
							modifiedWeapon.setDamageType(damageTypePreview);
							modifiedWeapon.setColours(dyePreviews);
							// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
							GameState.game.getPlayerCell().getInventory().addWeapon(GameState.game.getItemGen().generateWeapon(modifiedWeapon), finalCount);
							GameState.game.addEvent(new EventLogEntry("Dyed & Reforged", modifiedWeapon.getDisplayName(true)), false);
						}
					}
				};

			} else if (index == 11) {
				if(dyePreviews.equals(weapon.getColours())) {
					return new Response("Dye all",
							"You need to choose different colours before being able to dye the " + weapon.getName() + "!",
							null); 
				}
				
				List<AbstractWeapon> weaponMatches = new ArrayList<>();
				int stackCount = 0;
				if(owner!=null) {
					for(Entry<AbstractWeapon, Integer> entry : owner.getAllWeaponsInInventory().entrySet()) {
						if(entry.getKey().getWeaponType().equals(weapon.getWeaponType())) {
							weaponMatches.add(entry.getKey());
							stackCount += entry.getValue();
						}
					}
				} else {
					for(Entry<AbstractWeapon, Integer> entry : GameState.game.getPlayerCell().getInventory().getAllWeaponsInInventory().entrySet()) {
						if(entry.getKey().getWeaponType().equals(weapon.getWeaponType())) {
							weaponMatches.add(entry.getKey());
							stackCount += entry.getValue();
						}
					}
				}
				
				int finalCount = stackCount;
				
				if(stackCount==1) {
					return new Response("Dye all",
							"You only have one "+weapon.getName()+", so you should dye it using the individual action...",
							null); 
				}
				
				if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
					int dyeBrushCount = GameState.game.getPlayer().getItemCount(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH));
					
					if(dyeBrushCount<stackCount) {
						return new Response("Dye all",
								"You do not have enough dye brushes to dye all the " + weapon.getNamePlural() + "! You have "+dyeBrushCount+" dye brushes, but there are "+stackCount+" "+weapon.getNamePlural()+" in total...",
								null); 
					}
				}
				
				return new Response("Dye all",
						"Dye all " + weapon.getNamePlural() + " which are in this weapon stack ("+stackCount+" in total) in the colours you have chosen."
								+ (GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
										?" This action is permanent, but thanks to your proficiency with [style.boldEarth(Earth spells)], you can dye them a different colour at any time."
										:" This action is permanent, and you'll need another dye-brush if you want to change their colour again."),
						INVENTORY_MENU) {
					@Override
					public void effects(){
						if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
							GameState.game.getPlayer().removeItem(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH), finalCount);
							GameState.game.getTextEndStringBuilder().append(
									"<p style='text-align:center;'>"
										+ getDyeBrushEffects(weapon, dyePreviews.get(0))
									+ "</p>"
									+ "<p>"
									+ "<b>The "+weapon.getName()+(weapon.getWeaponType().isPlural()?"have":"has")+" been dyed</b>!"
									+ "</p>");
							
							GameState.game.getTextEndStringBuilder().append("<p>You then repeat this for the other "+Util.intToString(finalCount-1)+" "+weapon.getNamePlural()+"...</p>");
							
							GameState.game.getTextEndStringBuilder().append("<p>"
										+ (isClothingDyeActionAvailable()
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH))
														+ "</b> dye-brush" + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH)) == 1 ? "" : "es") + " left!"
												:"You have <b>0</b> dye-brushes left!")
									+ "</p>");
							
						} else {
							GameState.game.getTextEndStringBuilder().append(
									"<p>"
											+ "Thanks to your proficiency with [style.boldEarth(Earth spells)], you are able to dye "
												+ (finalCount==2
													?"both"
													:"all "+Util.intToString(finalCount))
												+" of the " + weapon.getNamePlural() + " without needing to use a single dye-brush!"
										+ "</p>");
						}
						

						if(owner!=null) {
							for(AbstractWeapon w : weaponMatches) {
								int weaponCount = owner.getAllWeaponsInInventory().get(w);
								owner.removeWeapon(w, weaponCount);
								AbstractWeapon dyedWeapon = GameState.game.getItemGen().generateWeapon(w);
								dyedWeapon.setColours(dyePreviews);
								owner.addWeapon(dyedWeapon, weaponCount, false, false);
								GameState.game.addEvent(new EventLogEntry("Dyed", dyedWeapon.getDisplayName(true)), false);
							}
							
						} else {
							for(AbstractWeapon w : weaponMatches) {
								int weaponCount = GameState.game.getPlayerCell().getInventory().getAllWeaponsInInventory().get(w);
								GameState.game.getPlayerCell().getInventory().removeWeapon(w, weaponCount);
								AbstractWeapon dyedWeapon = GameState.game.getItemGen().generateWeapon(w);
								dyedWeapon.setColours(dyePreviews);
								GameState.game.getPlayerCell().getInventory().addWeapon(dyedWeapon, weaponCount);
								GameState.game.addEvent(new EventLogEntry("Dyed", dyedWeapon.getDisplayName(true)), false);
							}
						}
					}
				};

			} else if (index == 12) {
				if(damageTypePreview == weapon.getDamageType()) {
					return new Response("Reforge all",
							"You need to choose a different damage type before being able to reforge the " + weapon.getName() + "!",
							null); 
				}
				
				List<AbstractWeapon> weaponMatches = new ArrayList<>();
				int stackCount = 0;
				if(owner!=null) {
					for(Entry<AbstractWeapon, Integer> entry : owner.getAllWeaponsInInventory().entrySet()) {
						if(entry.getKey().getWeaponType().equals(weapon.getWeaponType())) {
							weaponMatches.add(entry.getKey());
							stackCount += entry.getValue();
						}
					}
				} else {
					for(Entry<AbstractWeapon, Integer> entry : GameState.game.getPlayerCell().getInventory().getAllWeaponsInInventory().entrySet()) {
						if(entry.getKey().getWeaponType().equals(weapon.getWeaponType())) {
							weaponMatches.add(entry.getKey());
							stackCount += entry.getValue();
						}
					}
				}
				
				int finalCount = stackCount;
				
				if(stackCount==1) {
					return new Response("Reforge all",
							"You only have one "+weapon.getName()+", so you should reforge it using the individual action...",
							null); 
				}
				
				if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
					int reforgeHammerCount = GameState.game.getPlayer().getItemCount(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER));
					
					if(reforgeHammerCount<stackCount) {
						return new Response("Reforge all",
								"You do not have enough reforging hammers to reforge all of the " + weapon.getNamePlural() + "! You have "+reforgeHammerCount+" reforging hammers, but there are "+stackCount+" "+weapon.getNamePlural()+" in total...",
								null); 
					}
				}
				
				return new Response("Reforge all",
						"Reforge all " + weapon.getNamePlural() + " which are in this weapon stack ("+stackCount+" in total) into the damage type you have chosen."
								+ (GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
										?" This action is permanent, but thanks to your proficiency with [style.boldEarth(Earth spells)], you can reforge them at any time."
										:" This action is permanent, and you'll need another reforging hammer if you want to change their damage type again."),
						INVENTORY_MENU) {
					@Override
					public void effects(){
						if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
							GameState.game.getPlayer().removeItem(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER), finalCount);
							GameState.game.getTextEndStringBuilder().append(
									"<p style='text-align:center;'>"
										+ getReforgeHammerEffects(weapon, damageTypePreview)
									+ "</p>"
									+ "<p>"
									+ "<b>The "+weapon.getName()+(weapon.getWeaponType().isPlural()?"have":"has")+" been reforged</b>!"
									+ "</p>");
							
							GameState.game.getTextEndStringBuilder().append("<p>You then repeat this for the other "+Util.intToString(finalCount-1)+" "+weapon.getNamePlural()+"...</p>");
							
							GameState.game.getTextEndStringBuilder().append("<p>"
										+ (GameState.game.getPlayer().hasItemType(ItemType.REFORGE_HAMMER) || GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER))
														+ "</b> reforging hammer" + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER)) == 1 ? "" : "s") + " left!"
												:"You have <b>0</b> reforging hammers left!")
									+ "</p>");
							
						} else {
							GameState.game.getTextEndStringBuilder().append(
									"<p>"
											+ "Thanks to your proficiency with [style.boldEarth(Earth spells)], you are able to reforge "
												+ (finalCount==2
													?"both"
													:"all "+Util.intToString(finalCount))
												+" of the " + weapon.getNamePlural() + " without needing to use a single reforging hammer!"
										+ "</p>");
						}

						if(owner!=null) {
							for(AbstractWeapon w : weaponMatches) {
								int weaponCount = owner.getAllWeaponsInInventory().get(w);
								owner.removeWeapon(w, weaponCount);
								AbstractWeapon modifiedWeapon = GameState.game.getItemGen().generateWeapon(w);
								modifiedWeapon.setDamageType(damageTypePreview);
								// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
								owner.addWeapon(GameState.game.getItemGen().generateWeapon(modifiedWeapon), weaponCount, false, false);
								GameState.game.addEvent(new EventLogEntry("Reforged", modifiedWeapon.getDisplayName(true)), false);
							}

						} else {
							for(AbstractWeapon w : weaponMatches) {
								int weaponCount = GameState.game.getPlayerCell().getInventory().getAllWeaponsInInventory().get(w);
								GameState.game.getPlayerCell().getInventory().removeWeapon(w, weaponCount);
								AbstractWeapon modifiedWeapon = GameState.game.getItemGen().generateWeapon(w);
								modifiedWeapon.setDamageType(damageTypePreview);
								// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
								GameState.game.getPlayerCell().getInventory().addWeapon(GameState.game.getItemGen().generateWeapon(modifiedWeapon), weaponCount);
								GameState.game.addEvent(new EventLogEntry("Reforged", modifiedWeapon.getDisplayName(true)), false);
							}
						}
					}
				};

			} else if (index == 13) {
				if(damageTypePreview == weapon.getDamageType()) {
					return new Response("Dye & reforge all",
							"You need to choose a different damage type before being able to reforge the " + weapon.getName() + "!",
							null); 
				}
				
				if(dyePreviews.equals(weapon.getColours())) {
					return new Response("Dye & reforge all",
							"You need to choose different colours before being able to dye the " + weapon.getName() + "!",
							null); 
				}
				
				List<AbstractWeapon> weaponMatches = new ArrayList<>();
				int stackCount = 0;
				if(owner!=null) {
					for(Entry<AbstractWeapon, Integer> entry : owner.getAllWeaponsInInventory().entrySet()) {
						if(entry.getKey().getWeaponType().equals(weapon.getWeaponType())) {
							weaponMatches.add(entry.getKey());
							stackCount += entry.getValue();
						}
					}
				} else {
					for(Entry<AbstractWeapon, Integer> entry : GameState.game.getPlayerCell().getInventory().getAllWeaponsInInventory().entrySet()) {
						if(entry.getKey().getWeaponType().equals(weapon.getWeaponType())) {
							weaponMatches.add(entry.getKey());
							stackCount += entry.getValue();
						}
					}
				}
				
				int finalCount = stackCount;
				
				if(stackCount==1) {
					return new Response("Dye & reforge all",
							"You only have one "+weapon.getName()+", so you should dye & reforge it using the individual action...",
							null); 
				}
				
				if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
					int dyeBrushCount = GameState.game.getPlayer().getItemCount(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH));
					int reforgeHammerCount = GameState.game.getPlayer().getItemCount(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER));
					
					if(dyeBrushCount<stackCount || reforgeHammerCount<stackCount) {
						return new Response("Dye & reforge all",
								"You do not have enough dye brushes or reforge hammers to dye & reforge all "+stackCount+" "+weapon.getNamePlural()+" in this stack...",
								null); 
					}
				}
				
				return new Response("Dye & reforge all",
						"Dye & reforge all " + weapon.getNamePlural() + " which are in this weapon stack ("+stackCount+" in total) in the colours and damage type you have chosen."
								+ (GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
										?" This action is permanent, but thanks to your proficiency with [style.boldEarth(Earth spells)], you can do this at any time."
										:" This action is permanent, and you'll need another dye-brush and reforging hammer if you want to do this again."),
						INVENTORY_MENU) {
					@Override
					public void effects(){
						if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
							GameState.game.getPlayer().removeItem(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH), finalCount);
							GameState.game.getTextEndStringBuilder().append(
									"<p style='text-align:center;'>"
										+ getDyeBrushEffects(weapon, dyePreviews.get(0))
									+ "</p>"
									+ "<p style='text-align:center;'>"
										+ getReforgeHammerEffects(weapon, damageTypePreview)
									+ "</p>"
									+ "<p>"
									+ "<b>The "+weapon.getName()+(weapon.getWeaponType().isPlural()?"have":"has")+" been dyed and reforged</b>!"
									+ "</p>");
							
							GameState.game.getTextEndStringBuilder().append("<p>You then repeat this for the other "+Util.intToString(finalCount-1)+" "+weapon.getNamePlural()+"...</p>");
							
							GameState.game.getTextEndStringBuilder().append("<p>"
										+ (GameState.game.getPlayer().hasItemType(ItemType.DYE_BRUSH)
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH))
														+ "</b> dye-brush" + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH)) == 1 ? "" : "es") + " left!"
												:"You have <b>0</b> dye-brushes left!")
									+ "</p>");

							GameState.game.getTextEndStringBuilder().append("<p>"
										+ (GameState.game.getPlayer().hasItemType(ItemType.REFORGE_HAMMER)
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER))
														+ "</b> reforging hammer" + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER)) == 1 ? "" : "s") + " left!"
												:"You have <b>0</b> reforging hammers left!")
									+ "</p>");
							
						} else {
							GameState.game.getTextEndStringBuilder().append(
									"<p>"
											+ "Thanks to your proficiency with [style.boldEarth(Earth spells)], you are able to dye and reforge "
												+ (finalCount==2
													?"both"
													:"all "+Util.intToString(finalCount))
												+" of the " + weapon.getNamePlural() + " without needing to use a single dye-brush or reforging hammer!"
										+ "</p>");
						}
						
						if(owner!=null) {
							for(AbstractWeapon w : weaponMatches) {
								int weaponCount = owner.getAllWeaponsInInventory().get(w);
								owner.removeWeapon(w, weaponCount);
								AbstractWeapon modifiedWeapon = GameState.game.getItemGen().generateWeapon(w);
								modifiedWeapon.setDamageType(damageTypePreview);
								modifiedWeapon.setColours(dyePreviews);
								// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
								owner.addWeapon(GameState.game.getItemGen().generateWeapon(modifiedWeapon), weaponCount, false, false);
								GameState.game.addEvent(new EventLogEntry("Dyed & Reforged", modifiedWeapon.getDisplayName(true)), false);
							}
							
						} else {
							for(AbstractWeapon w : weaponMatches) {
								int weaponCount = GameState.game.getPlayerCell().getInventory().getAllWeaponsInInventory().get(w);
								GameState.game.getPlayerCell().getInventory().removeWeapon(w, weaponCount);
								AbstractWeapon modifiedWeapon = GameState.game.getItemGen().generateWeapon(w);
								modifiedWeapon.setDamageType(damageTypePreview);
								modifiedWeapon.setColours(dyePreviews);
								// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
								GameState.game.getPlayerCell().getInventory().addWeapon(GameState.game.getItemGen().generateWeapon(modifiedWeapon), weaponCount);
								GameState.game.addEvent(new EventLogEntry("Dyed & Reforged", modifiedWeapon.getDisplayName(true)), false);
							}
						}
					}
				};

			} else {
				return null;
			}
		}

		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.INVENTORY;
		}
	};

	public static final DialogueNode DYE_EQUIPPED_WEAPON = new DialogueNode("Dye Weapon", "", true) {

		@Override
		public String getContent() {
			return getWeaponDyeUI();
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Return to the previous menu.", INVENTORY_MENU);

			} else if (index == 1) {
				if (!GameState.game.getPlayer().hasItemType(ItemType.DYE_BRUSH)
						&& !GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
						&& !GameState.game.isDebugMode()) {
					return new Response("Dye",
							"You do not have a dye-brush, so cannot change the colours of the " + weapon.getName() + "...",
							null); 
				}
				
				if(dyePreviews.equals(weapon.getColours())) {
					return new Response("Dye",
							"You need to choose different colours before being able to dye the " + weapon.getName() + "!",
							null); 
				}
				
				return new Response("Dye",
						"Dye the " + weapon.getName() + " in the colours you have chosen."
								+ (GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
										?" This action is permanent, but thanks to your proficiency with [style.boldEarth(Earth spells)], you can dye it a different colour at any time."
										:" This action is permanent, and you'll need another dye-brush if you want to change its colour again."),
						INVENTORY_MENU){
					@Override
					public void effects(){
						if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
							GameState.game.getPlayer().useItem(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH), owner, false);
							GameState.game.getTextEndStringBuilder().append(
									"<p style='text-align:center;'>"
										+ getDyeBrushEffects(weapon, dyePreviews.get(0))
									+ "</p>"
									+ "<p>"
										+ "<b>The " + weapon.getName() + " " + (weapon.getWeaponType().isPlural() ? "have been" : "has been") + " dyed</b>!"
									+ "</p>"
									+ "<p>"
										+ (isClothingDyeActionAvailable()
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH))
														+ "</b> dye-brush" + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH)) == 1 ? "" : "es") + " left!"
												:"You have <b>0</b> dye-brushes left!")
									+ "</p>");
							
						} else {
							GameState.game.getTextEndStringBuilder().append(
									"<p>"
											+ "Thanks to your proficiency with [style.boldEarth(Earth spells)], you are able to dye the " + weapon.getName() + " without needing to use a dye-brush!"
										+ "</p>");
						}
						
						

						owner.unequipWeaponIntoVoid(weaponSlot, weapon, true);
						AbstractWeapon modifiedWeapon = GameState.game.getItemGen().generateWeapon(weapon);
						modifiedWeapon.setColours(dyePreviews);
						
						if(weaponSlot==InventorySlot.WEAPON_MAIN_1
								|| weaponSlot==InventorySlot.WEAPON_MAIN_2
								|| weaponSlot==InventorySlot.WEAPON_MAIN_3) {
							// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
							owner.equipMainWeaponFromNowhere(GameState.game.getItemGen().generateWeapon(modifiedWeapon));
							
						} else {
							// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
							owner.equipOffhandWeaponFromNowhere(GameState.game.getItemGen().generateWeapon(modifiedWeapon));
						}

//						weapon.setPrimaryColour(dyePreviewPrimary);
//						weapon.setSecondaryColour(dyePreviewSecondary);
						GameState.game.addEvent(new EventLogEntry("Dyed", weapon.getDisplayName(true)), false);
					}
				};

			} else if (index == 2) {
				if (!GameState.game.getPlayer().hasItemType(ItemType.REFORGE_HAMMER)
						&& !GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
						&& !GameState.game.isDebugMode()) {
					return new Response("Reforge",
							"You do not have a reforging hammer, so cannot change the damage type of the " + weapon.getName() + "...",
							null); 
				}
				
				if(damageTypePreview == weapon.getDamageType()) {
					return new Response("Reforge",
							"You need to choose a different damage type before being able to reforge the " + weapon.getName() + "!",
							null); 
				}
				
				return new Response("Reforge",
						"Reforge the " + weapon.getName() + " into the damage type you have chosen."
								+ (GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
										?" This action is permanent, but thanks to your proficiency with [style.boldEarth(Earth spells)], you can reforge it at any time."
										:" This action is permanent, and you'll need another reforging hammer if you want to change its damage type again."),
						INVENTORY_MENU){
					@Override
					public void effects(){
						if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
							GameState.game.getPlayer().useItem(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER), owner, false);
							GameState.game.getTextEndStringBuilder().append(
									"<p style='text-align:center;'>"
										+ getReforgeHammerEffects(weapon, damageTypePreview)
									+ "</p>"
									+ "<p>"
										+ "<b>The " + weapon.getName() + " " + (weapon.getWeaponType().isPlural() ? "have been" : "has been") + " reforged</b>!"
									+ "</p>"
									+ "<p>"
										+ (GameState.game.getPlayer().hasItemType(ItemType.REFORGE_HAMMER) || GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER))
														+ "</b> reforging " + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER)) == 1 ? "hammer" : "hammers") + " left!"
												:"You have <b>0</b> reforging hammers left!")
									+ "</p>");
							
						} else {
							GameState.game.getTextEndStringBuilder().append(
									"<p>"
											+ "Thanks to your proficiency with [style.boldEarth(Earth spells)], you are able to reforge the " + weapon.getName() + " without needing to use a reforging hammer!"
										+ "</p>");
						}

						owner.unequipWeaponIntoVoid(weaponSlot, weapon, true);
						AbstractWeapon modifiedWeapon = GameState.game.getItemGen().generateWeapon(weapon);
						modifiedWeapon.setDamageType(damageTypePreview);
						
						if(weaponSlot==InventorySlot.WEAPON_MAIN_1
								|| weaponSlot==InventorySlot.WEAPON_MAIN_2
								|| weaponSlot==InventorySlot.WEAPON_MAIN_3) {
							// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
							owner.equipMainWeaponFromNowhere(GameState.game.getItemGen().generateWeapon(modifiedWeapon));
							
						} else {
							// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
							owner.equipOffhandWeaponFromNowhere(GameState.game.getItemGen().generateWeapon(modifiedWeapon));
						}
						
						GameState.game.addEvent(new EventLogEntry("Reforged", weapon.getDisplayName(true)), false);
					}
				};

			} else if (index == 3) {
				if ((!GameState.game.getPlayer().hasItemType(ItemType.REFORGE_HAMMER) || !GameState.game.getPlayer().hasItemType(ItemType.DYE_BRUSH))
						&& !GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
						&& !GameState.game.isDebugMode()) {
					return new Response("Dye & reforge",
							"You do not have both a dye brush and a reforging hammer, so cannot dye and reforge the " + weapon.getName() + "...",
							null); 
				}
				
				if(damageTypePreview == weapon.getDamageType()) {
					return new Response("Dye & reforge",
							"You need to choose a different damage type before being able to reforge the " + weapon.getName() + "!",
							null); 
				}
				
				if(dyePreviews.equals(weapon.getColours())) {
					return new Response("Dye & reforge",
							"You need to choose different colours before being able to dye the " + weapon.getName() + "!",
							null); 
				}
				
				return new Response("Dye & reforge",
						"Dye and reforge the " + weapon.getName() + " into the colours and damage type you have chosen."
								+ (GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
										?" This action is permanent, but thanks to your proficiency with [style.boldEarth(Earth spells)], you can dye and reforge it at any time."
										:" This action is permanent, and you'll need another dye-brush and another reforging hammer if you want to change its colours and damage type again."),
						INVENTORY_MENU){
					@Override
					public void effects(){
						if(!GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH) && !GameState.game.isDebugMode()) {
							GameState.game.getPlayer().useItem(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH), owner, false);
							GameState.game.getPlayer().useItem(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER), owner, false);
							GameState.game.getTextEndStringBuilder().append(
									"<p style='text-align:center;'>"
										+ getDyeBrushEffects(weapon, dyePreviews.get(0))
									+ "</p>"
									+ "<p style='text-align:center;'>"
										+ getReforgeHammerEffects(weapon, damageTypePreview)
									+ "</p>"
									+ "<p>"
										+ "<b>The " + weapon.getName() + " " + (weapon.getWeaponType().isPlural() ? "have been" : "has been") + " reforged</b>!"
									+ "</p>"
									+ "<p>"
										+ (isClothingDyeActionAvailable()
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH))
														+ "</b> dye-brush" + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.DYE_BRUSH)) == 1 ? "" : "es") + " left!"
												:"You have <b>0</b> dye-brushes left!")
										+"<br/>"
										+ (GameState.game.getPlayer().hasItemType(ItemType.REFORGE_HAMMER) || GameState.game.getPlayer().isSpellSchoolSpecialAbilityUnlocked(SpellSchool.EARTH)
												?"You have <b>" + GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER))
														+ "</b> reforging " + (GameState.game.getPlayer().getAllItemsInInventory().get(GameState.game.getItemGen().generateItem(ItemType.REFORGE_HAMMER)) == 1 ? "hammer" : "hammers") + " left!"
												:"You have <b>0</b> reforging hammers left!")
									+ "</p>");
							
						} else {
							GameState.game.getTextEndStringBuilder().append(
									"<p>"
											+ "Thanks to your proficiency with [style.boldEarth(Earth spells)], you are able to dye and reforge the " + weapon.getName() + " without needing to use a dye-brush or reforging hammer!"
										+ "</p>");
						}

						owner.unequipWeaponIntoVoid(weaponSlot, weapon, true);
						AbstractWeapon modifiedWeapon = GameState.game.getItemGen().generateWeapon(weapon);
						modifiedWeapon.setColours(dyePreviews);
						modifiedWeapon.setDamageType(damageTypePreview);
						
						if(weaponSlot==InventorySlot.WEAPON_MAIN_1
								|| weaponSlot==InventorySlot.WEAPON_MAIN_2
								|| weaponSlot==InventorySlot.WEAPON_MAIN_3) {
							// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
							owner.equipMainWeaponFromNowhere(GameState.game.getItemGen().generateWeapon(modifiedWeapon));
							
						} else {
							// For some reason, if you add the modifiedWeapon directly, it won't stack with other identical weapons... Have to generateWeapon(modifiedWeapon) again to get it to start stacking properly:
							owner.equipOffhandWeaponFromNowhere(GameState.game.getItemGen().generateWeapon(modifiedWeapon));
						}
						
//						weapon.setDamageType(damageTypePreview);
//						weapon.setPrimaryColour(dyePreviewPrimary);
//						weapon.setSecondaryColour(dyePreviewSecondary);
						GameState.game.addEvent(new EventLogEntry("Dyed & Reforged", weapon.getDisplayName(true)), false);
					}
				};

			} else {
				return null;
			}
		}

		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.INVENTORY;
		}
	};
	
	
	
	// Utility methods:
	
	private static String getDyeBrushEffects(AbstractClothing clothing, Colour colour) {
		return "<p>"
					+ "As you take hold of the Dye-brush, you see the purple glow around the tip growing in strength."
					+ " The closer you move it to your " + clothing.getName() + ", the brighter the glow becomes, until suddenly, images of different colours start flashing through your mind."
					+ " As you touch the bristles to the " + clothing.getName() + "'s surface, the Dye-brush instantly evaporates!"
					+ " You see that the arcane enchantment has dyed the " + clothing.getName() + " " + colour.getName() + "."
				+ "</p>";
	}
	
	private static String getDyeBrushEffects(AbstractWeapon weapon, Colour colour) {
		return "<p>"
					+ "As you take hold of the Dye-brush, you see the purple glow around the tip growing in strength."
					+ " The closer you move it to your " + weapon.getName() + ", the brighter the glow becomes, until suddenly, images of different colours start flashing through your mind."
					+ " As you touch the bristles to the " + weapon.getName() + "'s surface, the Dye-brush instantly evaporates!"
					+ " You see that the arcane enchantment has dyed the " + weapon.getName() + " " + colour.getName() + "."
				+ "</p>";
	}
	
	private static String getReforgeHammerEffects(AbstractWeapon weapon, DamageType damageType) {
		return "<p>"
					+ "As you take hold of the reforging hammer, you see the metal head start to emit a deep purple glow."
					+ " The closer you move it to your " + weapon.getName() + ", the brighter this glow becomes, until suddenly, images of different damage types start flashing through your mind."
					+ " As you touch the metal head  to the " + weapon.getName() + ", the reforge hammer instantly evaporates!"
					+ " You see that the arcane enchantment has reforged the " + weapon.getName() + " so that it now deals " + damageType.getName() + " damage."
				+ "</p>";
	}
	
	private static String getItemDisplayPanel(AbstractCoreItem item, String SVGString, String title, String description) {
		return "<div class='inventoryImage'>" // style='width: calc(50% - 4px);'
					+ "<div class='inventoryImage-content'>"
						+ SVGString
					+ "</div>"
				+ "</div>"
				+ "<h5 style='margin-bottom:0; padding-bottom:0;'><b>"+title+"</b></h5>"
				+ "<p style='margin-top:0; padding-top:0;'>"
					+ UtilText.parse(item, description)
				+ "</p>";
	}
	
	private static String getGeneralResponseTabTitle(int index) {
		if(index==0) {
			return "Overview";
		} else if(index==1) {
			return "Selected item";
		} else {
			return null;
		}
	}
	
	private static Response getCloseInventoryResponse() {
		if(interactionType == InventoryInteraction.CHARACTER_CREATION) {
			return new Response("Back", "Return to looking in the mirror at your appearance.", CharacterCreation.CHOOSE_ADVANCED_APPEARANCE){
				@Override
				public int getSecondsPassed() {
					return -CharacterCreation.TIME_TO_CLOTHING;
				}
				@Override
				public void effects(){
					item = null;
					clothing = null;
					weapon = null;
				}
			};
			
		} else {
			return new ResponseEffectsOnly("Close inventory", "Close the Inventory menu."){
				@Override
				public void effects(){
					item = null;
					clothing = null;
					weapon = null;
					GameState.getRenderSink().openInventory();
				}
			};
		}
	}
	
	private static Response getBuybackResponse() {
		if (buyback) {
			return new Response("Normal trade", "Switch back to the normal trade menu.", INVENTORY_MENU){
				@Override
				public void effects(){
					buyback = !buyback;
				}
			};
		} else {
			if(GameState.game.getPlayer().getBuybackStack()==null || GameState.game.getPlayer().getBuybackStack().isEmpty()) {
				return new Response("Buyback", "You have not sold any items, so cannot buy anything back...", null);
			} else {
				return new Response("Buyback", "Switch to viewing the buyback menu.", INVENTORY_MENU){
					@Override
					public void effects(){
						buyback = !buyback;
					}
				};
			}
		}
	}
	
	private static Response getQuickTradeResponse() {
		//TODO move this into options
		// Also, instead of being a response, this needs to be a button within the inventory UI
		return null;
		
//		if (GameState.game.getDialogueFlags().quickTrade) {
//			return new Response("Quick-Manage: <b style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>",
//					"Quick-Manage is turned <b style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>!<br/>"
//							+ "That means you can buy and sell items with a single click when trading, and pick-up and drop items with a single click when in normal inventory mode.", INVENTORY_MENU){
//				
//				@Override
//				public DialogueNodeOld getNextDialogue() {
//					return GameState.game.getCurrentDialogueNode();
//				}
//				
//				@Override
//				public void effects(){
//					GameState.game.getDialogueFlags().quickTrade = !GameState.game.getDialogueFlags().quickTrade;
//				}
//			};
//			
//		} else {
//			return new Response("Quick-Manage: <b style='color:" + PresetColour.TEXT_GREY.toWebHexString() + ";'>OFF</b>",
//					"Quick-Manage is turned <b style='color:" + PresetColour.TEXT_GREY.toWebHexString() + ";'>OFF</b>.<br/>"
//							+ "That means when you click on an item, you get a detailed view of the item before deciding whether to buy/sell or pick-up/drop it.", INVENTORY_MENU){
//
//				@Override
//				public DialogueNodeOld getNextDialogue() {
//					return GameState.game.getCurrentDialogueNode();
//				}
//				
//				@Override
//				public void effects(){
//					GameState.game.getDialogueFlags().quickTrade = !GameState.game.getDialogueFlags().quickTrade;
//				}
//			};
//		}
	}
	
	private static Response getJinxRemovalResponse(boolean selfUnseal) {
		boolean ownsKey = GameState.game.getPlayer().getUnlockKeyMap().containsKey(owner.getId()) && GameState.game.getPlayer().getUnlockKeyMap().get(owner.getId()).contains(clothing.getSlotEquippedTo());
		int removalCost = clothing.getJinxRemovalCost(GameState.game.getPlayer(), selfUnseal);
		
		if(clothing.getEffects().stream().filter(ie->ie.getSecondaryModifier()==TFModifier.CLOTHING_SEALING).findFirst().get().getPotency()==TFPotency.SPECIAL) {
			return new Response("Unseal ([style.colourTerrible(Impossible)])",
					"The "+clothing.getName()+" has a special sealing enchantment and cannot be unsealed via regular means!",
					null);
		}
		
		if(interactionType==InventoryInteraction.COMBAT) {
			return new Response("Unseal"+(ownsKey?"(Use key)":"(<i>"+removalCost+" Essences</i>)"),
					"You can't unseal clothing in combat!",
					null);
		}

		if(interactionType==InventoryInteraction.SEX) {
			if(GameState.sex.isCharacterImmobilised(GameState.game.getPlayer())) {
				return new Response("Unseal"+(ownsKey?"(Use key)":"(<i>"+removalCost+" Essences</i>)"),
						UtilText.parse(owner, "You can't unseal [npc.namePos] clothing as you are currently [style.colourTerrible(immobilised)]!"),
						null);
			}
			if(!selfUnseal && GameState.sex.getInitialSexManager().isHidden(GameState.game.getPlayer())) {
				return new Response("Unseal"+(ownsKey?"(Use key)":"(<i>"+removalCost+" Essences</i>)"),
						UtilText.parse(owner, "As you're hiding, you can't unseal [npc.namePos] clothing!"),
						null);
			}
			if(!GameState.sex.getInitialSexManager().isAbleToRemoveClothingSeals(GameState.game.getPlayer())) {
				return new Response("Unseal"+(ownsKey?"(Use key)":"(<i>"+removalCost+" Essences</i>)"),
						"You can't unseal clothing in this sex scene!",
						null);
			}
		}
		
		if(!ownsKey) {
			if(!GameState.game.getPlayer().isQuestCompleted(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)) {
				return new Response("Unseal", "You don't know how to unseal clothing! Perhaps you should pay Lilaya a visit and ask her about it...", null);
			}
			if(GameState.game.getPlayer().getClothingCurrentlyEquipped().stream().anyMatch(c -> c.isSelfTransformationInhibiting())) {
				return new Response("Unseal",
						"Although you are normally able to unseal clothing, you cannot do so due to an enchantment on one or more pieces of your equipped clothing!"
						+ "<br/>[style.italicsArcane(Visit Lilaya to get your sealed clothing removed!)]",
						null);
			}
			if(GameState.game.getPlayer().getTattoos().values().stream().anyMatch(c -> c.isSelfTransformationInhibiting())) {
				return new Response("Unseal",
						"Although you are normally able to unseal clothing, you cannot do so due to an enchantment on one or more of your tattoos!"
								+ "<br/>[style.italicsArcane(Visit Kate to get the tattoo removed!)]",
						null);
			}
		}
		
		if(ownsKey || GameState.game.getPlayer().getEssenceCount()>=removalCost) {
			return new Response("Unseal "+(ownsKey?"([style.italicsGood(Use key)])":"([style.italicsArcane("+removalCost+" Essences)])"),
						ownsKey
							?"As you own the key which unlocks this piece of clothing, you can remove it without having to spend any arcane essences!"
							:("Spend "+removalCost+" arcane essences on unsealing this piece of clothing."
								+ (GameState.game.getPlayer().hasFetish(Fetish.FETISH_BONDAGE_VICTIM) && selfUnseal
									?"<br/>[style.italicsMinorBad(You have to pay 5 times the standard unseal cost due to your '"+Fetish.FETISH_BONDAGE_VICTIM.getName(GameState.game.getPlayer())+"' fetish!)]"
									:"")),
						interactionType==InventoryInteraction.SEX
							?GameState.sex.SEX_DIALOGUE
							:INVENTORY_MENU) {
				@Override
				public void effects() {
					String s = "";
					if(ownsKey) {
						if(!GameState.game.isInSex()) {
							GameState.game.getPlayer().removeFromUnlockKeyMap(owner.getId(), clothing.getSlotEquippedTo());
						}
						s = "<p>"
								+ "Using the key which is in your possession, you unlock the "+clothing.getName()+"!"
							+ "</p>";
						
					} else {
						GameState.game.getPlayer().incrementEssenceCount(-removalCost, false);
						s = UtilText.parse(owner,
								"<p>"
									+ "You channel the power of your arcane essences into [npc.namePos] "+clothing.getName()+", and with a bright purple flash, you manage to remove the seal!"
								+ "</p>"
								+ "<p style='text-align:center;'>"
									+ "Removing the seal has cost you [style.boldBad("+removalCost+")] [style.boldArcane(Arcane Essences)]!"
								+ "</p>");
					}
					
					// Have to remove and then re-add the clothing as setting the sealed status affects the clothing's hashCode
					List<DisplacementType> clothingDisplacementTypes = new ArrayList<>();
					if(GameState.game.isInSex() && GameState.sex.getAllParticipants().contains(owner)) {
						if(GameState.sex.getClothingPreSexMap().get(owner).get(clothing.getSlotEquippedTo()).get(clothing)!=null) {
							clothingDisplacementTypes.addAll(GameState.sex.getClothingPreSexMap().get(owner).get(clothing.getSlotEquippedTo()).get(clothing));
						}
						GameState.sex.getClothingPreSexMap().get(owner).get(clothing.getSlotEquippedTo()).remove(clothing);
					}
					clothing.setSealed(false);
					if(GameState.game.isInSex() && GameState.sex.getAllParticipants().contains(owner)) {
						GameState.sex.getClothingPreSexMap().get(owner).get(clothing.getSlotEquippedTo()).put(clothing, clothingDisplacementTypes);
					}
					
					if(interactionType==InventoryInteraction.SEX) {
						GameState.sex.setUnequipClothingText(clothing, s);
						GameState.getRenderSink().openInventory();
						GameState.sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
						GameState.sex.setSexStarted(true);
						
					} else {
						GameState.game.getTextEndStringBuilder().append(s);
					}
				}
			};
			
		} else {
			return new Response("Unseal (<i>"+removalCost+" Essences</i>)",
					"You need at least "+removalCost+" arcane essences in order to unseal this piece of clothing!"
							+ (GameState.game.getPlayer().hasFetish(Fetish.FETISH_BONDAGE_VICTIM)
									?"<br/>[style.italicsMinorBad(This cost is)] [style.italicsBad(5 times)] [style.italicsMinorBad(more than normal due to your '"+Fetish.FETISH_BONDAGE_VICTIM.getName(GameState.game.getPlayer())+"' fetish!)]"
									:""),
					null);
		}
	}
	
	
	
	// Items:
	
	private static void transferItems(GameCharacter from, GameCharacter to, AbstractItem item, int count) {
		if (!to.isInventoryFull() || to.hasItem(item) || item.getRarity()==Rarity.QUEST) {
			from.removeItem(item, count);
			to.addItem(item, count, false, to.isPlayer());
		}
		resetPostAction();
	}
	
	private static void dropItems(GameCharacter from, AbstractItem item, int count) {
		if (!GameState.game.getPlayerCell().getInventory().isInventoryFull() || GameState.game.getPlayerCell().getInventory().hasItem(item)) {
			from.dropItem(item, count, from.isPlayer());
		}
		resetPostAction();
	}
	
	private static void pickUpItems(GameCharacter to, AbstractItem item, int count) {
		if (!to.isInventoryFull() || to.hasItem(item) || item.getRarity()==Rarity.QUEST) {
			to.addItem(item, count, true, to.isPlayer());
		}
		resetPostAction();
	}
	
	private static void sellItems(GameCharacter from, GameCharacter to, AbstractItem item, int count, int itemPrice) {
		GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.removeTraderDescription, false);
		
		if (!to.isPlayer() || !to.isInventoryFull() || to.hasItem(item) || item.getRarity()==Rarity.QUEST) {
			from.incrementMoney(itemPrice*count);
			to.incrementMoney(-itemPrice*count);
			
			if(buyback && to.isPlayer()) {
				GameState.game.getPlayer().addItem(item, count, false, true);
				GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).incrementCount(-count);
				if(GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getCount()<=0) {	
					GameState.game.getPlayer().getBuybackStack().remove(buyBackIndex);
				}
				
			} else {
				if(from.isPlayer()) {
					GameState.game.getPlayer().getBuybackStack().push(new ShopTransaction(item, itemPrice, count));
				} else {
					to.addItem(item, count, false, true);
				}
				from.removeItem(item, count);
			}
			
			if(from.isPlayer()) {
				GameState.game.addEvent(new EventLogEntry("Sold",
								count+"x <span style='color:"+item.getRarity().getColour().toWebHexString()+";'>"+(count==1?item.getName():item.getNamePlural())+"</span> for "+UtilText.formatAsMoney(itemPrice*count)),
						false);
			}
			
			if(to.isPlayer()) {
				//((NPC) from).handleSellingEffects(item, count, itemPrice); // Delete: This was replaced by applyItemTransactionEffects
				GameState.game.addEvent(new EventLogEntry("Bought",
								count+"x <span style='color:"+item.getRarity().getColour().toWebHexString()+";'>"+(count==1?item.getName():item.getNamePlural())+"</span> for "+UtilText.formatAsMoney(itemPrice*count)),
						false);
			}
			
			if(!from.isPlayer()) {
				((NPC)from).applyItemTransactionEffects(item, count, itemPrice, true);
				
			} else {
				((NPC)to).applyItemTransactionEffects(item, count, itemPrice, false);
			}
		}
		
		resetPostAction();
	}
	
	
	// Weapons:
	
	private static void transferWeapons(GameCharacter from, GameCharacter to, AbstractWeapon weapon, int count) {
		if (!to.isInventoryFull() || to.hasWeapon(weapon) || weapon.getRarity()==Rarity.QUEST) {
			from.removeWeapon(weapon, count);
			to.addWeapon(weapon, count, false, to.isPlayer());
		}
		resetPostAction();
	}
	
	private static void dropWeapons(GameCharacter from, AbstractWeapon weapon, int count) {
		if (!GameState.game.getPlayerCell().getInventory().isInventoryFull() || GameState.game.getPlayerCell().getInventory().hasWeapon(weapon)) {
			from.dropWeapon(weapon, count, from.isPlayer());
		}
		resetPostAction();
	}
	
	private static void pickUpWeapons(GameCharacter to, AbstractWeapon weapon, int count) {
		if (!to.isInventoryFull() || to.hasWeapon(weapon) || weapon.getRarity()==Rarity.QUEST) {
			to.addWeapon(weapon, count, true, to.isPlayer());
		}
		resetPostAction();
	}
	
	private static void sellWeapons(GameCharacter from, GameCharacter to, AbstractWeapon weapon, int count, int itemPrice) {
		GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.removeTraderDescription, false);
		if (!to.isPlayer() || !to.isInventoryFull() || to.hasWeapon(weapon) || weapon.getRarity()==Rarity.QUEST) {

			from.incrementMoney(itemPrice*count);
			to.incrementMoney(-itemPrice*count);
			
			if(buyback && to.isPlayer()) {
				GameState.game.getPlayer().addWeapon(GameState.game.getItemGen().generateWeapon(weapon), count, false, true);
				GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).incrementCount(-count);
				if(GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getCount()<=0) {	
					GameState.game.getPlayer().getBuybackStack().remove(buyBackIndex);
				}
				
			} else {
				if(from.isPlayer()) {
					GameState.game.getPlayer().getBuybackStack().push(new ShopTransaction(weapon, itemPrice, count));
				} else {
					to.addWeapon(GameState.game.getItemGen().generateWeapon(weapon), count, false, true);
				}
				from.removeWeapon(weapon, count);
			}
			
			if(from.isPlayer()) {
				GameState.game.addEvent(new EventLogEntry("Sold",
								count+"x <span style='color:"+weapon.getRarity().getColour().toWebHexString()+";'>"+(count==1?weapon.getName():weapon.getNamePlural())+"</span> for "+UtilText.formatAsMoney(itemPrice*count)),
						false);
			}
			
			if(to.isPlayer()) {
				//((NPC) from).handleSellingEffects(weapon, count, itemPrice); // Delete: This was replaced by applyItemTransactionEffects
				GameState.game.addEvent(new EventLogEntry("Bought",
								count+"x <span style='color:"+weapon.getRarity().getColour().toWebHexString()+";'>"+(count==1?weapon.getName():weapon.getNamePlural())+"</span> for "+UtilText.formatAsMoney(itemPrice*count)),
						false);
			}
			
			if(!from.isPlayer()) {
				((NPC)from).applyItemTransactionEffects(weapon, count, itemPrice, true);
				
			} else {
				((NPC)to).applyItemTransactionEffects(weapon, count, itemPrice, false);
			}
		}
		
		
		resetPostAction();
	}
	
	
	// Clothing:
	
	private static void transferClothing(GameCharacter from, GameCharacter to, AbstractClothing clothing, int count) {
		if (!to.isInventoryFull() || to.hasClothing(clothing) || clothing.getRarity()==Rarity.QUEST) {
			from.removeClothing(clothing, count);
			to.addClothing(clothing, count, false, to.isPlayer());
			owner = to;
		}
		resetPostAction();
	}
	
	
	private static void dropClothing(GameCharacter from, AbstractClothing clothing, int count) {
		if (!GameState.game.getPlayerCell().getInventory().isInventoryFull() || GameState.game.getPlayerCell().getInventory().hasClothing(clothing)) {
			from.dropClothing(clothing, count, from.isPlayer());
			
			if(from.getClothingCount(clothing) == 0) {
				owner = null;
			}
		}
		resetPostAction();
	}
	
	private static void pickUpClothing(GameCharacter to, AbstractClothing clothing, int count) {
		if (!to.isInventoryFull() || to.hasClothing(clothing) || clothing.getRarity()==Rarity.QUEST) {
			to.addClothing(clothing, count, true, to.isPlayer());
			
			owner = to;
		}
		resetPostAction();
	}
	
	private static void sellClothing(GameCharacter from, GameCharacter to, AbstractClothing clothing, int count, int itemPrice) {
		GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.removeTraderDescription, false);
		if (!to.isPlayer() || !to.isInventoryFull() || to.hasClothing(clothing) || clothing.getRarity()==Rarity.QUEST) {
			from.incrementMoney(itemPrice*count);
			to.incrementMoney(-itemPrice*count);
			
			if(buyback && to.isPlayer()) {
				GameState.game.getPlayer().addClothing(new AbstractClothing(clothing) {}, count, false, true);
				GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).incrementCount(-count);
				if(GameState.game.getPlayer().getBuybackStack().get(buyBackIndex).getCount()<=0) {	
					GameState.game.getPlayer().getBuybackStack().remove(buyBackIndex);
				}
				
			} else {
				if(from.isPlayer()) {
					GameState.game.getPlayer().getBuybackStack().push(new ShopTransaction(clothing, itemPrice, count));
				} else {
					to.addClothing(new AbstractClothing(clothing) {}, count, false, true);
				}
				from.removeClothing(clothing, count);
			}
			
			if(from.isPlayer()) {
				GameState.game.addEvent(new EventLogEntry("Sold",
								count+"x <span style='color:"+clothing.getRarity().getColour().toWebHexString()+";'>"+(count==1?clothing.getName():clothing.getNamePlural())+"</span> for "+UtilText.formatAsMoney(itemPrice*count)),
						false);
			}
			
			if(to.isPlayer()) {
				//((NPC) from).handleSellingEffects(clothing, count, itemPrice); // Delete: This was replaced by applyItemTransactionEffects
				GameState.game.addEvent(new EventLogEntry("Bought",
								count+"x <span style='color:"+clothing.getRarity().getColour().toWebHexString()+";'>"+(count==1?clothing.getName():clothing.getNamePlural())+"</span> for "+UtilText.formatAsMoney(itemPrice*count)),
						false);
			}
			
			if(!from.isPlayer()) {
				((NPC)from).applyItemTransactionEffects(clothing, count, itemPrice, true);
				
			} else {
				((NPC)to).applyItemTransactionEffects(clothing, count, itemPrice, false);
			}
		}
		
		resetPostAction();
	}
	
	private static String unequipClothingToFloor(GameCharacter unequipper, AbstractClothing clothing) {
		String unequipDescription = "";
		if(clothing.isDiscardedOnUnequip(clothing.getSlotEquippedTo())) {
			unequipDescription = owner.unequipClothingIntoVoid(clothing, true, unequipper);
		} else {
			unequipDescription = owner.unequipClothingOntoFloor(clothing, true, unequipper);
		}
		owner = null;
		resetPostAction();
		
		return unequipDescription;
	}
	
	private static String unequipClothingToUnequippersInventory(GameCharacter unequipper, AbstractClothing clothing) {
		String unequipDescription = "";
		if(clothing.isDiscardedOnUnequip(clothing.getSlotEquippedTo())) {
			unequipDescription = owner.unequipClothingIntoVoid(clothing, true, unequipper);
		} else {
			unequipDescription = owner.unequipClothingIntoUnequippersInventory(clothing, true, unequipper);
		}
		resetPostAction();
		
		return unequipDescription;
	}
	
	private static String unequipClothingToInventory(GameCharacter unequipper, AbstractClothing clothing) {
		String unequipDescription = "";
		if(clothing.isDiscardedOnUnequip(clothing.getSlotEquippedTo())) {
			unequipDescription = owner.unequipClothingIntoVoid(clothing, true, unequipper);
		} else {
			unequipDescription = owner.unequipClothingIntoInventory(clothing, true, unequipper);
		}
		resetPostAction();
		
		return unequipDescription;
	}
	
	private static String equipClothingFromInventory(GameCharacter to, InventorySlot slot, GameCharacter equipper, AbstractClothing clothing) {
		String equipDescription = to.equipClothingFromInventory(clothing, slot, true, equipper, owner);
		owner = to;
		resetPostAction();
		return equipDescription;
	}
	
	private static String equipClothingFromGround(GameCharacter to, InventorySlot slot, GameCharacter equipper, AbstractClothing clothing) {
		owner = to;
		resetPostAction();
		return to.equipClothingFromGround(clothing, slot, true, equipper);
	}
	
	private static Response getCondomSabotageResponse(AbstractClothing clothing) {
		if(clothing.getCondomEffect().getPotency().isNegative()) {
			if(GameState.game.getPlayer().getEssenceCount() >= 1) {
				return new Response("Repair ([style.italicsArcane(1 Essence)])",
						"Spend 1 arcane essence to repair the condom.", CLOTHING_INVENTORY) {
					@Override
					public void effects() {
						GameState.game.getPlayer().incrementEssenceCount(-1, false);
						GameState.game.getTextEndStringBuilder().append(
								"<p>"
									+ "You channel the power of an arcane essence into the condom, and, after emitting a faint purple glow, it is repaired!"
								+ "</p>"
								+ "<p style='text-align:center;'>"
									+ "Repairing the condom has cost you [style.boldBad(1)] [style.boldArcane(Arcane Essence)]!"
								+ "</p>");
						AbstractClothing c = (AbstractClothing) EnchantmentDialogue.craftAndApplyFullInventoryEffects(clothing, clothing.getClothingType().getEffects());

						GameState.game.getPlayer().removeClothing(c);
						c.setName(c.getClothingType().getName());
						setClothing(c);
						GameState.game.getPlayer().addClothing(c, false);
						
						RenderingEngine.setPage(GameState.game.getPlayer(), c);
					}
				};
			} else {
				return new Response("Repair (<i>1 Essence</i>)", "You need at least 1 arcane essence in order to repair the condom!", null);
			}
			
		} else {
			return new Response("Sabotage", "By making a small tear in the end of this condom, you can ensure that it will break at the moment of orgasm!", CLOTHING_INVENTORY) {
				@Override
				public void effects(){
					EnchantmentDialogue.setOutputName(clothing.getClothingType().getName());
					AbstractClothing c = (AbstractClothing) EnchantmentDialogue.craftAndApplyFullInventoryEffects(clothing,
							Util.newArrayListOfValues(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.CLOTHING_CONDOM, TFModifier.ARCANE_BOOST, TFPotency.MAJOR_DRAIN, 0)),
							false);

					GameState.game.getPlayer().removeClothing(c);
					setClothing(c);
					GameState.game.getPlayer().addClothing(c, false);

					RenderingEngine.setPage(GameState.game.getPlayer(), c);
					GameState.game.getTextEndStringBuilder().append(
							"<p>"
								+ "By making a tiny, near-invisible tear in the end of the condom, you ensure that it will split when filled with cum..."
							+ "</p>"
							+ "<p style='text-align:center;'>"
								+ "[style.italicsBad(The condom is now guaranteed to break upon orgasm)]!"
							+ "</p>");
				}
			};
		}
	}
	
	private static void resetPostAction() {
		GameState.game.setResponseTab(0);
		resetItems();
	}
	
	public static void resetItems() {
		item = null;
		clothing = null;
		weapon = null;
	}

	public static AbstractItem getItem() {
		return item;
	}

	public static void setItem(AbstractItem item) {
		resetItems();
		InventoryDialogue.item = item;
		if(GameState.getProperties().addItemDiscovered(item.getItemType())) {
			GameState.game.addEvent(new EventLogEntryEncyclopediaUnlock(item.getItemType().getName(false), item.getRarity().getColour()), true);
		}
	}

	public static AbstractWeapon getWeapon() {
		return weapon;
	}

	public static void setWeapon(InventorySlot slot, AbstractWeapon weapon) {
		resetItems();
		InventoryDialogue.weaponSlot = slot;
		InventoryDialogue.weapon = weapon;
		if (GameState.getProperties().addWeaponDiscovered(weapon.getWeaponType())) {
			GameState.game.addEvent(new EventLogEntryEncyclopediaUnlock(weapon.getWeaponType().getName(), weapon.getWeaponType().getRarity().getColour()), true);
		}
	}

	public static AbstractClothing getClothing() {
		return clothing;
	}

	public static void setClothing(AbstractClothing clothing) {
		resetItems();
		InventoryDialogue.clothing = clothing;
		if(GameState.getProperties().addClothingDiscovered(clothing.getClothingType())) {
			GameState.game.addEvent(new EventLogEntryEncyclopediaUnlock(clothing.getClothingType().getName(), clothing.getClothingType().getRarity().getColour()), true);
		}
	}

	public static boolean isBuyback() {
		return buyback;
	}

	public static void setBuyback(boolean buyback) {
		InventoryDialogue.buyback = buyback;
	}

	public static int getBuyBackPrice() {
		return buyBackPrice;
	}

	public static void setBuyBackPrice(int buyBackPrice) {
		InventoryDialogue.buyBackPrice = buyBackPrice;
	}

	public static int getBuyBackIndex() {
		return buyBackIndex;
	}

	public static void setBuyBackIndex(int buyBackIndex) {
		InventoryDialogue.buyBackIndex = buyBackIndex;
	}

	public static GameCharacter getOwner() {
		return owner;
	}

	public static void setOwner(GameCharacter owner) {
		InventoryDialogue.owner = owner;
	}

	public static NPC getInventoryNPC() {
		return inventoryNPC;
	}

	public static void setInventoryNPC(NPC inventoryNPC) {
		InventoryDialogue.inventoryNPC = inventoryNPC;
	}

	public static InventoryInteraction getNPCInventoryInteraction() {
		return interactionType;
	}

	public static void setNPCInventoryInteraction(InventoryInteraction npcInventoryInteraction) {
		interactionType = npcInventoryInteraction;
	}

}