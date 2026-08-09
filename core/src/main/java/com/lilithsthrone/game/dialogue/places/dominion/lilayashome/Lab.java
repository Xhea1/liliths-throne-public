package com.lilithsthrone.game.dialogue.places.dominion.lilayashome;

import java.util.ArrayList;
import java.util.List;

import com.lilithsthrone.game.character.EquipClothingSetting;
import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.attributes.CorruptionLevel;
import com.lilithsthrone.game.character.effects.StatusEffect;
import com.lilithsthrone.game.character.fetishes.Fetish;
import com.lilithsthrone.game.character.npc.dominion.Arthur;
import com.lilithsthrone.game.character.npc.dominion.Daddy;
import com.lilithsthrone.game.character.npc.dominion.Lilaya;
import com.lilithsthrone.game.character.npc.dominion.Rose;
import com.lilithsthrone.game.character.npc.submission.Lyssieth;
import com.lilithsthrone.game.character.quests.Quest;
import com.lilithsthrone.game.character.quests.QuestLine;
import com.lilithsthrone.game.character.race.RaceStage;
import com.lilithsthrone.game.character.race.Subspecies;
import com.lilithsthrone.game.dialogue.DialogueFlagValue;
import com.lilithsthrone.game.dialogue.DialogueManager;
import com.lilithsthrone.game.dialogue.DialogueNode;
import com.lilithsthrone.game.dialogue.npcDialogue.dominion.DaddyDialogue;
import com.lilithsthrone.game.dialogue.places.submission.LyssiethPalaceDialogue;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.responses.ResponseEffectsOnly;
import com.lilithsthrone.game.dialogue.responses.ResponseSex;
import com.lilithsthrone.game.dialogue.responses.ResponseTag;
import com.lilithsthrone.game.dialogue.utils.UtilText;
import com.lilithsthrone.game.inventory.clothing.AbstractClothing;
import com.lilithsthrone.game.inventory.item.ItemType;
import com.lilithsthrone.game.sex.managers.universal.SMGeneric;
import com.lilithsthrone.game.sex.managers.universal.SMSitting;
import com.lilithsthrone.game.sex.positions.slots.SexSlotSitting;
import com.lilithsthrone.main.GameState;
import com.lilithsthrone.utils.Util;
import com.lilithsthrone.utils.Util.Value;
import com.lilithsthrone.utils.Vector2i;
import com.lilithsthrone.utils.colours.Colour;
import com.lilithsthrone.utils.colours.PresetColour;
import com.lilithsthrone.world.Cell;
import com.lilithsthrone.world.WorldType;
import com.lilithsthrone.world.places.GenericPlace;
import com.lilithsthrone.world.places.PlaceType;
import com.lilithsthrone.world.places.PlaceUpgrade;

/**
 * @since 0.1.75
 * @version 0.3.9
 * @author Innoxia
 */
public class Lab {
	
	public static boolean isLilayaAngryAtPlayerDemonTF() {
		return GameState.game.getDialogueFlags().hasFlag("innoxia_child_of_lyssieth")
				&& GameState.game.getPlayer().isQuestProgressGreaterThan(QuestLine.MAIN, Quest.MAIN_2_D_MEETING_A_LILIN)
				&& GameState.game.getNpc(Lilaya.class).getSubspeciesOverride()!=Subspecies.DEMON;
	}
	
	public static Cell addArthurRoom() {
		Vector2i labLocation = GameState.game.getWorlds().get(WorldType.LILAYAS_HOUSE_GROUND_FLOOR).getCell(PlaceType.LILAYA_HOME_LAB).getLocation();
		Cell arthurRoomCell = GameState.game.getWorlds().get(WorldType.LILAYAS_HOUSE_GROUND_FLOOR).getCell(labLocation.increment(1, 0));
		arthurRoomCell.setPlace(new GenericPlace(PlaceType.LILAYA_HOME_ROOM_WINDOW_GROUND_FLOOR), true);
		arthurRoomCell.addPlaceUpgrade(PlaceUpgrade.LILAYA_ARTHUR_ROOM);
		arthurRoomCell.setTravelledTo(true);
		return arthurRoomCell;
	}
	
	public static final DialogueNode LAB = new DialogueNode("Lilaya's Laboratory", "", false) {
		@Override
		public String getContent() {
			if(GameState.game.getNpc(Lilaya.class).getLocationPlaceType()==PlaceType.LILAYA_HOME_LAB) {
				if(GameState.game.getNpc(Lilaya.class).getBaseFetishDesire(Fetish.FETISH_PREGNANCY).isNegative()) {
					if(GameState.game.getNpc(Lilaya.class).hasStatusEffect(StatusEffect.PREGNANT_0)) {
						return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_PREGNANCY_RISK");
					
					} else if(GameState.game.getNpc(Lilaya.class).isPregnant() && GameState.game.getNpc(Lilaya.class).isCharacterReactedToPregnancy(GameState.game.getPlayer())) {
						return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_PREGNANT");
						
					} else if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.waitingOnLilayaBirthNews)) {
						return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_PREGNANCY_RESOLVED");
					}
				}
			}
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB");
		}

		@Override
		public String getResponseTabTitle(int index) {
			return LilayaHomeGeneric.getLilayasHouseStandardResponseTabs(index);
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if(responseTab==1) {
				return LilayaHomeGeneric.getLilayasHouseFastTravelResponses(index);
			}
			if(index==1) {
				if(GameState.game.getNpc(Lilaya.class).getBaseFetishDesire(Fetish.FETISH_PREGNANCY).isNegative()) {
					if(GameState.game.getNpc(Lilaya.class).hasStatusEffect(StatusEffect.PREGNANT_0)) {
						return new Response("Enter", "The door to Lilaya's laboratory is firmly shut. You'd better come back later.", null);
						
					} else if((GameState.game.getNpc(Lilaya.class).isPregnant() && GameState.game.getNpc(Lilaya.class).isCharacterReactedToPregnancy(GameState.game.getPlayer()))) {
						return new Response("Enter", "The door to Lilaya's laboratory is firmly shut. You're not going to be able to get back in until her pregnancy is resolved.", null);
					}
				}
				
				if(GameState.game.getNpc(Lilaya.class).getLocationPlaceType()!=PlaceType.LILAYA_HOME_LAB) {
					return new Response("Enter", "The door to Lilaya's laboratory is firmly shut, and, considering the hour, she's probably sleeping upstairs.", null);
				}
				
				return new Response("Enter", "Step through the door and enter Lilaya's laboratory.", LAB_ENTRY) {
					@Override
					public void effects() {
						if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.roseToldOnYou)
								&& GameState.game.getPlayer().getQuest(QuestLine.MAIN) != Quest.MAIN_1_I_ARTHURS_TALE
								&& !GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.waitingOnLilayaPregnancyResults)
								&& GameState.game.getNpc(Lilaya.class).getAffection(GameState.game.getPlayer())>0) {
							GameState.game.getTextEndStringBuilder().append(GameState.game.getNpc(Lilaya.class).incrementAffection(GameState.game.getPlayer(), -10));
						}
					}
				};
				
			} else if(index==2) {
				if(GameState.game.getNpc(Lilaya.class).getBaseFetishDesire(Fetish.FETISH_PREGNANCY).isNegative() && (GameState.game.getNpc(Lilaya.class).hasStatusEffect(StatusEffect.PREGNANT_0) || GameState.game.getNpc(Lilaya.class).isPregnant())) {
					return null;
				}
				if(GameState.game.getNpc(Lilaya.class).getLocationPlaceType()!=PlaceType.LILAYA_HOME_LAB || GameState.game.getNpc(Arthur.class).getLocationPlaceType()==PlaceType.LILAYA_HOME_LAB) {
					return null;
				}
				return DialogueManager.getDialogueFromId("acexp_dominion_lilaya_lab_voyeurism").getResponse(0, 1);
			}
			return null;
		}
	};
	
	private static void setEntryFlags() {
		GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.roseToldOnYou, false);
		if(GameState.game.getNpc(Lilaya.class).getFetishDesire(Fetish.FETISH_PREGNANCY).isNegative()) {
			GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.waitingOnLilayaPregnancyResults, false);
			if(GameState.game.getNpc(Lilaya.class).isPregnant()) {
				GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.waitingOnLilayaBirthNews, true);
			} else {
				GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.waitingOnLilayaBirthNews, false);
			}
		} else {
			GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.waitingOnLilayaPregnancyResults, false);
			GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.waitingOnLilayaBirthNews, false);
		}
		if(GameState.game.getNpc(Lilaya.class).isVisiblyPregnant()) {
			GameState.game.getNpc(Lilaya.class).setCharacterReactedToPregnancy(GameState.game.getPlayer(), true);
		}
	}
	
	private static List<Response> getLabEntryGeneratedResponses() {
		List<Response> generatedResponses = new ArrayList<>();
		
		if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.lilayaHug)) {
			generatedResponses.add(new Response("Hug",
					"You've already given Lilaya an unexpected hug today, and although she liked it, she seemed serious about not doing again..."
							+ "<br/><i>You should wait until tomorrow before giving her another hug!</i>",
					null));
		} else if(isLilayaAngryAtPlayerDemonTF()) {
			generatedResponses.add(new Response("Hug",
					"Due to her resentment towards you for being a full demon, Lilaya absolutely does not want a hug!",
					null));
		} else {
			generatedResponses.add(new Response("Hug", "[pc.Step] up to Lilaya and give her a big hug.", LAB_LILAYA_HUG));
		}
		
		if(GameState.game.getPlayer().isVisiblyPregnant()) {
			if (!GameState.game.getPlayer().isQuestProgressGreaterThan(QuestLine.MAIN, Quest.MAIN_1_A_LILAYAS_TESTS)) {
				generatedResponses.add(new Response("Pregnancy", "You'll need to complete Lilaya's initial tests before she'll agree to help you deal with your pregnancy.", null));
				
			} else {
				if(GameState.game.getPlayer().getQuest(QuestLine.SIDE_FIRST_TIME_PREGNANCY) == Quest.SIDE_PREGNANCY_CONSULT_LILAYA) {
					generatedResponses.add(new Response("Pregnancy", "Speak to Lilaya about your pregnancy.", LilayaBirthing.LILAYA_ASSISTS_PREGNANCY){
						@Override
						public void effects() {
							setEntryFlags();
							GameState.game.getTextEndStringBuilder().append(GameState.game.getPlayer().setQuestProgress(QuestLine.SIDE_FIRST_TIME_PREGNANCY, Quest.SIDE_PREGNANCY_LILAYA_THE_MIDWIFE));
						}
					});
					
				} else {
					generatedResponses.add(new Response("Pregnancy", "Speak to Lilaya about your pregnancy.", LilayaBirthing.LILAYA_ASSISTS_PREGNANCY_REPEAT){
						@Override
						public void effects() {
							setEntryFlags();
						}
					});
				}
			}
		}

//		if(!GameState.game.getPlayer().getIncubatingLitters().isEmpty()) {
//			if (!GameState.game.getPlayer().isQuestProgressGreaterThan(QuestLine.MAIN, Quest.MAIN_1_A_LILAYAS_TESTS)) {
//				generatedResponses.add(new Response("Incubation", "You'll need to complete Lilaya's initial tests before she'll agree to help you deal with the eggs you're currently incubating.", null));
//				
//			} else {
//				if(GameState.game.getPlayer().getQuest(QuestLine.SIDE_FIRST_TIME_INCUBATION) == Quest.SIDE_INCUBATION_CONSULT_LILAYA) {
//					generatedResponses.add(new Response("Incubation", "Speak to Lilaya about the eggs you're currently incubating.", LilayaBirthing.LILAYA_ASSISTS_INCUBATION){
//						@Override
//						public void effects() {
//							setEntryFlags();
//							GameState.game.getTextEndStringBuilder().append(GameState.game.getPlayer().setQuestProgress(QuestLine.SIDE_FIRST_TIME_INCUBATION, Quest.SIDE_INCUBATION_LILAYA_HELP));
//						}
//					});
//					
//				} else {
//					generatedResponses.add(new Response("Incubation", "Speak to Lilaya about the eggs you're currently incubating.", LilayaBirthing.LILAYA_ASSISTS_INCUBATION_REPEAT){
//						@Override
//						public void effects() {
//							setEntryFlags();
//						}
//					});
//				}
//			}
//		}
		
		if(GameState.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)) {
			if(!GameState.game.getPlayer().isQuestCompleted(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)) {
				if (!GameState.game.getPlayer().isQuestProgressGreaterThan(QuestLine.MAIN, Quest.MAIN_1_A_LILAYAS_TESTS)) {
					generatedResponses.add(new Response("Essences & Enchantments", "You'll need to complete Lilaya's initial tests before you're able to ask her about that strange energy you absorbed.", null));
					
				} else {
					generatedResponses.add(new Response("Essences & Enchantments", "Ask Lilaya about that strange energy you absorbed.", LILAYA_EXPLAINS_ESSENCES){
						@Override
						public void effects() {
							setEntryFlags();
						}
					});
				}
				
			} else {
				generatedResponses.add(new Response("Extract Essences",
						GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.essenceExtractionKnown)
								?"Ask Lilaya if you can use her equipment to extract some essences."
								:"Ask Lilaya if there's any way to extract essences you've absorbed",
							ESSENCE_EXTRACTION){
					@Override
					public void effects() {
						setEntryFlags();
					}
				});
			}
		}
		
		if(GameState.game.getPlayer().hasQuest(QuestLine.SIDE_SLAVERY)) {
			if(GameState.game.getPlayer().getQuest(QuestLine.SIDE_SLAVERY) == Quest.SIDE_SLAVER_NEED_RECOMMENDATION) {
				if (!GameState.game.getPlayer().isQuestProgressGreaterThan(QuestLine.MAIN, Quest.MAIN_1_A_LILAYAS_TESTS)) {
					generatedResponses.add(new Response("Slaver", "You'll need to complete Lilaya's initial tests before you can ask her for a letter of recommendation.", null));
					
				} else {
					generatedResponses.add(new Response("Slaver", "Ask Lilaya for a letter of recommendation in order to obtain a slaver license.", LILAYA_SLAVER_RECOMMENDATION){
						@Override
						public void effects() {
							setEntryFlags();
						}
					});
				}
			}
		}
		
		if(GameState.game.getPlayer().hasQuest(QuestLine.SIDE_ACCOMMODATION) && !GameState.game.getPlayer().isQuestCompleted(QuestLine.SIDE_ACCOMMODATION)) {
			if (!GameState.game.getPlayer().isQuestProgressGreaterThan(QuestLine.MAIN, Quest.MAIN_1_A_LILAYAS_TESTS)) {
				generatedResponses.add(new Response("Accommodation", "You'll need to complete Lilaya's initial tests before you can ask her about inviting friends home!", null));
				
			} else {
				generatedResponses.add(new Response("Accommodation", "Ask Lilaya about inviting your new friend to live in one of the many spare rooms in the mansion.", LILAYA_FRIEND_ACCOMMODATION){
					@Override
					public void effects() {
						setEntryFlags();
						GameState.game.getTextEndStringBuilder().append(GameState.game.getPlayer().setQuestProgress(QuestLine.SIDE_ACCOMMODATION, Quest.SIDE_UTIL_COMPLETE));
					}
				});
			}
		}
		
		if(GameState.game.getPlayer().hasQuest(QuestLine.SIDE_DOLL_STORAGE) && !GameState.game.getPlayer().isQuestCompleted(QuestLine.SIDE_DOLL_STORAGE)) {
			if (!GameState.game.getPlayer().isQuestProgressGreaterThan(QuestLine.MAIN, Quest.MAIN_1_A_LILAYAS_TESTS)) {
				generatedResponses.add(new Response("Doll storage", "You'll need to complete Lilaya's initial tests before you can ask her about storing sex dolls in the mansion!", null));
				
			} else {
				generatedResponses.add(new Response("Doll storage", "Ask Lilaya if you can use one of the spare rooms in the mansion to store sex dolls.", LILAYA_DOLL_STORAGE){
					@Override
					public void effects() {
						setEntryFlags();
						GameState.game.getTextEndStringBuilder().append(GameState.game.getPlayer().setQuestProgress(QuestLine.SIDE_DOLL_STORAGE, Quest.SIDE_UTIL_COMPLETE));
					}
				});
			}
		}
		
		
		
		if(!GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.lilayaDateTalk)
				&& GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.knowsDate)) {
			generatedResponses.add(new Response("Current Date", "Ask Lilaya why the calendar in your room is three years ahead of the correct date.", LILAYA_CURRENT_DATE_TALK) {
				@Override
				public void effects() {
					setEntryFlags();
					GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.lilayaDateTalk, true);
				}
			});
		}
		
		if(GameState.game.getPlayer().hasItemType(ItemType.PRESENT) && !GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.givenLilayaPresent3)) {
			if(isLilayaAngryAtPlayerDemonTF()) {
				generatedResponses.add(new Response("Give Present", "Although you have a present in your inventory, Lilaya is not interested in receiving it, due to her resentment towards you for being a full demon, while she is not.", null));
				
			} else {
				generatedResponses.add(new Response("Give Present", "Give the present in your inventory to Lilaya.", LILAYA_PRESENT) {
					@Override
					public void effects() {
						setEntryFlags();
						GameState.game.getPlayer().removeItem(GameState.game.getItemGen().generateItem(ItemType.PRESENT));
						
						if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.givenLilayaPresent2)) {
							GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.givenLilayaPresent3, true);
							
						} else if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.givenLilayaPresent1)) {
							GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.givenLilayaPresent2, true);
							
						} else {
							GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.givenLilayaPresent1, true);
						}
					}
				});
			}
		}
		
		if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.givenLilayaPresent3)) {
			if(isLilayaAngryAtPlayerDemonTF()) {
				generatedResponses.add(new Response("Geisha Lilaya", "Lilaya is not interested in showing off her kimono, nor having sex with you, until she's a full demon as well.", null));
				
			} else {
				generatedResponses.add(new Response("Geisha Lilaya", "Ask Lilaya if she'd like to wear the gifts you got for her.", LILAYA_GEISHA) {
					@Override
					public void effects() {
						setEntryFlags();
						GameState.game.getPlayer().setLocation(WorldType.LILAYAS_HOUSE_FIRST_FLOOR, PlaceType.LILAYA_HOME_ROOM_LILAYA, true);
						GameState.game.getNpc(Lilaya.class).setLocation(WorldType.LILAYAS_HOUSE_FIRST_FLOOR, PlaceType.LILAYA_HOME_ROOM_LILAYA, true);
						
						((Lilaya) GameState.game.getNpc(Lilaya.class)).applyGeishaChange();
					}
				});
			}
		}
		
		if(GameState.game.getPlayer().hasQuest(QuestLine.SIDE_DADDY)) {
			if(GameState.game.getPlayer().getQuest(QuestLine.SIDE_DADDY) == Quest.DADDY_ACCEPTED) {
				if(!Daddy.isAvailable()) {
					generatedResponses.add(new Response("Meeting [daddy.name]", Daddy.getAvailabilityText(), null));
					
				} else if(GameState.game.getPlayer().hasCompanions()) {
					generatedResponses.add(new Response("Meeting [daddy.name]",
							"[style.italicsBad(You cannot ask Lilaya to go with you and meet [daddy.name] while you have companions in your party!)]",
							null));
					
				} else {
					generatedResponses.add(new Response("Meeting [daddy.name]", "Convince Lilaya to go out for dinner with you and [daddy.name].", DaddyDialogue.CONVINCING_LILAYA) {
						@Override
						public void effects() {
							setEntryFlags();
							GameState.game.getTextEndStringBuilder().append(GameState.game.getPlayer().setQuestProgress(QuestLine.SIDE_DADDY, Quest.DADDY_LILAYA_MEETING));
						}
					});
					
				}
	
			} else if(GameState.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_DADDY, Quest.DADDY_LILAYA_MEETING)) {
				if(!Daddy.isAvailable()) {
					generatedResponses.add(new Response("Visit [daddy.name]", Daddy.getAvailabilityText(), null));
					
				} else if(GameState.game.getPlayer().hasCompanions()) {
					generatedResponses.add(new Response("Visit [daddy.name]",
							"[style.italicsBad(You cannot ask Lilaya to go with you and vist [daddy.name] while you have companions in your party!)]",
							null));
					
				} else  {
					generatedResponses.add(new Response("Visit [daddy.name]", "Ask Lilaya if she'd like to join you in paying [daddy.name] a visit.", DaddyDialogue.MEETING) {
						@Override
						public int getSecondsPassed() {
							return 30*60;
						}
						@Override
						public void effects() {
							setEntryFlags();
							((Lilaya) GameState.game.getNpc(Lilaya.class)).applyDinnerDateChange();
							
							GameState.game.getPlayer().setLocation(WorldType.DADDYS_APARTMENT, PlaceType.DADDY_APARTMENT_ENTRANCE);
							GameState.game.getNpc(Lilaya.class).setLocation(GameState.game.getPlayer(), false);
	
							if(GameState.game.getPlayer().isVisiblyPregnant() && !GameState.game.getPlayer().isCharacterReactedToPregnancy(GameState.game.getNpc(Daddy.class))) {
								GameState.game.getTextEndStringBuilder().append(GameState.game.getPlayer().incrementMoney(500));
							}
							if(GameState.game.getNpc(Lilaya.class).isVisiblyPregnant() && !GameState.game.getNpc(Lilaya.class).isCharacterReactedToPregnancy(GameState.game.getNpc(Daddy.class))) {
								GameState.game.getTextEndStringBuilder().append(GameState.game.getNpc(Lilaya.class).incrementMoney(500));
							}
						}
					});
					
				}
			}
		}
		
		if(GameState.game.getPlayer().isQuestCompleted(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)
				&& GameState.game.getPlayer().getClothingCurrentlyEquipped().stream().anyMatch(c -> c.isSelfTransformationInhibiting())
				&& GameState.game.getPlayer().getClothingCurrentlyEquipped().stream().anyMatch(c -> c.isSealed())) {
			generatedResponses.add(new Response("Sealed problem",
					"Tell Lilaya that you have some enchanted clothing sealed onto you, and that due to another enchantment on some of your clothing, you cannot remove it."
							+ "<br/>[style.italicsMinorGood(Lilaya will remove the 'servitude' enchantment from all of your clothing!)]",
						LAB_JINX_REMOVAL){
				@Override
				public void effects() {
					for(AbstractClothing clothing : new ArrayList<>(GameState.game.getPlayer().getClothingCurrentlyEquipped())) {
						if(clothing.isSelfTransformationInhibiting()) {
							clothing.removeServitudeEnchantment();
						}
					}
				}
			});
		}
		
		if(GameState.game.getPlayer().getQuest(QuestLine.SIDE_WES)==Quest.WES_1) {
			if(GameState.game.getCurrentDialogueNode()==LILAYA_ELLE_HELP) {
				generatedResponses.add(new Response("Elle's location", "You are already asking Lilaya about where Elle could be!", null));
				
			} else {
				generatedResponses.add(new Response("Elle's location",
						GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.wesQuestLilayaHelp)
							?"Ask Lilaya if she can remind you where Elle is teleporting to."
							:"Ask Lilaya for some help in figuring out where Elle is teleporting to.",
						LILAYA_ELLE_HELP){
					@Override
					public void effects() {
						if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.wesQuestLilayaHelp)) {
							GameState.game.getTextStartStringBuilder().append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_ELLE_HELP_REPEAT"));
						} else {
							GameState.game.getTextStartStringBuilder().append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_ELLE_HELP"));
							GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.wesQuestLilayaHelp, true);
						}
						setEntryFlags();
					}
				});
			}
		}
		
		if(!GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.obtainedScientistClothing)) {
			generatedResponses.add(new Response("Scientist outfit",
					"Ask Lilaya if she has a spare lab coat you could have.",
					LILAYA_SCIENTIST_OUTFIT){
				@Override
				public void effects() {
					GameState.game.getTextEndStringBuilder().append(GameState.game.getPlayer().addClothing(GameState.game.getItemGen().generateClothing("innoxia_scientist_lab_coat", false), false));
					GameState.game.getTextEndStringBuilder().append(GameState.game.getPlayer().addClothing(GameState.game.getItemGen().generateClothing("innoxia_scientist_safety_goggles", false), false));
					GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.obtainedScientistClothing, true);
					setEntryFlags();
				}
			});
		}
		
		return generatedResponses;
	}
	
	public static final DialogueNode LAB_ENTRY = new DialogueNode("Lilaya's Laboratory", "", true) {

		@Override
		public String getContent() {
			UtilText.nodeContentSB.setLength(0);
			
			if(GameState.game.getPlayer().getQuest(QuestLine.MAIN) == Quest.MAIN_1_I_ARTHURS_TALE) {
				if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.waitingOnLilayaPregnancyResults)) {
					UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_ARTHUR_PREGNANCY_BASE"));
					
					if(GameState.game.getNpc(Lilaya.class).isVisiblyPregnant()) {
						UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_ARTHUR_PREGNANT"));
						
					} else {
						UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_ARTHUR_NOT_PREGNANT"));
					}
					
				} else {
					UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_ARTHUR_BASE"));
				}

				UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_ARTHUR"));
				
			} else {
				if(GameState.game.getNpc(Lilaya.class).getRaceStage()==RaceStage.GREATER) { // Lilaya a full demon:
					if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.roseToldOnYou)) {
						UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_ROSE_TOLD_ON_YOU"));
					} else {
						UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_BASE_END"));
					}
					
				} else { // Lilaya is not a full demon:
					if(isLilayaAngryAtPlayerDemonTF() && !GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.lilayaReactedToPlayerAsDemon)) {
						if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.waitingOnLilayaPregnancyResults) || GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.waitingOnLilayaBirthNews)) {
							UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_DEMON_REACTION_PREGNANCY_RESOLVED"));
						} else {
							UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_DEMON_REACTION_NO_PREGNANCY"));
						}
						UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_DEMON_REACTION"));
						
					} else if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.waitingOnLilayaPregnancyResults)) {
						if(GameState.game.getNpc(Lilaya.class).isVisiblyPregnant()) {
							UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_PREGNANT"));
							
						} else {
							UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_NOT_PREGNANT"));
						}
						
					} else if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.waitingOnLilayaBirthNews)) {
						UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_PREGNANCY_RESOLVED"));
						
					} else {
						UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_BASE"));
	
						UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_NAUGHTY_ROSE"));
						
						if(isLilayaAngryAtPlayerDemonTF()) {
							if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.roseToldOnYou)) {
								UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_ROSE_TOLD_ON_YOU_DEMON"));
							} else {
								UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_BASE_END_DEMON"));
							}
							
						} else {
							if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.roseToldOnYou)) {
								UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_ROSE_TOLD_ON_YOU"));
							} else {
								UtilText.nodeContentSB.append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ENTRY_BASE_END"));
							}
						}
					}
				}
			}
			
			return UtilText.nodeContentSB.toString();
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if(isLilayaAngryAtPlayerDemonTF() && !GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.lilayaReactedToPlayerAsDemon)) {
				if(index == 1) {
					return new Response("Agree",
							"Tell Lilaya that you'll help her convince Lyssieth to turn her into a full demon.<br/>[style.italicsDemon(This will end with Lilaya being permanently transformed into a demon!)]",
							LAB_DEMON_TF_AGREE){
						@Override
						public Colour getHighlightColour() {
							return PresetColour.RACE_DEMON;
						}
						@Override
						public void effects() {
							setEntryFlags();
							GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.lilayaReactedToPlayerAsDemon, true);
							GameState.game.getTextStartStringBuilder().append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_DEMON_TF_AGREE"));
							GameState.game.getTextStartStringBuilder().append(GameState.game.getNpc(Lilaya.class).incrementAffection(GameState.game.getPlayer(), 50));
						}
					};
					
				} else if(index == 2) {
					return new Response("Refuse",
							"Try and convince Lilaya that she should stay as a half-demon.<br/>[style.italics(You can always change your mind later and tell Lilaya you'll help her to become a full demon.)]",
							LAB_DEMON_TF_REFUSE){
						@Override
						public void effects() {
							setEntryFlags();
							GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.lilayaReactedToPlayerAsDemon, true);
						}
					};
				}
				return null;
					
			} else if(GameState.game.getPlayer().getQuest(QuestLine.MAIN) == Quest.MAIN_1_I_ARTHURS_TALE || GameState.game.getPlayer().getQuest(QuestLine.MAIN) == Quest.MAIN_1_J_ARTHURS_ROOM) {
				if(index == 1) {
					return new Response("Agree", "Knowing how fierce your [lilaya.relation(pc)] can get when she's in one of these moods, you realise that you don't really have much of a choice...", LAB_ARTHURS_TALE){
						@Override
						public void effects() {
							setEntryFlags();
						}
					};
				}
				return null;
				
			} else if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.waitingOnLilayaPregnancyResults)
					&& GameState.game.getNpc(Lilaya.class).isVisiblyPregnant()
					&& GameState.game.getNpc(Lilaya.class).getRaceStage()!=RaceStage.GREATER) {
				if(index==0) {
					return new Response("Thrown out", "Lilaya is clearly not interested in talking to you until her pregnancy has been resolved...", LAB_EXIT_THROWN_OUT) {
						@Override
						public void effects() {
							setEntryFlags();
						}
					};
				}
				return null;
				
			} else {
				List<Response> generatedResponses = getLabEntryGeneratedResponses();
				
				// Return responses:
				if(index==0) {
					return new Response("Leave", "Say goodbye to Lilaya and exit her lab.", LAB) {
						@Override
						public void effects() {
							setEntryFlags();
							GameState.game.getTextStartStringBuilder().append(
									"<p>"
										+ "You tell Lilaya that you've got to get going, and, after saying goodbye, you head over to the lab's door and make your exit."
									+ "</p>");
						}
					};
					
				} else if (index == 1) {
					if(isLilayaAngryAtPlayerDemonTF()) {
						return new Response("Full demon",
								"Tell Lilaya that you'll help her convince her mother to turn her into a full demon.<br/>[style.italicsDemon(This will end with Lilaya being permanently transformed into a full demon!)]",
								LAB_DEMON_TF_AGREE) {
							@Override
							public Colour getHighlightColour() {
								return PresetColour.RACE_DEMON;
							}
							@Override
							public void effects() {
								setEntryFlags();
								GameState.game.getTextStartStringBuilder().append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_DEMON_TF_AGREE_AFTER_REPEAT"));
								GameState.game.getTextStartStringBuilder().append(GameState.game.getNpc(Lilaya.class).incrementAffection(GameState.game.getPlayer(), 50));
							}
						};
						
					} else {
						if (GameState.game.getPlayer().getQuest(QuestLine.MAIN) == Quest.MAIN_1_A_LILAYAS_TESTS) {
							return new Response("Tests", "Let Lilaya know that you're here to let her run her tests on you.", AUNT_HOME_LABORATORY_TESTING){
								@Override
								public void effects() {
									setEntryFlags();
								}
							};
							
						} else {
							if(GameState.game.getNpc(Arthur.class).getLocationPlace().getPlaceType().equals(PlaceType.LILAYA_HOME_LAB)) {
								return new Response("\"Tests\"", "Lilaya can't run any \"tests\" on you while Arthur is still present in her lab. Find him a suitable room first.", null);
								
							} else if (GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.hadSexWithLilaya)) {
								return new Response("\"Tests\"", "Let Lilaya know that you're here to let her run more of her \"tests\" on you.", AUNT_HOME_LABORATORY_TESTING_MORE_SEX){
									@Override
									public void effects() {
										setEntryFlags();
									}
								};
							} else {
								return new Response("Tests", "Tell Lilaya that you want her to run more of her 'tests' on you.", AUNT_HOME_LABORATORY_TESTING_REPEAT){
									@Override
									public void effects() {
										setEntryFlags();
									}
								};
							}
						}
					}

				} else if(index<10) {
					if(index-2 < generatedResponses.size()) {
						return generatedResponses.get(index-2);
					} else {
						return null;
					}
					
				} else if(index==11 && GameState.game.getPlayer().isQuestProgressGreaterThan(QuestLine.MAIN, Quest.MAIN_2_D_MEETING_A_LILIN)) {
					// Teleport
					return new Response("Lyssieth's office", "Ask Lilaya to use the resonance stone to contact Lyssieth, and ask her to teleport you to her office.", LyssiethPalaceDialogue.LYSSIETH_OFFICE_TALK){
						@Override
						public void effects() {
							GameState.game.getPlayer().setLocation(WorldType.LYSSIETH_PALACE, PlaceType.LYSSIETH_PALACE_OFFICE);
							if(GameState.game.getPlayer().hasCompanions()) { //TODO test
								for(GameCharacter companion : GameState.game.getPlayer().getCompanions()) {
									companion.setLocation(WorldType.LYSSIETH_PALACE, PlaceType.LYSSIETH_PALACE_SIREN_OFFICE, false);
								}
								GameState.game.getTextEndStringBuilder().append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_TELEPORT_TO_LYSSIETHS_OFFICE_COMPANION", GameState.game.getPlayer().getMainCompanion()));
							} else {
								GameState.game.getTextEndStringBuilder().append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_TELEPORT_TO_LYSSIETHS_OFFICE"));
							}
						}
					};
				}
				
				return null;
			}
		}

		@Override
		public boolean isInventoryDisabled() {
			return false;
		}
	};
	
	public static final DialogueNode LILAYA_END_SEX = new DialogueNode("Get up", "Lilaya really needs to get back to work.", true) {

		@Override
		public String getDescription() {
			if((GameState.game.getNpc(Lilaya.class).hasStatusEffect(StatusEffect.CREAMPIE_VAGINA) || ((Lilaya)GameState.game.getNpc(Lilaya.class)).isAmazonsSecretImpregnation())
					&& GameState.game.getNpc(Lilaya.class).getFetishDesire(Fetish.FETISH_PREGNANCY).isNegative()
					&& !GameState.game.getNpc(Lilaya.class).isVisiblyPregnant()) {
				return "Lilaya looks pretty angry, maybe you should have pulled out...";
			} else {
				return "Lilaya really needs to get back to work.";
			}
		}

		@Override
		public String getContent() {
			if((GameState.game.getNpc(Lilaya.class).hasStatusEffect(StatusEffect.CREAMPIE_VAGINA) || ((Lilaya)GameState.game.getNpc(Lilaya.class)).isAmazonsSecretImpregnation())
					&& GameState.game.getNpc(Lilaya.class).getFetishDesire(Fetish.FETISH_PREGNANCY).isNegative()
					&& !GameState.game.getNpc(Lilaya.class).isVisiblyPregnant()) {
				return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_END_SEX_CREAMPIE");
			} else {
				if(GameState.sex.getNumberOfOrgasms(GameState.game.getNpc(Lilaya.class))==0) {
					return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_END_SEX_NO_ORGASM");
				} else {
					return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_END_SEX");
				}
			}
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				if((GameState.game.getNpc(Lilaya.class).hasStatusEffect(StatusEffect.CREAMPIE_VAGINA) || ((Lilaya)GameState.game.getNpc(Lilaya.class)).isAmazonsSecretImpregnation())
						&& GameState.game.getNpc(Lilaya.class).getFetishDesire(Fetish.FETISH_PREGNANCY).isNegative()
						&& !GameState.game.getNpc(Lilaya.class).isVisiblyPregnant()) {
					return new Response("Thrown out", "Maybe it's best to leave Lilaya to cool down for a while.", Lab.LAB_EXIT_THROWN_OUT){
						@Override
						public void effects() {
							if((GameState.game.getNpc(Lilaya.class).hasStatusEffect(StatusEffect.CREAMPIE_VAGINA) || ((Lilaya)GameState.game.getNpc(Lilaya.class)).isAmazonsSecretImpregnation())
									&& !GameState.game.getNpc(Lilaya.class).isVisiblyPregnant()) {
								GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.waitingOnLilayaPregnancyResults, true);
							}
							GameState.game.getNpc(Lilaya.class).washAllOrifices(true);
							GameState.game.getNpc(Rose.class).setLocation(WorldType.LILAYAS_HOUSE_GROUND_FLOOR, PlaceType.LILAYA_HOME_LAB, false);
							
							GameState.game.getNpc(Lilaya.class).equipClothing(EquipClothingSetting.getAllClothingSettings());
							GameState.game.getNpc(Lilaya.class).setLocation(WorldType.LILAYAS_HOUSE_GROUND_FLOOR, PlaceType.LILAYA_HOME_LAB, true);
						}
					};
					
				} else {
					return new Response("Continue", "Leave the lab and let Lilaya carry on with her work.", Lab.LAB_EXIT) {
						@Override
						public void effects() {
							GameState.game.getNpc(Rose.class).setLocation(WorldType.LILAYAS_HOUSE_GROUND_FLOOR, PlaceType.LILAYA_HOME_LAB, false);
							GameState.game.getNpc(Lilaya.class).washAllOrifices(true);
							
							GameState.game.getNpc(Lilaya.class).equipClothing(EquipClothingSetting.getAllClothingSettings());
							GameState.game.getNpc(Lilaya.class).setLocation(WorldType.LILAYAS_HOUSE_GROUND_FLOOR, PlaceType.LILAYA_HOME_LAB, true);
						}
					};
				}
				
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode LAB_EXIT = new DialogueNode("Lilaya's Laboratory", "", true) {
		
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_EXIT");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			return LAB_ENTRY.getResponse(0, index);
		}
	};
	
	public static final DialogueNode LAB_EXIT_THROWN_OUT = new DialogueNode("Lilaya's Laboratory", "", false) {
		
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_EXIT_THROWN_OUT");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			return null;
		}
	};
	
	public static final DialogueNode LAB_DEMON_TF_AGREE = new DialogueNode("", "", true, true) {
		
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_DEMON_TF_AGREE_CORE");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if(index==1) {
				return new Response("Teleported", "Lyssieth teleports the three of you back to her palace...", LyssiethPalaceDialogue.LILAYA_DEMON_TF_START){
					@Override
					public void effects() {
						GameState.game.getPlayer().setLocation(WorldType.LYSSIETH_PALACE, PlaceType.LYSSIETH_PALACE_OFFICE);
						if(GameState.game.getPlayer().hasCompanions()) { //TODO test
							for(GameCharacter companion : GameState.game.getPlayer().getCompanions()) {
								companion.setLocation(WorldType.LILAYAS_HOUSE_GROUND_FLOOR, PlaceType.LILAYA_HOME_LAB, false);
							}
						}
						GameState.game.getNpc(Lilaya.class).setLocation(GameState.game.getPlayer(), false);
						GameState.game.getNpc(Lyssieth.class).setLocation(GameState.game.getPlayer(), false);
					}
				};
			}
			return null;
		}
	};
	
	public static final DialogueNode LAB_DEMON_TF_REFUSE = new DialogueNode("", "", true, true) {
		
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_DEMON_TF_REFUSE");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			return LAB_ENTRY.getResponse(responseTab, index);
		}
	};
	
	
	public static final DialogueNode LILAYA_PRESENT = new DialogueNode("Lilaya's Laboratory", "", true) {
		
		@Override
		public String getContent() {

			if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.givenLilayaPresent3)) {
				return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_PRESENT_3");
				
			} else if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.givenLilayaPresent2)) {
				return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_PRESENT_2");
					
			} else {
				return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_PRESENT_1");
			}
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			return LAB_ENTRY.getResponse(0, index);
		}
	};
	
	public static final DialogueNode LILAYA_GEISHA = new DialogueNode("Lilaya's Bedroom", "", true) {
		

		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_GEISHA");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new ResponseSex("Sex", "Start having sex with Lilaya.",
						Util.newArrayListOfValues(Fetish.FETISH_INCEST), null, CorruptionLevel.FOUR_LUSTFUL, null, null, null,
						true, true,
						new SMGeneric(
								Util.newArrayListOfValues(GameState.game.getPlayer()),
								Util.newArrayListOfValues(GameState.game.getNpc(Lilaya.class)),
						null,
						null),
						END_SEX_GEISHA,
						UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_GEISHA_SEX_START")){
					@Override
					public void effects() {
						GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.hadSexWithLilaya, true);
					}
				};

			} if (index == 2) {
				return new ResponseSex("Submissive Sex", "Start having submissive sex with Lilaya.",
						Util.newArrayListOfValues(Fetish.FETISH_INCEST), null, CorruptionLevel.FOUR_LUSTFUL, null, null, null,
						true, true,
						new SMGeneric(
								Util.newArrayListOfValues(GameState.game.getNpc(Lilaya.class)),
								Util.newArrayListOfValues(GameState.game.getPlayer()),
						null,
						null),
						END_SEX_GEISHA,
						UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_GEISHA_SUBMISSIVE_SEX_START")){
					@Override
					public void effects() {
						GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.hadSexWithLilaya, true);
					}
				};

			} else if (index == 3) {
				return new Response("Leave",
						"Tell Lilaya that she looks stunning, but you don't want to have sex with her.",
						RoomPlayer.ROOM){
					@Override public void effects() {

						GameState.game.getNpc(Lilaya.class).setLocation(WorldType.LILAYAS_HOUSE_GROUND_FLOOR, PlaceType.LILAYA_HOME_LAB, true);
						GameState.game.getNpc(Lilaya.class).resetInventory(false);
						
						GameState.game.getPlayer().setLocation(WorldType.LILAYAS_HOUSE_FIRST_FLOOR, PlaceType.LILAYA_HOME_ROOM_PLAYER, true);
						
						GameState.game.getNpc(Lilaya.class).equipClothing(EquipClothingSetting.getAllClothingSettings());
						
						GameState.game.getTextStartStringBuilder().append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_GEISHA_SEX_REFUSED"));
					}
				};

			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode END_SEX_GEISHA = new DialogueNode("Finished", "Lilaya collapses back on her bed with a satisfied sigh.", true) {
		
		@Override
		public String getDescription() {
			if((GameState.game.getNpc(Lilaya.class).hasStatusEffect(StatusEffect.CREAMPIE_VAGINA) || ((Lilaya)GameState.game.getNpc(Lilaya.class)).isAmazonsSecretImpregnation())
					&& GameState.game.getNpc(Lilaya.class).getFetishDesire(Fetish.FETISH_PREGNANCY).isNegative()
					&& !GameState.game.getNpc(Lilaya.class).isVisiblyPregnant()) {
				return "Lilaya looks pretty angry, maybe you should have pulled out...";
			} else {
				return "Lilaya collapses back on her bed with a satisfied sigh.";
			}
		}

		@Override
		public String getContent() {
			if((GameState.game.getNpc(Lilaya.class).hasStatusEffect(StatusEffect.CREAMPIE_VAGINA) || ((Lilaya)GameState.game.getNpc(Lilaya.class)).isAmazonsSecretImpregnation())
					&& GameState.game.getNpc(Lilaya.class).getFetishDesire(Fetish.FETISH_PREGNANCY).isNegative()
					&& !GameState.game.getNpc(Lilaya.class).isVisiblyPregnant()) {
				return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "END_SEX_GEISHA_CREAMPIE");
				
			} else {
				if(GameState.sex.getNumberOfOrgasms(GameState.game.getNpc(Lilaya.class))==0) {
					return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "END_SEX_GEISHA_NO_ORGASM");
				} else {
					return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "END_SEX_GEISHA");
				}
			}
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				if((GameState.game.getNpc(Lilaya.class).hasStatusEffect(StatusEffect.CREAMPIE_VAGINA) || ((Lilaya)GameState.game.getNpc(Lilaya.class)).isAmazonsSecretImpregnation())
						&& GameState.game.getNpc(Lilaya.class).getFetishDesire(Fetish.FETISH_PREGNANCY).isNegative()
						&& !GameState.game.getNpc(Lilaya.class).isVisiblyPregnant()) {
					return new Response("Thrown out", "Maybe it's best to leave Lilaya to cool down for a while.", RoomPlayer.ROOM){
						@Override
						public void effects() {
							GameState.game.getPlayer().setLocation(WorldType.LILAYAS_HOUSE_FIRST_FLOOR, PlaceType.LILAYA_HOME_ROOM_PLAYER, true);
							if((GameState.game.getNpc(Lilaya.class).hasStatusEffect(StatusEffect.CREAMPIE_VAGINA) || ((Lilaya)GameState.game.getNpc(Lilaya.class)).isAmazonsSecretImpregnation())
									&& !GameState.game.getNpc(Lilaya.class).isVisiblyPregnant()) {
								GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.waitingOnLilayaPregnancyResults, true);
							}
							GameState.game.getNpc(Lilaya.class).washAllOrifices(true);

							GameState.game.getNpc(Lilaya.class).equipClothing(EquipClothingSetting.getAllClothingSettings());
							GameState.game.getNpc(Lilaya.class).setLocation(WorldType.LILAYAS_HOUSE_GROUND_FLOOR, PlaceType.LILAYA_HOME_LAB, true);
						}
					};
					
				} else {
					return new Response("To your room", "Head back to your room.", RoomPlayer.ROOM){
						@Override
						public void effects() {
							GameState.game.getPlayer().setLocation(WorldType.LILAYAS_HOUSE_FIRST_FLOOR, PlaceType.LILAYA_HOME_ROOM_PLAYER, true);
							GameState.game.getNpc(Lilaya.class).washAllOrifices(true);
							
							GameState.game.getNpc(Lilaya.class).equipClothing(EquipClothingSetting.getAllClothingSettings());
							GameState.game.getNpc(Lilaya.class).setLocation(WorldType.LILAYAS_HOUSE_GROUND_FLOOR, PlaceType.LILAYA_HOME_LAB, true);
						}
					};
				}
				
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode LAB_JINX_REMOVAL = new DialogueNode("", "", true) {
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_JINX_REMOVAL");
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			return LAB_ENTRY.getResponse(0, index);
		}
	};

	public static final DialogueNode LILAYA_ELLE_HELP = new DialogueNode("", "", true) {
		@Override
		public String getContent() {
			return "";
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			return LAB_ENTRY.getResponse(0, index);
		}
	};
	
	public static final DialogueNode LILAYA_SCIENTIST_OUTFIT = new DialogueNode("", "", true) {
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_SCIENTIST_OUTFIT");
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			return LAB_ENTRY.getResponse(0, index);
		}
	};
	
	public static final DialogueNode LAB_LILAYA_HUG = new DialogueNode("", "", true) {
		@Override
		public void applyPreParsingEffects() {
			GameState.game.getTextEndStringBuilder().append(GameState.game.getNpc(Lilaya.class).incrementAffection(GameState.game.getPlayer(), 5));
			GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.lilayaHug, true);
		}
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_LILAYA_HUG");
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			return LAB_ENTRY.getResponse(0, index);
		}
	};
	
	public static final DialogueNode LILAYA_EXPLAINS_ESSENCES = new DialogueNode("", "", true, true) {
		
		@Override
		public String getContent() {
			if(getJinxedClothingExample() != null) {
				UtilText.addSpecialParsingString(getJinxedClothingExample().getName(), true);
				UtilText.addSpecialParsingString((getJinxedClothingExample().getClothingType().isPlural()?"them":"it"), false);
				UtilText.addSpecialParsingString((getJinxedClothingExample().getClothingType().isPlural()?"these":"this"), false);
			}
			
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_EXPLAINS_ESSENCES");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("What's wrong?", "Ask Lilaya what's wrong.", LILAYA_EXPLAINS_ESSENCES_2) {
					@Override
					public void effects() {
						if(!GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.essenceBottledDiscovered)
								&& !GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.essenceOrgasmDiscovered)
								&& !GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.essencePostCombatDiscovered)) {
							GameState.game.getPlayer().incrementEssenceCount(1, false);
						}
					}
				};

			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode LILAYA_EXPLAINS_ESSENCES_2 = new DialogueNode("", "", true, true) {
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_EXPLAINS_ESSENCES_2");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Listen", "Listen as Lilaya shows you how to use your stored essences in order to enchant items.", LILAYA_EXPLAINS_ESSENCES_3);

			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode LILAYA_EXPLAINS_ESSENCES_3 = new DialogueNode("", "", true, true) {
		
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_EXPLAINS_ESSENCES_3");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Thank her", "Thank Lilaya for showing you how to enchant items.", LAB_EXIT){
					@Override
					public void effects() {
						GameState.game.getTextStartStringBuilder().append(
								UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_EXPLAINS_ESSENCES_END")
								+ GameState.game.getPlayer().setQuestProgress(QuestLine.SIDE_ENCHANTMENT_DISCOVERY, Quest.SIDE_UTIL_COMPLETE));
					}
				};

			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode ESSENCE_EXTRACTION = new DialogueNode("Lilaya's Lab", "-", true, false) {
		
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "ESSENCE_EXTRACTION");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			
			if(index == 1) {
				if((!GameState.game.getPlayer().isInventoryFull() || GameState.game.getPlayer().hasItem(GameState.game.getItemGen().generateItem(ItemType.BOTTLED_ESSENCE_ARCANE)))) {
					if(GameState.game.getPlayer().getEssenceCount()>=1) {
						return new Response("Extract (1)", "Extract one of your arcane essences.", ESSENCE_EXTRACTION_BOTTLED) {
							@Override
							public void effects() {
								GameState.game.getPlayer().addItem(GameState.game.getItemGen().generateItem(ItemType.BOTTLED_ESSENCE_ARCANE), false, false);
								int count = GameState.game.getPlayer().getItemCount(GameState.game.getItemGen().generateItem(ItemType.BOTTLED_ESSENCE_ARCANE));
								
								GameState.game.getTextEndStringBuilder().append(
										"<p style='text-align:center;'>"
											+ "<b style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>Item added to inventory:</b> <b>" + (ItemType.BOTTLED_ESSENCE_ARCANE).getDisplayName(true) + "</b>"
										+ "</p>"
										+ "<p>"
											+ "You now have <b>"+count+" "+(count>1?ItemType.BOTTLED_ESSENCE_ARCANE.getNamePlural(true):ItemType.BOTTLED_ESSENCE_ARCANE.getName(true))+"</b> in your inventory."
										+ "</p>");
								GameState.game.getPlayer().incrementEssenceCount(-1, false);
							}
						};
						
					} else {
						return new Response("Extract (1)", "You don't have any arcane essences!", null);
					}
				} else {
					return new Response("Extract (1)", "You don't have any free space in your inventory!", null);
				}
				
				
			} else if(index == 2) {
				if((!GameState.game.getPlayer().isInventoryFull() || GameState.game.getPlayer().hasItem(GameState.game.getItemGen().generateItem(ItemType.BOTTLED_ESSENCE_ARCANE)))) {
					if(GameState.game.getPlayer().getEssenceCount()>=5) {
						return new Response("Extract (5)", "Extract five of your arcane essences.", ESSENCE_EXTRACTION_BOTTLED) {
							@Override
							public void effects() {
								for(int i =0; i<5; i++) {
									GameState.game.getPlayer().addItem(GameState.game.getItemGen().generateItem(ItemType.BOTTLED_ESSENCE_ARCANE), false, false);
								}
								int count = GameState.game.getPlayer().getItemCount(GameState.game.getItemGen().generateItem(ItemType.BOTTLED_ESSENCE_ARCANE));
								GameState.game.getTextEndStringBuilder().append(
										"<p>"
											+ "Grabbing another vial, you set about repeating the process several times..."
										+ "</p>"
										+ "<p style='text-align:center;'>"
											+ "<b style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>Items added to inventory:</b> <b>5x</b> <b>" + ItemType.BOTTLED_ESSENCE_ARCANE.getDisplayName(true) + "</b>"
										+ "</p>"
										+ "<p>"
											+ "You now have <b>"+count+" "+(count>1?ItemType.BOTTLED_ESSENCE_ARCANE.getNamePlural(true):ItemType.BOTTLED_ESSENCE_ARCANE.getName(true))+"</b> in your inventory."
										+ "</p>");
								GameState.game.getPlayer().incrementEssenceCount(-5, false);
							}
						};
						
					} else {
						return new Response("Extract (5)", "You don't have enough arcane essences!", null);
					}
				} else {
					return new Response("Extract (5)", "You don't have any free space in your inventory!", null);
				}
				
			} else if(index == 3) {
				if((!GameState.game.getPlayer().isInventoryFull() || GameState.game.getPlayer().hasItem(GameState.game.getItemGen().generateItem(ItemType.BOTTLED_ESSENCE_ARCANE)))) {
					if(GameState.game.getPlayer().getEssenceCount()>=25) {
						return new Response("Extract (25)", "Extract twenty-five of your arcane essences.", ESSENCE_EXTRACTION_BOTTLED) {
							@Override
							public void effects() {
								for(int i =0; i<25; i++) {
									GameState.game.getPlayer().addItem(GameState.game.getItemGen().generateItem(ItemType.BOTTLED_ESSENCE_ARCANE), false, false);
								}
								int count = GameState.game.getPlayer().getItemCount(GameState.game.getItemGen().generateItem(ItemType.BOTTLED_ESSENCE_ARCANE));
								GameState.game.getTextEndStringBuilder().append(
										"<p>"
											+ "Grabbing another vial, you set about repeating the process several times..."
										+ "</p>"
										+ "<p style='text-align:center;'>"
											+ "<b style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>Items added to inventory:</b> <b>25x</b> <b>" + ItemType.BOTTLED_ESSENCE_ARCANE.getDisplayName(true) + "</b>"
										+ "</p>"
										+ "<p>"
											+ "You now have <b>"+count+" "+(count>1?ItemType.BOTTLED_ESSENCE_ARCANE.getNamePlural(true):ItemType.BOTTLED_ESSENCE_ARCANE.getName(true))+"</b> in your inventory."
										+ "</p>");
								GameState.game.getPlayer().incrementEssenceCount(-25, false);
							}
						};
						
					} else {
						return new Response("Extract (25)", "You don't have enough arcane essences!", null);
					}
				} else {
					return new Response("Extract (25)", "You don't have any free space in your inventory!", null);
				}
				
			} else if(index == 4) {
				if((!GameState.game.getPlayer().isInventoryFull() || GameState.game.getPlayer().hasItem(GameState.game.getItemGen().generateItem(ItemType.BOTTLED_ESSENCE_ARCANE)))) {
					if(GameState.game.getPlayer().getEssenceCount()>=1) {
						return new Response("Extract (all)", "Extract all of your arcane essences.", ESSENCE_EXTRACTION_BOTTLED) {
							@Override
							public void effects() {
								for(int i =0; i<GameState.game.getPlayer().getEssenceCount(); i++) {
									GameState.game.getPlayer().addItem(GameState.game.getItemGen().generateItem(ItemType.BOTTLED_ESSENCE_ARCANE), false, false);
								}
								int count = GameState.game.getPlayer().getItemCount(GameState.game.getItemGen().generateItem(ItemType.BOTTLED_ESSENCE_ARCANE));
								GameState.game.getTextEndStringBuilder().append(
										"<p>"
											+ "Grabbing another vial, you set about repeating the process several times..."
										+ "</p>"
										+ "<p style='text-align:center;'>"
											+ "<b style='color:" + PresetColour.GENERIC_GOOD.toWebHexString() + ";'>Items added to inventory:</b> <b>"+GameState.game.getPlayer().getEssenceCount()+"x</b> <b>"
												+ ItemType.BOTTLED_ESSENCE_ARCANE.getDisplayName(true) + "</b>"
										+ "</p>"
										+ "<p>"
											+ "You now have <b>"+count+" "+(count>1?ItemType.BOTTLED_ESSENCE_ARCANE.getNamePlural(true):ItemType.BOTTLED_ESSENCE_ARCANE.getName(true))+"</b> in your inventory."
										+ "</p>");
								GameState.game.getPlayer().incrementEssenceCount(-GameState.game.getPlayer().getEssenceCount(), false);
								
							}
						};
						
					} else {
						return new Response("Extract (all)", "You don't have any essences!", null);
					}
				} else {
					return new Response("Extract (all)", "You don't have any free space in your inventory!", null);
				}
				
			} else if (index == 0) {
				return new Response("Back", "Stop extracting essences.", LAB_ENTRY) {
					@Override
					public void effects() {
						GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.essenceExtractionKnown, true);
					}
				};

			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode ESSENCE_EXTRACTION_BOTTLED = new DialogueNode("Lilaya's Lab", "-", true, false) {
		
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "ESSENCE_EXTRACTION_BOTTLED");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			return ESSENCE_EXTRACTION.getResponse(0, index);
		}
	};
	
	
	public static final DialogueNode LILAYA_CURRENT_DATE_TALK = new DialogueNode("Lilaya's Lab", "-", true, false) {
		
		@Override
		public String getContent() {
			if(GameState.game.getPlayer().isQuestProgressGreaterThan(QuestLine.MAIN, Quest.MAIN_2_D_MEETING_A_LILIN)) {
				return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_CURRENT_DATE_TALK_KNOW_TRUTH");
				
			} else {
				return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_CURRENT_DATE_TALK");
			}
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if(index==1) {
				return new Response("Thank her", "Thank Lilaya for her information (or lack thereof), and think about asking her something else.", LAB_EXIT);
			} else {
				return null;
			}
		}
	};
	
	
	//----------------------------------

	public static final DialogueNode AUNT_HOME_LABORATORY_TESTING = new DialogueNode("", "", true, true) {
		
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "AUNT_HOME_LABORATORY_TESTING");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Returning home", "Ask Lilaya if she's found a way to send you back home.", AUNT_HOME_LABORATORY_TESTING_ARTHUR){
					@Override
					public void effects() {
						if (GameState.game.getPlayer().getQuest(QuestLine.MAIN) == Quest.MAIN_1_A_LILAYAS_TESTS) {
							GameState.game.getPlayer().setQuestProgress(QuestLine.MAIN, Quest.MAIN_1_B_DEMON_HOME);
							((Arthur) GameState.game.getNpc(Arthur.class)).generateNewTile();
						}
						if (GameState.game.getPlayer().isVisiblyPregnant() && !GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.reactedToPregnancyLilaya)) {
							GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.reactedToPregnancyLilaya, true);
						}
					}
				};

			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode AUNT_HOME_LABORATORY_TESTING_REPEAT = new DialogueNode("", "", true, true) {
		
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "AUNT_HOME_LABORATORY_TESTING_REPEAT");
			
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response(
						!GameState.game.getPlayer().isTaur()
							?"Sit down"
							:"Step forwards",
						"You know exactly why Lilaya seems embarrassed about these 'tests'...",
						AUNT_HOME_LABORATORY_TESTING_HORNY_LILAYA){
					@Override
					public void effects() {
						if (GameState.game.getPlayer().isVisiblyPregnant() && !GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.reactedToPregnancyLilaya)) {
							GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.reactedToPregnancyLilaya, true);
						}
					}
				};

			} else if (index == 2) {
				return new Response("Decline", "Tell Lilaya that you've changed your mind. While she'll probably be a little disappointed, you can always come back later to take up her offer if you should change your mind.", LAB_EXIT){
					@Override public void effects() {
						GameState.game.getTextStartStringBuilder().append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "AUNT_HOME_LABORATORY_TESTING_LEAVE"));
						if (GameState.game.getPlayer().isVisiblyPregnant() && !GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.reactedToPregnancyLilaya)) {
							GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.reactedToPregnancyLilaya, true);
						}
					}
				};

			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode AUNT_HOME_LABORATORY_TESTING_MORE_SEX = new DialogueNode("", "", true, true) {

		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "AUNT_HOME_LABORATORY_TESTING_MORE_SEX");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				if(!GameState.game.getPlayer().isTaur()) {
					return new ResponseSex("Sex",
							"Start having sex with Lilaya.",
							Util.newArrayListOfValues(Fetish.FETISH_INCEST), null, CorruptionLevel.FOUR_LUSTFUL, null, null, null,
							true, true,
							new SMSitting(
									Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotSitting.SITTING)),
									Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Lilaya.class), SexSlotSitting.SITTING_IN_LAP))),
							null,
							null,
							LILAYA_END_SEX,
							UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "AUNT_HOME_LABORATORY_TESTING_MORE_SEX_START")){
						@Override
						public void effects() {
							GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.hadSexWithLilaya, true);
							GameState.game.getNpc(Rose.class).setLocation(WorldType.LILAYAS_HOUSE_FIRST_FLOOR, PlaceType.LILAYA_HOME_ROOM_ROSE, false);
						}
					};
				} else {
					return new ResponseSex("Sex",
							"Start having sex with Lilaya.",
							Util.newArrayListOfValues(Fetish.FETISH_INCEST), null, CorruptionLevel.FOUR_LUSTFUL, null, null, null,
							true, true,
							new SMGeneric(
									Util.newArrayListOfValues(GameState.game.getPlayer()),
									Util.newArrayListOfValues(GameState.game.getNpc(Lilaya.class)),
									null,
									null,
									ResponseTag.PREFER_ORAL),
							null,
							null,
							LILAYA_END_SEX,
							UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "AUNT_HOME_LABORATORY_TESTING_MORE_SEX_START_TAUR")){
						@Override
						public void effects() {
							GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.hadSexWithLilaya, true);
							GameState.game.getNpc(Rose.class).setLocation(WorldType.LILAYAS_HOUSE_FIRST_FLOOR, PlaceType.LILAYA_HOME_ROOM_ROSE, false);
						}
					};
				}


			} else if (index == 2) {
				return new Response("Stop", "Tell Lilaya that you need to get going. While she'll definitely be disappointed that you're stopping so soon, you can always come back later if you should change your mind.",
						LAB_EXIT){
					@Override public void effects() {
						GameState.game.getTextStartStringBuilder().append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "AUNT_HOME_LABORATORY_TESTING_MORE_SEX_STOP"));
					}
				};

			} else {
				return null;
			}
		}
	};

	public static final DialogueNode AUNT_HOME_LABORATORY_TESTING_ARTHUR = new DialogueNode("", "", true, true) {
		
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "AUNT_HOME_LABORATORY_TESTING_ARTHUR");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response(
						"'Tests'",
						"Accept Lilaya's offer of more 'tests'. You're not sure what her intentions really are, but you're confident that you'll be able to stop her if she tries any funny business.",
						AUNT_HOME_LABORATORY_TESTING_HORNY_LILAYA);

			} else if (index == 2) {
				return new Response("Decline",
						"Tell Lilaya that you're not up for this sort of thing. While she'll probably be a little disappointed, you can always come back later to take up her offer if you should change your mind.",
						LAB_EXIT){
					@Override public void effects() {
						GameState.game.getTextStartStringBuilder().append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "AUNT_HOME_LABORATORY_TESTING_ARTHUR_DECLINED"));
					}
				};

			} else {
				return null;
			}
		}
	};

	public static final DialogueNode AUNT_HOME_LABORATORY_TESTING_HORNY_LILAYA = new DialogueNode("", "", true, true) {
		
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "AUNT_HOME_LABORATORY_TESTING_HORNY_LILAYA");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Open your mouth",
						"Let Lilaya push her finger into your mouth. After all, maybe this is just part of the test?",
						AUNT_HOME_LABORATORY_TESTING_HORNY_LILAYA_WANTS_SEX);

			} else if (index == 2) {
				return new Response("Stop this",
						"Stand up and tell Lilaya that this is going too far. While she'll undoubtedly be upset at this sudden end to her advances, you're sure that she'd try to hit on you again if you changed your mind in the future.",
						LAB_EXIT){
					@Override public void effects() {
						GameState.game.getTextStartStringBuilder().append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "AUNT_HOME_LABORATORY_TESTING_HORNY_LILAYA_DECLINED"));
					}
				};

			} else {
				return null;
			}
		}
	};

	public static final DialogueNode AUNT_HOME_LABORATORY_TESTING_HORNY_LILAYA_WANTS_SEX = new DialogueNode("", "", true, true) {
		
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "AUNT_HOME_LABORATORY_TESTING_HORNY_LILAYA_WANTS_SEX");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				if(!GameState.game.getPlayer().isTaur()) {
					return new ResponseSex("Let it happen",
							GameState.game.getPlayer().hasFetish(Fetish.FETISH_INCEST)
								?"You know that this can only end one way, and the fact that Lilaya reminds you of your aunt Lily only makes it all the more exciting..."
								:"You know that this can only end one way. Although Lilaya reminds you of your [lilaya.relation(pc)] Lily, you don't think it will get in the way of you enjoying this...",
							Util.newArrayListOfValues(Fetish.FETISH_INCEST), null, CorruptionLevel.FOUR_LUSTFUL, null, null, null,
							true, true,
							new SMSitting(
									Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotSitting.SITTING)),
									Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Lilaya.class), SexSlotSitting.SITTING_IN_LAP))),
							null,
							null,
							LILAYA_END_SEX,
							UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "AUNT_HOME_LABORATORY_TESTING_HORNY_LILAYA_WANTS_SEX_START")){
						@Override
						public void effects() {
							GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.hadSexWithLilaya, true);
							GameState.game.getNpc(Rose.class).setLocation(WorldType.LILAYAS_HOUSE_FIRST_FLOOR, PlaceType.LILAYA_HOME_ROOM_ROSE, false);
						}
					};
					
				} else {
					return new ResponseSex("Let it happen",
							GameState.game.getPlayer().hasFetish(Fetish.FETISH_INCEST)
								?"You know that this can only end one way, and the fact that Lilaya reminds you of your aunt Lily only makes it all the more exciting..."
								:"You know that this can only end one way. Although Lilaya reminds you of your [lilaya.relation(pc)] Lily, you don't think it will get in the way of you enjoying this...",
							Util.newArrayListOfValues(Fetish.FETISH_INCEST), null, CorruptionLevel.FOUR_LUSTFUL, null, null, null,
							true, true,
							new SMGeneric(
									Util.newArrayListOfValues(GameState.game.getPlayer()),
									Util.newArrayListOfValues(GameState.game.getNpc(Lilaya.class)),
									null,
									null,
									ResponseTag.PREFER_ORAL),
							null,
							null,
							LILAYA_END_SEX,
							UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "AUNT_HOME_LABORATORY_TESTING_HORNY_LILAYA_WANTS_SEX_START_TAUR")){
						@Override
						public void effects() {
							GameState.game.getDialogueFlags().setFlag(DialogueFlagValue.hadSexWithLilaya, true);
							GameState.game.getNpc(Rose.class).setLocation(WorldType.LILAYAS_HOUSE_FIRST_FLOOR, PlaceType.LILAYA_HOME_ROOM_ROSE, false);
						}
					};
				}

			} else if (index == 2) {
				return new Response("Stop this",
						"Stand up and tell Lilaya that this has gone too far. While she'll undoubtedly be upset at this sudden end, you're sure that she'd try to hit on you again if you changed your mind in the future.",
						LAB_EXIT){
					@Override public void effects() {
						GameState.game.getTextStartStringBuilder().append(UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "AUNT_HOME_LABORATORY_TESTING_HORNY_LILAYA_WANTS_SEX_DECLINED"));
					}
				};

			} else {
				return null;
			}
		}
	};

	private static AbstractClothing getJinxedClothingExample() {
		for (AbstractClothing c : GameState.game.getPlayer().getClothingCurrentlyEquipped()) {
			if(c.isSealed()) {
				return c;
			}
		}
		return null;
	}
	
	public static final DialogueNode LILAYA_FRIEND_ACCOMMODATION = new DialogueNode("", "", true, true) {
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_FRIEND_ACCOMMODATION");
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Continue", "You've now got Lilaya's permission to invite friends back home!", LAB_EXIT);
			}
			return null;
		}
	};
	
	public static final DialogueNode LILAYA_DOLL_STORAGE = new DialogueNode("", "", true, true) {
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_DOLL_STORAGE");
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Continue", "You've now got Lilaya's permission to store dolls in her mansion!", LAB_EXIT);
			}
			return null;
		}
	};
	
	public static final DialogueNode LILAYA_SLAVER_RECOMMENDATION = new DialogueNode("", "", true, true) {
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_SLAVER_RECOMMENDATION");
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Accommodation", "Agree with Lilaya's observation that you'll need somewhere to keep your slaves.", LILAYA_SLAVER_RECOMMENDATION_SLAVE_ACCOMMODATION) {
					@Override
					public void effects() {
						if (GameState.game.getPlayer().getQuest(QuestLine.SIDE_SLAVERY) == Quest.SIDE_SLAVER_NEED_RECOMMENDATION) {
							GameState.game.getPlayer().setQuestProgress(QuestLine.SIDE_SLAVERY, Quest.SIDE_SLAVER_RECOMMENDATION_OBTAINED);
						}
					}
				};
			}
			return null;
		}
	};
	
	public static final DialogueNode LILAYA_SLAVER_RECOMMENDATION_SLAVE_ACCOMMODATION = new DialogueNode("", "", true, true) {
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LILAYA_SLAVER_RECOMMENDATION_SLAVE_ACCOMMODATION");
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 1) {
				return new Response("Continue", "Now that you've got Lilaya's letter of recommendation, you should head back to Slaver Alley and talk to [finch.name].", LAB_EXIT);
			}
			return null;
		}
	};

	public static final DialogueNode LAB_ARTHURS_TALE = new DialogueNode("Lilaya's Laboratory", "", true, true) {
		@Override
		public int getSecondsPassed() {
			return 30*60;
		}
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/dominion/lilayasHome/lab", "LAB_ARTHURS_TALE");
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			if(index == 1) {
				return new ResponseEffectsOnly("Clear storeroom", "Head on over to the lab's storeroom and help Rose clear it out so that Arthur can use it as a bedroom.") {
					@Override
					public void effects() {
						Cell arthurRoomCell = addArthurRoom();
						
						GameState.game.getPlayer().setLocation(arthurRoomCell);
						GameState.game.getNpc(Arthur.class).setLocation(arthurRoomCell, true);
						
						GameState.game.setContent(new Response("", "", PlaceUpgrade.LILAYA_ARTHUR_ROOM.getInstallationDialogue(arthurRoomCell)));
					}
				};
			}
			return null;
		}
	};
	
}
