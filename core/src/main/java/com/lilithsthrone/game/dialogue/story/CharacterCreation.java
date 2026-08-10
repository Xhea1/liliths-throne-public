package com.lilithsthrone.game.dialogue.story;

import com.lilithsthrone.utils.storage.Storage;

import java.io.File;
import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lilithsthrone.game.Game;
import com.lilithsthrone.game.PropertyValue;
import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.PlayerCharacter;
import com.lilithsthrone.game.character.attributes.Attribute;
import com.lilithsthrone.game.character.body.coverings.BodyCoveringType;
import com.lilithsthrone.game.character.body.coverings.Covering;
import com.lilithsthrone.game.character.body.valueEnums.BodyHair;
import com.lilithsthrone.game.character.body.valueEnums.BreastShape;
import com.lilithsthrone.game.character.body.valueEnums.CupSize;
import com.lilithsthrone.game.character.body.valueEnums.Femininity;
import com.lilithsthrone.game.character.body.valueEnums.LabiaSize;
import com.lilithsthrone.game.character.body.valueEnums.PenisLength;
import com.lilithsthrone.game.character.effects.Perk;
import com.lilithsthrone.game.character.effects.StatusEffect;
import com.lilithsthrone.game.character.fetishes.FetishDesire;
import com.lilithsthrone.game.character.gender.Gender;
import com.lilithsthrone.game.character.markings.TattooCounterType;
import com.lilithsthrone.game.character.markings.TattooType;
import com.lilithsthrone.game.character.npc.dominion.Lilaya;
import com.lilithsthrone.game.character.npc.dominion.Rose;
import com.lilithsthrone.game.character.npc.misc.PrologueFemale;
import com.lilithsthrone.game.character.npc.misc.PrologueMale;
import com.lilithsthrone.game.character.persona.Name;
import com.lilithsthrone.game.character.persona.NameTriplet;
import com.lilithsthrone.game.character.persona.Occupation;
import com.lilithsthrone.game.character.persona.SexualOrientation;
import com.lilithsthrone.game.character.quests.Quest;
import com.lilithsthrone.game.character.quests.QuestLine;
import com.lilithsthrone.game.character.quests.QuestType;
import com.lilithsthrone.game.character.race.Race;
import com.lilithsthrone.game.character.race.RaceStage;
import com.lilithsthrone.game.character.race.RacialBody;
import com.lilithsthrone.game.character.race.Subspecies;
import com.lilithsthrone.game.combat.DamageType;
import com.lilithsthrone.game.combat.spells.Spell;
import com.lilithsthrone.game.combat.spells.SpellSchool;
import com.lilithsthrone.game.dialogue.DialogueNode;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.responses.ResponseEffectsOnly;
import com.lilithsthrone.game.dialogue.utils.BodyChanging;
import com.lilithsthrone.game.dialogue.utils.CharacterModificationUtils;
import com.lilithsthrone.game.dialogue.utils.CosmeticsDialogue;
import com.lilithsthrone.game.dialogue.utils.InventoryDialogue;
import com.lilithsthrone.game.dialogue.utils.InventoryInteraction;
import com.lilithsthrone.game.dialogue.utils.OptionsDialogue;
import com.lilithsthrone.game.dialogue.utils.UtilText;
import com.lilithsthrone.game.inventory.InventorySlot;
import com.lilithsthrone.game.inventory.clothing.AbstractClothing;
import com.lilithsthrone.game.inventory.clothing.AbstractClothingType;
import com.lilithsthrone.game.inventory.clothing.ClothingType;
import com.lilithsthrone.game.inventory.item.AbstractItem;
import com.lilithsthrone.game.inventory.item.ItemType;
import com.lilithsthrone.game.inventory.weapon.WeaponType;
import com.lilithsthrone.game.sex.SexAreaOrifice;
import com.lilithsthrone.game.sex.SexAreaPenetration;
import com.lilithsthrone.game.sex.SexParticipantType;
import com.lilithsthrone.game.sex.SexType;
import com.lilithsthrone.main.GameState;
import com.lilithsthrone.utils.Util;
import com.lilithsthrone.utils.colours.Colour;
import com.lilithsthrone.utils.colours.PresetColour;
import com.lilithsthrone.world.Weather;
import com.lilithsthrone.world.WorldType;
import com.lilithsthrone.world.places.PlaceType;

/**
 * @since 0.1.0
 * @version 0.4
 * @author Innoxia
 */
public class CharacterCreation {

	public static final int TIME_TO_NAME = 120;
	public static final int TIME_TO_APPEARANCE = 60;
	public static final int TIME_TO_CLOTHING = 30;
	public static final int TIME_TO_BACKGROUND = 150;
	public static final int TIME_TO_JOB = 150;
	public static final int TIME_TO_SEX_EXPERIENCE = 150;
	public static final int TIME_TO_FINAL_CHECK = 150;

	public static SpellSchool getStartingTomeSpellSchool() {
		if(GameState.game.getPlayer().getBirthMonth().getValue() % 4 == 1) {
			return SpellSchool.EARTH;
		} else if(GameState.game.getPlayer().getBirthMonth().getValue() % 4 == 2) {
			return SpellSchool.AIR;
		} else if(GameState.game.getPlayer().getBirthMonth().getValue()  % 4 == 3) {
			return SpellSchool.WATER;
		}
		return SpellSchool.FIRE;
	}
	
	public static SpellSchool getStartingDemonstoneSpellSchool() {
		if(GameState.game.getPlayer().getBirthMonth().getValue() % 4 == 2) {
			return SpellSchool.EARTH;
		} else if(GameState.game.getPlayer().getBirthMonth().getValue() % 4 == 3) {
			return SpellSchool.AIR;
		} else if(GameState.game.getPlayer().getBirthMonth().getValue()  % 4 == 0) {
			return SpellSchool.WATER;
		}
		return SpellSchool.FIRE;
	}

	public static final DialogueNode CHARACTER_CREATION_START = new DialogueNode("Disclaimer", "", true) {

		@Override
		public String getContent() {
			return GameState.disclaimer;
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Agree", "You agree that you are the legal age to view pornographic material, and consent to being exposed to graphic content.", ALPHA_MESSAGE);
			} else {
				return null;
			}
		}
	};

	public static final DialogueNode ALPHA_MESSAGE = new DialogueNode("", "", true) {
		
		@Override
		public String getLabel() {
			return "Version " + GameState.VERSION_NUMBER + " | <b style='color:" + PresetColour.BASE_YELLOW_LIGHT.toWebHexString() + ";'>"+GameState.VERSION_DESCRIPTION+"</b>";
		}
		
		@Override
		public String getContent() {
			return GameState.getPatchNotes();
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Start", "Proceed to character creation.", CHOOSE_APPEARANCE){
					@Override
					public void effects() {
						GameState.game.clearTextStartStringBuilder();
						GameState.game.clearTextEndStringBuilder();
						GameState.getProperties().setValue(PropertyValue.newWeaponDiscovered, false);
						GameState.getProperties().setValue(PropertyValue.newClothingDiscovered, false);
						GameState.getProperties().setValue(PropertyValue.newItemDiscovered, false);
						GameState.game.getPlayer().calculateStatusEffects(0);
						GameState.game.getPlayer().setHealthPercentage(1);
						GameState.game.getPlayer().setManaPercentage(1);
						getDressed();
						resetBodyAppearance();
						
						GameState.game.setRenderAttributesSection(true);
						GameState.game.getPlayer().setName(new NameTriplet("Unknown", "Unknown", "Unknown"));
						GameState.game.getPlayer().setSurname("");
						BodyChanging.setTarget(GameState.game.getPlayer());
					}
				};
				
			} else if (index == 2) {
				return new Response("Start (Import)", "Import a character from a previous version to use on game start.", IMPORT_CHOOSE) {
					@Override
					public void effects() {
						GameState.game.getPlayerCell().resetInventory();
					}
				};
			}
			return null;
		};
	};

	public static void resetBodyAppearance() {
		GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.HUMAN, PresetColour.SKIN_LIGHT), true);
		GameState.game.getNpc(Lilaya.class).setSkinCovering(new Covering(BodyCoveringType.HUMAN, GameState.game.getPlayer().getCovering(BodyCoveringType.HUMAN).getPrimaryColour()), true);
		GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.EYE_HUMAN, PresetColour.EYE_BROWN), true);
		GameState.game.getPlayer().setHairCovering(new Covering(BodyCoveringType.HAIR_HUMAN, PresetColour.COVERING_BROWN), true);
		GameState.game.getPlayer().setBreastShape(BreastShape.ROUND);
		GameState.game.getPlayer().setVaginaLabiaSize(LabiaSize.TWO_AVERAGE.getValue());
		
		GameState.game.getPlayer().setFacialHair(BodyHair.ZERO_NONE);
		resetFemininityAppearance();
	}
	
	public static void resetFemininityAppearance() {
		switch(GameState.game.getPlayer().getFemininity()) {
			case MASCULINE_STRONG:
				GameState.game.getPlayer().setUnderarmHair(BodyHair.FOUR_NATURAL);
				GameState.game.getPlayer().setAssHair(BodyHair.FOUR_NATURAL);
				GameState.game.getPlayer().setPubicHair(BodyHair.FOUR_NATURAL);
				GameState.game.getPlayer().setPenisSize(PenisLength.TWO_AVERAGE.getMedianValue()+3);
				break;
			case MASCULINE:
				GameState.game.getPlayer().setUnderarmHair(BodyHair.FOUR_NATURAL);
				GameState.game.getPlayer().setAssHair(BodyHair.FOUR_NATURAL);
				GameState.game.getPlayer().setPubicHair(BodyHair.FOUR_NATURAL);
				GameState.game.getPlayer().setPenisSize(PenisLength.TWO_AVERAGE);
				break;
			case ANDROGYNOUS:
				GameState.game.getPlayer().setUnderarmHair(BodyHair.ZERO_NONE);
				GameState.game.getPlayer().setAssHair(BodyHair.TWO_MANICURED);
				GameState.game.getPlayer().setPubicHair(BodyHair.FOUR_NATURAL);
				if(GameState.game.getPlayer().hasPenis()) {
					GameState.game.getPlayer().setPenisSize(PenisLength.ONE_TINY);
				}
				if(GameState.game.getPlayer().hasVagina()) {
					GameState.game.getPlayer().setBreastSize(CupSize.A);
				}
				break;
			case FEMININE:
				GameState.game.getPlayer().setUnderarmHair(BodyHair.ZERO_NONE);
				GameState.game.getPlayer().setAssHair(BodyHair.TWO_MANICURED);
				GameState.game.getPlayer().setPubicHair(BodyHair.THREE_TRIMMED);
				GameState.game.getPlayer().setBreastSize(CupSize.C);
				break;
			case FEMININE_STRONG:
				GameState.game.getPlayer().setUnderarmHair(BodyHair.ZERO_NONE);
				GameState.game.getPlayer().setAssHair(BodyHair.ZERO_NONE);
				GameState.game.getPlayer().setPubicHair(BodyHair.ZERO_NONE);
				GameState.game.getPlayer().setBreastSize(CupSize.DD);
				break;
		}
	}
	
	public static void setGenderFemale() {
		Femininity fem = Femininity.FEMININE;
		switch(BodyChanging.getTarget().getFemininity()) {
			case ANDROGYNOUS:
				fem = Femininity.ANDROGYNOUS;
				break;
			case MASCULINE:
				fem = Femininity.FEMININE;
				break;
			case MASCULINE_STRONG:
				fem = Femininity.FEMININE_STRONG;
				break;
			default:
				break;
		}
		BodyChanging.getTarget().setBody(Gender.F_V_B_FEMALE, RacialBody.HUMAN, RaceStage.HUMAN, false);
		BodyChanging.getTarget().setFemininity(fem.getMedianFemininity());
		
		if(BodyChanging.getTarget().isPlayer()) {
			getDressed();
			CharacterCreation.resetBodyAppearance();
		}
	}
	
	public static void setGenderMale() {
		Femininity fem = Femininity.MASCULINE;
		switch(BodyChanging.getTarget().getFemininity()) {
			case ANDROGYNOUS:
				fem = Femininity.ANDROGYNOUS;
				break;
			case FEMININE:
				fem = Femininity.MASCULINE;
				break;
			case FEMININE_STRONG:
				fem = Femininity.MASCULINE_STRONG;
				break;
			default:
				break;
		}
		BodyChanging.getTarget().setBody(Gender.M_P_MALE, RacialBody.HUMAN, RaceStage.HUMAN, false);
		BodyChanging.getTarget().setFemininity(fem.getMedianFemininity());

		if(BodyChanging.getTarget().isPlayer()) {
			getDressed();
			CharacterCreation.resetBodyAppearance();
		}
	}
	
	private static void equipPiercings() {
		Colour colour1 = PresetColour.CLOTHING_BLACK_STEEL;
		Colour colour2 = PresetColour.CLOTHING_STEEL;

		if(GameState.game.getPlayer().getFemininity()==Femininity.FEMININE_STRONG) {
			colour1 = PresetColour.CLOTHING_PLATINUM;
			colour2 = PresetColour.CLOTHING_GOLD;
		} else if(GameState.game.getPlayer().isFeminine()) {
			colour1 = PresetColour.CLOTHING_SILVER;
			colour2 = PresetColour.CLOTHING_SILVER;
		}
		
		Map<InventorySlot, AbstractClothing> pendingPiercings = new HashMap<>();
		
		// Ear piercings:
		if(GameState.game.getPlayer().isPiercedEar()) {
			if(GameState.game.getPlayer().getFemininity()==Femininity.FEMININE_STRONG) {
				pendingPiercings.put(InventorySlot.PIERCING_EAR, GameState.game.getItemGen().generateClothing("innoxia_piercing_ear_chain_dangle", colour1, false));
			} else if(GameState.game.getPlayer().getFemininity()==Femininity.FEMININE) {
				pendingPiercings.put(InventorySlot.PIERCING_EAR, GameState.game.getItemGen().generateClothing("innoxia_piercing_ear_ring", colour1, false));
			} else {
				pendingPiercings.put(InventorySlot.PIERCING_EAR, GameState.game.getItemGen().generateClothing("innoxia_piercing_ear_ball_studs", colour1, false));
			}
		}
		
		// Lip piercings:
		if(GameState.game.getPlayer().isPiercedLip()) {
			pendingPiercings.put(InventorySlot.PIERCING_LIP, GameState.game.getItemGen().generateClothing("innoxia_piercing_lip_double_ring", colour1, false));
		}
		
		// Navel piercings:
		if(GameState.game.getPlayer().isPiercedNavel()) {
			if(GameState.game.getPlayer().isFeminine()) {
				pendingPiercings.put(InventorySlot.PIERCING_STOMACH, GameState.game.getItemGen().generateClothing("innoxia_piercing_gemstone_barbell", colour2, false));
			} else {
				pendingPiercings.put(InventorySlot.PIERCING_STOMACH, GameState.game.getItemGen().generateClothing("innoxia_piercing_ringed_barbell", colour2, false));
			}
		}

		// Nipples piercings:
		if(GameState.game.getPlayer().isPiercedNipple()) {
			pendingPiercings.put(InventorySlot.PIERCING_NIPPLE, GameState.game.getItemGen().generateClothing("innoxia_piercing_basic_barbell_pair", colour2, false));
		}

		// Nose piercings:
		if(GameState.game.getPlayer().isPiercedNose()) {
			if(GameState.game.getPlayer().isFeminine()) {
				pendingPiercings.put(InventorySlot.PIERCING_NOSE, GameState.game.getItemGen().generateClothing("innoxia_piercing_nose_ring", colour1, false));
			} else {
				pendingPiercings.put(InventorySlot.PIERCING_NOSE, GameState.game.getItemGen().generateClothing("innoxia_piercing_nose_ball_stud", colour1, false));
			}
		}

		// Penis piercings:
		if(GameState.game.getPlayer().hasPenis() && GameState.game.getPlayer().isPiercedPenis()) {
			pendingPiercings.put(InventorySlot.PIERCING_PENIS, GameState.game.getItemGen().generateClothing("innoxia_piercing_penis_ring", colour2, false));
		}

		// Tongue piercings:
		if(GameState.game.getPlayer().isPiercedTongue()) {
			pendingPiercings.put(InventorySlot.PIERCING_TONGUE, GameState.game.getItemGen().generateClothing("innoxia_piercing_basic_barbell", colour1, false));
		}

		// Vagina piercings:
		if(GameState.game.getPlayer().hasVagina() && GameState.game.getPlayer().isPiercedVagina()) {
			pendingPiercings.put(InventorySlot.PIERCING_VAGINA, GameState.game.getItemGen().generateClothing("innoxia_piercing_ringed_barbell", colour2, false));
		}
		
		for(InventorySlot slot : InventorySlot.getPiercingSlots()) {
			AbstractClothing clothingCurrentlyInSlot = GameState.game.getPlayer().getClothingInSlot(slot);
			
			if(pendingPiercings.get(slot)!=null){
				if(clothingCurrentlyInSlot==null || clothingCurrentlyInSlot.getClothingType()!=pendingPiercings.get(slot).getClothingType()) {
					GameState.game.getPlayer().equipClothingFromNowhere(pendingPiercings.get(slot), slot, true, GameState.game.getPlayer());
				}
				
			} else if(clothingCurrentlyInSlot!=null){
				GameState.game.getPlayer().unequipClothingIntoVoid(slot, true, GameState.game.getPlayer());
			}
		}
	}
	
	public static void getDressed() {
		getDressed(GameState.game.getPlayer(), true);
	}
	
	public static void getDressed(GameCharacter character, boolean spawnClothingOnFloor) {
		character.resetInventory(false);
		GameState.game.getPlayerCell().resetInventory();
		
		equipPiercings();
		
		switch(character.getFemininity()) {
			case MASCULINE_STRONG:
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_groin_briefs", PresetColour.CLOTHING_WHITE, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_torso_long_sleeved_shirt", PresetColour.CLOTHING_WHITE, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_neck_tie", PresetColour.CLOTHING_RED, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_torsoOver_suit_jacket", PresetColour.CLOTHING_BLACK, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_leg_trousers", PresetColour.CLOTHING_BLACK, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_sock_socks", PresetColour.CLOTHING_BLACK, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_foot_mens_smart_shoes", PresetColour.CLOTHING_BLACK, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_finger_ring", PresetColour.CLOTHING_GOLD, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing(ClothingType.WRIST_MENS_WATCH, PresetColour.CLOTHING_GOLD, false), true, character);
				
				if(spawnClothingOnFloor) {
					spawnClothingInArea();
				}
				break;
				
			case MASCULINE:
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_groin_boxers", PresetColour.CLOTHING_BLACK, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_torso_short_sleeved_shirt", PresetColour.CLOTHING_WHITE, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_leg_trousers", PresetColour.CLOTHING_BLACK, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_sock_socks", PresetColour.CLOTHING_BLACK, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_foot_mens_smart_shoes", PresetColour.CLOTHING_BLACK, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_finger_ring", PresetColour.CLOTHING_SILVER, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing(ClothingType.WRIST_MENS_WATCH, PresetColour.CLOTHING_SILVER, false), true, character);

				if(spawnClothingOnFloor) {
					spawnClothingInArea();
				}
				break;
				
			case ANDROGYNOUS:
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_groin_panties", PresetColour.CLOTHING_WHITE, false), true, character);
				if(character.getBreastRawSizeValue()!=0) {
					character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_chest_croptop_bra", PresetColour.CLOTHING_WHITE, false), true, character);
				} else {
					GameState.game.getPlayerCell().getInventory().addClothing(GameState.game.getItemGen().generateClothing("innoxia_chest_croptop_bra", PresetColour.CLOTHING_WHITE, false));
				}
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_torso_short_sleeved_shirt", PresetColour.CLOTHING_WHITE, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_leg_jeans", PresetColour.CLOTHING_BLUE_GREY, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_sock_socks", PresetColour.CLOTHING_WHITE, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_foot_low_top_skater_shoes", PresetColour.CLOTHING_RED, false), true, character);
				
				if(spawnClothingOnFloor) {
					spawnClothingInArea();
				}
				break;
				
			case FEMININE:
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_groin_panties", PresetColour.CLOTHING_WHITE, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_chest_plunge_bra", PresetColour.CLOTHING_WHITE, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_torso_skater_dress", PresetColour.CLOTHING_BLACK, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_sock_trainer_socks", PresetColour.CLOTHING_WHITE, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_foot_heels", PresetColour.CLOTHING_BLACK, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing(ClothingType.WRIST_WOMENS_WATCH, PresetColour.CLOTHING_PINK_LIGHT, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_finger_ring", PresetColour.CLOTHING_SILVER, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_neck_heart_necklace", PresetColour.CLOTHING_SILVER, false), true, character);

				if(spawnClothingOnFloor) {
					spawnClothingInArea();
				}
				break;
				
			case FEMININE_STRONG:
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_groin_thong", PresetColour.CLOTHING_BLACK, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_chest_plunge_bra", PresetColour.CLOTHING_BLACK, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_torso_slip_dress", PresetColour.CLOTHING_RED_BURGUNDY, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_sock_pantyhose", PresetColour.CLOTHING_BLACK, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_foot_stiletto_heels", PresetColour.CLOTHING_RED_BURGUNDY, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing(ClothingType.WRIST_WOMENS_WATCH, PresetColour.CLOTHING_BLACK, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_finger_ring", PresetColour.CLOTHING_GOLD, false), true, character);
				character.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_neck_heart_necklace", PresetColour.CLOTHING_GOLD, false), true, character);

				if(spawnClothingOnFloor) {
					spawnClothingInArea();
				}
				break;
			default:
				break;
		}
		
		if(character.isPlayer()
				&& ((character.getName(false).equals("James") || character.getName(false).equals("Jane") || character.getName(false).equals("Tracy")) && character.getSurname().equals("Bond"))) {
			character.equipMainWeaponFromNowhere(GameState.game.getItemGen().generateWeapon(WeaponType.getWeaponTypeFromId("innoxia_western_kkp_western_kkp")));
		}
	}
	
	private static void generateClothingOnFloor(String clothingType, Colour colour) {
		generateClothingOnFloor(ClothingType.getClothingTypeFromId(clothingType), colour, null, null);
	}
	
	private static void generateClothingOnFloor(AbstractClothingType clothingType, Colour colour) {
		generateClothingOnFloor(clothingType, colour, null, null);
	}
	
	private static void generateClothingOnFloor(AbstractClothingType clothingType, Colour colour, Colour colour2, Colour colour3) {
		for(AbstractClothing clothing : GameState.game.getPlayer().getClothingCurrentlyEquipped()) {
			if(clothing.getClothingType()==clothingType) {
				return;
			}
		}
		GameState.game.getPlayerCell().getInventory().addClothing(GameState.game.getItemGen().generateClothing(clothingType, colour, colour2, colour3, false));
	}
	
	private static void spawnClothingInArea() {
		switch(GameState.game.getPlayer().getFemininity()) {
			case MASCULINE:
			case MASCULINE_STRONG:
				generateClothingOnFloor("bloom_wasp609_rainCoat_rain_coat", PresetColour.CLOTHING_BLUE_NAVY);
				generateClothingOnFloor(ClothingType.getClothingTypeFromId("innoxia_foot_trainers"), PresetColour.CLOTHING_WHITE, PresetColour.CLOTHING_BLUE_GREY, PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_foot_work_boots", PresetColour.CLOTHING_TAN);
				generateClothingOnFloor("innoxia_foot_low_top_skater_shoes", PresetColour.CLOTHING_RED);
				generateClothingOnFloor("innoxia_sock_socks", PresetColour.CLOTHING_WHITE);
				generateClothingOnFloor("innoxia_leg_cargo_trousers", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_leg_jeans", PresetColour.CLOTHING_BLUE_GREY);
				generateClothingOnFloor("innoxia_groin_boxers", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_eye_aviators", PresetColour.CLOTHING_BLACK_STEEL);
				generateClothingOnFloor("innoxia_eye_glasses", PresetColour.CLOTHING_BLACK_STEEL);
				generateClothingOnFloor("innoxia_hand_gloves", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_head_cap", PresetColour.CLOTHING_BLUE);
				generateClothingOnFloor("innoxia_neck_scarf", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_torsoOver_hoodie", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_torsoOver_ribbed_jumper", PresetColour.CLOTHING_GREY);
				generateClothingOnFloor("innoxia_torso_short_sleeved_shirt", PresetColour.CLOTHING_WHITE);
				generateClothingOnFloor("innoxia_torso_tshirt", PresetColour.CLOTHING_BLUE_LIGHT);
				generateClothingOnFloor("innoxia_groin_briefs", PresetColour.CLOTHING_WHITE);
				generateClothingOnFloor("innoxia_torso_long_sleeved_shirt", PresetColour.CLOTHING_WHITE);
				generateClothingOnFloor("innoxia_neck_tie", PresetColour.CLOTHING_RED);
				generateClothingOnFloor("innoxia_torsoOver_suit_jacket", PresetColour.CLOTHING_BLACK);
				break;
				
			case ANDROGYNOUS:
				generateClothingOnFloor("bloom_wasp609_rainCoat_rain_coat", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor(ClothingType.getClothingTypeFromId("innoxia_foot_trainers"), PresetColour.CLOTHING_WHITE, PresetColour.CLOTHING_PURPLE_DARK, PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_foot_heels", PresetColour.CLOTHING_BLACK);
				
				generateClothingOnFloor("innoxia_groin_thong", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_groin_lacy_panties", PresetColour.CLOTHING_RED);
				generateClothingOnFloor("innoxia_groin_briefs", PresetColour.CLOTHING_WHITE);

				generateClothingOnFloor("innoxia_chest_lacy_plunge_bra", PresetColour.CLOTHING_RED);

				generateClothingOnFloor("innoxia_sock_kneehigh_socks", PresetColour.CLOTHING_WHITE);

				generateClothingOnFloor("innoxia_leg_cargo_trousers", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_leg_trousers", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_leg_skirt", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_leg_yoga_pants", PresetColour.CLOTHING_PINK_LIGHT);
				generateClothingOnFloor("innoxia_leg_tight_jeans", PresetColour.CLOTHING_BLUE_NAVY);
				generateClothingOnFloor("innoxia_leg_jeans", PresetColour.CLOTHING_BLUE_GREY);
				generateClothingOnFloor("innoxia_leg_distressed_jeans", PresetColour.CLOTHING_BLUE_GREY);

				generateClothingOnFloor("innoxia_neck_scarf", PresetColour.CLOTHING_RED);
				
				generateClothingOnFloor("innoxia_head_cap", PresetColour.CLOTHING_BLUE);
				
				generateClothingOnFloor("innoxia_stomach_underbust_corset", PresetColour.CLOTHING_BLACK);

				generateClothingOnFloor("innoxia_torso_tshirt", PresetColour.CLOTHING_BLUE_LIGHT);
				generateClothingOnFloor("innoxia_torso_blouse", PresetColour.CLOTHING_BLUE_LIGHT);
				generateClothingOnFloor("innoxia_torso_cami_straps", PresetColour.CLOTHING_GREEN);
				
				generateClothingOnFloor("innoxia_torsoOver_hoodie", PresetColour.CLOTHING_PINK_LIGHT);
				generateClothingOnFloor("innoxia_torsoOver_open_front_cardigan", PresetColour.CLOTHING_BLACK);

				generateClothingOnFloor("innoxia_finger_ring", PresetColour.CLOTHING_SILVER);
				generateClothingOnFloor("innoxia_neck_heart_necklace", PresetColour.CLOTHING_SILVER);
				generateClothingOnFloor("innoxia_wrist_bangle", PresetColour.CLOTHING_SILVER);
				generateClothingOnFloor("innoxia_ankle_anklet", PresetColour.CLOTHING_SILVER);
				
				generateClothingOnFloor("innoxia_eye_glasses", PresetColour.CLOTHING_BLACK_STEEL);
				break;
				
			case FEMININE:
			case FEMININE_STRONG:
				generateClothingOnFloor("bloom_wasp609_rainCoat_rain_coat", PresetColour.CLOTHING_PURPLE_DARK);
				generateClothingOnFloor("innoxia_torsoOver_womens_winter_coat", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_sock_stockings", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor(ClothingType.HIPS_SUSPENDER_BELT, PresetColour.CLOTHING_BLACK);
				
				generateClothingOnFloor("innoxia_groin_panties", PresetColour.CLOTHING_WHITE);
				generateClothingOnFloor("innoxia_groin_thong", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_groin_lacy_panties", PresetColour.CLOTHING_RED);
				generateClothingOnFloor("innoxia_groin_vstring", PresetColour.CLOTHING_BLACK);

				generateClothingOnFloor("innoxia_chest_lacy_plunge_bra", PresetColour.CLOTHING_RED);
				generateClothingOnFloor("innoxia_chest_fullcup_bra", PresetColour.CLOTHING_BLACK);

				generateClothingOnFloor("innoxia_sock_pantyhose", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_sock_kneehigh_socks", PresetColour.CLOTHING_WHITE);
				generateClothingOnFloor("innoxia_sock_thighhigh_socks", PresetColour.CLOTHING_WHITE);

				generateClothingOnFloor("innoxia_eye_aviators", PresetColour.CLOTHING_ROSE_GOLD);
				generateClothingOnFloor("innoxia_eye_glasses", PresetColour.CLOTHING_BLACK_STEEL);

				generateClothingOnFloor("innoxia_foot_ankle_boots", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_foot_low_top_skater_shoes", PresetColour.CLOTHING_PINK_LIGHT);
				generateClothingOnFloor("innoxia_foot_thigh_high_boots", PresetColour.CLOTHING_TAN);
				generateClothingOnFloor("innoxia_foot_stiletto_heels", PresetColour.CLOTHING_RED);
				generateClothingOnFloor("innoxia_foot_heels", PresetColour.CLOTHING_BLACK);

				generateClothingOnFloor("innoxia_hand_elbow_length_gloves", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_head_headband", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor(ClothingType.getClothingTypeFromId("innoxia_head_headband_bow"), PresetColour.CLOTHING_PINK_LIGHT, PresetColour.CLOTHING_BLACK, PresetColour.CLOTHING_PINK);

				generateClothingOnFloor("innoxia_leg_hotpants", PresetColour.CLOTHING_WHITE);
				generateClothingOnFloor("innoxia_leg_mini_skirt", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_leg_skirt", PresetColour.CLOTHING_PINK);
				generateClothingOnFloor("innoxia_leg_yoga_pants", PresetColour.CLOTHING_PINK_LIGHT);
				generateClothingOnFloor("innoxia_leg_pencil_skirt", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_leg_tight_jeans", PresetColour.CLOTHING_BLUE_NAVY);
				generateClothingOnFloor("innoxia_leg_jeans", PresetColour.CLOTHING_BLUE_GREY);
				generateClothingOnFloor("innoxia_leg_distressed_jeans", PresetColour.CLOTHING_BLUE_GREY);
				
				generateClothingOnFloor("innoxia_neck_scarf", PresetColour.CLOTHING_RED);
				
				generateClothingOnFloor("innoxia_stomach_underbust_corset", PresetColour.CLOTHING_BLACK);

				generateClothingOnFloor(ClothingType.getClothingTypeFromId("innoxia_torso_feminine_short_sleeve_shirt"), PresetColour.CLOTHING_BLUE_LIGHT);
				generateClothingOnFloor("innoxia_torso_blouse", PresetColour.CLOTHING_BLUE_LIGHT);
				generateClothingOnFloor("innoxia_torso_cami_straps", PresetColour.CLOTHING_GREEN);
				generateClothingOnFloor("innoxia_torso_long_sleeve_dress", PresetColour.CLOTHING_BLACK);
				generateClothingOnFloor("innoxia_torso_short_croptop", PresetColour.CLOTHING_PINK);
				generateClothingOnFloor("innoxia_torso_virgin_killer_sweater", PresetColour.CLOTHING_WHITE);
				generateClothingOnFloor("innoxia_torso_slip_dress", PresetColour.CLOTHING_RED);
				generateClothingOnFloor("innoxia_torso_skater_dress", PresetColour.CLOTHING_BLACK);
				
				generateClothingOnFloor("innoxia_torsoOver_open_front_cardigan", PresetColour.CLOTHING_BLACK);
				
				generateClothingOnFloor("innoxia_wrist_bangle", PresetColour.CLOTHING_GOLD);
				generateClothingOnFloor("innoxia_ankle_anklet", PresetColour.CLOTHING_GOLD);
				break;
		}
	}
	
	public static final DialogueNode CHOOSE_APPEARANCE = new DialogueNode("A Night Out", "", true) {
		
		@Override
		public String getHeaderContent() {
			return "<p>"
						+ "By the time the taxi finally pulls up to the British Museum, you're already almost five minutes late."
						+ " The whole reason you're visiting London is to attend your aunt Lily's opening evening for her new exhibition,"
							+ " and as you hurriedly pay the driver his fare and step out of the car, you hope that she hasn't started her speech yet."
					+ "</p>"
					+ "<p>"
						+ "The street lights flicker into life as you rush over to the entrance, illuminating your surroundings with a dull orange glow."
						+ " It only takes a moment before you're standing at the museum's front doors, where, much to your dismay, you see that a small queue has formed."
						+ " Having no choice but to step in line and wait your turn, you briefly glance over at the large glass windows of the building's modern facade to see your blurry reflection in the glass..."
					+ "</p>"
					+ "<br/>"
					
					+ CharacterModificationUtils.getStartDateDiv()
					
					+ "<div class='cosmetics-container' style='background:transparent;'>"
					
						+ CharacterModificationUtils.getGenderChoiceDiv()
						
						+ CharacterModificationUtils.getFemininityChoiceDiv()
						
						+ "<div class='container-full-width' style='text-align:center;'>"
							+ "You will be referred to as <span style='color:"+GameState.game.getPlayer().getGender().getColour().toWebHexString()+";'>"
								+UtilText.generateSingularDeterminer(GameState.game.getPlayer().getGender().getName())+ " " + GameState.game.getPlayer().getGender().getName()+"</span>.<br/>"
							+ "<i>You can change all gender names in the options menu.</i>"
						+ "</div>"

						+ CharacterModificationUtils.getBirthdayChoiceDiv()
						
						+ CharacterModificationUtils.getOrientationChoiceDiv()
						
						+ CharacterModificationUtils.getPersonalityChoiceDiv(false)
						
					+"</div>";
		}
		
		@Override
		public String getContent() {
			return "";
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Continue", "Wait your turn, and hope that the event hasn't started yet.", CHOOSE_NAME) {
					@Override
					public int getSecondsPassed() {
						return TIME_TO_NAME;
					}
					@Override
					public void effects() {
//						getDressed();
					}
				};
			}
//			else if (index == 0) {
//				return new Response("Back", "Return to the main menu.", OptionsDialogue.MENU);
//			}
			return null;
		}
	};
	
	public static final DialogueNode CHOOSE_NAME = new DialogueNode("A Night Out", "", true) {

		boolean unsuitableName = false, unsuitableSurname = false;
		
		@Override
		public String getHeaderContent() {
			return "<p>"
						+ "[npcMale.speech("+(GameState.game.getPlayer().isFeminine()?"Miss":"Sir")+",)]"
						+ " the doorman calls out to you, evidently having finished with the other people in the queue,"
						+ " [npcMale.speech(do you have an invitation?)]"
					+ "</p>"
					+ "<p>"
						+ "You turn away from the glass and step forwards, smiling."
						+ " [pc.speech(Yes, I have it right here... erm... hold on...)]"
					+ "</p>"
					+ "<p>"
						+ "Reaching into your "+(GameState.game.getPlayer().isFeminine()?"purse":"pocket")+", you feel your heart start to race as you discover that the invitation isn't in there."
						+ " [pc.speech(No, no, no! I must have left it in the taxi!)]"
					+ "</p>"
					+ "<p>"
						+ "[npcMale.speech(Well, don't worry,)]"
						+ " the man replies,"
						+ " [npcMale.speech(if you give me your name, I can check to make sure that you're on the list.)]"
					+ "</p>"
					+ "<p>"
						+ "Breathing a sigh of relief, you tell the man your name..."
					+ "</p>"
					+"<br/>"
					+ "<div class='container-full-width' style='text-align:center;'>"
						+ "<div style='position:relative; display:inline-block; padding-bottom:0; margin 0 auto; vertical-align:middle; width:100%; text-align:center;'>"
							+ "<i>"
								+ "Your first name can be set as three values; your masculine name, androgynous name, and feminine name."
								+ " Your name will automatically switch to the one which corresponds to your body femininity."
							+ "</i>"
							+ "<br/>"
							+ "<p style='display:inline-block; padding:0; margin:0; height:32px; line-height:32px; width:100px;'>First name: </p>"
							+ "</form style='display:inline-block; padding:0; margin:0; text-align:center;'>"
									+ "<input type='text' id='nameMasculineInput' style=' color:"+PresetColour.MASCULINE.toWebHexString()+";' value='"+ UtilText.parseForHTMLDisplay(GameState.game.getPlayer().getNameTriplet().getMasculine())+ "'>"
									
							+ "</form style='display:inline-block; padding:0; margin:0; text-align:center;'>"
								+ "<input type='text' id='nameAndrogynousInput' style=' color:"+PresetColour.ANDROGYNOUS.toWebHexString()+";' value='"+ UtilText.parseForHTMLDisplay(GameState.game.getPlayer().getNameTriplet().getAndrogynous())+ "'>"
								
							+ "</form style='display:inline-block; padding:0; margin:0; text-align:center;'>"
								+ "<input type='text' id='nameFeminineInput' style=' color:"+PresetColour.FEMININE.toWebHexString()+";' value='"+ UtilText.parseForHTMLDisplay(GameState.game.getPlayer().getNameTriplet().getFeminine())+ "'>"
							
							+ "<br/>"
							+ "<p style='display:inline-block; padding:0; margin:0; height:32px; line-height:32px; width:100px;'>Surname: </p>"
							+ "<form style='display:inline-block; padding:0; margin:0; text-align:center;'><input type='text' id='surnameInput' value='"+ UtilText.parseForHTMLDisplay(GameState.game.getPlayer().getSurname())+ "'></form>"
						+ "</div>"
						+ "<br/>"
						+ "<i>Your name must be between 2 and 32 characters long. You cannot use the square bracket characters or full stops. (Surname may be left blank.)</i>"
						+ (unsuitableName ? "<p style='text-align:center;padding-top:0;'><b style=' color:"+ PresetColour.GENERIC_BAD.toWebHexString()+ ";'>Invalid name.</b></p>" : "")
						+ (unsuitableSurname ? "<p style='text-align:center;padding-top:0;'><b style=' color:"+ PresetColour.GENERIC_BAD.toWebHexString()+ ";'>Invalid Surname.</b></p>" : "")
					+ "</div>"
					
					+ "<p id='hiddenFieldName' style='display:none;'></p>"
					+ "<p id='hiddenFieldSurname' style='display:none;'></p>";
		}
		
		@Override
		public String getContent() {
			return "";
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new ResponseEffectsOnly("Continue", "Use this name and continue to the next stage of the character creation screen."){
					@Override
					public int getSecondsPassed() {
						if (unsuitableName || unsuitableSurname)  {
							return super.getSecondsPassed();
						}
						return TIME_TO_APPEARANCE;
					}
					@Override
					public void effects() {
						List<String> fieldsList = Util.newArrayListOfValues("nameMasculineInput", "nameAndrogynousInput", "nameFeminineInput");
						List<String> namesList = new ArrayList<>();
						for(String s : fieldsList) {
							GameState.getRenderSink().executeScript("document.getElementById('hiddenFieldName').innerHTML=document.getElementById('"+s+"').value;");
							if(GameState.getRenderSink().getDocument()!=null) {
								if (GameState.getRenderSink().getDocument().getElementById("hiddenFieldName").getTextContent().length() < 2
										|| GameState.getRenderSink().getDocument().getElementById("hiddenFieldName").getTextContent().length() > 32
										|| !GameState.getRenderSink().getDocument().getElementById("hiddenFieldName").getTextContent().matches("[^\\[\\]\\.]+")) {
									unsuitableName = true;
								} else {
									unsuitableName = false;
									namesList.add(GameState.getRenderSink().getDocument().getElementById("hiddenFieldName").getTextContent());
								}
							}
						}
						GameState.getRenderSink().executeScript("document.getElementById('hiddenFieldSurname').innerHTML=document.getElementById('surnameInput').value;");
						if(GameState.getRenderSink().getDocument()!=null) {
							if (GameState.getRenderSink().getDocument().getElementById("hiddenFieldSurname").getTextContent().length()>=1
									&& (GameState.getRenderSink().getDocument().getElementById("hiddenFieldSurname").getTextContent().length() > 32
											|| !GameState.getRenderSink().getDocument().getElementById("hiddenFieldSurname").getTextContent().matches("[^\\[\\]\\.]+"))) {
								unsuitableSurname = true;
							} else {
								unsuitableSurname = false;
							}
						}
						if (unsuitableName || unsuitableSurname)  {
							GameState.game.setContent(new Response("" ,"", CHOOSE_NAME));
							
						} else {
							GameState.game.getPlayer().setName(new NameTriplet(namesList.get(0), namesList.get(1), namesList.get(2)));
							GameState.game.getPlayer().setSurname(GameState.getRenderSink().getDocument().getElementById("hiddenFieldSurname").getTextContent());

							GameState.game.getPlayerCell().resetInventory();
							GameState.game.getPlayer().moveToAdjacentMatchingCellType(false, PlaceType.MUSEUM_LOBBY);
							GameState.game.setContent(new Response("" ,"", CHOOSE_ADVANCED_APPEARANCE));
							getDressed();
						}
					}
				};
				
			} else if (index == 2) {
				return new Response("Random", "Generate a random name based on your gender.", CHOOSE_NAME){
					@Override
					public void effects() {
						GameState.getRenderSink().executeScript("document.getElementById('hiddenFieldSurname').innerHTML=document.getElementById('surnameInput').value;");
						if(GameState.getRenderSink().getDocument()!=null) {
							if (GameState.getRenderSink().getDocument().getElementById("hiddenFieldSurname").getTextContent().length()>=1
									&& (GameState.getRenderSink().getDocument().getElementById("hiddenFieldSurname").getTextContent().length() > 16
											|| !GameState.getRenderSink().getDocument().getElementById("hiddenFieldSurname").getTextContent().matches("[^\\[\\]\\.]+")))
								unsuitableSurname = true;
							else {
								unsuitableSurname = false;
							}
						}
						if(!unsuitableSurname) {
							GameState.game.getPlayer().setSurname(GameState.getRenderSink().getDocument().getElementById("hiddenFieldSurname").getTextContent());
						}

						GameState.game.getPlayer().setName(Name.getRandomTriplet(Subspecies.HUMAN));
					}
				};
				
			} else if (index == 3) {
				return new Response("Random Surname", "Generate a random surname.", CHOOSE_NAME){
					@Override
					public void effects() {
						List<String> fieldsList = Util.newArrayListOfValues("nameMasculineInput", "nameAndrogynousInput", "nameFeminineInput");
						List<String> namesList = new ArrayList<>();
						for(String s : fieldsList) {
							GameState.getRenderSink().executeScript("document.getElementById('hiddenFieldName').innerHTML=document.getElementById('"+s+"').value;");
							if(GameState.getRenderSink().getDocument()!=null) {
								if (GameState.getRenderSink().getDocument().getElementById("hiddenFieldName").getTextContent().length() < 2
										|| GameState.getRenderSink().getDocument().getElementById("hiddenFieldName").getTextContent().length() > 16
										|| !GameState.getRenderSink().getDocument().getElementById("hiddenFieldName").getTextContent().matches("[^\\[\\]\\.]+"))
									unsuitableName = true;
								else {
									unsuitableName = false;
									namesList.add(GameState.getRenderSink().getDocument().getElementById("hiddenFieldName").getTextContent());
								}
							}
						}
						if(!unsuitableName) {
							GameState.game.getPlayer().setName(new NameTriplet(namesList.get(0), namesList.get(1), namesList.get(2)));
						}
						
						GameState.game.getPlayer().setSurname(Name.getSurname(GameState.game.getPlayer()));
					}
				};
				
			} else if (index == 0) {
				return new Response("Back", "Return to gender selection.", CHOOSE_APPEARANCE) {
					@Override
					public int getSecondsPassed() {
						return -TIME_TO_NAME;
					}
				};
				
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode CHOOSE_ADVANCED_APPEARANCE = new DialogueNode("In the Museum", "", true) {
		
		@Override
		public String getHeaderContent() {
			return "<p>"
						+ "[pc.speech(It's "+(GameState.game.getPlayer().getSurname().length()!=0?"[pc.surname], [pc.name] [pc.surname]":"[pc.name]")+",)]"
						+ " you say, impatiently looking down at the man's clipboard as he scans through his list."
					+ "</p>"
					+ "<p>"
						+ "Finally, you see his finger trace over your name, and with a smile, he steps to one side and beckons you forwards."
						+ " [npcMale.speech(Have a good evening, "+(GameState.game.getPlayer().getSurname().length()!=0
								?(GameState.game.getPlayer().isFeminine()?"Miss":"Mr.")+" [pc.surname]"
								:(GameState.game.getPlayer().isFeminine()?"Miss":"Sir"))+".)]"
					+ "</p>"
					+ "<p>"
						+ "Thanking him, you hurry through the entranceway, and within moments, find yourself stepping into the museum's enormous central lobby."
						+ " Large banners have been hung from the upper floor's balconies; their bold font proudly declaring this to be the 'Akkadian Empire Exhibit: Opening Evening'."
						+ " On the far side of the grand hall, you see throngs of people surrounding a large stage, and you breathe a sigh of relief as you notice that it's currently empty."
					+ "</p>"
					+ "<p>"
						+ "[pc.thought(Phew... I made it in time after all...)]"
					+ "</p>"
					+ "<p>"
						+ "As Lily's opening speech seems to be running just as late as you are, you decide to step over to a nearby mirror to make sure that you're looking presentable..."
					+ "</p>"
					+ "<br/>"
					+ "<div class='container-full-width'>"
						+ "<h5 style='text-align:center;'>Appearance</h5>"
						+ GameState.game.getPlayer().getBodyDescription()
					+ "</div>"
					+ "<br/>"
					+ "<div class='container-full-width' style='text-align:center;'>"
						+ "<i>You can modify your appearance by entering each of the sub-menus below.</i>"
					+ "</div>";
		}
		
		@Override
		public String getContent() {
			return "";
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Continue",
						"Your clothes are a little messy after rushing here. Tidy yourself up before proceeding to the main stage.",
						InventoryDialogue.INVENTORY_MENU) {
					@Override
					public int getSecondsPassed() {
						return TIME_TO_CLOTHING;
					}
					@Override
					public void effects() {
						equipPiercings();
						InventoryDialogue.setBuyback(false);
						InventoryDialogue.setInventoryNPC(null);
						InventoryDialogue.setNPCInventoryInteraction(InventoryInteraction.CHARACTER_CREATION);
					}
				};
				
			} else if (index == 2) {
				return new Response("Core", "Enter the customisation menu for all of your body's core aspects.", CHOOSE_ADVANCED_APPEARANCE_CORE);
				
			} else if (index == 3) {
				return new Response("Face", "Enter the customisation menu for aspects related to your face.", CHOOSE_ADVANCED_APPEARANCE_FACE);
				
			} else if (index == 4) {
				return new Response("Hair", "Enter the customisation menu for your hair.", CHOOSE_ADVANCED_APPEARANCE_HAIR);
				
			} else if (index == 5) {
				return new Response("Breasts", "Enter the customisation menu for your breasts.", CHOOSE_ADVANCED_APPEARANCE_BREASTS);
				
			} else if (index == 6) {
				return new Response("Ass & Hips", "Enter the customisation menu for aspects related to your ass, hips, and anus.", CHOOSE_ADVANCED_APPEARANCE_ASS);
				
			} else if (index == 7) {
				return new Response((GameState.game.getPlayer().hasPenis()?"Penis":"Vagina"), "Enter the customisation menu for aspects related to your "+(GameState.game.getPlayer().hasPenis()?"penis":"vagina")+".", CHOOSE_ADVANCED_APPEARANCE_GENITALS);
				
			}  else if (index == 8) {
				return new Response("Makeup", "Enter the customisation menu for makeup.", CHOOSE_ADVANCED_APPEARANCE_COSMETICS);
				
			} else if (index == 9) {
				return new Response("Piercings", "Enter the customisation menu for body piercings.", CHOOSE_ADVANCED_APPEARANCE_PIERCINGS);
				
			} else if (index == 10) {
				return new Response("Tattoos", "Enter the customisation menu for tattoos.", CHOOSE_ADVANCED_APPEARANCE_TATTOOS);
				
			} else if (index == 11) {
				return new Response("Extra hair", "Enter the customisation menu for facial, pubic, and body hair.", CHOOSE_ADVANCED_APPEARANCE_BODY_HAIR);
				
			} else if (index == 0) {
				return new Response("Back", "Confirm your choices and return to the content preferences menu.", CHOOSE_NAME) {
					@Override
					public int getSecondsPassed() {
						return -TIME_TO_APPEARANCE;
					}
					@Override
					public void effects() {
						GameState.game.getPlayer().setLocation(WorldType.MUSEUM, PlaceType.MUSEUM_ENTRANCE, false);
					}
				};
				
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode CHOOSE_ADVANCED_APPEARANCE_CORE = new DialogueNode("Core Body Appearance", "", true) {
		
		@Override
		public String getHeaderContent() {
			return "<div class='container-full-width' style='text-align:center;'>"
						+ "<i>All of these options can be influenced later on in the game.</i>"
					+ "</div>"
						
					+ CharacterModificationUtils.getHeightChoiceDiv(true)
					
					+ CharacterModificationUtils.getKatesDivCoveringsNew(false, Race.HUMAN, BodyCoveringType.HUMAN, "Skin Colour", "The colour of the skin that's covering your body.", true, false, false)
					
					+ "<div class='cosmetics-container' style='background:transparent;'>"
					
						+ CharacterModificationUtils.getBodySizeChoiceDiv()
						
						+ CharacterModificationUtils.getMuscleChoiceDiv()
						
						+ "<div class='container-full-width' style='text-align:center;'>"
							+ "Your muscle and body size values result in your appearance being:<br/>"
							+ "<b style='color:"+GameState.game.getPlayer().getBodyShape().toWebHexStringColour()+";'>"+Util.capitaliseSentence(GameState.game.getPlayer().getBodyShape().getName(false))+"</b>"
						+ "</div>"
					
					+"</div>";
		}
		
		@Override
		public String getContent() {
			return "";
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Confirm your choices and return to the content preferences menu.", CHOOSE_ADVANCED_APPEARANCE);
				
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode CHOOSE_ADVANCED_APPEARANCE_FACE = new DialogueNode("Face Appearance", "", true) {
		
		@Override
		public String getHeaderContent() {
			return "<div class='container-full-width' style='text-align:center;'>"
						+ "<i>All of these options can be influenced later on in the game.</i>"
					+ "</div>"

					+ CharacterModificationUtils.getLipSizeDiv()
					
					+ CharacterModificationUtils.getLipPuffynessDiv()

					+ CharacterModificationUtils.getKatesDivCoveringsNew(false, GameState.game.getPlayer().getEyeType().getRace(), BodyCoveringType.EYE_HUMAN, "Iris Colour", "The colour of your eye's irises.", true, false, false);
		}
		
		@Override
		public String getContent() {
			return "";
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Confirm your choices and return to the content preferences menu.", CHOOSE_ADVANCED_APPEARANCE);
				
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode CHOOSE_ADVANCED_APPEARANCE_HAIR = new DialogueNode("Hair Appearance", "", true) {
		
		@Override
		public String getHeaderContent() {
			return "<div class='container-full-width' style='text-align:center;'>"
						+ "<i>All of these options can be influenced later on in the game.</i>"
					+ "</div>"

					+ CharacterModificationUtils.getKatesDivHairLengths(false, "Hair Length", "Choose how long your hair is.")
					
					+ CharacterModificationUtils.getKatesDivHairStyles(false, "Hair Style", "Choose your hair style. Certain hair styles are unavailable at shorter hair lengths.")

					+ CharacterModificationUtils.getKatesDivCoveringsNew(false, GameState.game.getPlayer().getHairType().getRace(), BodyCoveringType.HAIR_HUMAN, "Hair Colour", "The colour of your hair.", true, false);
		}
		
		@Override
		public String getContent() {
			return "";
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Confirm your choices and return to the content preferences menu.", CHOOSE_ADVANCED_APPEARANCE);
				
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode CHOOSE_ADVANCED_APPEARANCE_BREASTS = new DialogueNode("Breasts Appearance", "", true) {
		
		@Override
		public String getHeaderContent() {
			return "<div class='container-full-width' style='text-align:center;'>"
						+ "<i>All of these options can be influenced later on in the game.</i>"
					+ "</div>"
						
					+ CharacterModificationUtils.getBreastSizeDiv()
					
					+ CharacterModificationUtils.getBreastShapeDiv()
					
					+ CharacterModificationUtils.getNippleSizeDiv()
					
					+ CharacterModificationUtils.getAreolaeSizeDiv()
					
					+ CharacterModificationUtils.getNipplePuffynessDiv()
					
					+ CharacterModificationUtils.getSelfTransformLactationDiv();
		}
		
		@Override
		public String getContent() {
			return "";
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Confirm your choices and return to the content preferences menu.", CHOOSE_ADVANCED_APPEARANCE);
				
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode CHOOSE_ADVANCED_APPEARANCE_ASS = new DialogueNode("Ass Appearance", "", true) {
		
		@Override
		public String getHeaderContent() {
			return "<div class='container-full-width' style='text-align:center;'>"
						+ "<i>All of these options can be influenced later on in the game.</i>"
					+ "</div>"
						
					+ CharacterModificationUtils.getAssSizeDiv()
					
					+ CharacterModificationUtils.getHipSizeDiv()
					
					+ CharacterModificationUtils.getBleachedAnusDiv();
		}
		
		@Override
		public String getContent() {
			return "";
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Confirm your choices and return to the content preferences menu.", CHOOSE_ADVANCED_APPEARANCE);
				
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode CHOOSE_ADVANCED_APPEARANCE_GENITALS = new DialogueNode("Genitals Appearance", "", true) {
		
		@Override
		public String getLabel() {
			if(GameState.game.getPlayer().hasPenis()) {
				return "Penis Appearance";
			} else {
				return "Vagina Appearance";
			}
		}
		
		@Override
		public String getHeaderContent() {
			if(GameState.game.getPlayer().hasPenis()) {
				return "<div class='container-full-width' style='text-align:center;'>"
							+ "<i>All of these options can be influenced later on in the game.</i>"
						+ "</div>"
							
							+ CharacterModificationUtils.getPenisSizeDiv()
							
							+ CharacterModificationUtils.getTesticleSizeDiv()
							
							+ CharacterModificationUtils.getSelfTransformCumProductionDiv();
				
			} else {
				return "<div class='container-full-width' style='text-align:center;'>"
							+ "<i>All of these options can be influenced later on in the game.</i>"
						+ "</div>"
	
							+ CharacterModificationUtils.getVaginaCapacityDiv()
							
							+ CharacterModificationUtils.getLabiaSizeDiv()
							
							+ CharacterModificationUtils.getClitorisSizeDiv();
				
			}
		}
		
		@Override
		public String getContent() {
			return "";
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Confirm your choices and return to the content preferences menu.", CHOOSE_ADVANCED_APPEARANCE);
				
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode CHOOSE_ADVANCED_APPEARANCE_PIERCINGS = new DialogueNode("Piercings", "", true) {
		
		@Override
		public String getHeaderContent() {
			return "<div class='container-full-width' style='text-align:center;'>"
						+ "<i>All of these options can be influenced later on in the game.</i>"
					+ "</div>"
						
					+CharacterModificationUtils.getKatesDivPiercings(true);
		}
		
		@Override
		public String getContent() {
			return "";
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Confirm your choices and return to the content preferences menu.", CHOOSE_ADVANCED_APPEARANCE);
				
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode CHOOSE_ADVANCED_APPEARANCE_TATTOOS = new DialogueNode("Tattoos", "", true) {
		
		@Override
		public String getHeaderContent() {
			return "<div class='container-full-width' style='text-align:center;'>"
						+ "<i>Later on in the game, you can get enchanted and glowing tattoos. For now, however, your tattoo choices are limited to more mundane options.</i>"
					+ "</div>"
					+CharacterModificationUtils.getKatesDivTattoos();
		}
		
		@Override
		public String getContent() {
			return "";
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Confirm your choices and return to the content preferences menu.", CHOOSE_ADVANCED_APPEARANCE);
				
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode CHOOSE_ADVANCED_APPEARANCE_TATTOOS_ADD = new DialogueNode("Succubi's Secrets", "-", true) {

		@Override
		public String getLabel() {
			return "Add Tattoo: "+Util.capitaliseSentence(CharacterModificationUtils.tattooInventorySlot.getTattooSlotName());
		}
		
		@Override
		public String getContent() {
			return CharacterModificationUtils.getKatesDivTattoosAdd();
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				if(CharacterModificationUtils.tattoo.getType().equals(TattooType.getTattooTypeFromId("innoxia_misc_none"))
						&& CharacterModificationUtils.tattoo.getWriting().getText().isEmpty()
						&& CharacterModificationUtils.tattoo.getCounter().getType()==TattooCounterType.NONE) {
					return new Response("Apply", "You need to select a tattoo type, add some writing, or add a counter in order to make a tattoo!", null);
					
				} else {
					return new Response("Apply", 
							UtilText.parse(BodyChanging.getTarget(), "Add this tattoo."), CHOOSE_ADVANCED_APPEARANCE_TATTOOS) {
						@Override
						public void effects() {
							GameState.getRenderSink().executeScript("document.getElementById('hiddenPField').innerHTML=document.getElementById('tattoo_name').value;");
							CharacterModificationUtils.tattoo.getWriting().setText(GameState.getRenderSink().getDocument().getElementById("hiddenPField").getTextContent());
							CharacterModificationUtils.tattoo.setName(CharacterModificationUtils.tattoo.getType().getName());
							BodyChanging.getTarget().addTattoo(CharacterModificationUtils.tattooInventorySlot, CharacterModificationUtils.tattoo);
						}
					};
				}
			
			} else if(index==2) {
				return new Response("Save/Load", "Save/Load tattoo presets.", CosmeticsDialogue.TATTOO_SAVE_LOAD) {
					@Override
					public void effects() {
						CosmeticsDialogue.initTattooSaveLoadDialogue(CHOOSE_ADVANCED_APPEARANCE_TATTOOS_ADD);
					}
				};
			
			} else if(index==0) {
				return new Response("Back", "Decide not to get this tattoo and return to the main selection screen.", CHOOSE_ADVANCED_APPEARANCE_TATTOOS);
			}
			
			return null;
		}

		@Override
		public boolean reloadOnRestore() {
			return true;
		}
	};
	
	public static final DialogueNode CHOOSE_ADVANCED_APPEARANCE_COSMETICS = new DialogueNode("Cosmetics", "", true) {
		
		@Override
		public String getHeaderContent() {
			return "<div class='container-full-width' style='text-align:center;'>"
						+ "<i>All of these options can be influenced later on in the game.</i>"
					+ "</div>"
							
					+CharacterModificationUtils.getKatesDivCoveringsNew(
							false, Race.NONE, BodyCoveringType.MAKEUP_BLUSHER, "Blusher", "Blusher (also called rouge) is used to colour the cheeks so as to provide a more youthful appearance, and to emphasise the cheekbones.", true, false)
					
					+CharacterModificationUtils.getKatesDivCoveringsNew(
							false, Race.NONE, BodyCoveringType.MAKEUP_LIPSTICK, "Lipstick", "Lipstick is used to provide colour, texture, and protection to the wearer's lips.", true, false)

					+CharacterModificationUtils.getKatesDivCoveringsNew(
							false, Race.NONE, BodyCoveringType.MAKEUP_EYE_LINER, "Eyeliner", "Eyeliner is applied around the contours of the eyes to help to define shape or highlight different features.", true, false)

					+CharacterModificationUtils.getKatesDivCoveringsNew(
							false, Race.NONE, BodyCoveringType.MAKEUP_EYE_SHADOW, "Eye shadow", "Eye shadow is used to make the wearer's eyes stand out or look more attractive.", true, false)

					+CharacterModificationUtils.getKatesDivCoveringsNew(
							false, Race.NONE, BodyCoveringType.MAKEUP_NAIL_POLISH_HANDS, "Nail polish", "Nail polish is used to colour and protect the nails on your [pc.hands].", true, false)

					+CharacterModificationUtils.getKatesDivCoveringsNew(
							false, Race.NONE, BodyCoveringType.MAKEUP_NAIL_POLISH_FEET, "Toenail polish", "Toenail polish is used to colour and protect the nails on your [pc.feet].", true, false);
		}
		
		@Override
		public String getContent() {
			return "";
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Confirm your choices and return to the content preferences menu.", CHOOSE_ADVANCED_APPEARANCE);
				
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode CHOOSE_ADVANCED_APPEARANCE_BODY_HAIR = new DialogueNode("Body Hair", "", true) {
		
		@Override
		public String getHeaderContent() {
			UtilText.nodeContentSB.setLength(0);
			
			UtilText.nodeContentSB.append("<div class='container-full-width' style='text-align:center;'>"
												+ "<i>All of these options can be influenced later on in the game.</i>"
											+ "</div>");
			
			if(GameState.game.isPubicHairEnabled() || GameState.game.isFacialHairEnabled() || GameState.game.isBodyHairEnabled()) {
				UtilText.nodeContentSB.append(CharacterModificationUtils.getKatesDivCoveringsNew(
						false, Race.NONE, GameState.game.getPlayer().getBodyHairCoveringType(), "Body hair", "This is the hair that covers all areas other than the head.", false, false, false));
			} else {
				UtilText.nodeContentSB.append(CharacterModificationUtils.getKatesDivGenericBodyHairDisabled(
						"Body hair", "This is the hair that covers all areas other than the head.", "All extra body hair options are disabled. You will not see any extra hair content."));
				
				return UtilText.nodeContentSB.toString();
			}
			
			if(GameState.game.isFacialHairEnabled()) {
				if (GameState.game.isFemaleFacialHairEnabled()) {
					UtilText.nodeContentSB.append(CharacterModificationUtils.getKatesDivFacialHair(false, "Facial hair", "The body hair found on your face."));
				} else {
					UtilText.nodeContentSB.append(CharacterModificationUtils.getKatesDivFacialHair(false, "Facial hair", "The body hair found on your face. Feminine characters cannot grow facial hair."));
				}
			} else {
				UtilText.nodeContentSB.append(CharacterModificationUtils.getKatesDivGenericBodyHairDisabled(
						"Facial hair", "The body hair found on your face. Feminine characters cannot grow facial hair.", "Facial hair is currently disabled in the options. You will not see any facial hair content while it is disabled."));
			}
			
			if(GameState.game.isPubicHairEnabled()) {
				UtilText.nodeContentSB.append(CharacterModificationUtils.getKatesDivPubicHair(false, "Pubic hair", "The body hair found in the genital area; located on and around your sex organs and crotch."));
				
			} else {
				UtilText.nodeContentSB.append(CharacterModificationUtils.getKatesDivGenericBodyHairDisabled(
						"Pubic hair", "The body hair found in the genital area; located on and around your sex organs and crotch.", "Pubic hair is currently disabled in the options. You will not see any pubic hair content while it is disabled."));
			}
			
			if(GameState.game.isBodyHairEnabled()) {
				UtilText.nodeContentSB.append(
						CharacterModificationUtils.getKatesDivUnderarmHair(false, "Underarm hair", "The body hair found in your armpits."));
				
			} else {
				UtilText.nodeContentSB.append(CharacterModificationUtils.getKatesDivGenericBodyHairDisabled(
						"Underarm hair", "The hair found in your armpits.", "Underarm hair is currently disabled in the options. You will not see any underarm hair content while it is disabled."));
			}
			
			if(GameState.game.isAssHairEnabled()) {
				UtilText.nodeContentSB.append(CharacterModificationUtils.getKatesDivAssHair(false, "Ass hair", "The body hair found around your asshole."));
				
			} else {
				UtilText.nodeContentSB.append(CharacterModificationUtils.getKatesDivGenericBodyHairDisabled(
						"Ass hair", "The body hair found around your asshole.", "Ass hair is currently disabled in the options. You will not see any ass hair content while it is disabled."));
			}
			
			return UtilText.nodeContentSB.toString();
		}
		
		@Override
		public String getContent() {
			return "";
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Confirm your choices and return to the content preferences menu.", CHOOSE_ADVANCED_APPEARANCE);
				
			} else {
				return null;
			}
		}
	};
	
	public static String getCheckingClothingDescription() {
		StringBuilder sb = new StringBuilder();

		File dir = Storage.res("");
		if(!dir.exists()) {
			sb.append("<p style='text-align:center;'>"
						+ "[style.italicsBad(The game cannot read the 'res' folder, and as such, vital items of clothing will be missing! Please refer to the 'MISSING FOLDERS' section of the README.txt before continuing!)]"
					+ "</p>");
		}
		
		sb.append("<div class='container-full-width' style='background:transparent;'>"
					+ "<p>"
						+ "There doesn't seem to be any sign of activity on the main stage, so, afforded a few more minutes, you decide to smarten up your clothes a little."
						+ " After all, this is a big evening for Lily, and you want her to see that you've put some effort into your appearance."
					+ "</p>"
					+ "<p>"
						+ "Turning this way and that to get a better look at yourself in the mirror, you begin to notice just how "+(GameState.game.getPlayer().isFeminine()?"hot":"handsome")+" you're looking tonight..."
					+ "</p>"
					+ "<p>"
						+ "[pc.thought(Why am I feeling so horny all of a sudden?)]"
					+ "</p>"
					+ "<div class='container-full-width' style='text-align:center;'>"
						+ "<i>Choose what you decided to wear to the museum.</i><br/>"
						+ "<i>You'll need to be wearing some kind of footwear, as well as clothing that conceals your genitals and chest, before being able to proceed.</i>"
					+ "</div>"
				+ "</div>");
		
		return sb.toString();
	}
	
	public static void moveNPCIntoPlayerTile() {
		if(GameState.game.getPlayer().getSexualOrientation()==SexualOrientation.ANDROPHILIC || (GameState.game.getPlayer().getSexualOrientation()==SexualOrientation.AMBIPHILIC && GameState.game.getPlayer().hasVagina())) {
			GameState.game.getNpc(PrologueMale.class).setLocation(GameState.game.getPlayer().getWorldLocation(), GameState.game.getPlayer().getLocation(), false);
			
		} else {
			GameState.game.getNpc(PrologueFemale.class).setLocation(GameState.game.getPlayer().getWorldLocation(), GameState.game.getPlayer().getLocation(), false);
			GameState.game.getNpc(PrologueFemale.class).addStatusEffect(StatusEffect.PROMISCUITY_PILL_PROLOGUE, 60*60*24*3); // 3 days
		}
	}
	
	public static void moveNPCOutOfPlayerTile() {
		GameState.game.getNpc(PrologueMale.class).setLocation(WorldType.EMPTY, PlaceType.GENERIC_HOLDING_CELL, false);
		GameState.game.getNpc(PrologueFemale.class).setLocation(WorldType.EMPTY, PlaceType.GENERIC_HOLDING_CELL, false);
	}
	
	public static boolean femalePrologueNPC() {
		return GameState.game.getPlayer().getSexualOrientation()==SexualOrientation.GYNEPHILIC || (GameState.game.getPlayer().getSexualOrientation()==SexualOrientation.AMBIPHILIC && GameState.game.getPlayer().hasPenis());
	}
	
	public static final DialogueNode CHOOSE_BACKGROUND = new DialogueNode("In the Museum", "-", true) {
		
		@Override
		public String getContent() {
			UtilText.nodeContentSB.setLength(0);
			
			UtilText.nodeContentSB.append("<p>"
						+ "Satisfied with your appearance, you turn away from the mirror and begin to walk towards the main stage."
						+ " With each step you take, you inexplicably find yourself getting more and more turned on, and by the time you've barely covered half the distance to the bustling crowd of visitors,"
							+(GameState.game.getPlayer().hasPenis()
									?" you're struggling to keep yourself from getting an erection."
									:" you can feel your pussy getting wet from arousal.")
					+ "</p>"
					+ "<p>"
						+ "Ducking behind a nearby pillar, you shake your head to try and dislodge the dirty thoughts that are starting to seep into your mind."
						+ " As you lean back against the cold stone and take a deep breath, a voice suddenly interrupts your thoughts,");
			
			if(!femalePrologueNPC()) {
				UtilText.nodeContentSB.append(" [prologueMale.speech(Taking a break from the crowds as well?)]"
						+ "</p>"
						+ "<p>"
							+ "Turning around, you see a tall, handsome-looking man, who must be only a couple of years older than you, giving you the most charming smile you've ever seen."
							+ " Before you know what you're doing, your eyes are travelling up and down every [unit.size] of his manly, muscular body, and you only just manage to stop yourself from letting out a desperate little whine."
						+ "</p>"
						+ "<p>"
							+ "[pc.thought(Focus, [pc.name], focus!)] you think, trying to act as casual as possible as you smile back at the stranger before you."
						+ "</p>"
						+ "<p>"
							+ "[pc.speech(Actually,)] you say, [pc.speech(I've only just arrived. I thought I was going to be late, but it looks like nothing's started yet.)]"
						+ "</p>"
						+ "<p>"
							+ "[prologueMale.speech(Ah, you must have just missed the announcement,)] he replies, [prologueMale.speech(the opening speech is being delayed by half an hour."
								+ " I tried hanging around in that crowd back there, but I'm no historian, and most of the conversation is pretty dry...)]"
						+ "</p>"
						+ "<p>"
							+ "[pc.speech(Haha,)]"
							+ " you laugh, desperately trying not to imagine how he looks naked,"
							+ " [pc.speech(I know <i>exactly</i> what you mean. My aunt is the lady giving the opening speech, and every time I meet her friends from the museum, I can never follow their conversations."
									+ " Well, apart from Arthur that is. He's closer to our age, and is really easy-going and fun to talk to.)]"
						+ "</p>"
						+ "<p>"
							+ "[prologueMale.speech(Hah! You know Arthur? I'm here by his invitation. He and I go way back,)]"
							+ " the man cheerily replies, his smile causing your heart to race."
							+ " [prologueMale.speech(I'm [prologueMale.name] by the way, pleased to meet you "+(GameState.game.getPlayer().isFeminine()?"Ms. ...?":"Mr. ...?")+")]"
						+ "</p>"
						+ "<p>"
							+ "[pc.speech(Likewise,)] you respond, shaking his offered hand while trying not to think of how powerful and dominant his grip is. [pc.speech(I'm [pc.Name].)]"
						+ "</p>"
						+ "<p>"
							+ "You and [prologueMale.name] continue talking with one another as you wait for the presentation to start."
							+ " Before long, the subject shifts to work, and you find out that he's an airline pilot, based out of the airport on the city's outskirts."
							+ " Conversation then moves on to what it is you do, and you end up talking about that for a little while..."
						+ "</p>");
				
			} else {
				UtilText.nodeContentSB.append(" [prologueFemale.speech(Taking a break from the crowds as well?)]"
						+ "</p>"
						+ "<p>"
							+ "Turning around, you see a beautiful woman, who looks to be about the same age as you, giving you the most stunning smile you've ever seen."
							+ " Before you know what you're doing, your eyes are travelling up and down every [unit.size] of her curvy, womanly body, and you only just manage to stop yourself from letting out a hungry groan."
						+ "</p>"
						+ "<p>"
							+ "[pc.thought(Focus [pc.name], focus!)] you think, trying to act as casual as possible as you smile back at the stranger before you."
						+ "</p>"
						+ "<p>"
							+ "[pc.speech(Actually,)] you say, [pc.speech(I've only just arrived. I thought I was going to be late, but it looks like nothing's started yet.)]"
						+ "</p>"
						+ "<p>"
							+ "[prologueFemale.speech(Ah, you must have just missed the announcement,)] she replies, [prologueFemale.speech(the opening speech is being delayed by half an hour."
								+ " I tried hanging around in that crowd back there, but I'm no historian, and most of the conversation is pretty dry...)]"
						+ "</p>"
						+ "<p>"
							+ "[pc.speech(Haha,)]"
							+ " you laugh, desperately trying not to imagine how she looks naked,"
							+ " [pc.speech(I know <i>exactly</i> what you mean. My aunt is the lady giving the opening speech, and every time I meet her friends from the museum, I can never follow their conversations."
									+ " Well, apart from Arthur that is. He's closer to our age, and is really easy-going and fun to talk to.)]"
						+ "</p>"
						+ "<p>"
							+ "[prologueFemale.speech(Oh! You know Arthur? I'm here by his invitation, actually. He and I go way back,)]"
							+ " the woman cheerily replies, her smile causing your heart to race."
							+ " [prologueFemale.speech(I'm [prologueFemale.name] by the way, pleased to meet you "+(GameState.game.getPlayer().isFeminine()?"Ms. ...?":"Mr. ...?")+")]"
						+ "</p>"
						+ "<p>"
							+ "[pc.speech(Likewise,)] you respond, shaking her offered hand while trying not to think of how soft and delicate her skin is. [pc.speech(I'm [pc.Name].)]"
						+ "</p>"
						+ "<p>"
							+ "You and [prologueFemale.name] continue talking with one another as you wait for the presentation to start."
							+ " Before long, the subject shifts to work, and you find out that she's training to become a doctor, and is studying here at the city's university."
							+ " Conversation then moves on to what it is you do, and you end up talking about that for a little while..."
						+ "</p>");
			}
			
			return UtilText.nodeContentSB.toString();
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new ResponseEffectsOnly("Back", "Return to clothing selection.") {
					@Override
					public int getSecondsPassed() {
						return -TIME_TO_BACKGROUND;
					}
					@Override
					public void effects() {
						moveNPCOutOfPlayerTile();
						InventoryDialogue.setBuyback(false);
						InventoryDialogue.setInventoryNPC(null);
						InventoryDialogue.setNPCInventoryInteraction(InventoryInteraction.CHARACTER_CREATION);
						GameState.game.setContent(new Response("", "", InventoryDialogue.INVENTORY_MENU));
					}
				};
				
			} else if (index == 1) {
				return new Response("Select Job", "Proceed to the job selection screen.", BACKGROUND_SELECTION_MENU) {
					@Override
					public int getSecondsPassed() {
						return TIME_TO_JOB;
					}
				};
				
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode BACKGROUND_SELECTION_MENU = new DialogueNode("In the Museum", "-", true) {
		
		@Override
		public String getContent() {
			UtilText.nodeContentSB.setLength(0);

			UtilText.nodeContentSB.append("<div class='container-full-width'>"
									+ "<h6 style='text-align:center'>Job Selection</h6>"
									+ "<p style='text-align:center'>Click on the icon next to the job that you'd like, and then choose 'Continue'.</p>"
								+ "</div>");

			UtilText.nodeContentSB.append("<div class='container-full-width'>");
			for(Occupation history : Occupation.getAvailableHistories(GameState.game.getPlayer())) {
				UtilText.nodeContentSB.append(
						"<div class='container-full-width'>"
							+"<div class='container-full-width' style='margin:0;padding:0;'>"
								+ "<h6 style='color:"+history.getAssociatedPerk().getColour().toWebHexString()+";'>"+Util.capitaliseSentence(history.getName(GameState.game.getPlayer()))+"</h6>"
							+ "</div>"
							+"<div class='container-full-width' style='margin:0 8px; width: calc(10% - 16px);'>"
								+ "<div id='OCCUPATION_" + history + "' class='fetish-icon full"
									+ (GameState.game.getPlayer().getHistory()==history
										? " owned' style='border:2px solid " + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>"
										: " unlocked' style='border:2px solid " + PresetColour.TEXT_GREY.toWebHexString() + ";" + "'>")
									+ "<div class='fetish-icon-content'>"+history.getAssociatedPerk().getSVGString(GameState.game.getPlayer())+"</div>"
								+ "</div>"
							+ "</div>"
							+"<div class='container-full-width' style='margin:0 8px; width: calc(90% - 16px);'>"
								+ "<p>"
									+ history.getDescription(GameState.game.getPlayer())
								+ "</p>"
							+ "</div>"
						+ "</div>");
			}
			
			UtilText.nodeContentSB.append("</div>");
			
			return UtilText.nodeContentSB.toString();
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 0) {
				return new Response("Back", "Return to the previous screen.", CHOOSE_BACKGROUND) {
					@Override
					public int getSecondsPassed() {
						return -TIME_TO_JOB;
					}
				};
				
			} else if (index == 1) {
				if(GameState.game.getPlayer().getHistory().getAssociatedPerk()==null) {
					return new Response("Continue", "You need to select a job before continuing!", null);
				} else {
					return new Response("Continue", femalePrologueNPC()?"Tell [prologueFemale.name] what it is you do for a living.":"Tell [prologueMale.name] what it is you do for a living.", CHOOSE_SEX_EXPERIENCE) {
						@Override
						public int getSecondsPassed() {
							return TIME_TO_SEX_EXPERIENCE;
						}
						@Override
						public void effects() {
							GameState.game.getPlayer().getVirginityLossMap().replaceAll((k, v) ->
								(GameState.game.getPlayer().getSexualOrientation()==SexualOrientation.GYNEPHILIC
									|| (GameState.game.getPlayer().getSexualOrientation()==SexualOrientation.AMBIPHILIC && !GameState.game.getPlayer().isFeminine()))
									?new SimpleEntry<>("", "your girlfriend")
									:new SimpleEntry<>("", "your boyfriend"));
						}
					};
				}
				
			} else {
				return null;
			}
		}
	};
	
	
	public static final DialogueNode CHOOSE_SEX_EXPERIENCE = new DialogueNode("Start", "", true) {
		
		@Override
		public String getContent() {
			UtilText.nodeContentSB.setLength(0);
			
			UtilText.nodeContentSB.append("<p>");
			switch(GameState.game.getPlayer().getHistory()) {
				case ATHLETE:
					UtilText.nodeContentSB.append(
							"[pc.speech(I'm a professional athlete,)]"
							+ " you explain,"
							+ " [pc.speech(and I spend most of my time training for and attending competitions.)]");
					break;
				case BUTLER:
					UtilText.nodeContentSB.append(
							"[pc.speech(I work as the butler for a highly influential family here in the city,)]"
							+ " you explain,"
							+ " [pc.speech(but I took tonight off so I could attend Lily's presentation.)]");
					break;
				case CHEF:
					UtilText.nodeContentSB.append(
							"[pc.speech(I'm the head chef at a restaurant just around the corner from here,)]"
							+ " you explain,"
							+ " [pc.speech(but I took tonight off so I could attend Lily's presentation.)]");
					break;
				case CONSTRUCTION_WORKER:
					UtilText.nodeContentSB.append(
							"[pc.speech(I'm a construction worker,)]"
							+ " you explain,"
							+ " [pc.speech(and I'm currently managing a large project on the outskirts of the city.)]");
					break;
				case MAID:
					UtilText.nodeContentSB.append(
							"[pc.speech(I work as the head maid for a highly influential family here in the city,)]"
							+ " you explain,"
							+ " [pc.speech(but I took tonight off so I could attend Lily's presentation.)]");
					break;
				case MUSICIAN:
					UtilText.nodeContentSB.append(
							"[pc.speech(I'm a member of the city orchestra,)]"
							+ " you explain,"
							+ " [pc.speech(and I also do private music tutoring.)]");
					break;
				case OFFICE_WORKER:
					UtilText.nodeContentSB.append(
							"[pc.speech(I work in one of the corporate offices in the centre of the city,)]"
							+ " you explain,"
							+ " [pc.speech(mostly doing admin and paper work.)]");
					break;
				case SOLDIER:
					UtilText.nodeContentSB.append(
							"[pc.speech(I'm in the army,)]"
							+ " you explain,"
							+ " [pc.speech(I'm on leave for the rest of the week, and then it's back to the barracks for me.)]");
					break;
				case STUDENT:
					UtilText.nodeContentSB.append(
							"[pc.speech(I'm a student at the city uni,)]"
							+ " you explain,"
							+ " [pc.speech(although I haven't quite decided what to take as my major yet.)]");
					break;
				case TEACHER:
					UtilText.nodeContentSB.append(
							"[pc.speech(I'm a teacher at a local secondary school,)]"
							+ " you explain,"
							+ " [pc.speech(but seeing as it's half-term, I get to take it easy this week.)]");
					break;
				case UNEMPLOYED:
					UtilText.nodeContentSB.append(
							"[pc.speech(I'm in-between jobs at the moment,)]"
							+ " you explain,"
							+ " [pc.speech(I've actually been thinking about applying to work here at the museum.)]");
					break;
				case WRITER:
					UtilText.nodeContentSB.append(
							"[pc.speech(I'm a professional author,)]" // I write erotic novels...
							+ " you explain,"
							+ " [pc.speech(and I'm currently waiting to hear back from my publisher about my latest novel.)]");
					break;
				case ARISTOCRAT:
					UtilText.nodeContentSB.append(
							"[pc.speech(I don't need to concern myself with working,)]"
							+ " you explain,"
							+ " [pc.speech(My family estate provides all the income I need, so I spend my time travelling and enjoying life.)]");
					break;
				case TOURIST:
					UtilText.nodeContentSB.append(
							"[pc.speech(I'm here on vacation,)]"
							+ " you explain,"
							+ " [pc.speech(While I'm here in the UK, I don't want to be thinking about work.)]");
					break;
				default:
					break;
			}
			UtilText.nodeContentSB.append("</p>");
			
			if(femalePrologueNPC()) {
				UtilText.nodeContentSB.append(
						"<p>"
							+ "As the two of you continue to talk, first about work, and then about more general subjects, you find yourself getting more and more turned on."
							+ " What's more, you begin to notice that [prologueFemale.namePos] cheeks are starting to flush red, and she keeps on glancing hungrily down at your body when she thinks that you aren't looking."
						+ "</p>"
						+ "<p>"
							+ "As final evidence that she's getting just as turned on as you are, she starts openly talking about her sex life."
							+ " To begin with, you're a little taken aback at her openness, but the more she talks, the more comfortable you find yourself with talking to this relative stranger about sex."
						+ "</p>"
						+ "<p>"
							+ "And so, after talking with [prologueFemale.name] for no longer than ten minutes, you're telling her every little detail about your sexual experiences..."
						+ "</p>");
				
			} else {
				UtilText.nodeContentSB.append(
						"<p>"
							+ "As the two of you continue to talk, first about work, and then about more general subjects, you find yourself getting more and more turned on."
							+ " What's more, you begin to notice that [prologueMale.namePos] cheeks are starting to flush red, and he keeps on glancing hungrily down at your body when he thinks that you aren't looking."
						+ "</p>"
						+ "<p>"
							+ "As final evidence that he's getting just as turned on as you are, he starts openly talking about his sex life."
							+ " To begin with, you're a little taken aback at his openness, but the more he talks, the more comfortable you find yourself with talking to this relative stranger about sex."
						+ "</p>"
						+ "<p>"
							+ "And so, after talking with [prologueMale.name] for no longer than ten minutes, you're telling him every little detail about your sexual experiences..."
						+ "</p>");
			}
			
			UtilText.nodeContentSB.append(
						"<div class='container-full-width' style='text-align:center;'>"
							+ "<i>More sexual experience will result in gaining more corruption. (You can see your corruption, along with your other attributes, in the character panel to the left of the screen.)"
							+ "<br/>"
							+ "Selecting '<span style='color:"+FetishDesire.FOUR_LOVE.getColour().toWebHexString()+";'>"+FetishDesire.FOUR_LOVE.getName()+"</span>'"
								+ " for any fetish desire will result in your character starting the game with that fetish, while the other four desires simply determine your character's attitude towards that fetish.</i>"
						+ "</div>"
						+CharacterModificationUtils.getSexualExperienceDiv()
						+CharacterModificationUtils.getFetishChoiceDiv());
			
			return UtilText.nodeContentSB.toString();
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Continue", "Once you're happy with your sexual experience, proceed to the final part of the character creation.", FINAL_CHECK) {
					@Override
					public int getSecondsPassed() {
						return TIME_TO_FINAL_CHECK;
					}
					@Override
					public void effects() {
						if(!GameState.game.getPlayer().hasPenis()) {
							for(SexAreaOrifice ot : SexAreaOrifice.values()) {
								SexType st = new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, ot);
								GameState.game.getPlayer().resetVirginityLoss(st);
								st = new SexType(SexParticipantType.SELF, SexAreaPenetration.PENIS, ot);
								GameState.game.getPlayer().resetVirginityLoss(st);
							}
							GameState.game.getPlayer().setPenisVirgin(true);
							
						}
						if(!GameState.game.getPlayer().hasVagina()) {
							for(SexAreaPenetration pt : SexAreaPenetration.values()) {
								SexType st = new SexType(SexParticipantType.NORMAL, SexAreaOrifice.VAGINA, pt);
								GameState.game.getPlayer().resetVirginityLoss(st);
								st = new SexType(SexParticipantType.SELF, SexAreaOrifice.VAGINA, pt);
								GameState.game.getPlayer().resetVirginityLoss(st);
							}
							GameState.game.getPlayer().setVaginaVirgin(true);
						}
					}
				};
				
			} else if (index == 0) {
				return new Response("Back", "Return to background selection.", BACKGROUND_SELECTION_MENU) {
					@Override
					public int getSecondsPassed() {
						return -TIME_TO_SEX_EXPERIENCE;
					}
				};
				
			} else {
				return null;
			}
		}
	};

	private static void applyGameStart() {
		CharacterModificationUtils.resetImpossibeSexExperience();
		
		GameState.getProperties().addRaceDiscovered(Subspecies.HUMAN);
		GameState.game.getPlayer().setGenderIdentity(GameState.game.getPlayer().getGender());
		
		GameState.game.getNpc(Lilaya.class).setSkinCovering(new Covering(BodyCoveringType.HUMAN, GameState.game.getPlayer().getCovering(BodyCoveringType.HUMAN).getPrimaryColour()), true);

		GameState.game.getNpc(Lilaya.class).setBirthday(LocalDateTime.of(GameState.game.getPlayer().getBirthday().getYear()-22+18, GameState.game.getNpc(Lilaya.class).getBirthMonth(), GameState.game.getNpc(Lilaya.class).getDayOfBirth(), 12, 0));
		
		GameState.game.clearTextStartStringBuilder();
		GameState.game.clearTextEndStringBuilder();

		GameState.game.setWeatherInSeconds(Weather.MAGIC_STORM, 5*60*60);
		
		GameState.game.getPlayerCell().resetInventory();

		GameState.game.getPlayer().addItem(GameState.game.getItemGen().generateItem("innoxia_quest_clothing_keys"), false);
	}

	private static void applySkipPrologueStart(boolean imported) {
		GameState.game.getPlayer().addCharacterEncountered(GameState.game.getNpc(Lilaya.class));
		GameState.game.getPlayer().addCharacterEncountered(GameState.game.getNpc(Rose.class));
		
		GameState.getProperties().addRaceDiscovered(GameState.game.getNpc(Lilaya.class).getSubspecies());
		GameState.getProperties().addRaceDiscovered(GameState.game.getNpc(Rose.class).getSubspecies());
		
		GameState.game.applyStartingDateChange();
		if(!imported) {
			GameState.game.getPlayer().setAgeAppearanceDifference(-Game.TIME_SKIP_YEARS);
		}

		GameState.game.getPlayer().addSpecialPerk(Perk.SPECIAL_PLAYER);
		
		moveNPCOutOfPlayerTile();
	}
	
	public static final DialogueNode FINAL_CHECK = new DialogueNode("Start", "", true) {
		
		@Override
		public String getContent() {
			UtilText.nodeContentSB.setLength(0);
			UtilText.nodeContentSB.append(
				"<div class='container-full-width' style='text-align:center;'>"
					+ "<i>Once you're happy with your appearance, press the 'Start Game' button to begin!<br/>"
					+ "[style.colourGood(This is the end of character creation, so only proceed once you're happy with your choices!)]</i>"
				+ "</div>"
				+ "<br/>"
				+ "<div class='container-full-width'>"
					+ "<h5 style='text-align:center;'>Final Appearance</h5>"
					+ GameState.game.getPlayer().getBodyDescription()
				+ "</div>");
			
			return UtilText.nodeContentSB.toString();
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Start Game", "Use this character and start the game at the very beginning, with trying to find Arthur in the museum.", PrologueDialogue.INTRO){
					@Override
					public void effects() {
						GameState.game.getTextEndStringBuilder().append(GameState.game.getPlayer().startQuest(QuestLine.MAIN));
						
						applyGameStart();
					}
				};
				
			} else if (index == 2) {
				return new ResponseEffectsOnly("Skip prologue", "Start the game and skip the prologue.<br/><br/><i style='color:" + PresetColour.GENERIC_BAD.toWebHexString() + ";'>Not recommended for first time playing!</i>"){
					@Override
					public int getSecondsPassed() {
						return 60*60;
					}
					@Override
					public void effects() {
						
						GameState.game.setRenderMap(true);
						
						GameState.game.getTextStartStringBuilder().append(GameState.game.getPlayer().startQuest(QuestLine.MAIN));
						GameState.game.getTextStartStringBuilder().append(GameState.game.getPlayer().setQuestProgress(QuestLine.MAIN, Quest.MAIN_1_A_LILAYAS_TESTS));
						
						GameState.game.getPlayer().incrementMoney(5000);

						DamageType damageType = DamageType.FIRE;
						switch(CharacterCreation.getStartingDemonstoneSpellSchool()) {
							case AIR:
								damageType = DamageType.POISON;
								break;
							case EARTH:
								damageType = DamageType.PHYSICAL;
								break;
							case ARCANE:
							case FIRE:
								damageType = DamageType.FIRE;
								break;
							case WATER:
								damageType = DamageType.ICE;
								break;
						}
						if(GameState.game.getPlayer().getMainWeapon(0)==null) {
							GameState.game.getPlayer().equipMainWeaponFromNowhere(GameState.game.getItemGen().generateWeapon("innoxia_crystal_rare", damageType));
						} else {
							GameState.game.getPlayer().addWeapon(GameState.game.getItemGen().generateWeapon("innoxia_crystal_rare", damageType), false);
						}
						
						Spell startingSpell = Spell.FIREBALL;
						switch(getStartingTomeSpellSchool()) {
							case AIR:
								startingSpell = Spell.POISON_VAPOURS;
								break;
							case EARTH:
								startingSpell = Spell.SLAM;
								break;
							case FIRE:
							case ARCANE:
								startingSpell = Spell.FIREBALL;
								break;
							case WATER:
								startingSpell = Spell.ICE_SHARD;
								break;
						}
						AbstractItem spellBook = GameState.game.getItemGen().generateItem(ItemType.getSpellBookType(startingSpell));
						GameState.game.getWorlds().get(WorldType.LILAYAS_HOUSE_FIRST_FLOOR).getCell(PlaceType.LILAYA_HOME_ROOM_PLAYER).getInventory().addItem(spellBook);
						
						applyGameStart();
						applySkipPrologueStart(false);
						GameState.game.getPlayer().setLocation(WorldType.LILAYAS_HOUSE_FIRST_FLOOR, PlaceType.LILAYA_HOME_ROOM_PLAYER);

						GameState.game.getPlayer().setHealth(GameState.game.getPlayer().getAttributeValue(Attribute.HEALTH_MAXIMUM));
						GameState.game.getPlayer().setMana(GameState.game.getPlayer().getAttributeValue(Attribute.MANA_MAXIMUM));
						GameState.game.getPlayer().setLustNoText(GameState.game.getPlayer().getRestingLust());
						
						GameState.game.setContent(new Response("", "", GameState.game.getDefaultDialogue(false)));
					}
				};
				
			} else if (index == 0) {
				return new Response("Back", "Return to background selection.", CHOOSE_SEX_EXPERIENCE){
					@Override
					public int getSecondsPassed() {
						return -TIME_TO_FINAL_CHECK;
					}
					@Override
					public void effects() {
						// Remove attribute gain sentences in the start game screen:
						GameState.game.clearTextEndStringBuilder();
					}
				};
				
			} else {
				return null;
			}
		}
	};
	
	
	private static StringBuilder importSB;
	public static final DialogueNode IMPORT_CHOOSE = new DialogueNode("Import", "", true) {
		
		@Override
		public String getContent(){
			importSB = new StringBuilder();

			importSB.append("<p style='text-align:center;'>"
					+ "These characters are being read from the 'data/characters' folder."
					+ " If you want to import a character from a previous version, follow these steps:<br/><br/>"
					+ "<b>1.</b> Open up the old game version, and export your old character (menu -> options -> export).<br/>"
					+ "<b>2.</b> Copy the exported .xml file (in the old version's <i>data/characters</i> folder).<br/>"
					+ "<b>3.</b> Paste it into this version's <i>data/characters</i> folder.<br/>"
					+ "<b>4.</b> Press 'Refresh', and your old character file should show up in the list below!<br/><br/>"
//					+ "(If it doesn't work, please let me know as a comment on my blog, and I'll get it fixed ASAP!)"
					+ "</p>"
					+ "<p>"
					+ "<table align='center'>"
					+ "<tr>"
					+ "<th></th>"
					+ "<th>Name</th>"
					+ "<th></th>"
					+ "</tr>");
			
			int i=1;
			for(File f : GameState.getCharactersForImport()){
				importSB.append(getImportRow(i, f.getName()));
				i++;
			}

			importSB.append("</table>"
					+ "</p>"
					+ "<p id='hiddenPField' style='display:none;'></p>");

			return importSB.toString();
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Refresh", "Refresh this page.", IMPORT_CHOOSE);
				
			} else if (index == 0) {
				return new Response("Back", "Return to main menu.", OptionsDialogue.MENU);
				
			} else {
				return null;
			}
		}
	};
	private static String getImportRow(int i, String name) {
		String baseName = Util.getFileName(name);
		String identifier = Util.getFileIdentifier(name);
		
		return "<tr>"
				+ "<td>"
					+ i+"."
				+ "</td>"
				+ "<td style='min-width:200px;'>"
					+ baseName
				+ "</td>"
				+ "<td>"
					+ "<div class='saveLoadButton' id='IMPORT_CHARACTER_" + identifier + "' style='color:"+PresetColour.GENERIC_GOOD.toWebHexString()+";'>Load</div>"
				+ "</td>"
				+ "</tr>";
	}

	private static boolean resetImportedCharacter = false;

	public static final DialogueNode START_GAME_WITH_IMPORT = new DialogueNode("Start game", "", true) {
		
		@Override
		public String getLabel() {
			return "Imported Character";
		}
		
		@Override
		public String getContent() {
			return "<p>"
						+ "<b>TODO:</b> I will enable the ability to go through the full character creation with imported characters at some point!"
					+ "</p>"
					+ "<br/>"
					+"<details>"
						+ "<summary class='quest-title' style='color:" + QuestType.MAIN.getColour().toWebHexString() + ";'>Import Log</summary>"
						+ GameState.game.getCharacterUtils().getCharacterImportLog()
					+ "</details>"
					+ "<div class='container-full-width'>"
						+ "<h5 style='text-align:center;'>Appearance</h5>"
						+ GameState.game.getPlayer().getBodyDescription()
					+ "</div>";
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Start", "Use this character and start the game at the very beginning.", INTRO_2_FROM_IMPORT){
					@Override
					public void effects() {
						if(resetImportedCharacter){
							resetPlayerCharacter();
						}
						GameState.game.getPlayer().resetAllQuests();
						GameState.game.getPlayer().getCharactersEncountered().clear();
						GameState.game.getTextEndStringBuilder().append(GameState.game.getPlayer().startQuest(QuestLine.MAIN));
						applyGameStart();
					}
				};
				
			} else if (index == 2) {
				return new ResponseEffectsOnly("Skip prologue", "Start the game and skip the prologue.<br/><br/><i style='color:" + PresetColour.GENERIC_BAD.toWebHexString() + ";'>Not recommended for first time playing!</i>"){
					@Override
					public void effects() {
						GameState.game.setRenderMap(true);
						if(resetImportedCharacter){
							resetPlayerCharacter();
						}
						GameState.game.getPlayer().incrementMoney(5000);

						GameState.game.getPlayer().resetAllQuests();
						GameState.game.getPlayer().getCharactersEncountered().clear();
						GameState.game.getTextStartStringBuilder().append(GameState.game.getPlayer().startQuest(QuestLine.MAIN));
						GameState.game.getTextStartStringBuilder().append(GameState.game.getPlayer().setQuestProgress(QuestLine.MAIN, Quest.MAIN_1_A_LILAYAS_TESTS));

						DamageType damageType = DamageType.FIRE;
						switch(CharacterCreation.getStartingDemonstoneSpellSchool()) {
							case AIR:
								damageType = DamageType.POISON;
								break;
							case EARTH:
								damageType = DamageType.PHYSICAL;
								break;
							case ARCANE:
							case FIRE:
								damageType = DamageType.FIRE;
								break;
							case WATER:
								damageType = DamageType.ICE;
								break;
						}
						if(GameState.game.getPlayer().getMainWeapon(0)==null) {
							GameState.game.getPlayer().equipMainWeaponFromNowhere(GameState.game.getItemGen().generateWeapon("innoxia_crystal_rare", damageType));
						} else {
							GameState.game.getPlayer().addWeapon(GameState.game.getItemGen().generateWeapon("innoxia_crystal_rare", damageType), false);
						}
						
						AbstractItem spellBook = GameState.game.getItemGen().generateItem(ItemType.getSpellBookType(Spell.FIREBALL));
						if(GameState.game.getPlayer().getBirthMonth().getValue() % 4 == 1) {
							spellBook = GameState.game.getItemGen().generateItem(ItemType.getSpellBookType(Spell.SLAM));
						} else if(GameState.game.getPlayer().getBirthMonth().getValue() % 4 == 2) {
							spellBook = GameState.game.getItemGen().generateItem(ItemType.getSpellBookType(Spell.POISON_VAPOURS));
						} else if(GameState.game.getPlayer().getBirthMonth().getValue()  % 4 == 3) {
							spellBook = GameState.game.getItemGen().generateItem(ItemType.getSpellBookType(Spell.ICE_SHARD));
						}
						GameState.game.getWorlds().get(WorldType.LILAYAS_HOUSE_FIRST_FLOOR).getCell(PlaceType.LILAYA_HOME_ROOM_PLAYER).getInventory().addItem(spellBook);
						
						applyGameStart();
						applySkipPrologueStart(true);
						GameState.game.getPlayer().setLocation(WorldType.LILAYAS_HOUSE_FIRST_FLOOR, PlaceType.LILAYA_HOME_ROOM_PLAYER);
						GameState.game.setContent(new Response("", "", GameState.game.getDefaultDialogue(false)));
					}
				};

			} else if (index == 5) {
				return new ResponseEffectsOnly(resetImportedCharacter
						?"Reset Character: <span style='color:" + PresetColour.GENERIC_BAD.toWebHexString() + ";'>ON</span>"
						:"Reset Character: <span style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>OFF</span>",
						"Resets experience and flames to 0 and clears your entire inventory, except equipped clothing and weapons. " +
								"Spells and spell perks are removed as well."){
					@Override
					public void effects(){
						resetImportedCharacter = !resetImportedCharacter;
					}
				};


			}
			// Throws error when going back and then resuming
//			else if (index == 0) {
//				return new Response("Back", "Return to new game screen.", OptionsDialogue.MENU);
//			}
			else {
				return null;
			}
		}
	};

	private static void resetPlayerCharacter(){
		PlayerCharacter player = GameState.game.getPlayer();
		player.clearNonEquippedInventory(true);
		player.setEssenceCount(0);
		player.incrementExperience(player.getExperienceNeededForNextLevel(player.getLevel()), false);
		player.setLevel(1);
		player.resetSpells();
		player.resetPerksMap(false);
	}

	public static final DialogueNode INTRO_2_FROM_IMPORT = new DialogueNode("In the Museum", "", true) {

		@Override
		public int getSecondsPassed() {
			return 60*10;
		}
		
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("misc/prologue", "INTRO_2");
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			return PrologueDialogue.INTRO_2.getResponse(responseTab, index);
		}
	};
	
}
