package com.lilithsthrone.game.dialogue.places.submission.gamblingDen;

import java.util.List;
import java.util.Map;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.attributes.CorruptionLevel;
import com.lilithsthrone.game.character.body.CoverableArea;
import com.lilithsthrone.game.character.body.types.FluidType;
import com.lilithsthrone.game.character.effects.Addiction;
import com.lilithsthrone.game.character.fetishes.Fetish;
import com.lilithsthrone.game.character.npc.submission.Roxy;
import com.lilithsthrone.game.character.npc.submission.Vengar;
import com.lilithsthrone.game.character.quests.Quest;
import com.lilithsthrone.game.character.quests.QuestLine;
import com.lilithsthrone.game.dialogue.DialogueFlagValue;
import com.lilithsthrone.game.dialogue.DialogueNode;
import com.lilithsthrone.game.dialogue.places.submission.dicePoker.Dice;
import com.lilithsthrone.game.dialogue.places.submission.dicePoker.DiceFace;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.responses.ResponseSex;
import com.lilithsthrone.game.dialogue.responses.ResponseTrade;
import com.lilithsthrone.game.dialogue.utils.UtilText;
import com.lilithsthrone.game.inventory.InventorySlot;
import com.lilithsthrone.game.inventory.clothing.AbstractClothing;
import com.lilithsthrone.game.inventory.enchanting.ItemEffectType;
import com.lilithsthrone.game.inventory.item.AbstractItem;
import com.lilithsthrone.game.inventory.item.ItemType;
import com.lilithsthrone.game.inventory.weapon.AbstractWeapon;
import com.lilithsthrone.game.inventory.weapon.WeaponType;
import com.lilithsthrone.game.sex.InitialSexActionInformation;
import com.lilithsthrone.game.sex.SexAreaOrifice;
import com.lilithsthrone.game.sex.SexAreaPenetration;
import com.lilithsthrone.game.sex.SexParticipantType;
import com.lilithsthrone.game.sex.SexType;
import com.lilithsthrone.game.sex.managers.submission.SMRoxyPussyLicker;
import com.lilithsthrone.game.sex.managers.universal.SMGeneric;
import com.lilithsthrone.game.sex.positions.slots.SexSlotLyingDown;
import com.lilithsthrone.game.sex.sexActions.baseActions.TongueVagina;
import com.lilithsthrone.main.GameState;
import com.lilithsthrone.utils.Util;
import com.lilithsthrone.utils.Util.Value;
import com.lilithsthrone.utils.colours.Colour;
import com.lilithsthrone.utils.colours.PresetColour;
import com.lilithsthrone.world.WorldType;
import com.lilithsthrone.world.places.PlaceType;

/**
 * @since 0.2.6
 * @version 0.3.21
 * @author Innoxia, DSG
 */
public class RoxysShop {
    
        public static final String REBEL_BASE_ROXY_TIMER = "rebel_base_roxy_timer";

	private static boolean isAddictedToRoxy() {
		Addiction ratGCumAdd = GameState.game.getPlayer().getAddiction(FluidType.GIRL_CUM_RAT_MORPH);
		return ratGCumAdd!=null && ratGCumAdd.getProviderIDs().contains(GameState.game.getNpc(Roxy.class).getId());
	}
	
	public static final DialogueNode TRADER_EXTERIOR = new DialogueNode("Roxy's Fun Box", "", false) {
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "TRADER_EXTERIOR");
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			if(index==1) {
				return new Response("Enter", "[pc.Step] inside 'Roxy's Fun Box' and take a look around...", TRADER);
			}
			return null;
		}
	};
	
	public static final DialogueNode TRADER = new DialogueNode("Roxy's Fun Box", "", true) {
		@Override
		public void applyPreParsingEffects() {
			ItemEffectType.CIGARETTE.applyEffect(null, null, null, 0, GameState.game.getNpc(Roxy.class), GameState.game.getNpc(Roxy.class), null);
		}
		@Override
		public String getContent() {
			if(GameState.game.getNpc(Vengar.class).isSlave() && !GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.roxyVengarOwnerIntroduced)) {
				StringBuilder sb = new StringBuilder();
				sb.append(UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "TRADER_VENGAR_INTRO_START"));
				if(isAddictedToRoxy()) {
					sb.append(UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "TRADER_VENGAR_INTRO_ADDICTED"));
				} else if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.roxyAddicted)) {
					sb.append(UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "TRADER_VENGAR_INTRO_BEATEN_ADDICTION"));
				} else {
					sb.append(UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "TRADER_VENGAR_INTRO_NOT_ADDICTED"));
				}
				sb.append(UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "TRADER_VENGAR_INTRO_END"));
				return sb.toString();
				
			} else if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.roxyIntroduced)) {
				if(isAddictedToRoxy()) {
					return UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "TRADER_REPEAT_ADDICT");
					
				} else if(GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.roxyAddicted)) {
					return UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "TRADER_REPEAT_BEATEN_ADDICTION");
					
				} else {
					return UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "TRADER_REPEAT");
				}
				
			} else {
				return UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "TRADER");
			}
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			if(index==0) {
				return new Response("Exit", "Head out of Roxy's shop...", PlaceType.GAMBLING_DEN_CORRIDOR.getDialogue(false)){
					@Override
					public void effects() {
						GameState.game.getDialogueFlags().values.add(DialogueFlagValue.roxyIntroduced);
						GameState.game.getPlayer().setNearestLocation(WorldType.GAMBLING_DEN, PlaceType.GAMBLING_DEN_CORRIDOR, false);
					}
				};
				
			} else if (index == 1) {
				if(!GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.roxyIntroduced)) {
					return new Response("Refuse", "Tell Roxy that you're only interested in having a look around her shop.", TRADER_REPLY_NO){
						@Override
						public void effects() {
							GameState.game.getDialogueFlags().values.add(DialogueFlagValue.roxyIntroduced);
						}
					};
					
				} else {
					return new ResponseTrade("Trade", "Trade with Roxy.", GameState.game.getNpc(Roxy.class)){
						@Override
						public void effects() {
							GameState.game.getDialogueFlags().values.add(DialogueFlagValue.roxyIntroduced);
						}
					};
				}
				
			} else if (index == 2) {
				if(isAddictedToRoxy()) { // Repeat oral:
					if(!GameState.game.getPlayer().isAbleToAccessCoverableArea(CoverableArea.MOUTH, true)) {
						return new Response("Get fix ("+UtilText.formatAsMoneyUncoloured(1000, "span")+")",
								"You can only service Roxy if you're able to gain access to your mouth!",
								null);
						
					} else if(GameState.game.getPlayer().getMoney()<1000) {
						return new Response("Get fix ("+UtilText.formatAsMoneyUncoloured(1000, "span")+")",
								"You don't have the one thousand flames that Roxy is asking for!",
								null);
						
					} else {
						return new ResponseSex("Get fix ("+UtilText.formatAsMoney(1000, "span")+")",
								"Desperate to get another fix of her addictive girl cum, you agree to <b>pay Roxy 1000 flames</b> to get her to sit on your face for an hour.",
								Util.newArrayListOfValues(Fetish.FETISH_ORAL_GIVING), null, CorruptionLevel.TWO_HORNY, null, null, null,
								true, false,
								new SMRoxyPussyLicker(
										Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Roxy.class), SexSlotLyingDown.FACE_SITTING)),
										Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotLyingDown.LYING_DOWN))),
								null,
								null,
								AFTER_ROXY_SEX_ADDICT,
								UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "ROXY_SEX_START_ADDICT")){
							@Override
							public void effects() {
								GameState.game.getTextEndStringBuilder().append(GameState.game.getPlayer().incrementMoney(-1000));
								GameState.game.getNpc(Roxy.class).incrementMoney(1000);
							}
							@Override
							public List<InitialSexActionInformation> getInitialSexActions() {
								return Util.newArrayListOfValues(new InitialSexActionInformation(GameState.game.getNpc(Roxy.class), GameState.game.getPlayer(), TongueVagina.RECEIVING_CUNNILINGUS_START, false, true));
							}
						};
					}
					
				} else { // Perform oral:
					if(GameState.game.getPlayer().isAbleToAccessCoverableArea(CoverableArea.MOUTH, true)) {
						return new ResponseSex("Lick for item", "Agree to let Roxy sit on your face and eat her out until she cums in exchange for a random item from her shop.",
								Util.newArrayListOfValues(Fetish.FETISH_ORAL_GIVING), null, CorruptionLevel.TWO_HORNY, null, null, null,
								true, false,
								new SMRoxyPussyLicker(
										Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Roxy.class), SexSlotLyingDown.FACE_SITTING)),
										Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotLyingDown.LYING_DOWN))),
								null,
								null,
								AFTER_ROXY_SEX,
								UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "ROXY_SEX_START")){
							@Override
							public void effects() {
								GameState.game.getDialogueFlags().values.add(DialogueFlagValue.roxyIntroduced);
							}
							@Override
							public List<InitialSexActionInformation> getInitialSexActions() {
								return Util.newArrayListOfValues(new InitialSexActionInformation(GameState.game.getNpc(Roxy.class), GameState.game.getPlayer(), TongueVagina.RECEIVING_CUNNILINGUS_START, false, true));
							}
						};
						
					} else {
						return new Response("Lick for item", "You can only service Roxy if you're able to gain access to your mouth!", null);
					}
				}
				
			} else if(index==3 && GameState.game.getNpc(Vengar.class).isSlave()) {
				return new Response("Vengar", "Ask Roxy if you can talk to Vengar.", VENGAR);
				
				
			} else if(index==4
//					&& GameState.game.getDialogueFlags().values.contains(DialogueFlagValue.axelExplainedVengar)
					&& GameState.game.getPlayer().hasQuest(QuestLine.SIDE_REBEL_BASE_FIREBOMBS)
					&& !GameState.game.getPlayer().isQuestCompleted(QuestLine.SIDE_REBEL_BASE_FIREBOMBS)
					&& !GameState.game.getPlayer().isQuestFailed(QuestLine.SIDE_REBEL_BASE_FIREBOMBS)) {
				if(GameState.game.getPlayer().isQuestProgressLessThan(QuestLine.SIDE_REBEL_BASE_FIREBOMBS, Quest.REBEL_BASE_FIREBOMBS_FINISH)) {
					if(GameState.game.getPlayer().hasWeaponType(WeaponType.getWeaponTypeFromId("dsg_hlf_weap_pbomb"), true)) {
						return new Response("Firebombs", "Show Roxy the firebombs you recovered to see if she has a way of getting or making more.<br/>[style.boldBad(You will lose one firebomb.)] ", FIREBOMBS) {
							@Override
							public void effects() {
								GameState.game.getTextEndStringBuilder().append(GameState.game.getPlayer().setQuestProgress(QuestLine.SIDE_REBEL_BASE_FIREBOMBS, Quest.REBEL_BASE_FIREBOMBS_FINISH));
								GameState.game.getDialogueFlags().setSavedLong(REBEL_BASE_ROXY_TIMER, GameState.game.getMinutesPassed());
								
								// Shuffle at least one instance of the arcane firebomb into the player's inventory if they've got one equipped but none in their inventory
								if (!GameState.game.getPlayer().hasWeaponType(WeaponType.getWeaponTypeFromId("dsg_hlf_weap_pbomb"), false)) {
									int armRow = 0;
									boolean fireBombShuffled = false;
									for (AbstractWeapon weapon : GameState.game.getPlayer().getMainWeaponArray()) {
										if (weapon.getWeaponType().equals(WeaponType.getWeaponTypeFromId("dsg_hlf_weap_pbomb"))) {
											GameState.game.getPlayer().unequipMainWeapon(armRow, false, false);
											break;
										}
										armRow++;
									}
									if (!fireBombShuffled) {
										for (AbstractWeapon weapon : GameState.game.getPlayer().getOffhandWeaponArray()) {
											if (weapon.getWeaponType().equals(WeaponType.getWeaponTypeFromId("dsg_hlf_weap_pbomb"))) {
												GameState.game.getPlayer().unequipOffhandWeapon(armRow, false, false);
												break;
											}
											armRow++;
										}
									}
								}
								
								GameState.game.getPlayer().removeWeapon(GameState.game.getItemGen().generateWeapon(WeaponType.getWeaponTypeFromId("dsg_hlf_weap_pbomb")));
							}
						};
						
					} else {
						return new Response("Firebombs",
								"As you don't have any firebombs on you, you're going to have to try describing them to Roxy in the hopes that she can find someone to replicate them."
										+ " [style.boldBad(It would probably be best to have a physical example though.)]",
								FIREBOMBS_FAILED) {
							@Override
							public Colour getHighlightColour() {
								return PresetColour.GENERIC_BAD;
							}
						};
					}
					
				} else if(GameState.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_REBEL_BASE_FIREBOMBS, Quest.REBEL_BASE_FIREBOMBS_START)) {
					if((GameState.game.getMinutesPassed() - GameState.game.getDialogueFlags().getSavedLong(REBEL_BASE_ROXY_TIMER)) < 2880) { // Roxy needs 2 days to get firebombs
						return new Response("Firebombs", "Roxy hasn't had enough time to get more firebombs yet.", null);
					} else {
						return new Response("Firebombs", "It's been two days since you asked Roxy about getting more firebombs, better check in.", FIREBOMBS_COMPLETE);
					}
				}
			}
			
			return null;
		}
	};
	
	public static final DialogueNode FIREBOMBS = new DialogueNode("Roxy's Fun Box", "", true, true) {
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "FIREBOMBS");
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			return TRADER.getResponse(responseTab, index);
		}
	};
	
	public static final DialogueNode FIREBOMBS_COMPLETE = new DialogueNode("Roxy's Fun Box", "", true, true) {
		@Override
		public void applyPreParsingEffects() {
               GameState.game.getTextEndStringBuilder().append(GameState.game.getPlayer().setQuestProgress(QuestLine.SIDE_REBEL_BASE_FIREBOMBS, Quest.SIDE_UTIL_COMPLETE));
            GameState.game.getNpc(Roxy.class).addWeapon(GameState.game.getItemGen().generateWeapon("dsg_hlf_weap_pbomb"), 10, false, false);
		}
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "FIREBOMBS_COMPLETE");
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			return TRADER.getResponse(responseTab, index);
		}
	};
	
	public static final DialogueNode FIREBOMBS_FAILED = new DialogueNode("Roxy's Fun Box", "", true, true) {
		@Override
		public void applyPreParsingEffects() {
			GameState.game.getTextEndStringBuilder().append(GameState.game.getPlayer().setQuestFailed(QuestLine.SIDE_REBEL_BASE_FIREBOMBS, Quest.REBEL_BASE_FIREBOMBS_FAILED));
		}
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "FIREBOMBS_FAILED");
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			return TRADER.getResponse(responseTab, index);
		}
	};
	
	public static final DialogueNode TRADER_REPLY_NO = new DialogueNode("Roxy's Fun Box", "", true, true) {
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "TRADER_REPLY_NO");
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			return TRADER.getResponse(responseTab, index);
		}
	};
	
	public static final DialogueNode AFTER_ROXY_SEX = new DialogueNode("Roxy stands up", "Roxy is finished, and, not caring whether you've had any fun or not, starts to stand up.", true) {
		
		@Override
		public int getSecondsPassed() {
			return 60*60;
		}
		
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "AFTER_ROXY_SEX");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if(index==1) {
				return new Response("Roll", "Watch Roxy roll her dice.", AFTER_ROXY_SEX_ITEM_OBTAINED) {
					@Override
					public void effects() {
						Dice d = new Dice(Util.newHashMapOfValues(new Value<>(DiceFace.ONE, 8f), new Value<>(DiceFace.TWO, 4f), new Value<>(DiceFace.THREE, 2f)));
						d.roll();
						int d1 = d.getFace().getValue();
						d.roll();
						int d2 = d.getFace().getValue();
						AbstractItem item;
						
						int dTotal = d1 + d2;
						if(dTotal<=3) {
							item = GameState.game.getItemGen().generateItem("innoxia_race_human_vanilla_water");
						} else if(dTotal<=5) {
							item = GameState.game.getItemGen().generateItem("innoxia_race_bat_fruit_bats_juice_box");
						} else if(dTotal<=7) {
							item = GameState.game.getItemGen().generateItem(ItemType.MOTHERS_MILK);
						} else if(dTotal<=9) {
							item = GameState.game.getItemGen().generateItem("innoxia_race_rat_black_rats_rum");
						} else if(dTotal<=11) {
							item = GameState.game.getItemGen().generateItem("innoxia_race_rat_brown_rats_burger");
						} else {
							item = GameState.game.getItemGen().generateItem("innoxia_race_human_bread_roll");
						}
						
						UtilText.addSpecialParsingString(Util.intToString(d1), true);
						UtilText.addSpecialParsingString(Util.intToString(d2), false);
						UtilText.addSpecialParsingString(Util.intToString(dTotal), false);
						UtilText.addSpecialParsingString(item.getName(true, false), false);
						UtilText.addSpecialParsingString(item.getName(), false);
						
						GameState.game.getTextEndStringBuilder().append(UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "AFTER_ROXY_SEX_DICE_ROLL"));
						
						GameState.game.getTextEndStringBuilder().append(GameState.game.getPlayer().addItem(item, false));
					}
				};
				
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNode AFTER_ROXY_SEX_ITEM_OBTAINED = new DialogueNode("Roxy's Fun Box", "", false, true) {
		
		@Override
		public int getSecondsPassed() {
			return 60*60;
		}
		
		@Override
		public String getContent() {
			return "";
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			return TRADER.getResponse(responseTab, index);
		}
	};
	
	public static final DialogueNode AFTER_ROXY_SEX_ADDICT = new DialogueNode("Roxy stands up", "Roxy is finished, and, not caring whether you've had any fun or not, starts to stand up.", false) {

		@Override
		public int getSecondsPassed() {
			return 60*60;
		}
		
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "AFTER_ROXY_SEX_ADDICT");
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			return TRADER.getResponse(responseTab, index);
		}
	};
	
	
	private static int VENGAR_SUB_SEX_COST = 1000;
	private static int VENGAR_SUB_DOM_COST = 1500;
	
	public static final DialogueNode VENGAR = new DialogueNode("", "", true) {
		@Override
		public String getContent() {
			return UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "VENGAR");
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			if(index==1) {
				if(GameState.game.getPlayer().getMoney()<VENGAR_SUB_DOM_COST) {
					return new Response("Fuck ("+UtilText.formatAsMoney(VENGAR_SUB_DOM_COST, "span")+")", "You don't have enough money to buy a session of dominant sex with Vengar.", null);
				}
				return new ResponseSex(
						"Fuck ("+UtilText.formatAsMoney(VENGAR_SUB_DOM_COST, "span")+")",
						"Tell Roxy that you're willing to pay in order to fuck Vengar.",
						true,
						false,
						new SMGeneric(
								Util.newArrayListOfValues(
										GameState.game.getPlayer()),
								Util.newArrayListOfValues(
										GameState.game.getNpc(Vengar.class)),
								null,
								null){
							@Override
							public SexType getMainSexPreference(GameCharacter character, GameCharacter targetedCharacter) {
								if(!character.isPlayer()) {
									if(targetedCharacter.hasPenis()) {
										if(GameState.game.isAnalContentEnabled()) {
											return new SexType(SexParticipantType.NORMAL, SexAreaOrifice.ANUS, SexAreaPenetration.PENIS);
										} else {
											return new SexType(SexParticipantType.NORMAL, SexAreaOrifice.MOUTH, SexAreaPenetration.PENIS);
										}
									}
								}
								return super.getMainSexPreference(character, targetedCharacter);
							}
						},
						AFTER_VENGAR_SEX,
						UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "VENGAR_SEX_DOM")) {
					@Override
					public void effects() {
						GameState.game.getTextStartStringBuilder().append(GameState.game.getPlayer().incrementMoney(-VENGAR_SUB_DOM_COST));
						GameState.game.getNpc(Roxy.class).incrementMoney(VENGAR_SUB_DOM_COST);
					}
				};
				
			} else if(index==2) {
				if(GameState.game.getPlayer().getMoney()<VENGAR_SUB_SEX_COST) {
					return new Response("Fucked ("+UtilText.formatAsMoney(VENGAR_SUB_SEX_COST, "span")+")", "You don't have enough money to buy a session of submissive sex with Vengar.", null);
				}
				return new ResponseSex(
						"Fucked ("+UtilText.formatAsMoney(VENGAR_SUB_SEX_COST, "span")+")",
						"Tell Roxy that you're willing to pay in order to get Vengar to fuck you.",
						true,
						false,
						new SMGeneric(
								Util.newArrayListOfValues(
										GameState.game.getNpc(Vengar.class)),
								Util.newArrayListOfValues(
										GameState.game.getPlayer()),
								null,
								null){
							@Override
							public Map<GameCharacter, List<CoverableArea>> exposeAtStartOfSexMap() {
								return Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Vengar.class), Util.newArrayListOfValues(CoverableArea.PENIS)));
							}
							@Override
							public SexType getMainSexPreference(GameCharacter character, GameCharacter targetedCharacter) {
								if(!character.isPlayer()) {
									if(targetedCharacter.hasVagina()) {
										return new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, SexAreaOrifice.VAGINA);
									} else if(GameState.game.isAnalContentEnabled()) {
										return new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, SexAreaOrifice.ANUS);
									}
								}
								return super.getMainSexPreference(character, targetedCharacter);
							}
						},
						AFTER_VENGAR_SEX,
						UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "VENGAR_SEX_SUB")) {
					@Override
					public void effects() {
						AbstractClothing cage = GameState.game.getNpc(Vengar.class).getClothingInSlot(InventorySlot.PENIS);
						if(cage!=null) {
							GameState.game.getNpc(Vengar.class).forceUnequipClothingIntoVoid(GameState.game.getNpc(Roxy.class), cage);
						}
						GameState.game.getTextStartStringBuilder().append(GameState.game.getPlayer().incrementMoney(-VENGAR_SUB_SEX_COST));
						GameState.game.getNpc(Roxy.class).incrementMoney(VENGAR_SUB_SEX_COST);
					}
				};
				
			} else if(index==3 && !GameState.game.getNpc(Vengar.class).isFeminine()) {
				if(GameState.game.getPlayer().getEssenceCount()<100 || !GameState.game.getPlayer().hasItemType(ItemType.FETISH_UNREFINED)) {
					return new Response("Sissify",
							"Tell Roxy that Vengar would behave a lot better if she were to turn him into a sissy."
							+ "<br/>Requires: "
							+ (GameState.game.getPlayer().getEssenceCount()<100?"[style.italicsBad(":"[style.italicsGood(")
							+"At least 100 essences)] and "
							+ (GameState.game.getPlayer().hasItemType(ItemType.FETISH_UNREFINED)
									?"[style.italicsBad("
									:"[style.italicsGood(")
								+"[#ITEM_FETISH_UNREFINED.getDeterminer()] [#ITEM_FETISH_UNREFINED.getName(false)])].",
							null);
				}
				return new Response("Sissify",
						"Tell Roxy that Vengar would behave a lot better if she were to turn him into a sissy."
						+ "<br/>Will consume: [style.italicsArcane(100 essences)] and [style.italicsMinorGood(one [#ITEM_FETISH_UNREFINED.getName(false)])].",
						VENGAR_SISSIFY) {
					@Override
					public void effects() {
						GameState.game.getTextStartStringBuilder().append(UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "VENGAR_SISSIFY"));
						GameState.game.getTextStartStringBuilder().append(((Vengar)GameState.game.getNpc(Vengar.class)).applySissification());
						GameState.game.getTextStartStringBuilder().append(UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "VENGAR_SISSIFY_END"));
						GameState.game.getTextEndStringBuilder().append(GameState.game.getPlayer().incrementEssenceCount(-100, false));
						GameState.game.getPlayer().removeItemByType(ItemType.FETISH_UNREFINED);
						GameState.game.getTextEndStringBuilder().append("<p style='text-align:center;'>[style.italicsMinorBad(You lost <b>1</b> [#ITEM_FETISH_UNREFINED.getName(false)]!)]</p>");
					}
				};
				
			} else if(index==0) {
				return new Response("Back", "Decide against doing anything with Vengar.", TRADER);
			}
			return null;
		}
	};
	
	public static final DialogueNode AFTER_VENGAR_SEX = new DialogueNode("Finished", "You and Vengar are finished...", true) {
		@Override
		public void applyPreParsingEffects() {
			GameState.game.getNpc(Vengar.class).equipClothing();
		}
		@Override
		public int getSecondsPassed() {
			return 5*60;
		}
		@Override
		public String getContent() {
			if(GameState.sex.isDom(GameState.game.getPlayer())) {
				return UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "AFTER_VENGAR_SEX_AS_DOM");
			}
			return UtilText.parseFromXMLFile("places/submission/gamblingDen/roxysShop", "AFTER_VENGAR_SEX_AS_SUB");
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			return TRADER.getResponse(responseTab, index);
		}
	};
	
	public static final DialogueNode VENGAR_SISSIFY = new DialogueNode("", "", true) {
		@Override
		public int getSecondsPassed() {
			return 5*60;
		}
		@Override
		public String getContent() {
			return "";
		}
		@Override
		public Response getResponse(int responseTab, int index) {
			return VENGAR.getResponse(responseTab, index);
		}
	};
}
