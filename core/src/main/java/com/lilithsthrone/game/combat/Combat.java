package com.lilithsthrone.game.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.attributes.AbstractAttribute;
import com.lilithsthrone.game.character.attributes.Attribute;
import com.lilithsthrone.game.character.effects.AbstractStatusEffect;
import com.lilithsthrone.game.character.effects.AppliedStatusEffect;
import com.lilithsthrone.game.character.effects.Perk;
import com.lilithsthrone.game.character.effects.StatusEffect;
import com.lilithsthrone.game.character.fetishes.Fetish;
import com.lilithsthrone.game.character.npc.NPC;
import com.lilithsthrone.game.character.npc.NPCFlagValue;
import com.lilithsthrone.game.character.quests.QuestLine;
import com.lilithsthrone.game.character.race.Subspecies;
import com.lilithsthrone.game.combat.moves.AbstractCombatMove;
import com.lilithsthrone.game.combat.moves.CombatMove;
import com.lilithsthrone.game.combat.moves.CombatMoveType;
import com.lilithsthrone.game.combat.spells.Spell;
import com.lilithsthrone.game.combat.spells.SpellUpgrade;
import com.lilithsthrone.game.dialogue.DialogueFlagValue;
import com.lilithsthrone.game.dialogue.DialogueNode;
import com.lilithsthrone.game.dialogue.DialogueNodeType;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.responses.ResponseEffectsOnly;
import com.lilithsthrone.game.dialogue.utils.UtilText;
import com.lilithsthrone.game.inventory.AbstractCoreItem;
import com.lilithsthrone.game.inventory.InventorySlot;
import com.lilithsthrone.game.inventory.clothing.AbstractClothing;
import com.lilithsthrone.game.inventory.item.AbstractItem;
import com.lilithsthrone.game.inventory.weapon.AbstractWeapon;
import com.lilithsthrone.game.inventory.weapon.AbstractWeaponType;
import com.lilithsthrone.main.GameState;
import com.lilithsthrone.utils.Util;
import com.lilithsthrone.utils.Util.Value;
import com.lilithsthrone.utils.colours.Colour;
import com.lilithsthrone.utils.colours.PresetColour;

/**
 * Call initialiseCombat() before using.
 *
 * @since 0.1.0
 * @version 0.4.9
 * @author Innoxia, Irbynx
 */
public class Combat {
	
	private NPC activeNPC;
	private GameCharacter targetedAlly;
	private GameCharacter targetedEnemy;
	private NPC enemyLeader;
	
	private List<NPC> allies = new ArrayList<>();
	private List<NPC> enemies = new ArrayList<>();
	private List<NPC> allCombatants = new ArrayList<>();
	private List<GameCharacter> activeCombatants; // A list of combatants who are still active in the fight. This is updated at the very end of each combat turn, and removes characters which have been defeated during the last turn.
	
	private float escapeChance = 0;
	private boolean submitBlocked = false;
	private Map<GameCharacter, Float> totalDamageTaken;
	private int turn = 0;
	private boolean attemptedEscape = false;
	private boolean escaped = false;
	private boolean playerVictory = false;
	private StringBuilder postCombatStringBuilder = new StringBuilder();
	
	private StringBuilder combatTurnResolutionStringBuilder = new StringBuilder();

	private Map<GameCharacter, GameCharacter> preferredTargets;
	
	private Map<GameCharacter, Stack<Float>> manaBurnStack;
	
	private Map<GameCharacter, Map<AbstractStatusEffect, Integer>> statusEffectsToApply;
	
	private Map<GameCharacter, List<String>> combatContent;
	private Map<GameCharacter, List<String>> predictionContent;
	private Map<GameCharacter, List<String>> escapeDescriptionMap;
	
	private Map<GameCharacter, List<Value<GameCharacter, AbstractItem>>> itemsToBeUsed;

	// Maps characters -> inventory slots (to track which slot the weapon was thrown from) -> weapon type and number of weapon type that has been thrown
	private Map<GameCharacter, Map<InventorySlot, Map<AbstractWeapon, Integer>>> weaponsThrownDuringTurn;
	private Map<GameCharacter, Map<InventorySlot, Map<AbstractWeapon, Integer>>> weaponsThrownDuringCombat;
	private Map<GameCharacter, Map<InventorySlot, AbstractWeaponType>> thrownWeaponsDepleted; // Only for use in UI rendering
	
	// Used if the ResponseCombat which initialises combat came from an external dialogue file:
	private DialogueNode playerPostVictoryDialogue;
	private DialogueNode playerPostDefeatDialogue;
	
	
	public Combat() {
	}

	public void initialiseCombat(
			List<NPC> allies,
			boolean addElementalsToAllies,
			NPC enemyLeader,
			List<NPC> enemies,
			Map<GameCharacter, String> openingDescriptions) {
		initialiseCombat(allies,
				addElementalsToAllies,
				enemyLeader,
				enemies,
				openingDescriptions,
				false,
				false);
	}
	/**
	 * @param allies A list of allies who are fighting with you. <b>Do not include GameState.game.getPlayer() in this!</b>
	 * @param enemies A list of enemies you're fighting. The first enemy in the list is considered the leader.
	 * @param escapePercentage The base chance of escaping in this combat situation. TODO
	 * @param openingDescriptions A map of opening descriptions for characters. If a description is not provided, one is generated automatically.
	 * @param escapeBlocked Whether or not escape action is blocked during this combat.
	 */
	public void initialiseCombat(
			List<NPC> allies,
			boolean addElementalsToAllies,
			NPC enemyLeader,
			List<NPC> enemies,
			Map<GameCharacter, String> openingDescriptions,
			boolean escapeBlocked,
			boolean submitBlocked) {
		
		// These should be set manually after initialising combat
		playerPostVictoryDialogue = null;
		playerPostDefeatDialogue = null;
		
		allCombatants = new ArrayList<>();
		this.allies = new ArrayList<>();
		this.enemyLeader = enemyLeader;
		this.enemies = new ArrayList<>();
		activeCombatants = new ArrayList<>();

		predictionContent = new HashMap<>();
		combatContent = new HashMap<>();
		escapeDescriptionMap = new HashMap<>();
		itemsToBeUsed = new HashMap<>();
		preferredTargets = new HashMap<>();
		manaBurnStack = new HashMap<>();
		statusEffectsToApply = new HashMap<>();
		
		predictionContent.put(GameState.game.getPlayer(), new ArrayList<>());
		itemsToBeUsed.put(GameState.game.getPlayer(), new ArrayList<>());
		manaBurnStack.put(GameState.game.getPlayer(), new Stack<>());
		statusEffectsToApply.put(GameState.game.getPlayer(), new HashMap<>());
		combatContent.put(GameState.game.getPlayer(), new ArrayList<>());
		activeCombatants.add(GameState.game.getPlayer());

		weaponsThrownDuringTurn = new HashMap<>();
		weaponsThrownDuringCombat = new HashMap<>();
		thrownWeaponsDepleted = new HashMap<>();
		resetWeaponsThrownDuringTurn(GameState.game.getPlayer());
		resetWeaponsThrownDuringCombat(GameState.game.getPlayer());
		resetThrownWeaponsDepleted(GameState.game.getPlayer());
		
		if(addElementalsToAllies) {
			if(GameState.game.getPlayer().isElementalSummoned()) {
				this.addAlly(GameState.game.getPlayer().getElemental());
				GameState.game.getPlayer().getElemental().setLocation(GameState.game.getPlayer(), false);
			}
			if(allies!=null){
				for(NPC ally : allies) {
					this.addAlly(ally);
					if(ally.isElementalSummoned()) {
						this.addAlly(ally.getElemental());
						ally.getElemental().setLocation(ally, false);
					}
				}
			}
		}
		for(NPC enemy : enemies) {
			this.addEnemy(enemy);
			if(enemy.isElementalSummoned()) {
				this.addEnemy(enemy.getElemental());
				enemy.getElemental().setLocation(enemy, false);
			}
		}
		enemies.sort((enemy1, enemy2) -> enemy2.getLevel()-enemy1.getLevel());
		
		
		targetedEnemy = enemies.get(0);
		targetedAlly = GameState.game.getPlayer();
		activeNPC = enemies.get(0);

		attemptedEscape = false;
		escaped = false;
		playerVictory = false;
				
		totalDamageTaken = new HashMap<>();
		turn = 0;
		postCombatStringBuilder.setLength(0);
		combatTurnResolutionStringBuilder.setLength(0);
		
		if(escapeBlocked) {
			escapeChance = 0;
		} else {
			escapeChance = ((NPC) enemies.get(0)).getEscapeChance();
			if (GameState.game.getPlayer().hasTrait(Perk.RUNNER, true)) {
				escapeChance *= 1.5f;
			} else if (GameState.game.getPlayer().hasTrait(Perk.RUNNER_2, true)) {
				escapeChance *= 2f;
			}
			if(escapeChance >0 && GameState.game.getPlayer().hasTrait(Perk.JOB_ATHLETE, true)) {
				escapeChance = 100;
			}
			if(escapeChance >0 && GameState.game.getPlayer().getSubspecies()==Subspecies.CAT_MORPH_CHEETAH) {
				boolean cheetahEnemy = false;
				for(GameCharacter enemy : getEnemies(GameState.game.getPlayer())) {
					if(enemy.getSubspecies()==Subspecies.CAT_MORPH_CHEETAH) {
						cheetahEnemy = true;
					}
				}
				if(!cheetahEnemy) {
					escapeChance = 100;
				}
			}
		}
		
		this.submitBlocked = submitBlocked;
		
		String startingEffect = "";
		
		if(GameState.game.getPlayer().hasSpellUpgrade(SpellUpgrade.TELEPATHIC_COMMUNICATION_3)) {
			GameState.game.getPlayer().addStatusEffect(StatusEffect.TELEPATHIC_COMMUNICATION_POWER_OF_SUGGESTION, 11);
			startingEffect = Spell.getBasicStatusEffectApplication(GameState.game.getPlayer(), true, Util.newHashMapOfValues(new Value<>(StatusEffect.TELEPATHIC_COMMUNICATION_POWER_OF_SUGGESTION, 10)));
			
		} else if(GameState.game.getPlayer().hasSpellUpgrade(SpellUpgrade.TELEPATHIC_COMMUNICATION_2)) {
			GameState.game.getPlayer().addStatusEffect(StatusEffect.TELEPATHIC_COMMUNICATION_PROJECTED_TOUCH, 11);
			startingEffect = Spell.getBasicStatusEffectApplication(GameState.game.getPlayer(), true, Util.newHashMapOfValues(new Value<>(StatusEffect.TELEPATHIC_COMMUNICATION_PROJECTED_TOUCH, 10)));
			
		} else if(GameState.game.getPlayer().hasSpellUpgrade(SpellUpgrade.TELEPATHIC_COMMUNICATION_1)) {
			GameState.game.getPlayer().addStatusEffect(StatusEffect.TELEPATHIC_COMMUNICATION, 11);
			startingEffect = Spell.getBasicStatusEffectApplication(GameState.game.getPlayer(), true, Util.newHashMapOfValues(new Value<>(StatusEffect.TELEPATHIC_COMMUNICATION, 10)));
		}
		
		// Soldier:
		if(GameState.game.getPlayer().hasTrait(Perk.JOB_SOLDIER, true)) {
			GameState.game.getPlayer().addStatusEffect(StatusEffect.COMBAT_JOB_SOLDIER, 2);
			combatContent.get(GameState.game.getPlayer()).add(
					"Any "+Attribute.HEALTH_MAXIMUM.getName()+" damage you deal in this first turn is [style.boldExcellent(doubled)] thanks to your"
						+ " <b style='color:"+Perk.JOB_SOLDIER.getColour().toWebHexString()+";'>"+Perk.JOB_SOLDIER.getName(GameState.game.getPlayer())+"</b> ability.");
		}
		
		combatContent.get(GameState.game.getPlayer()).add(
				openingDescriptions!=null && openingDescriptions.containsKey(GameState.game.getPlayer())
					?openingDescriptions.get(GameState.game.getPlayer())
					:"You prepare to make a move...");
		
		String pregProtection = getPregnancyProtectionText(GameState.game.getPlayer());
		if(!pregProtection.isEmpty()) {
			combatContent.get(GameState.game.getPlayer()).add(pregProtection);
		}
		
		combatContent.get(GameState.game.getPlayer()).add(startingEffect);
		
		for(NPC combatant : allCombatants) {
			startingEffect="";
			if(combatant.hasSpellUpgrade(SpellUpgrade.TELEPATHIC_COMMUNICATION_3)) {
				combatant.addStatusEffect(StatusEffect.TELEPATHIC_COMMUNICATION_POWER_OF_SUGGESTION, 11);
				startingEffect = Spell.getBasicStatusEffectApplication(combatant, true, Util.newHashMapOfValues(new Value<>(StatusEffect.TELEPATHIC_COMMUNICATION_POWER_OF_SUGGESTION, 10)));
				
			} else if(combatant.hasSpellUpgrade(SpellUpgrade.TELEPATHIC_COMMUNICATION_2)) {
				combatant.addStatusEffect(StatusEffect.TELEPATHIC_COMMUNICATION_PROJECTED_TOUCH, 11);
				startingEffect = Spell.getBasicStatusEffectApplication(combatant, true, Util.newHashMapOfValues(new Value<>(StatusEffect.TELEPATHIC_COMMUNICATION_PROJECTED_TOUCH, 10)));
				
			} else if(combatant.hasSpellUpgrade(SpellUpgrade.TELEPATHIC_COMMUNICATION_1)) {
				combatant.addStatusEffect(StatusEffect.TELEPATHIC_COMMUNICATION, 11);
				startingEffect = Spell.getBasicStatusEffectApplication(combatant, true, Util.newHashMapOfValues(new Value<>(StatusEffect.TELEPATHIC_COMMUNICATION, 10)));
			}

			// Soldier:
			if(combatant.hasTrait(Perk.JOB_SOLDIER, true)) {
				combatant.addStatusEffect(StatusEffect.COMBAT_JOB_SOLDIER, 2);
				combatContent.get(combatant).add(UtilText.parse(combatant,
						"Any "+Attribute.HEALTH_MAXIMUM.getName()+" damage [npc.name] deals in this first turn is [style.boldExcellent(doubled)] thanks to [npc.her]"
								+ " <b style='color:"+Perk.JOB_SOLDIER.getColour().toWebHexString()+";'>"+Perk.JOB_SOLDIER.getName(combatant)+"</b> ability."));
			}
			
			combatContent.get(combatant).add(UtilText.parse(combatant,
					openingDescriptions!=null && openingDescriptions.containsKey(combatant)
						?openingDescriptions.get(combatant)
						:"[npc.Name] prepares to make a move..."));
			
			pregProtection = getPregnancyProtectionText(combatant);
			if(!pregProtection.isEmpty()) {
				combatContent.get(combatant).add(pregProtection);
			}
			
			combatContent.get(combatant).add(startingEffect);
		}
		
		GameState.game.getPlayer().calculateStatusEffects(0); // Calculate status effects to make sure combat SEs are initialised before selecting moves
		GameState.game.getPlayer().resetSelectedMoves();
		GameState.game.getPlayer().resetMoveCooldowns();
		applyNewTurnShielding(GameState.game.getPlayer());
		GameState.game.getPlayer().setRemainingAP(GameState.game.getPlayer().getMaxAP(), null, null);
		
		combatTurnResolutionStringBuilder.append(getCharactersTurnDiv(GameState.game.getPlayer(), getTurn()==0?"Preparation":"", combatContent.get(GameState.game.getPlayer())));

		GameState.game.setInCombat(true);
		
		for(NPC npc : allCombatants) {
			npc.calculateStatusEffects(0); // Calculate status effects to make sure combat SEs are initialised before selecting moves
			combatTurnResolutionStringBuilder.append(getCharactersTurnDiv(npc, getTurn()==0?"Preparation":"", combatContent.get(npc)));
			
			npc.resetSelectedMoves();
			npc.resetDefaultMoves(); // Resetting to take into account any newly obtained moves. Does not do anything to player party members.
			npc.resetMoveCooldowns();
			applyNewTurnShielding(npc);
			npc.setRemainingAP(npc.getMaxAP(), null, null);
			// Sets up NPC ally/enemy lists that include player
			List<GameCharacter> npcAllies = getAllies(npc);
			List<GameCharacter> npcEnemies = getEnemies(npc);
			
//			System.out.println(npc.getName());
//			for(AbstractCombatMove move : npc.getAvailableMoves()) {
//				System.out.println(move.getIdentifier());
//			}
//			System.out.println("---");
//			for(AbstractCombatMove move : npc.getEquippedMoves()) {
//				System.out.println(move.getIdentifier());
//			}
			
			// Selects the moves
			npc.selectMoves(npcEnemies, npcAllies);
			predictionContent.put(npc, npc.getMovesPredictionString(npcEnemies, npcAllies));
		}
		
		// I don't know why openInventory() was being called here, so I commented it out in v0.4.6.8. It caused a bug that was hard to reproduce but which threw this error:
			//Exception in thread "JavaFX Application Thread" java.lang.NullPointerException
			//at com.lilithsthrone.rendering.RenderingEngine.getInventoryIconsForPage(RenderingEngine.java:954)
			//at com.lilithsthrone.rendering.RenderingEngine.getInventoryDiv(RenderingEngine.java:797)
//		GameState.getRenderSink().openInventory();
	}

	public void setCharacterTurnContent(GameCharacter character, List<String> descriptions) {
		combatContent.put(character, descriptions);
	}
	
	private String getCharactersTurnDiv(GameCharacter character, String title, List<String> descriptions) {
		String effects = applyEffects(character);
		StringBuilder sb = new StringBuilder();
		
		boolean enemy = enemies.contains(character);
		
		sb.append("<div class='container-full-width' style='text-align:center; box-sizing: border-box; border:6px solid "+(enemy?PresetColour.GENERIC_MINOR_BAD:PresetColour.GENERIC_MINOR_GOOD).getShades()[0]+"; border-radius:5px;'>");

			sb.append(
					"<div class='container-full-width' style='margin:2px; padding:4px; width:100%; border-radius:5px; text-align:center;'>"
						+ "<b style='color:"+character.getFemininity().getColour().toWebHexString()+";'>"
							+ UtilText.parse(character, "[npc.Name]")
						+ "</b>"
					+ "</div>");
		
			sb.append("<div class='container-full-width'>");
				for(String s : descriptions) {
					if(!s.isEmpty()) {
						sb.append("<div class='container-half-width' style='margin:2px; padding:4px; width:100%; border-radius:5px; background:"+PresetColour.BACKGROUND.toWebHexString()+";'>"+s+"</div>");
					}
				}
				if(!effects.isEmpty()) {
					sb.append("<div class='container-half-width' style='margin:2px; padding:4px; width:100%; border-radius:5px; background:"+PresetColour.BACKGROUND.toWebHexString()+";'>"+effects+"</div>");
				}
			sb.append("</div>");
				
		sb.append("</div>");
		
		return sb.toString();
	}
	
	/**
	 * Ends combat, removing status effects and handling post-combat experience gains and loot drops.
	 * 
	 * @param playerVictory
	 */
	public void endCombat(boolean playerVictory) {
		
		postCombatStringBuilder.setLength(0);
		
		this.playerVictory = playerVictory;

		for(NPC enemy : enemies) {
			enemy.removeFlag(NPCFlagValue.playerEscapedLastCombat);
		}
		
		if (playerVictory) {
			// Give the player experience and money if they won:
			int xp = 0;
			int money = 0;
			for(NPC enemy : enemies) {
				xp+=enemy.getExperienceFromVictory();
				money+=enemy.getLootMoney();
				enemy.setLostCombatCount(enemy.getLostCombatCount()+1);
			}
			
			for(NPC ally : allies) {
				if(!(ally.isElemental())) {
					postCombatStringBuilder.append(ally.incrementExperience(xp, true));
				}
			}
			
			postCombatStringBuilder.append(GameState.game.getPlayer().incrementExperience(xp, true));
			
			if (money > 0) {
				postCombatStringBuilder.append(GameState.game.getPlayer().incrementMoney(money));
			}
			
			// Apply loot drop:
			Map<AbstractCoreItem, Integer> lootedItemsMap = new HashMap<>();
			
			for(NPC enemy : enemies) {
				if(enemy.getLootItems()!=null) {
					for(AbstractCoreItem item : enemy.getLootItems()) {
						lootedItemsMap.putIfAbsent(item, 0);
						lootedItemsMap.put(item, lootedItemsMap.get(item)+1);
						if(item instanceof AbstractItem) {
							GameState.game.getPlayer().addItem((AbstractItem) item, false, true);
						} else if(item instanceof AbstractWeapon) {
							GameState.game.getPlayer().addWeapon((AbstractWeapon) item, false);
						} else if(item instanceof AbstractClothing) {
							GameState.game.getPlayer().addClothing((AbstractClothing) item, false);
						}
					}
				}
			}

			List<String> itemsLooted = new ArrayList<>();
			for(Entry<AbstractCoreItem, Integer> entry : lootedItemsMap.entrySet()) {
				itemsLooted.add("<b style='color:"+entry.getKey().getRarity().getColour().toWebHexString()+";'>"+entry.getKey().getName()+"</b>"+(entry.getValue()>1?" <b>(x"+entry.getValue()+")</b>":""));
			}
			if(!itemsLooted.isEmpty()) {
				postCombatStringBuilder.append("<div class='container-full-width' style='text-align:center;'>You [style.boldGood(gained)] " + Util.stringsToStringList(itemsLooted, false) +"!</div>");
			}
			// Apply essence drops:
			boolean essenceDropFound = false;
			int totalEssencesGained = 0;
			for(NPC enemy : enemies) {
				int essencesGained = enemy.getLootEssenceDrops();
				totalEssencesGained += essencesGained;
				if(essencesGained>0) {
					if(!GameState.game.getDialogueFlags().values.contains(DialogueFlagValue.essencePostCombatDiscovered)) {
						GameState.game.getDialogueFlags().values.add(DialogueFlagValue.essencePostCombatDiscovered);
						
						if(!essenceDropFound) {
							if(!GameState.game.getPlayer().isQuestCompleted(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)) {
								postCombatStringBuilder.append(
										UtilText.parse(enemy,
										"<p>"
										+ "<i>"
											+ "[npc.Name] staggers back, defeated, but before you have a chance to react to your victory, the world around you seems to somehow shift out of focus."
											+ " The pants and gasps coming from [npc.her] mouth start to sound muffled and faint; as though you're listening to [npc.her] while submerged under water."
											+ " After fruitlessly trying to shake your head clear, you look down at [npc.name] to see if [npc.sheIs] being affected by this peculiar phenomenon as well, but as you do, you feel your eyes going wide in shock."
										+ "</p>"
										+ "<p>"
											+ "A shimmering pink glow has materialised around [npc.her] body, just like the one you saw in Lilaya's lab when she ran her tests on you."
											+ " Quickly realising that you're somehow able to see [npc.namePos] arcane aura, you watch, fascinated, as you see a sizable shard slowly start to break away from [npc.herHim]."
											+ " The moment it finally splits from the rest of [npc.her] aura, the shard of energy suddenly launches itself directly at you."
										+ "</p>"
										+ "<p>"
											+ "Slowed down and slightly dazed by the strange state you find yourself in, you have no chance to dodge, and as the shard makes contact with your chest, it pierces straight into your body."
											+ " The world around you instantly snaps back into focus as it enters your body, and you find yourself sharply inhaling as you feel the energy merging with your own arcane aura."
										+ "</p>"
										+ "<p>"
											+ "Looking back down at [npc.name], you see no sign of the shimmering pink field that was surrounding [npc.herHim] a moment ago, and, what's more, [npc.she] seems completely oblivious to what you've just witnessed."
											+ " You think that it would probably be best to go and ask Lilaya about what just happened, but first you'd better deal with this troublesome [npc.race]..."
										+ "</i>"
										+ "</p>"
										+(!GameState.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)?GameState.game.getPlayer().startQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY):"")));
								
							} else {
								postCombatStringBuilder.append(
										UtilText.parse(enemy,
										"<p>"
										+ "<i>"
											+ "[npc.Name] staggers back, defeated, but before you have a chance to react to your victory, the world around you seems to somehow shift out of focus."
											+ " The pants and gasps coming from [npc.her] mouth start to sound muffled and faint; as though you're listening to [npc.her] while submerged under water."
											+ " After fruitlessly trying to shake your head clear, you look down at [npc.name] to see if [npc.sheIs] being affected by this peculiar phenomenon as well, but as you do, you feel your eyes going wide in shock."
										+ "</p>"
										+ "<p>"
											+ "A shimmering pink glow has materialised around [npc.her] body, just like the one you saw in Lilaya's lab when she ran her tests on you."
											+ " Quickly realising that you're somehow able to see [npc.namePos] arcane aura, you watch, fascinated, as you see a sizable shard slowly start to break away from [npc.herHim]."
											+ " The moment it finally splits from the rest of [npc.her] aura, the shard of energy suddenly launches itself directly at you."
										+ "</p>"
										+ "<p>"
											+ "Slowed down and slightly dazed by the strange state you find yourself in, you have no chance to dodge, and as the shard makes contact with your chest, it pierces straight into your body."
											+ " The world around you instantly snaps back into focus as it enters your body, and you find yourself sharply inhaling as you feel the energy merging with your own arcane aura."
										+ "</p>"
										+ "<p>"
											+ "Looking back down at [npc.name], you see no sign of the shimmering pink field that was surrounding [npc.herHim] a moment ago, and, what's more, [npc.she] seems completely oblivious to what you've just witnessed."
											+ " You suddenly remember what Lilaya told you about absorbing essences, and how it's absolutely harmless for both parties involved."
											+ " Breathing a sigh of relief, you turn your attention back to this troublesome [npc.race]..."
										+ "</i>"
										+ "</p>"));
							}
						}
					}
				}
			}
			
			if(totalEssencesGained>0) {
				postCombatStringBuilder.append(
						"<div class='container-full-width' style='text-align:center;'>"
								+ GameState.game.getPlayer().incrementEssenceCount(totalEssencesGained, true)
						+ "</div>"
						+ "</br>");
			}
			
		} else { // Player lost combat:
			int xpGain = (GameState.game.getPlayer().getLevel()*2);
			
			for(NPC enemy : enemies) {
				if(!(enemy.isElemental())) {
					postCombatStringBuilder.append(enemy.incrementExperience(xpGain, true));
				}
			}
			
			long money = GameState.game.getPlayer().getMoney();
			int moneyLoss = (-enemyLeader.getLootMoney()/2)*enemies.size();
			if(moneyLoss!=0 && enemyLeader.isLootingPlayerAfterCombat()) {
				GameState.game.getPlayer().incrementMoney(moneyLoss);
				postCombatStringBuilder.append("<div class='container-full-width' style='text-align:center;'>You [style.boldBad(lost)] " + UtilText.formatAsMoney(Math.abs(GameState.game.getPlayer().getMoney()==0?money:moneyLoss)) + "!</div>");
			}
			
			for(NPC enemy : enemies) {
				enemy.setWonCombatCount(enemy.getWonCombatCount()+1);
			}
		}
		
		// Remove elementals:
		for(GameCharacter combatant : getAllCombatants(true)) {
			if(combatant.isElementalSummoned()) {
				combatant.getElemental().returnToHome();
				if((playerVictory && getEnemies(GameState.game.getPlayer()).contains(combatant))
						 || (!playerVictory && !getEnemies(GameState.game.getPlayer()).contains(combatant))) {
					combatant.setElementalSummoned(false);
					postCombatStringBuilder.append(UtilText.parse(combatant, combatant.getElemental(),
							"<div class='container-full-width' style='text-align:center;'><i>"
								+ "[npc.NamePos] elemental, <span style='colour:"+combatant.getElemental().getFemininity().getColour().toWebHexString()+";'>[npc2.name]</span>,"
									+ " is completely drained of energy and is [style.italicsBad(dispelled)]!"
							+ "</i></div>"));
				} else { 
					postCombatStringBuilder.append(UtilText.parse(combatant, combatant.getElemental(),
							"<div class='container-full-width' style='text-align:center;'><i>"
								+ "[npc.NamePos] elemental, <span style='colour:"+combatant.getElemental().getFemininity().getColour().toWebHexString()+";'>[npc2.name]</span>,"
									+ " is drained of energy and [style.italicsArcane(returns to [npc2.her] passive form)]!"
							+ "</i></div>"));
				}
			}
		}
		
		GameState.game.setInCombat(false);
		
		// Sort out effects after combat:
		for(GameCharacter character : getAllCombatants(true)) {
			// Handle thrown weapons:
			boolean anyWeaponsRecovered = false;
        	for(int i=0; i<Math.min(character.getArmRows(), character.getMainWeaponArray().length); i++) {
				for(Entry<AbstractWeapon, Integer> entry : weaponsThrownDuringCombat.get(character).get(InventorySlot.mainWeaponSlots[i]).entrySet()) {
					AbstractWeapon weapon = entry.getKey();
					for(int count=0; count<entry.getValue(); count++) {
						if(Math.random()*100 <= weapon.getWeaponType().getOneShotChanceToRecoverAfterCombat()) {
							if(character.getMainWeapon(i)==null) {
								character.equipMainWeapon(weapon, i, false);
							} else {
								character.addWeapon(weapon, 1, false, false);
							}
							if(!anyWeaponsRecovered) {
								postCombatStringBuilder.append("<div class='container-full-width' style='text-align:center;'><i>");
								if(character.isPlayer()) {
									postCombatStringBuilder.append("[style.boldGood(One-shot weapons recovered:)]");
								} else {
									postCombatStringBuilder.append(UtilText.parse(character, "[style.boldGood(One-shot weapons recovered by [npc.name]:)]"));
								}
								postCombatStringBuilder.append("<br/>");
								postCombatStringBuilder.append(Util.capitaliseSentence(weapon.getDisplayName(true)));
								anyWeaponsRecovered = true;
							} else {
								postCombatStringBuilder.append(", "+Util.capitaliseSentence(weapon.getDisplayName(true)));
							}
						}
					}
				}
			}
        	for(int i=0; i<Math.min(character.getArmRows(), character.getOffhandWeaponArray().length); i++) {
				for(Entry<AbstractWeapon, Integer> entry : weaponsThrownDuringCombat.get(character).get(InventorySlot.offhandWeaponSlots[i]).entrySet()) {
					AbstractWeapon weapon = entry.getKey();
					for(int count=0; count<entry.getValue(); count++) {
						if(Math.random()*100 <= weapon.getWeaponType().getOneShotChanceToRecoverAfterCombat()) {
							if(character.getOffhandWeapon(i)==null) {
								character.equipOffhandWeapon(weapon, i, false);
							} else {
								character.addWeapon(weapon, 1, false, false);
							}
							if(!anyWeaponsRecovered) {
								postCombatStringBuilder.append("<div class='container-full-width' style='text-align:center;'><i>");
								if(character.isPlayer()) {
									postCombatStringBuilder.append("[style.boldGood(One-shot weapons recovered:)]");
								} else {
									postCombatStringBuilder.append(UtilText.parse(character, "[style.boldGood(One-shot weapons recovered by [npc.name]:)]"));
								}
								postCombatStringBuilder.append("<br/>");
								postCombatStringBuilder.append(Util.capitaliseSentence(weapon.getDisplayName(true)));
								anyWeaponsRecovered = true;
							} else {
								postCombatStringBuilder.append(", "+Util.capitaliseSentence(weapon.getDisplayName(true)));
							}
						}
					}
				}
			}
			if(anyWeaponsRecovered) {
				postCombatStringBuilder.append("</i></div>");
			}
//        	resetWeaponsThrownDuringCombat(character); // This gets reset when combat is initialised anyway, and it might be useful for post-combat dialogue to reference thrown/one-shot weapons?
			
			if(enemies.contains(character)) {
				character.setMana(character.getAttributeValue(Attribute.MANA_MAXIMUM));
				character.setHealth(character.getAttributeValue(Attribute.HEALTH_MAXIMUM));
			} else {
				character.setMana(Math.max(character.getMana(), 5));
				character.setHealth(Math.max(character.getHealth(), 5));
			}
			character.clearCombatStatusEffects();
		}
		
		GameState.game.getTextStartStringBuilder().append(postCombatStringBuilder.toString());
	}

	private String npcStatus() {
		return "";
	}

	// DIALOGUES:
	public DialogueNode startCombat() {
		return ENEMY_ATTACK;
	}
	
	public boolean isCombatantDefeated(GameCharacter character) {
		return (character.getHealth() <= 0 || (character.getLust()>=100 && character.isVulnerableToLustLoss()));
	}
	
	public boolean isOpponent(GameCharacter character, GameCharacter target) {
		if(allies.contains(character) || character.isPlayer()) {
			return enemies.contains(target);
		} else {
			return allies.contains(target) || target.isPlayer();
		}
	}
	
	private boolean isAlliedPartyDefeated() {
		for(NPC ally : allies) {
			if(!isCombatantDefeated(ally)) {
				return false;
			}
		}
		return isCombatantDefeated(GameState.game.getPlayer());
	}
	
	private boolean isEnemyPartyDefeated() {
		for(NPC enemy : enemies) {
			if(!isCombatantDefeated(enemy)) {
				return false;
			}
		}
		return true;
	}
	
	private Response getEndCombatDialogue(boolean applyEffects, boolean playerVictory) {
		if(playerVictory && getPlayerPostVictoryDialogue()!=null) {
			return new Response("Victory", "You have won!", getPlayerPostVictoryDialogue());
		}
		if(!playerVictory && getPlayerPostDefeatDialogue()!=null) {
			return new Response("Defeat", "You have lost!", getPlayerPostDefeatDialogue());
		}
		return enemyLeader.endCombat(applyEffects, playerVictory);
	}

	public final DialogueNode ITEM_USED = new DialogueNode("Combat", "Use the item.", true) {
		@Override
		public String getLabel() {
			return getCombatLabel();
		}
		@Override
		public String getHeaderContent() {
			return npcStatus();
		}
		@Override
		public String getContent() {
			return getCombatContent();
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				if (isEnemyPartyDefeated()) {
					return new ResponseEffectsOnly("Victory", "<span style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>You are victorious!</span>"){
						@Override
						public void effects() {
							endCombat(true);
							GameState.game.setContent(getEndCombatDialogue(true, true));
						}
					};
				} else {
					return new Response("Continue", "Combat continues.", ENEMY_ATTACK){
						@Override
						public void effects() {
							endCombatTurn();//TODO test
						}
					};
				}
				
			} else {
				return null;
			}
		}
		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.NORMAL;
		}
	};

	public final DialogueNode SUBMIT = new DialogueNode("Combat", "Submit", true) {

		@Override
		public String getLabel() {
			return getCombatLabel();
		}

		@Override
		public String getHeaderContent() {
			return npcStatus();
		}

		@Override
		public String getContent() {
			return UtilText.parse(enemyLeader,
							"<p>"
									+ "Are you certain you want to <b>submit</b> to [npc.name]? <b>This will cause you to lose the fight, allowing [npc.herHim] to do anything [npc.she] wants with you!</b>"
							+ "</p>");
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Submit",
						UtilText.parse(enemyLeader,
								"Submit to [npc.name]. <span style='color:" + PresetColour.GENERIC_TERRIBLE.toWebHexString() + ";'>This will cause you to lose the current combat!</span>"),
						SUBMIT_CONFIRM){
					@Override
					public void effects() {
						StringBuilder sb = new StringBuilder();
						
						sb.append(getCharactersTurnDiv(GameState.game.getPlayer(), "Submit",
								Util.newArrayListOfValues(UtilText.parse(enemyLeader,
									"You kneel in front of [npc.name], lowering your head in submission as you mutter,"
										+ " [pc.speech(I don't want to fight any more, I submit.)]"))));

						sb.append(getCharactersTurnDiv(enemyLeader, "Victory",
								Util.newArrayListOfValues(UtilText.parse(enemyLeader,
									"[npc.Name] lets out a triumphant laugh, before moving forwards to take advantage of your submission..."))));
						
						GameState.game.getTextStartStringBuilder().append(sb.toString());
					}
				};
				
			} else if (index == 0) {
				return new Response("Cancel", "Carry on fighting.", ENEMY_ATTACK);
				
			}else {
				return null;
			}
		}

		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.NORMAL;
		}
	};
	public final DialogueNode SUBMIT_CONFIRM = new DialogueNode("Combat", "Submit", true) {

		@Override
		public String getLabel() {
			return getCombatLabel();
		}

		@Override
		public String getHeaderContent() {
			return npcStatus();
		}

		@Override
		public String getContent() {
			return "";
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new ResponseEffectsOnly("Continue", UtilText.parse(enemyLeader, "You wait for [npc.name] to make a move.")){
					@Override
					public void effects() {
						endCombat(false);
						GameState.game.setResponseTab(0);
						GameState.game.setContent(getEndCombatDialogue(true, false));
					}
				};
				
			} else {
				return null;
			}
		}

		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.NORMAL;
		}
	};

	public final DialogueNode ENEMY_ATTACK = new DialogueNode("Combat", "The enemy strikes back at you.", true) {

		@Override
		public String getLabel() {
			return getCombatLabel();
		}

		@Override
		public String getHeaderContent() {
			return npcStatus();
		}

		@Override
		public String getContent() {
			return getCombatContent();
		}
		
		@Override
		public String getResponseTabTitle(int index) {
			if(enemyLeader.interruptCombatSpecialCase()!=null) {
				return null;
			}
			if(index==0) {
				return "Core moves";
				
			} else if(index==1) {
				return "Basic moves";
				
			} else if(index==2) {
				return "Special moves";
				
			} else if(index==3) {
				return "Spells";
				
			} else if(index==4) {
				return "Commands";
			}
			return null;
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			//TODO
			if(enemyLeader.interruptCombatSpecialCase()!=null) {
				if(index == 1) {
					return enemyLeader.interruptCombatSpecialCase();
				}
				return null;
			}
			if(escaped) {
				if (index == 1) {
					return new ResponseEffectsOnly("Escaped!", "You got away!"){
						@Override
						public void effects() {
							enemyLeader.applyEscapeCombatEffects();
							for(NPC enemy : enemies) {
								enemy.addFlag(NPCFlagValue.playerEscapedLastCombat);
							}
							GameState.game.setInCombat(false);
							GameState.game.setResponseTab(0);
							GameState.game.setContent(new Response("", "", GameState.game.getDefaultDialogue(false)));
						}
					};
				} else {
					return null;
				}
				
			} else if(isEnemyPartyDefeated()) {
				if (index == 1) {
					return new ResponseEffectsOnly("Victory", UtilText.parse(enemyLeader, "<span style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>You have defeated [npc.name]!</span>")){
						@Override
						public void effects() {
							endCombat(true);
							GameState.game.setResponseTab(0);
							GameState.game.setContent(getEndCombatDialogue(true, true));
						}
					};
				}
				return null;
				
			} else if(isAlliedPartyDefeated()) {
				if (index == 1) {
					return new ResponseEffectsOnly("Defeat", "You have been defeated!"){
						@Override
						public void effects() {
							endCombat(false);
							GameState.game.setResponseTab(0);
							GameState.game.setContent(getEndCombatDialogue(true, false));
						}
					};
				}
				return null;
				
			} else if(GameState.game.getPlayer().isStunned()) {
				if (index == 1) {
					return new Response("Stunned!", "You are unable to make an action this turn!", ENEMY_ATTACK){
						@Override
						public void effects() {
							combatContent.put(GameState.game.getPlayer(), Util.newArrayListOfValues("You are stunned, and are unable to make a move!"));
							endCombatTurn();
						}
					};
				}
				return null;
				
			} else if(isCombatantDefeated(GameState.game.getPlayer())) {
				if (index == 1) {
					return new Response("Watch", "You have been defeated, and can only watch as your allies fight on!", ENEMY_ATTACK){
						@Override
						public void effects() {
							combatContent.put(GameState.game.getPlayer(), Util.newArrayListOfValues("You have been defeated, and can only watch as your allies continue the fight!"));
							endCombatTurn();
						}
					};
				}
				return null;
			}

			List<GameCharacter> pcEnemies = getEnemies(GameState.game.getPlayer());
			List<GameCharacter> pcAllies = getAllies(GameState.game.getPlayer());
			pcAllies.add(GameState.game.getPlayer());

			if(index == 0) {
				return new Response("End Turn",
						GameState.game.getPlayer().getRemainingAP()<=0?"Ends your current turn":"Ends your current turn. You still have unspent AP!",
						ENEMY_ATTACK){
					@Override
					public void effects() {
						endCombatTurn();
					}
					@Override
					public Colour getHighlightColour() {
						if(GameState.game.getPlayer().getRemainingAP() > 0) {
							return PresetColour.GENERIC_BAD;
						} else {
							return PresetColour.GENERIC_GOOD;
						}
					}
				};
				
			} else if(index<=10 || index>14) {
				
				int moveIndex =
						index<=10
							?index-1
							:index-5;
				
				if(responseTab==0 || responseTab==1) {
					moveIndex =
							index<=8
								?index-1
								:index-7;
					
					if(index==9) {
						if(GameState.combat.isSubmitBlocked()) {
							return new Response("Submit", "You cannot submit in this combat scene!", null);
						} else {
							return new Response("Submit",
									(getEnemies(GameState.game.getPlayer()).size()==1
										?"Surrender this fight to your opponent, allowing them to do whatever they want to you."
										:"Surrender this fight to your enemies, allowing them to do whatever they want to you."),
									SUBMIT);
						}
						
					} else if(index==10) {
						if (escapeChance == 0) {
							return new Response("Escape", "You can't run from this fight!", null);
							
						} else if(!GameState.game.getPlayer().isAbleToEscape()) {
							return new Response("Escape", GameState.game.getPlayer().getUnableToEscapeDescription(), null);
							
						} else {
							return new Response("Escape",
									"Try to escape.<br/><br/>"
									+ (escapeChance==100 && GameState.game.getPlayer().hasTrait(Perk.JOB_ATHLETE, true)
										?"Thanks to your <b style='color:"+Perk.JOB_ATHLETE.getColour().toWebHexString()+";'>"
													+Perk.JOB_ATHLETE.getName(GameState.game.getPlayer())+"</b> ability, you are easily able to sprint away from this fight! "
										:(escapeChance==100 && GameState.game.getPlayer().getSubspecies()==Subspecies.CAT_MORPH_CHEETAH
												?"Thanks to your cheetah-morph's body, you are easily able to sprint away from this fight! "
												:""))
									+"You have a "+escapeChance+"% chance to get away!",
									ENEMY_ATTACK){
								@Override
								public void effects() {
									escape(GameState.game.getPlayer());
									endCombatTurn();
								}
							};
						}

					}
				}
						
				if(responseTab==0) {
					if(GameState.game.getPlayer().getEquippedMoves().size()>moveIndex) {
						AbstractCombatMove move = GameState.game.getPlayer().getEquippedMoves().get(moveIndex);
						
						return getMoveResponse(move, pcEnemies, pcAllies);
						
					} else if(index<=8) {
						return new Response("-",
								"This is a free core combat move slot. You can add core combat moves when outside of combat by opening your phone menu and selecting the 'Combat Moves' action.",
								null);
					}
					
				} else if(responseTab==1) {
					if(GameState.game.getPlayer().getAvailableBasicMoves().size()>moveIndex) {
						AbstractCombatMove move = GameState.game.getPlayer().getAvailableBasicMoves().get(moveIndex);
						
						return getMoveResponse(move, pcEnemies, pcAllies);
					}
					
				} else if(responseTab==2) {
					if(GameState.game.getPlayer().getAvailableSpecialMoves().size()>moveIndex) {
						AbstractCombatMove move = GameState.game.getPlayer().getAvailableSpecialMoves().get(moveIndex);
						
						return getMoveResponse(move, pcEnemies, pcAllies);
					}
					
				} else if(responseTab==3) {
					if(GameState.game.getPlayer().getAvailableSpellMoves().size()>moveIndex) {
						AbstractCombatMove move = GameState.game.getPlayer().getAvailableSpellMoves().get(moveIndex);
						
						return getMoveResponse(move, pcEnemies, pcAllies);
					}
					
				} else if(responseTab==4) {
					String costDescription = "<br/>[style.colourMinorGood(This is a free move, and does not cost any AP, nor end your turn.)]";
					//TODO set behaviour and recalculate moves
					
					for(int i=1; i<=CombatBehaviour.values().length; i++) {
						if(index==i) {
							CombatBehaviour behaviour = CombatBehaviour.values()[i-1];
							
							if(targetedAlly.isPlayer()) {
								return new Response(Util.capitaliseSentence(behaviour.getName()), "You cannot issue commands to yourself!", null);
							}
							return new Response(
									Util.capitaliseSentence(behaviour.getName()),
									(targetedAlly.getCombatBehaviour()==behaviour
										?"[style.italicsMinorGood("+behaviour.getDescription(targetedAlly)+")]"
												+ UtilText.parse(targetedAlly, "<br/>[style.italics(You can select this action again to make [npc.name] recalculate [npc.her] moves.)]")
										:behaviour.getOrderDescription(targetedAlly)+costDescription),
									ENEMY_ATTACK) {
								@Override
								public Colour getHighlightColour() {
									if(targetedAlly.getCombatBehaviour()==behaviour) {
										return PresetColour.GENERIC_GOOD;
									}
									return super.getHighlightColour();
								}
								@Override
								public void effects() {
									targetedAlly.setCombatBehaviour(behaviour);
									
									// Sets up NPC ally/enemy lists that include player
									List<GameCharacter> npcAllies= getAllies(targetedAlly);
									List<GameCharacter> npcEnemies = getEnemies(targetedAlly);
									npcAllies.removeIf((character)->isCombatantDefeated(character));
									npcEnemies.removeIf((character)->isCombatantDefeated(character));
									
									// Figures out the new moves
									int i = 0;
									for(Value<GameCharacter, AbstractCombatMove> move : targetedAlly.getSelectedMoves()) {
										move.getValue().performOnDeselection(i,
												targetedAlly,
												move.getKey(),
												new ArrayList<>(npcEnemies),
												new ArrayList<>(npcAllies));
										targetedAlly.setCooldown(move.getValue().getIdentifier(), 0);
										i++;
									}
									targetedAlly.resetSelectedMoves();
									targetedAlly.setRemainingAP(targetedAlly.getMaxAP(), npcEnemies, npcAllies);
									targetedAlly.selectMoves(npcEnemies, npcAllies);
									
									predictionContent.put(targetedAlly, targetedAlly.getMovesPredictionString(npcEnemies, npcAllies));
								}
							};
						}
					}
					// Change targets:
					if(index==CombatBehaviour.values().length+1) {
						if(targetedAlly.isPlayer()) {
							return new Response("Cycle target", "You cannot issue commands to yourself!", null);
						}
						return new Response(
								"Cycle target",
								UtilText.parse(targetedAlly, "Tell [npc.name] to change [npc.her] target. [npc.She] is currently targeting [style.colourBad("
									+ (getPreferredTarget(targetedAlly)==null
										?" whoever [npc.she] wants"
										:UtilText.parse(getPreferredTarget(targetedAlly), " [npc.name]"))
									+")].")
									+costDescription,
								ENEMY_ATTACK) {
							@Override
							public void effects() {
								// Sets up NPC ally/enemy lists that include player
								List<GameCharacter> npcAllies= getAllies(targetedAlly);
								List<GameCharacter> npcEnemies = getEnemies(targetedAlly);
								npcAllies.removeIf((character)->isCombatantDefeated(character));
								npcEnemies.removeIf((character)->isCombatantDefeated(character));
								
								// Figures out the new moves
								int i = 0;
								for(Value<GameCharacter, AbstractCombatMove> move : targetedAlly.getSelectedMoves()) {
									move.getValue().performOnDeselection(i,
											targetedAlly,
											move.getKey(),
											new ArrayList<>(npcEnemies),
											new ArrayList<>(npcAllies));
									targetedAlly.setCooldown(move.getValue().getIdentifier(), 0);
									i++;
								}
								
								// Set the preferred target:
								int currentTargetIndex = 0;
								if(getPreferredTarget(targetedAlly)!=null) {
									currentTargetIndex = enemies.indexOf(getPreferredTarget(targetedAlly));
								}
								List<GameCharacter> enemiesDoubled = new ArrayList<>(enemies);
								enemiesDoubled.addAll(enemies);
								for(int enemyIdx=0; enemyIdx<enemiesDoubled.size(); enemyIdx++) {
									GameCharacter enemyAtIndex = enemiesDoubled.get(enemyIdx);
									if(!isCombatantDefeated(enemyAtIndex) && getPreferredTarget(targetedAlly)!=enemyAtIndex && (enemyIdx>currentTargetIndex || getPreferredTarget(targetedAlly)==null)) {
										setPreferredTarget(targetedAlly, enemyAtIndex);
										break;
									}
								}
								
								targetedAlly.resetSelectedMoves();
								targetedAlly.setRemainingAP(targetedAlly.getMaxAP(), npcEnemies, npcAllies);
								targetedAlly.selectMoves(npcEnemies, npcAllies);
								
								predictionContent.put(targetedAlly, targetedAlly.getMovesPredictionString(npcEnemies, npcAllies));
							}
						};
					}
					// Clear target:
					if(index==CombatBehaviour.values().length+2) {
						if(targetedAlly.isPlayer()) {
							return new Response("Clear target", "You cannot issue commands to yourself!", null);
						}
						if(getPreferredTarget(targetedAlly)==null) {
							return new Response("Clear target", UtilText.parse(targetedAlly, "[npc.Name] is already targeting whoever [npc.she] wants!"), null);
						}
						return new Response(
								"Clear target",
								UtilText.parse(targetedAlly, "Tell [npc.name] to target whoever [npc.she] wants. [npc.She] is currently targeting [style.colourBad("
									+ (getPreferredTarget(targetedAlly)==null
										?" whoever [npc.she] wants"
										:UtilText.parse(getPreferredTarget(targetedAlly), " [npc.name]"))
									+")].")
									+costDescription,
								ENEMY_ATTACK) {
							@Override
							public void effects() {
								// Sets up NPC ally/enemy lists that include player
								List<GameCharacter> npcAllies= getAllies(targetedAlly);
								List<GameCharacter> npcEnemies = getEnemies(targetedAlly);
								npcAllies.removeIf((character)->isCombatantDefeated(character));
								npcEnemies.removeIf((character)->isCombatantDefeated(character));
								
								// Figures out the new moves
								int i = 0;
								for(Value<GameCharacter, AbstractCombatMove> move : targetedAlly.getSelectedMoves()) {
									move.getValue().performOnDeselection(i,
											targetedAlly,
											move.getKey(),
											new ArrayList<>(npcEnemies),
											new ArrayList<>(npcAllies));
									targetedAlly.setCooldown(move.getValue().getIdentifier(), 0);
									i++;
								}
								
								// Set the preferred target:
								setPreferredTarget(targetedAlly, null);
								
								targetedAlly.resetSelectedMoves();
								targetedAlly.setRemainingAP(targetedAlly.getMaxAP(), npcEnemies, npcAllies);
								targetedAlly.selectMoves(npcEnemies, npcAllies);
								
								predictionContent.put(targetedAlly, targetedAlly.getMovesPredictionString(npcEnemies, npcAllies));
							}
						};
					}
				}
				
			} else if(index==11) {
				return new ResponseEffectsOnly("[style.colourGood(Target:)] "+(getTargetedAlliedCombatant().isPlayer()?"Yourself":Util.capitaliseSentence(getTargetedAlliedCombatant().getName())),
						"You can cycle through your targeted allied combatant by either using this action, or clicking on their name on the right-hand side of the screen.") {
					@Override
					public void effects() {
						List<GameCharacter> alliesPlusPlayer = Util.newArrayListOfValues(GameState.game.getPlayer());
						alliesPlusPlayer.addAll(getAllies(GameState.game.getPlayer()));
						if(alliesPlusPlayer.size()==1) {
							return;
						}
						for(int i=0; i<alliesPlusPlayer.size(); i++) {
							if(alliesPlusPlayer.get(i).equals(getTargetedAlliedCombatant())) {
								if(i+1<alliesPlusPlayer.size()) {
									setTargetedCombatant(alliesPlusPlayer.get(i+1));
									break;
								} else {
									setTargetedCombatant(alliesPlusPlayer.get(0));
									break;
								}
							}
						}
					}
				};

			} else if(index==12) {
				return new ResponseEffectsOnly("[style.colourBad(Target:)] "+Util.capitaliseSentence(getTargetedCombatant().getName()),
						"You can cycle through your targeted enemy combatant by either using this action, or clicking on their name on the right-hand side of the screen.") {
					@Override
					public void effects() {
						List<GameCharacter> playerEnemies = getEnemies(GameState.game.getPlayer());
						if(playerEnemies.size()==1) {
							return;
						}
						for(int i=0; i<playerEnemies.size(); i++) {
							if(playerEnemies.get(i).equals(getTargetedCombatant())) {
								if(i+1<playerEnemies.size()) {
									setTargetedCombatant(playerEnemies.get(i+1));
									break;
								} else {
									setTargetedCombatant(playerEnemies.get(0));
									break;
								}
							}
						}
					}
				};

			} else if(index==14) {
				return new Response("Reset",
						GameState.game.getPlayer().getSelectedMoves().size()==0
								?"You cannot reset your selected moves as you haven't selected any yet!"
								:"Resets your selected moves, allowing you to choose different ones for this turn of combat.",
							GameState.game.getPlayer().getSelectedMoves().size()==0
								?null
								:ENEMY_ATTACK) {
					@Override
					public void effects() {
						if(GameState.game.isInCombat()) {
							int i = 0;
							for(Value<GameCharacter, AbstractCombatMove> move : GameState.game.getPlayer().getSelectedMoves()) {
								move.getValue().performOnDeselection(i,
										GameState.game.getPlayer(),
										move.getKey(),
										new ArrayList<>(enemies),
										new ArrayList<>(allies));
								GameState.game.getPlayer().setCooldown(move.getValue().getIdentifier(), 0);
								i++;
							}
						}
						GameState.game.getPlayer().resetSelectedMoves();
						GameState.game.getPlayer().setRemainingAP(GameState.game.getPlayer().getMaxAP(), pcEnemies, pcAllies);
						predictionContent.put(GameState.game.getPlayer(), new ArrayList<>());
					}
				};

			}
			
			return null;
		}

		@Override
		public DialogueNodeType getDialogueNodeType() {
			return DialogueNodeType.NORMAL;
		}
	};
	
	private Response getMoveResponse(AbstractCombatMove move, List<GameCharacter> pcEnemies, List<GameCharacter> pcAllies) {
		GameCharacter moveTarget = move.isCanTargetAllies()||move.isCanTargetSelf()?getTargetedAlliedCombatant():getTargetedCombatant();

		int selectedMoveIndex = GameState.game.getPlayer().getSelectedMoves().size();
		
		String rejectionReason = move.isUsable(selectedMoveIndex, GameState.game.getPlayer(), moveTarget, pcEnemies, pcAllies);
		if(rejectionReason != null) {
			return new Response(Util.capitaliseSentence(move.getName(selectedMoveIndex, GameState.game.getPlayer())),
								rejectionReason,
					null);
		}
		StringBuilder moveStatblock = new StringBuilder();
		
		boolean isCrit = move.canCrit(selectedMoveIndex, GameState.game.getPlayer(), moveTarget, pcEnemies, pcAllies);
		
		if(move.getStatusEffects(GameState.game.getPlayer(), moveTarget, isCrit)!=null && !move.getStatusEffects(GameState.game.getPlayer(), moveTarget, isCrit).isEmpty()) {
			for(Entry<AbstractStatusEffect, Integer> entry : move.getStatusEffects(GameState.game.getPlayer(), moveTarget, isCrit).entrySet()) {
				moveStatblock.append("Applies <b style='color:"+entry.getKey().getColour().toWebHexString()+";'>"+Util.capitaliseSentence(entry.getKey().getName(moveTarget))+"</b>"
						+ " for <b>"+entry.getValue()+(entry.getValue()==1?" turn":" turns")+"</b><br/>");
			}
		}
		
		StringBuilder critText = new StringBuilder();
		critText.append("<br/>[style.colourCrit(Critical Requirement)]:");
		for(String s : move.getCritRequirements(GameState.game.getPlayer(), moveTarget, pcEnemies, pcAllies)) {
			critText.append(" "+s);
		}
		
		String predictionTooltip = move.getPrediction(selectedMoveIndex, GameState.game.getPlayer(), moveTarget, pcEnemies, pcAllies);
		
		return new Response(Util.capitaliseSentence(move.getName(selectedMoveIndex, GameState.game.getPlayer())),
			moveStatblock.toString()
				+ predictionTooltip
				+ critText.toString(),
			ENEMY_ATTACK){
			@Override
			public void effects() {
				GameState.game.getPlayer().selectMove(GameState.game.getPlayer().getSelectedMoves().size(), move, moveTarget, pcEnemies, pcAllies);
				// Reset prediction content as this selected move may have altered the prediction of previous moves:
				predictionContent.put(GameState.game.getPlayer(), new ArrayList<>());
				int i=0;
				for(Value<GameCharacter, AbstractCombatMove> selectedMove : GameState.game.getPlayer().getSelectedMoves()) {
					predictionContent.get(GameState.game.getPlayer()).add(selectedMove.getValue().getPrediction(i, GameState.game.getPlayer(), selectedMove.getKey(), pcEnemies, pcAllies));
					i++;
				}
			}
			@Override
			public Colour getHighlightColour() {
				return move.getColourByDamageType(selectedMoveIndex, GameState.game.getPlayer());
			}
			@Override
			public AbstractCombatMove getAssociatedCombatMove() {
				return move;
			}
		};
	}
	
	public List<String> applyExtraAttackEffects(GameCharacter attacker, GameCharacter target, Attack attackType, AbstractWeapon weapon, boolean isHit, boolean isCritical) {
		List<String> extraAttackEffects = new ArrayList<>();
		
		if(target.hasStatusEffect(StatusEffect.CLOAK_OF_FLAMES_3)
				&& (attackType==Attack.MAIN || attackType==Attack.OFFHAND || attackType==Attack.DUAL)
				&& (weapon==null || weapon.getWeaponType().isMelee())) {
			float cloakOfFlamesDamage = Math.round(5 * (1 + (target.getAttributeValue(Attribute.DAMAGE_FIRE)/100f)));
			cloakOfFlamesDamage = (Math.round(cloakOfFlamesDamage*10))/10f;
			if (cloakOfFlamesDamage < 1) {
				cloakOfFlamesDamage = 1;
			}
			attacker.incrementHealth(-cloakOfFlamesDamage);
			
			if(attacker.isPlayer()) {
				extraAttackEffects.add(UtilText.parse(target, "You take <b>"+cloakOfFlamesDamage+"</b> [style.boldFire(Fire Damage)] from [npc.namePos] [style.boldFire(Ring of Fire)]!"));
			} else {
				if(target.isPlayer()) {
					extraAttackEffects.add(UtilText.parse(attacker, "[npc.Name] takes <b>"+cloakOfFlamesDamage+"</b> [style.boldFire(Fire Damage)] from your [style.boldFire(Ring of Fire)]!"));
				} else {
					extraAttackEffects.add(UtilText.parse(attacker, target, "[npc1.Name] takes <b>"+cloakOfFlamesDamage+"</b> [style.boldFire(Fire Damage)] from [npc2.namePos] [style.boldFire(Ring of Fire)]!"));
				}
			}
		}
		
		if(isCritical && target.hasStatusEffect(StatusEffect.RAIN_CLOUD_DOWNPOUR_FOR_CLOUDBURST)) {
			target.removeStatusEffect(StatusEffect.RAIN_CLOUD);
			target.removeStatusEffect(StatusEffect.RAIN_CLOUD_CLOUDBURST);
			target.removeStatusEffect(StatusEffect.RAIN_CLOUD_DEEP_CHILL);
			target.removeStatusEffect(StatusEffect.RAIN_CLOUD_DOWNPOUR);
			target.removeStatusEffect(StatusEffect.RAIN_CLOUD_DOWNPOUR_FOR_CLOUDBURST);
			
			addStatusEffectToApply(target, StatusEffect.RAIN_CLOUD_CLOUDBURST, 6);
			
			extraAttackEffects.add(UtilText.parse(target, "As [npc.name] is critically hit, the rain cloud above [npc.her] head grows in size, and suddenly erupts into a torrential cloudburst!"));
			
			extraAttackEffects.add(Spell.getBasicStatusEffectApplication(target, false, Util.newHashMapOfValues(new Value<>(StatusEffect.RAIN_CLOUD_CLOUDBURST, 6))));
			
		}
		
		if(attacker.isPlayer() && attacker.hasFetish(Fetish.FETISH_SADIST) && isCritical && isHit) {
			extraAttackEffects.add(
							"Thanks to your [style.boldFetish(sadist fetish)], the arousal you feel from critically hitting someone manifests as an arcane essence!<br/>"
							+GameState.game.getPlayer().incrementEssenceCount(1, false));
		}
		
		if(target.isPlayer() && target.hasFetish(Fetish.FETISH_MASOCHIST) && isCritical && isHit) {
			extraAttackEffects.add(
							"Thanks to your [style.boldFetish(masochist fetish)], the arousal you feel from getting critically hit manifests as an arcane essence!<br/>"
							+GameState.game.getPlayer().incrementEssenceCount(1, false));
		}
		
		return extraAttackEffects;
	}

	private void escape(GameCharacter escapee) {
		attemptedEscape = true;
		
		boolean allEnemiesStunned = true;
		if(escapee.isPlayer() || getAllies(GameState.game.getPlayer()).contains(escapee)) {
			for(GameCharacter enemy : getEnemies(GameState.game.getPlayer())) {
				if(!enemy.isStunned()) {
					allEnemiesStunned = false;
				}
			}
		} else {
			if(GameState.game.getPlayer().isStunned()) {
				allEnemiesStunned = false;
			}
			for(GameCharacter ally : getAllies(GameState.game.getPlayer())) {
				if(ally.isStunned()) {
					allEnemiesStunned = false;
				}
			}
		}
		
		escapeDescriptionMap = new HashMap<>();
		StringBuilder escapeDescription = new StringBuilder();
		if(allEnemiesStunned) {
			escaped = true;
			escapeDescription.append("All of your enemies are stunned, so you're easily able to escape!");
		} else if (Util.random.nextInt(100) < escapeChance) {
			escaped = true;
			escapeDescription.append("You successfully managed to escape!");
		} else {
			escapeDescription.append("You failed to escape!");
		}
		escapeDescriptionMap.put(escapee, Util.newArrayListOfValues(escapeDescription.toString()));
		
		for(GameCharacter combatant : getAllCombatants(true)) {
			if(getAllies(escapee).contains(combatant) || combatant.equals(escapee)) {
				int i = 0;
				for(Value<GameCharacter, AbstractCombatMove> move : combatant.getSelectedMoves()) {
					move.getValue().performOnDeselection(i,
							combatant,
							move.getKey(),
							new ArrayList<>(enemies),
							new ArrayList<>(allies));
					combatant.setCooldown(move.getValue().getIdentifier(), 0);
					i++;
				}
				combatant.resetSelectedMoves();
				combatant.setRemainingAP(combatant.getMaxAP(), getEnemies(combatant), getAllies(combatant));
				predictionContent.put(combatant, new ArrayList<>());
				if(escaped && !combatant.equals(escapee)) {
					escapeDescriptionMap.put(combatant,
							Util.newArrayListOfValues(UtilText.parse(combatant, "[npc.Name] manages to escape with you!")));
				}
			} else {
				if(escaped) {
					escapeDescriptionMap.put(combatant,
							Util.newArrayListOfValues(UtilText.parse(combatant, "[npc.Name] tries to block your escape, but fails!")));
				}
			}
		}
		
		if(escaped) {
			// Remove elementals:
			for(GameCharacter combatant : getAllCombatants(true)) {
				if(combatant.isElementalSummoned()) {
					combatant.getElemental().returnToHome();
					escapeDescription.append(UtilText.parse(combatant, combatant.getElemental(),
							"<p style='text-align:center;'><i>"
								+ "[npc.NamePos] elemental, <span style='colour:"+combatant.getElemental().getFemininity().getColour().toWebHexString()+";'>[npc2.name]</span>,"
									+ " is drained of energy and [style.italicsArcane(returns to [npc2.her] passive form)]!"
							+ "</i></p>"));
				}
			}
		}
	}

	/**
	 * Calculations for enemy attack.
	 * @param character The character performing an attack turn.
	 * @return true if the character is able to perform an attack, false if they cannot (due to being defeated, stunned, or attempting to escape).
	 */
	private boolean attackCharacter(GameCharacter character) {
		if(escaped) {
			combatContent.put(character, escapeDescriptionMap.get(character));
			return false;
		}
		
		if(character.isPlayer()) {
			if (!activeCombatants.contains(character)) {
				combatContent.put(character,
						Util.newArrayListOfValues(UtilText.parse(character, "<span style='color:"+PresetColour.GENERIC_BAD.toWebHexString()+";'>[npc.NameHasFull] been defeated!</span>")));
				return false;
				
			} else if(attemptedEscape) {
				combatContent.put(character,
						Util.newArrayListOfValues(UtilText.parse(character, "<span style='color:"+PresetColour.GENERIC_MINOR_BAD.toWebHexString()+";'>You fail to escape!</span>")));
				return false;
				
			}
			
		} else if(allies.contains(character)) {
			if (!activeCombatants.contains(character)) {
				combatContent.put(character,
						Util.newArrayListOfValues(UtilText.parse(character, "<span style='color:"+PresetColour.GENERIC_BAD.toWebHexString()+";'>[npc.NameHasFull] been defeated!</span>")));
				return false;
				
			} else if(attemptedEscape) {
				combatContent.put(character,
						Util.newArrayListOfValues(UtilText.parse(character, "<span style='color:"+PresetColour.GENERIC_MINOR_BAD.toWebHexString()+";'>[npc.Name] fails to escape with you!</span>")));
				return false;
				
			}
			
		} else {
			if (!activeCombatants.contains(character)) {
				combatContent.put(character,
						Util.newArrayListOfValues(UtilText.parse(character, "<span style='color:"+PresetColour.GENERIC_BAD.toWebHexString()+";'>[npc.NameHasFull] been defeated!</span>")));
				return false;
			}
		}
		
		if(character.isStunned()) {
			combatContent.put(character,
					Util.newArrayListOfValues(UtilText.parse(character, "[npc.NameIsFull] stunned, and [npc.is] unable to make a move!")));

			character.resetSelectedMoves();
			return false;
		}
		
		return true;
	}

	private StringBuilder endTurnStatusEffectText = new StringBuilder();
	
	private void applyNewTurnShielding(GameCharacter character) {
	    character.resetShields();
	    
	    int bonusEnergyShielding = Math.round(character.getAttributeValue(Attribute.ENERGY_SHIELDING));
		character.incrementShields(DamageType.HEALTH, bonusEnergyShielding);
		
		DamageType[] damageTypes = new DamageType[] {DamageType.PHYSICAL, DamageType.FIRE, DamageType.ICE, DamageType.POISON};
		for(DamageType dt : damageTypes) {
			character.incrementShields(dt, Math.round(character.getAttributeValue(dt.getResistAttribute())));
		}
		
	    character.incrementShields(DamageType.LUST, Math.round(character.getAttributeValue(DamageType.LUST.getResistAttribute())));
	}
	
	public void endCombatTurn() {
		combatTurnResolutionStringBuilder.setLength(0);
		
		List<GameCharacter> combatants = getAllCombatants(true); // To avoid concurrent modification when the 'summon elemental' spell adds combatants.
		
		// Perform moves based on following ordering, so that all defensive and support abilities are applied before attacks start landing:
		CombatMoveType[] order = new CombatMoveType[] {CombatMoveType.DEFEND, CombatMoveType.SPELL, CombatMoveType.ATTACK};
		for(GameCharacter character : getAllCombatants(true)) {
			combatContent.put(character, new ArrayList<>());
		}
		for(int i=0;i<order.length;i++) {
			for(GameCharacter character : combatants) {
				if(attackCharacter(character)) {
					List<GameCharacter> npcAllies = getAllies(character);
					List<GameCharacter> npcEnemies = getEnemies(character);
					// Performs the actions
					character.performMoves(order[i], combatContent.get(character), npcEnemies, npcAllies);
				}
			}
			// After all defensive and supportive moves have been made, apply the queued up status effects before the attacks start hitting, as they should only be defensive or supportive-based.
			if(i==2) {
				for(GameCharacter character : combatants) {
					for(Entry<AbstractStatusEffect, Integer> entry : statusEffectsToApply.get(character).entrySet()) {
						character.addStatusEffect(entry.getKey(), entry.getValue()+1);// Add 1 to the status effect duration, as it gets immediately decremented by 1 within the getCharactersTurnDiv() method (as it calls the applyEffects() method).
					}
					statusEffectsToApply.put(character, new HashMap<>());
				}
			}
		}
		for(GameCharacter character : combatants) {
			// Handle thrown weapons:
			StringBuilder thrownWeaponsRecoveredDescriptions = new StringBuilder();
        	for(int i=0; i<Math.min(character.getArmRows(), character.getMainWeaponArray().length); i++) {
				for(Entry<AbstractWeapon, Integer> entry : weaponsThrownDuringTurn.get(character).get(InventorySlot.mainWeaponSlots[i]).entrySet()) {
					AbstractWeapon weapon = entry.getKey();
					for(int count=0; count<entry.getValue(); count++) {
						if(Math.random()*100 <= weapon.getWeaponType().getOneShotChanceToRecoverAfterTurn()) {
							if(character.getMainWeapon(i)==null) {
								character.equipMainWeapon(weapon, i, false);
							} else {
								character.addWeapon(weapon, 1, false, false);
							}
							incrementWeaponsThrownDuringCombat(character, InventorySlot.mainWeaponSlots[i], weapon, -1);
							if(thrownWeaponsRecoveredDescriptions.length()==0) {
								thrownWeaponsRecoveredDescriptions.append("[style.boldGood(One-shot weapons recovered:)]");
							}
							thrownWeaponsRecoveredDescriptions.append("<br/>");
							thrownWeaponsRecoveredDescriptions.append(weapon.getWeaponType().getOneShotEndTurnRecoveryDescription(character).trim());
						}
					}
				}
			}
        	for(int i=0; i<Math.min(character.getArmRows(), character.getOffhandWeaponArray().length); i++) {
				for(Entry<AbstractWeapon, Integer> entry : weaponsThrownDuringTurn.get(character).get(InventorySlot.offhandWeaponSlots[i]).entrySet()) {
					AbstractWeapon weapon = entry.getKey();
					for(int count=0; count<entry.getValue(); count++) {
						if(Math.random()*100 <= weapon.getWeaponType().getOneShotChanceToRecoverAfterTurn()) {
							if(character.getOffhandWeapon(i)==null) {
								character.equipOffhandWeapon(weapon, i, false);
							} else {
								character.addWeapon(weapon, 1, false, false);
							}
							incrementWeaponsThrownDuringCombat(character, InventorySlot.offhandWeaponSlots[i], weapon, -1);
							if(thrownWeaponsRecoveredDescriptions.length()==0) {
								thrownWeaponsRecoveredDescriptions.append("[style.boldGood(One-shot weapons recovered:)]");
							}
							thrownWeaponsRecoveredDescriptions.append("<br/>");
							thrownWeaponsRecoveredDescriptions.append(weapon.getWeaponType().getOneShotEndTurnRecoveryDescription(character).trim());
						}
					}
				}
			}
        	resetWeaponsThrownDuringTurn(character);
        	combatContent.get(character).add(thrownWeaponsRecoveredDescriptions.toString());
        	
			combatTurnResolutionStringBuilder.append(getCharactersTurnDiv(character, getTurn()==0?"Preparation":"", combatContent.get(character)));
			
			character.resetSelectedMoves();
		}
		
		attemptedEscape = false;
		
		// End turn effects:
		for(GameCharacter character : combatants) {
			for(Entry<AbstractStatusEffect, Integer> entry : statusEffectsToApply.get(character).entrySet()) {
				character.addStatusEffect(entry.getKey(), entry.getValue());
			}
			statusEffectsToApply.put(character, new HashMap<>());
			
			List<GameCharacter> npcAllies = getAllies(character);
			List<GameCharacter> npcEnemies = getEnemies(character);
			
			applyNewTurnShielding(character);
			character.lowerMoveCooldowns();
			character.setRemainingAP(character.getMaxAP(), npcEnemies, npcAllies);
			
			if(isCombatantDefeated(character)) {
				if(activeCombatants.remove(character)) {
					List<String> vampyres = new ArrayList<>();
					boolean playerVampyre = false;
					float manaAbsorbed = Math.round(character.getMana()/2);
					for(GameCharacter c2 : combatants) {
						if(!isCombatantDefeated(c2) && c2.hasTraitActivated(Perk.ARCANE_VAMPYRISM)) {
							if(c2.isPlayer()) {
								vampyres.add(0, UtilText.parse(c2,"[npc.name]"));
								playerVampyre = true;
							} else {
								vampyres.add(UtilText.parse(c2,"[npc.name]"));
							}
							c2.incrementMana(manaAbsorbed);
						}
					}
					if(!vampyres.isEmpty()) {
						character.setMana(manaAbsorbed);
	
						predictionContent.put(character, Util.newArrayListOfValues(
										UtilText.parse(character,
												"[style.boldArcane("+(Util.capitaliseSentence(Perk.ARCANE_VAMPYRISM.getName(GameState.game.getPlayer())))+":)]<br/>"
														+Util.capitaliseSentence(Util.stringsToStringList(vampyres, false))+(vampyres.size()>1 || playerVampyre?" absorb":" absorbs")
														+" half of [npc.namePos] remaining aura,"
														+ (enemies.contains(character)
																?"[style.colourGood("
																:"[style.colourBad(")
														+" gaining "+manaAbsorbed+" aura)]!")));
					} else {
						predictionContent.put(character, Util.newArrayListOfValues("<span style='color:"+PresetColour.GENERIC_BAD.toWebHexString()+";'>Defeated...</span>"));
					}
					
				} else {
					predictionContent.put(character, Util.newArrayListOfValues("<span style='color:"+PresetColour.GENERIC_BAD.toWebHexString()+";'>Defeated...</span>"));
				}
				
			} else if(character.isStunned()) {
				predictionContent.put(character, Util.newArrayListOfValues("<span style='color:"+PresetColour.GENERIC_BAD.toWebHexString()+";'>Stunned!</span>"));
				
			} else {
				if(character.isPlayer()) {
					predictionContent.put(character, new ArrayList<>());
					
				} else {
					npcAllies.removeIf((c)->isCombatantDefeated(c));
					npcEnemies.removeIf((c)->isCombatantDefeated(c));
					
					// Figures out new moves for NPCs:
					character.selectMoves(npcEnemies, npcAllies);
					predictionContent.put(character, character.getMovesPredictionString(npcEnemies, npcAllies));
				}
			}
		}
		
		if(isCombatantDefeated(targetedEnemy)) {
			for(NPC enemy : enemies) {
				if(!isCombatantDefeated(enemy)) {
					targetedEnemy = enemy;
					break;
				}
			}
		}
		
		if(isCombatantDefeated(targetedAlly)) {
			targetedAlly = GameState.game.getPlayer();
		}
		
		turn++;
	}

	private String getShieldsDisplayValue(AbstractAttribute att, int shields) {
		String valueForDisplay = String.valueOf(shields);
		if(att.isInfiniteAtUpperLimit() && shields>=att.getUpperLimit()) {
			valueForDisplay = UtilText.getInfinitySymbol(false);
		}
		return valueForDisplay;
	}
	
	private String getTitleResources(GameCharacter character) {
		int apRemaining = character.getRemainingAP();
		StringBuilder sb = new StringBuilder();
		
		sb.append(" <b>(<span style='color:"+(apRemaining==0?PresetColour.GENERIC_GOOD:PresetColour.GENERIC_BAD).toWebHexString()+";'>"+apRemaining+"</span>/"+character.getMaxAP()+" AP)</b>");
		
		sb.append("<div class='container-full-width' style='text-align:center;'>");
		
		boolean shieldsFound = false;
		int shields = character.getShields(DamageType.HEALTH);
		if(shields!=0) {
			shieldsFound = true;
			sb.append("<div style='display:inline-block; float:none; margin:auto; padding:0 2px; background-color:"+PresetColour.BACKGROUND.toWebHexString()+"; border-radius:5px; width:auto; position:relative;'>"
							+"<span style='color:"+DamageType.HEALTH.getColour().toWebHexString()+";'>"+UtilText.getShieldSymbol()+"</span> "+(shields<0?"[style.colourDisabled("+shields+")]":getShieldsDisplayValue(Attribute.ENERGY_SHIELDING, shields))
							+ "<div class='overlay' id='"+character.getId()+"_COMBAT_SHIELD_"+DamageType.HEALTH+"' style='cursor:default;'></div>"
						+ "</div>");
		}

		DamageType[] damageTypes = new DamageType[] {DamageType.PHYSICAL, DamageType.FIRE, DamageType.ICE, DamageType.POISON};
		for(DamageType dt : damageTypes) {
			shields = character.getShields(dt);
			if(shields!=0) {
				if(shieldsFound) {
					sb.append(" | ");
				}
				shieldsFound = true;
				sb.append(
						"<div style='display:inline-block; float:none; margin:auto; padding:0 2px; background-color:"+PresetColour.BACKGROUND.toWebHexString()+"; border-radius:5px; width:auto; position:relative;'>"
							+"<span style='color:"+dt.getColour().toWebHexString()+";'>"+UtilText.getShieldSymbol()+"</span> "+(shields<0?"[style.colourDisabled("+shields+")]":getShieldsDisplayValue(dt.getResistAttribute(), shields))
							+ "<div class='overlay' id='"+character.getId()+"_COMBAT_SHIELD_"+dt+"' style='cursor:default;'></div>"
						+ "</div>");
			}
		}

		shields = character.getShields(DamageType.LUST);
		if(shields!=0) {
			if(shieldsFound) {
				sb.append(" | ");
			}
			shieldsFound = true;
			sb.append(
					"<div style='display:inline-block; float:none; margin:auto; padding:0 2px; background-color:"+PresetColour.BACKGROUND.toWebHexString()+"; border-radius:5px; width:auto; position:relative;'>"
						+"<span style='color:"+DamageType.LUST.getColour().toWebHexString()+";'>"+UtilText.getShieldSymbol()+"</span> "+(shields<0?"[style.colourDisabled("+shields+")]":getShieldsDisplayValue(DamageType.LUST.getResistAttribute(), shields))
						+ "<div class='overlay' id='"+character.getId()+"_COMBAT_SHIELD_"+DamageType.LUST+"' style='cursor:default;'></div>"
					+ "</div>");
		}

		sb.append("</div>");
		
		return sb.toString();
	}
	
	private String getCombatContent() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<div class='container-full-width' style='text-align:center; box-sizing: border-box; border:6px solid "+PresetColour.BASE_WHITE.toWebHexString()+"; border-radius:5px;'>");

			sb.append("<div class='container-full-width' style='margin:2px; padding:4px; width:100%; border-radius:5px; text-align:center;'><b>Planning</b></div>");
		
			sb.append("<div class='container-half-width'>");
				
				sb.append("[style.boldGood(You)]"+ getTitleResources(GameState.game.getPlayer()));
				if(predictionContent.get(GameState.game.getPlayer()).isEmpty()) {
					sb.append("<div class='container-half-width' style='margin:2px; padding:4px; width:100%; border-radius:5px; background:"+PresetColour.BACKGROUND.toWebHexString()+";'>[style.colourDisabled(No moves selected...)]</div>");
				} else {
					for(String s : predictionContent.get(GameState.game.getPlayer())) {
						sb.append("<div class='container-half-width' style='margin:2px; padding:4px; width:100%; border-radius:5px; background:"+PresetColour.BACKGROUND.toWebHexString()+";'>"+s+"</div>");
					}
				}
				sb.append("</br>");
			
				for(GameCharacter ally : getAllies(GameState.game.getPlayer())) {
					sb.append(UtilText.parse(ally, "</br>[style.boldMinorGood([npc.Name])]")+ getTitleResources(ally));
					for(String s : predictionContent.get(ally)) {
						sb.append("<div class='container-half-width' style='margin:2px; padding:4px; width:100%; border-radius:5px; background:"+PresetColour.BACKGROUND.toWebHexString()+";'>"+s+"</div>");
					}
					sb.append("</br>");
				}
			sb.append("</div>");

			sb.append("<div class='container-half-width'>");
			for(GameCharacter enemy : getEnemies(GameState.game.getPlayer())) {
				sb.append(UtilText.parse(enemy, (enemyLeader.equals(enemy)?"[style.boldBad([npc.Name])]":"</br>[style.boldMinorBad([npc.Name])]"))+ getTitleResources(enemy));
				for(String s : predictionContent.get(enemy)) {
					sb.append("<div class='container-half-width' style='margin:2px; padding:4px; width:100%; border-radius:5px; background:"+PresetColour.BACKGROUND.toWebHexString()+";'>"+s+"</div>");
				}
				sb.append("</br>");
			}
			sb.append("</div>");
			
		sb.append("</div>");

		sb.append(combatTurnResolutionStringBuilder.toString());
		
		return sb.toString(); 
	}
	
	private String applyEffects(GameCharacter character) {
		endTurnStatusEffectText = new StringBuilder();
		List<AbstractStatusEffect> effectsToRemove = new ArrayList<>();
		for (AppliedStatusEffect appliedSe : character.getAppliedStatusEffects()) {
			AbstractStatusEffect se = appliedSe.getEffect();
			if (se.isCombatEffect()) {
				appliedSe.setSecondsPassed(turn);
				StringBuilder s = new StringBuilder();
				if(appliedSe.getEffect().getEffectInterval()<=0 || ((turn-appliedSe.getLastTimeAppliedEffect())>appliedSe.getEffect().getEffectInterval())) {
					if(appliedSe.getEffect().getEffectInterval()<=0) {
						s.append(se.applyEffect(character, 1, appliedSe.getSecondsPassed()));
						
					} else {
						for(int i=0; i<((GameState.game.getSecondsPassed()-appliedSe.getLastTimeAppliedEffect())/appliedSe.getEffect().getEffectInterval()); i++) {
							if(s.length()>0) {
								s.append("<br/>");
							}
							s.append(se.applyEffect(character, 1, appliedSe.getSecondsPassed()));
						}
					}
					
					appliedSe.setLastTimeAppliedEffect(GameState.game.getSecondsPassed());
					if(s.length()!=0) {
						endTurnStatusEffectText.append("<p><b style='color: " + se.getColour().toWebHexString() + "'>" + Util.capitaliseSentence(se.getName(character)) + ":</b> " + s.toString()+ "</p>");
					}
				}
				
				
//				String effectString = se.applyEffect(character, 1, appliedSe.getSecondsPassed());
//				if(!effectString.isEmpty()) {
//					endTurnStatusEffectText.append("<p><b style='color: " + se.getColour().toWebHexString() + "'>" + Util.capitaliseSentence(se.getName(character)) + ":</b> " + effectString+ "</p>");
//				}
				if(character.getStatusEffectDuration(se)>=0) { // Don't decrement below -1
					character.setCombatStatusEffectDuration(se, character.getStatusEffectDuration(se) - 1);
				}
				if (character.getStatusEffectDuration(se) == 0) { // Do not remove special effects (i.e. ones set at -1 duration)
					effectsToRemove.add(se);
				}
			}
		}
		for (AbstractStatusEffect se : effectsToRemove) {
			endTurnStatusEffectText.append(character.removeStatusEffectCombat(se));
		}
		return endTurnStatusEffectText.toString();
	}

	// Utility methods:
	private String getCombatLabel() {
		if(turn==0) {
			return "Combat - Start";
		} else {
			return "Combat - Turn "+turn;
		}
	}
	
	/**
	 * @return The enemy NPC which the player is targeting. Use CombatMove's getPreferredTarget for NPC targeting.
	 */
	public GameCharacter getTargetedCombatant() {
		return targetedEnemy;
	}

	/**
	 * @return The allied NPC which the player is targeting. Use COmbatMove's getPreferredTarget for NPC targeting.
	 */
	public GameCharacter getTargetedAlliedCombatant() {
		return targetedAlly;
	}

	public void setTargetedCombatant(GameCharacter targetedCombatant) {
		if(getEnemies(GameState.game.getPlayer()).contains(targetedCombatant)) {
			targetedEnemy = targetedCombatant;
		} else {
			targetedAlly = targetedCombatant;
		}
	}
	
	public NPC getActiveNPC() {
		return activeNPC;
	}

	public void setActiveNPC(NPC activeNPC) {
		this.activeNPC = activeNPC;
	}

	public void resetItemsToBeUsed(GameCharacter character) {
		itemsToBeUsed.put(character, new ArrayList<>());
	}
	
	public List<Value<GameCharacter, AbstractItem>> getItemsToBeUsed(GameCharacter user) {
		return itemsToBeUsed.get(user);
	}

	public void addItemToBeUsed(GameCharacter user, GameCharacter target, AbstractItem item) {
		itemsToBeUsed.get(user).add(new Value<>(target, item));
		//TODO test combatmove
		predictionContent.get(GameState.game.getPlayer()).add(CombatMove.ITEM_USAGE.getPrediction(user.getSelectedMoves().size(), user, target, getEnemies(user), getAllies(user)));
		GameState.game.getPlayer().selectMove(user.getSelectedMoves().size(), CombatMove.ITEM_USAGE, target, getEnemies(user), getAllies(user));
	}
	
	// Thrown weapon methods:
	
	public void resetWeaponsThrownDuringTurn(GameCharacter character) {
		weaponsThrownDuringTurn.put(character, new HashMap<>());
		for(InventorySlot slot : InventorySlot.allWeaponSlots) {
			weaponsThrownDuringTurn.get(character).put(slot, new HashMap<>());
		}
	}
	
	public Map<AbstractWeapon, Integer> getWeaponsThrownDuringTurn(GameCharacter user, InventorySlot slot) {
		return weaponsThrownDuringTurn.get(user).get(slot);
	}

	public void incrementWeaponsThrownDuringTurn(GameCharacter user, InventorySlot slot, AbstractWeapon weapon, int increment) {
		weaponsThrownDuringTurn.get(user).get(slot).putIfAbsent(weapon, 0);
		weaponsThrownDuringTurn.get(user).get(slot).put(weapon, weaponsThrownDuringTurn.get(user).get(slot).get(weapon)+increment);
	}
	
	public void resetWeaponsThrownDuringCombat(GameCharacter character) {
		weaponsThrownDuringCombat.put(character, new HashMap<>());
		for(InventorySlot slot : InventorySlot.allWeaponSlots) {
			weaponsThrownDuringCombat.get(character).put(slot, new HashMap<>());
		}
	}
	
	public Map<AbstractWeapon, Integer> getWeaponsThrownDuringCombat(GameCharacter user, InventorySlot slot) {
		return weaponsThrownDuringCombat.get(user).get(slot);
	}

	public void incrementWeaponsThrownDuringCombat(GameCharacter user, InventorySlot slot, AbstractWeapon weapon, int increment) {
		weaponsThrownDuringCombat.get(user).get(slot).putIfAbsent(weapon, 0);
		weaponsThrownDuringCombat.get(user).get(slot).put(weapon, weaponsThrownDuringCombat.get(user).get(slot).get(weapon)+increment);
	}

	public void resetThrownWeaponsDepleted(GameCharacter character) {
		thrownWeaponsDepleted.put(character, new HashMap<>());
		for(InventorySlot slot : InventorySlot.allWeaponSlots) {
			thrownWeaponsDepleted.get(character).put(slot, null);
		}
	}
	
	public AbstractWeaponType getThrownWeaponsDepleted(GameCharacter user, InventorySlot slot) {
		return thrownWeaponsDepleted.get(user).get(slot);
	}

	public void addThrownWeaponsDepleted(GameCharacter user, InventorySlot slot, AbstractWeaponType weapon) {
		thrownWeaponsDepleted.get(user).put(slot, weapon);
	}

	public void removeThrownWeaponsDepleted(GameCharacter user, InventorySlot slot) {
		thrownWeaponsDepleted.get(user).put(slot, null);
	}
	
	public String getPregnancyProtectionText(GameCharacter character) {
			return (character.isVisiblyPregnant()
					?UtilText.parse(character, "A powerful field of arcane energy is protecting [npc.namePos] pregnant belly, ensuring that no harm can come to [npc.her] unborn offspring.")
					:"");
	}

	public List<GameCharacter> getAllCombatants(boolean includePlayer) {
		List<GameCharacter> returnList = new ArrayList<>(allCombatants);
		if(includePlayer) {
			returnList.add(GameState.game.getPlayer());
		}
		return returnList;
	}
	
	public void addAlly(NPC ally) {
		if(!allies.contains(ally)) {
			allies.add(ally);
			allCombatants.add(ally);
			ally.resetMoveCooldowns();
			
			predictionContent.put(ally, new ArrayList<>());
			itemsToBeUsed.put(ally, new ArrayList<>());
			manaBurnStack.put(ally, new Stack<>());
			statusEffectsToApply.put(ally, new HashMap<>());
			combatContent.put(ally, new ArrayList<>());
			activeCombatants.add(ally);
			
			resetWeaponsThrownDuringTurn(ally);
			resetWeaponsThrownDuringCombat(ally);
			resetThrownWeaponsDepleted(ally);
			
			if(GameState.game.isInCombat()) {
				List<GameCharacter> npcAllies = getAllies(ally);
				List<GameCharacter> npcEnemies = getEnemies(ally);
				
				applyNewTurnShielding(ally);
				ally.setRemainingAP(ally.getMaxAP(), npcEnemies, npcAllies);
				
				npcAllies.removeIf((c)->isCombatantDefeated(c));
				npcEnemies.removeIf((c)->isCombatantDefeated(c));
				
				// Figures out new moves for NPCs:
				ally.selectMoves(npcEnemies, npcAllies);
				predictionContent.put(ally, ally.getMovesPredictionString(npcEnemies, npcAllies));
			}
		}
	}
	
	public void addEnemy(NPC enemy) {
		if(!enemies.contains(enemy)) {
			enemies.add(enemy);
			allCombatants.add(enemy);
			enemy.resetMoveCooldowns();
			enemy.setFoughtPlayerCount(enemy.getFoughtPlayerCount()+1);
			
			predictionContent.put(enemy, new ArrayList<>());
			itemsToBeUsed.put(enemy, new ArrayList<>());
			manaBurnStack.put(enemy, new Stack<>());
			statusEffectsToApply.put(enemy, new HashMap<>());
			combatContent.put(enemy, new ArrayList<>());
			activeCombatants.add(enemy);
	
			resetWeaponsThrownDuringTurn(enemy);
			resetWeaponsThrownDuringCombat(enemy);
			resetThrownWeaponsDepleted(enemy);
			
			if(GameState.game.isInCombat()) {
				List<GameCharacter> npcAllies = getAllies(enemy);
				List<GameCharacter> npcEnemies = getEnemies(enemy);
				
				applyNewTurnShielding(enemy);
				enemy.setRemainingAP(enemy.getMaxAP(), npcEnemies, npcAllies);
				
				npcAllies.removeIf((c)->isCombatantDefeated(c));
				npcEnemies.removeIf((c)->isCombatantDefeated(c));
				
				// Figures out new moves for NPCs:
				enemy.selectMoves(npcEnemies, npcAllies);
				predictionContent.put(enemy, enemy.getMovesPredictionString(npcEnemies, npcAllies));
			}
		}
	}
	
	/**
	 * @return A list of this combatant's allies. <b>Does not include</b> the combatant themselves.
	 */
	public List<GameCharacter> getAllies(GameCharacter combatant) {
		List<GameCharacter> returnList = new ArrayList<>();
		
		if(combatant.isPlayer()) {
			returnList.addAll(allies);
			
		} else if(allies.contains(combatant)) {
			returnList.add(GameState.game.getPlayer());
			returnList.addAll(allies);
			
		} else {
			returnList.addAll(enemies);
		}
		
		returnList.remove(combatant);
		
		return returnList;
	}

	public List<GameCharacter> getEnemies(GameCharacter combatant) {
		List<GameCharacter> returnList = new ArrayList<>();
		
		if(combatant.isPlayer()) {
			returnList.addAll(enemies);
			
		} else if(allies.contains(combatant)) {
			returnList.addAll(enemies);
			
		} else {
			returnList.add(GameState.game.getPlayer());
			returnList.addAll(allies);
		}
		
		return returnList;
	}

	/**
	 * @param target The character whose party member will be returned.
	 * @return A random member of the target's party. WIll attempt to return a member that isn't the target, but if the target's party only contains them, will return the target. 
	 */
	public GameCharacter getRandomAlliedPartyMember(GameCharacter target) {
		List<GameCharacter> possibleTargets = new ArrayList<>();
		for(GameCharacter character : getAllies(target)) {
			possibleTargets.add(character);
		}
		if(possibleTargets.size() == 0) {
			return target;
		}
		return possibleTargets.get(Util.random.nextInt(possibleTargets.size()));
	}

	public int getTurn() {
		return turn;
	}

	public float getTotalDamageTaken(GameCharacter character) {
		totalDamageTaken.putIfAbsent(character, 0f);
		return totalDamageTaken.get(character);
	}

	public void setTotalDamageTaken(GameCharacter character, float damage) {
		totalDamageTaken.put(character, damage);
	}

	public void incrementTotalDamageTaken(GameCharacter character, float increment) {
		setTotalDamageTaken(character, getTotalDamageTaken(character) + increment);
	}

	public boolean isCharacterVictory(GameCharacter character) {
		if(getEnemies(character).contains(GameState.game.getPlayer())) {
			return !playerVictory;
		}
		return playerVictory;
	}
	
	/**
	 * @return true if the last combat that took place resulted in the player's victory.
	 */
	public boolean isPlayerVictory() {
		return playerVictory;
	}

	public void setupManaBurnStackForOutOfCombat(GameCharacter character) {
		manaBurnStack = new HashMap<>();
		manaBurnStack.put(character, new Stack<>());
	}
	
	public Map<GameCharacter, Stack<Float>> getManaBurnStack() {
		return manaBurnStack;
	}

	/**
	 * Set target to null to remove preferred targeting behaviour.
	 */
	public void setPreferredTarget(GameCharacter character, GameCharacter target) {
		if(target==null) {
			preferredTargets.remove(character);
		} else {
			preferredTargets.put(character, target);
		}
	}
	
	/**
	 * Will typically be null, unless a target has been manually set. If the preferred target is defeated, this will return null.
	 */
	public GameCharacter getPreferredTarget(GameCharacter character) {
		if(!preferredTargets.containsKey(character) || isCombatantDefeated(preferredTargets.get(character))) {
			return null;
		}
		return preferredTargets.get(character);
	}
	
	public void addStatusEffectToApply(GameCharacter target, AbstractStatusEffect effect, int duration) {
		statusEffectsToApply.get(target).put(effect, duration);
//		statusEffectsToApply.get(target).putIfAbsent(effect, 0);
//		
//		statusEffectsToApply.get(target).put(effect, statusEffectsToApply.get(target).get(effect)+duration);
	}

	public Map<GameCharacter, Map<AbstractStatusEffect, Integer>> getStatusEffectsToApply() {
		return statusEffectsToApply;
	}

	public DialogueNode getPlayerPostVictoryDialogue() {
		return playerPostVictoryDialogue;
	}

	public void setPlayerPostVictoryDialogue(DialogueNode playerPostVictoryDialogue) {
		this.playerPostVictoryDialogue = playerPostVictoryDialogue;
	}

	public DialogueNode getPlayerPostDefeatDialogue() {
		return playerPostDefeatDialogue;
	}

	public void setPlayerPostDefeatDialogue(DialogueNode playerPostDefeatDialogue) {
		this.playerPostDefeatDialogue = playerPostDefeatDialogue;
	}

	public boolean isSubmitBlocked() {
		return submitBlocked;
	}
}
