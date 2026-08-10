package com.lilithsthrone.controller;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import com.lilithsthrone.controller.eventListeners.InventorySelectedItemEventListener;
import com.lilithsthrone.controller.eventListeners.SetContentEventListener;
import com.lilithsthrone.controller.eventListeners.buttons.ButtonCopyDialogueEventListener;
import com.lilithsthrone.controller.eventListeners.buttons.ButtonMoveEastEventListener;
import com.lilithsthrone.controller.eventListeners.buttons.ButtonMoveNorthEventListener;
import com.lilithsthrone.controller.eventListeners.buttons.ButtonMoveSouthEventListener;
import com.lilithsthrone.controller.eventListeners.buttons.ButtonMoveWestEventListener;
import com.lilithsthrone.controller.eventListeners.tooltips.TooltipHideEventListener;
import com.lilithsthrone.controller.eventListeners.tooltips.TooltipInformationEventListener;
import com.lilithsthrone.controller.eventListeners.tooltips.TooltipInventoryEventListener;
import com.lilithsthrone.controller.eventListeners.tooltips.TooltipMoveEventListener;
import com.lilithsthrone.controller.eventListeners.tooltips.TooltipResponseDescriptionEventListener;
import com.lilithsthrone.controller.eventListeners.tooltips.TooltipResponseMoveEventListener;
import com.lilithsthrone.game.Game;
import com.lilithsthrone.game.PropertyValue;
import com.lilithsthrone.game.character.CharacterChangeEventListener;
import com.lilithsthrone.game.character.FluidStored;
import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.attributes.AbstractAttribute;
import com.lilithsthrone.game.character.attributes.Attribute;
import com.lilithsthrone.game.character.body.CoverableArea;
import com.lilithsthrone.game.character.effects.AbstractPerk;
import com.lilithsthrone.game.character.effects.AbstractStatusEffect;
import com.lilithsthrone.game.character.effects.Perk;
import com.lilithsthrone.game.character.effects.StatusEffect;
import com.lilithsthrone.game.character.fetishes.AbstractFetish;
import com.lilithsthrone.game.character.fetishes.Fetish;
import com.lilithsthrone.game.character.gender.GenderNames;
import com.lilithsthrone.game.character.gender.GenderPronoun;
import com.lilithsthrone.game.character.npc.NPC;
import com.lilithsthrone.game.character.npc.dominion.Kay;
import com.lilithsthrone.game.character.npc.misc.Elemental;
import com.lilithsthrone.game.character.persona.NameTriplet;
import com.lilithsthrone.game.character.race.AbstractSubspecies;
import com.lilithsthrone.game.combat.moves.AbstractCombatMove;
import com.lilithsthrone.game.combat.spells.Spell;
import com.lilithsthrone.game.combat.spells.SpellSchool;
import com.lilithsthrone.game.dialogue.DialogueFlagValue;
import com.lilithsthrone.game.dialogue.DialogueNode;
import com.lilithsthrone.game.dialogue.DialogueNodeType;
import com.lilithsthrone.game.dialogue.companions.CompanionManagement;
import com.lilithsthrone.game.dialogue.companions.OccupantManagementDialogue;
import com.lilithsthrone.game.dialogue.npcDialogue.elemental.ElementalDialogue;
import com.lilithsthrone.game.dialogue.npcDialogue.offspring.GenericOffspringDialogue;
import com.lilithsthrone.game.dialogue.places.dominion.cityHall.CityHall;
import com.lilithsthrone.game.dialogue.places.dominion.cityHall.CityHallDemographics;
import com.lilithsthrone.game.dialogue.places.dominion.lilayashome.Library;
import com.lilithsthrone.game.dialogue.places.dominion.lilayashome.LilayaDressingRoomDialogue;
import com.lilithsthrone.game.dialogue.places.dominion.lilayashome.LilayaMilkingRoomDialogue;
import com.lilithsthrone.game.dialogue.places.dominion.lilayashome.RoomPlayer;
import com.lilithsthrone.game.dialogue.places.dominion.nightlife.NightlifeDistrict;
import com.lilithsthrone.game.dialogue.places.dominion.shoppingArcade.SuccubisSecrets;
import com.lilithsthrone.game.dialogue.places.dominion.slaverAlley.ScarlettsShop;
import com.lilithsthrone.game.dialogue.places.dominion.slaverAlley.SlaverAlleyDialogue;
import com.lilithsthrone.game.dialogue.places.dominion.warehouseDistrict.KaysWarehouse;
import com.lilithsthrone.game.dialogue.places.submission.dicePoker.DicePoker;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.responses.ResponseSex;
import com.lilithsthrone.game.dialogue.story.CharacterCreation;
import com.lilithsthrone.game.dialogue.utils.BodyChanging;
import com.lilithsthrone.game.dialogue.utils.CharacterModificationUtils;
import com.lilithsthrone.game.dialogue.utils.CharactersPresentDialogue;
import com.lilithsthrone.game.dialogue.utils.CombatMovesSetup;
import com.lilithsthrone.game.dialogue.utils.CosmeticsDialogue;
import com.lilithsthrone.game.dialogue.utils.DebugDialogue;
import com.lilithsthrone.game.dialogue.utils.EnchantmentDialogue;
import com.lilithsthrone.game.dialogue.utils.GiftDialogue;
import com.lilithsthrone.game.dialogue.utils.InventoryDialogue;
import com.lilithsthrone.game.dialogue.utils.InventoryInteraction;
import com.lilithsthrone.game.dialogue.utils.MiscDialogue;
import com.lilithsthrone.game.dialogue.utils.OptionsDialogue;
import com.lilithsthrone.game.dialogue.utils.PhoneDialogue;
import com.lilithsthrone.game.dialogue.utils.SpellManagement;
import com.lilithsthrone.game.dialogue.utils.UtilText;
import com.lilithsthrone.game.inventory.AbstractCoreItem;
import com.lilithsthrone.game.inventory.InventorySlot;
import com.lilithsthrone.game.inventory.clothing.AbstractClothing;
import com.lilithsthrone.game.inventory.item.AbstractItem;
import com.lilithsthrone.game.inventory.weapon.AbstractWeapon;
import com.lilithsthrone.game.occupantManagement.MilkingRoom;
import com.lilithsthrone.game.settings.KeyCodeWithModifiers;
import com.lilithsthrone.game.settings.KeyboardAction;
import com.lilithsthrone.game.sex.InitialSexActionInformation;
import com.lilithsthrone.game.sex.SexAreaInterface;
import com.lilithsthrone.game.sex.SexAreaOrifice;
import com.lilithsthrone.game.sex.SexAreaPenetration;
import com.lilithsthrone.game.sex.SexPace;
import com.lilithsthrone.game.sex.SexParticipantType;
import com.lilithsthrone.game.sex.SexType;
import com.lilithsthrone.game.sex.managers.SexManagerDefault;
import com.lilithsthrone.game.sex.positions.SexPosition;
import com.lilithsthrone.game.sex.positions.slots.SexSlotLyingDown;
import com.lilithsthrone.game.sex.sexActions.baseActions.PenisVagina;
import com.lilithsthrone.game.sex.sexActions.baseActionsMisc.PositioningMenu;
import com.lilithsthrone.main.GameState;
import com.lilithsthrone.main.Main;
import com.lilithsthrone.rendering.InformationTooltip;
import com.lilithsthrone.rendering.RenderSink;
import com.lilithsthrone.rendering.ImageCache;
import com.lilithsthrone.rendering.RenderingEngine;
import com.lilithsthrone.utils.Pathing;
import com.lilithsthrone.utils.Units;
import com.lilithsthrone.utils.Util;
import com.lilithsthrone.utils.Util.Value;
import com.lilithsthrone.utils.Vector2i;
import com.lilithsthrone.utils.colours.Colour;
import com.lilithsthrone.utils.colours.PresetColour;
import com.lilithsthrone.utils.time.DateAndTime;
import com.lilithsthrone.utils.time.DayPeriod;
import com.lilithsthrone.utils.time.SolarElevationAngle;
import com.lilithsthrone.world.Cell;
import com.lilithsthrone.world.WorldType;
import com.lilithsthrone.world.places.PlaceType;
import com.lilithsthrone.world.population.Population;

import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * @since 0.1.0
 * @version 0.4.6.4
 * @author Innoxia, Maxis010
 */
public class MainController implements Initializable, RenderSink {

	// FXML elements:
	@FXML
	private GridPane mainPane;
	@FXML
	private ListView<AbstractCoreItem> listViewInventoryCell, listViewInventoryPlayer;
	@FXML
	private VBox vBoxLeft;

	// UI-related elements:
	@FXML
	private WebView webViewMain, webViewAttributes, webViewRight, webViewButtonsLeft, webViewButtonsRight;

	static WebEngine webEngine;
	private WebEngine webEngineTooltip;
	private WebEngine webEngineAttributes;
	private WebEngine webEngineRight;
	private WebEngine webEngineButtonsLeft;
	private WebEngine webEngineButtonsRight;
	private WebView webviewTooltip;
	private Tooltip tooltip;
	private EventHandler<KeyEvent> actionKeyPressed, actionKeyReleased;

	private int tooltipWidth = 0;
	private int tooltipHeight = 0;
	
	// Responses:
	/** @deprecated read {@link RenderSink#RESPONSE_COUNT} instead; kept so existing desktop references resolve. */
	public static final int RESPONSE_COUNT = RenderSink.RESPONSE_COUNT;
	
	// Misc:
	private boolean allowInput;
	private KeyCode[] lastKeys;
	
	static Colour flashMessageColour = null;
	static String flashMessageText = null;

	java.net.CookieManager cookieManager = new java.net.CookieManager();
	
	// Hotkey binding:
	static KeyboardAction actionToBind;
	static boolean primaryBinding;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		allowInput = true;
		lastKeys = new KeyCode[5];

		actionToBind = null;
		primaryBinding = true;

		webviewTooltip = new WebView();
		webviewTooltip.setMaxWidth(400);
		webviewTooltip.setMaxHeight(400);
		webviewTooltip.getEngine().getHistory().setMaxSize(0);
		Scene scene = new Scene(webviewTooltip);
        scene.setFill(null);
		
		tooltip = new Tooltip();
		tooltip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		tooltip.setGraphic(webviewTooltip);
		tooltip.setMaxWidth(400);
		tooltip.setMaxHeight(400);
		tooltip.setAutoHide(true);
		tooltip.setConsumeAutoHidingEvents(false);

		vBoxLeft.getStyleClass().add("vbox");

		// Set up controls and buttons:
		setUpButtons();

		// Set up webViews:
		setUpWebViews();

		GameCharacter.addPlayerLocationChangeEventListener(new CharacterChangeEventListener() {
			@Override
			public void onChange() {
				if (GameState.game.getPlayer() != null) {
					GameState.game.getPlayer().discoverSurroundingCells();
					
					// Make sure that images of present characters are cached
					for (NPC character : GameState.game.getCharactersPresent()) {
						if (character.hasArtwork() && GameState.getProperties().hasValue(PropertyValue.artwork)) {
							ImageCache.INSTANCE.requestCache(character.getCurrentArtwork().getCurrentImage());
						}
					}
				}
			}
		});
		
		allowInput = true;
	}

	// All setup methods:
	// These stay on the RenderSink interface, but the logic itself now lives in core's Screens so
	// that non-JavaFX front-ends can open the same screens. Kept as thin delegates.
	@Override
	public void openOptions() {
		Screens.openOptions();
	}

	@Override
	public boolean isPhoneDisabled() {
		return Screens.isPhoneDisabled();
	}

	@Override
	public void openPhone() {
		Screens.openPhone();
	}

	@Override
	public void openPhone(DialogueNode toDialogue) {
		Screens.openPhone(toDialogue);
	}

	@Override
	public boolean isInventoryDisabled() {
		return Screens.isInventoryDisabled();
	}

	@Override
	public void openInventory() {
		Screens.openInventory();
	}

	@Override
	public void openInventory(NPC npc, InventoryInteraction interaction) {
		Screens.openInventory(npc, interaction);
	}

	@Override
	public void openCharactersPresent() {
		Screens.openCharactersPresent();
	}

	@Override
	public void openCharactersPresent(GameCharacter characterViewed) {
		Screens.openCharactersPresent(characterViewed);
	}

	public void zoomMap() {
		Screens.zoomMap();
	}

	/**
	 * Sets up buttons and hotkeys.
	 */
	public List<KeyCode> buttonsPressed = new ArrayList<>();

	private void setUpButtons() {
		// HOTKEYS:
		actionKeyPressed = new EventHandler<KeyEvent>() {
			
			private Map.Entry<KeyboardAction, KeyCodeWithModifiers> findExistingBinding(Map<KeyboardAction, KeyCodeWithModifiers> bindings, KeyEvent lookingFor){
				return bindings.entrySet()
					.stream()
					.filter(entry -> entry.getValue() != null)
					.filter(entry -> KeyCodes.matches(entry.getValue(), lookingFor))
					.findFirst().orElse(null);
			}
			
			private void printAlreadyExistingBinding(String primarySecondary, String actionName, String eventCodeName) {
				GameState.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + "<b style='color:" + PresetColour.GENERIC_BAD.toWebHexString() + ";'>The key '" + eventCodeName
						+ "' is already the " + primarySecondary + " bind for the action '" + actionName + "'!</b>" + "</p>");
				GameState.game.setContent(new Response("", "", GameState.game.getCurrentDialogueNode()));
			}
			
			private boolean handleExistingBindings(Map<KeyboardAction, KeyCodeWithModifiers> bindings, KeyEvent lookingFor, String primarySecondary) {
				Map.Entry<KeyboardAction, KeyCodeWithModifiers> existingBinding = findExistingBinding(bindings, lookingFor);
				boolean hasExistingBinding = existingBinding != null;
				if (hasExistingBinding) {
					actionToBind = null;
					printAlreadyExistingBinding(primarySecondary, existingBinding.getKey().getName(), existingBinding.getValue().getFullName());
				}
				return hasExistingBinding;
			}
			
			public void handle(KeyEvent event) {
				if (allowInput) {
					// Hotkey bindings:
					if (GameState.game.getCurrentDialogueNode() == OptionsDialogue.KEYBINDS) {
						if (actionToBind != null) {
							KeyCode eventCode = event.getCode();
							
							//System.out.println(eventCode.getName()+" "+actionToBind);
							
							if (eventCode == KeyCode.SHIFT || eventCode == KeyCode.CONTROL) {
								return; // these are explicitly blocked to allow SHIFT + key and CTRL + key
							}
							
							if (handleExistingBindings(GameState.getProperties().hotkeyMapPrimary, event, "primary")
								|| handleExistingBindings(GameState.getProperties().hotkeyMapSecondary, event, "secondary")) {
								return; //such a binding already exists
							}
							KeyCodeWithModifiers newBinding = new KeyCodeWithModifiers(KeyCodes.toLT(eventCode), event.isControlDown(), event.isShiftDown());
							if (primaryBinding)
								GameState.getProperties().hotkeyMapPrimary.put(actionToBind, newBinding);
							else
								GameState.getProperties().hotkeyMapSecondary.put(actionToBind, newBinding);
							actionToBind = null;
							GameState.saveProperties();
							GameState.game.setContent(new Response("", "", GameState.game.getCurrentDialogueNode()));
							return;
						}
						
					} else {
						actionToBind = null;
					}
					
					if (!buttonsPressed.contains(event.getCode())) {
						buttonsPressed.add(event.getCode());

						System.arraycopy(lastKeys, 0, lastKeys, 1, 4);
						lastKeys[0] = event.getCode();
						checkLastKeys();
						
						if(event.getCode()==KeyCode.END && GameState.DEBUG){
							
//							RandomEnchantment.initAllRandomEnchantments();
//							for(RandomEnchantment enchantment : RandomEnchantment.getAllNegativeClothingEnchantments()) {
//								AbstractClothing ring = GameState.game.getItemGen().generateClothing("innoxia_japanese_kanzashi", false);
//								enchantment.applyEffects(ring);
//								ring.setEnchantmentKnown(null, true);
//								GameState.game.getPlayerCell().getInventory().addClothing(ring);
//							}
							
//							GameState.game.getPlayerCell().getInventory().addClothing(GameState.game.getItemGen().generateClothing("innoxia_bdsm_ornate_chastity_cage", true));
//							GameState.game.getPlayerCell().getInventory().addClothing(GameState.game.getItemGen().generateClothing("innoxia_neck_cuff_choker_necklace_triple", true));
							
							
//							GameState.game.getNpc(Kate.class).addLipstickMarking(GameState.game.getPlayer(), InventorySlot.STOMACH, PresetColour.COVERING_BLACK);
//							GameState.game.getNpc(Kate.class).addLipstickMarking(GameState.game.getPlayer(), InventorySlot.TORSO_OVER, PresetColour.COVERING_PINK);
//							GameState.game.getNpc(Kate.class).addLipstickMarking(GameState.game.getPlayer(), InventorySlot.MOUTH, PresetColour.COVERING_RED);
//							GameState.game.getNpc(Kate.class).addLipstickMarking(GameState.game.getPlayer(), InventorySlot.GROIN, PresetColour.COVERING_BLUE);
//							GameState.game.getNpc(Kate.class).addLipstickMarking(GameState.game.getPlayer(), InventorySlot.GROIN, PresetColour.COVERING_GOLD);
//							GameState.game.getNpc(Kate.class).addLipstickMarking(GameState.game.getPlayer(), InventorySlot.GROIN, PresetColour.COVERING_WHITE);
							
//							int[] ints = new int[] {105, 159, 1090, 1792, 10_050, 10_995};
//							for(int i : ints) {
//								System.out.println(i+": "+Util.intToString(i));
//							}
							
//							for(int i=0; i<100; i++) {
//								GenericSexualPartner npc = new GenericSexualPartner();
//								try {
//									GameState.game.addNPC(npc, false);
//									npc.setRandomLocation(WorldType.getWorldTypeFromId("innoxia_fields_elis_tavern_taur"), PlaceType.getPlaceTypeFromId("innoxia_fields_elis_tavern_taur_tables"));
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//							}
							
//							int rndInt = Util.random.nextInt();
//							System.out.println(rndInt+ " = " +Util.intToIndividualNumbersString(rndInt));
							
//							if(GameState.game.isInSex()) {
//								System.out.println(GameState.sex.getInitialSexManager().isHidden(GameState.game.getPlayer()));
//								GameState.sex.getTargetedPartner(GameState.game.getPlayer()).setForeplayPreference(GameState.game.getPlayer(), new SexType(SexAreaOrifice.ANUS, SexAreaPenetration.TONGUE));
//								GameState.sex.getTargetedPartner(GameState.game.getPlayer()).setMainSexPreference(GameState.game.getPlayer(), new SexType(SexAreaPenetration.PENIS, SexAreaOrifice.MOUTH));
//								GameState.sex.recalculateSexActions();
//							}
							
//							for(GameCharacter partner : GameState.sex.getAllParticipants()) {
//								if(!partner.isPlayer()) {
//									System.out.println();
//									System.out.println("#####################################");
//									System.out.println("##### "+partner.getName()+" #####");
//									for(SexActionInterface sa : GameState.sex.getActionsAvailablePartner(partner, GameState.game.getPlayer())) {
//										System.out.println(sa.toString()+" | "+ sa.getActionTitle());
//									}
//								}
//							}
							
//							for(Colour c : ColourListPresets.ALL) {
//								System.out.println(c.getId());
//							}

//							System.out.println(GameState.game.getSavedDialogueNode()==null);
							
//							System.out.println(GameState.isVersionOlderThan(Game.loadingVersion, "0.4.8.10"));
							
//							if(GameState.game.isInSex()) {
//								for(GameCharacter c : GameState.sex.getAllParticipants()) {
//									if(c.hasPenis()) {
//										c.equipClothingFromNowhere(GameState.game.getItemGen().generateClothing("innoxia_penis_condom_super_strong", false), true, c);
//									}
//								}
//							}
							
//							System.out.println(PenisMouth.BLOWJOB_START.isBaseRequirementsMet());
							
//							System.out.println(GameState.sex.getOrgasmActionsPlayer().contains(GenericOrgasms.GENERIC_ORGASM_SELF_FACE));
//							System.out.println(GameState.sex.getOrgasmActionsPlayer().contains(GenericOrgasms.GENERIC_ORGASM_CREAMPIE));
							
//							System.out.println(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
							
							// Testing genetics/inheritance:
//							GameState.game.getPlayer().guaranteePregnancyOnNextRoll();
//							GameState.game.getPlayer().rollForPregnancy(GameState.game.getCharactersPresent().get(0), 100, true);
//							for(OffspringSeed os : GameState.game.getPlayer().getPregnantLitter().getOffspringSeed()) {
//								Covering covering = os.getBody().getCovering(os.getBody().getTorsoType().getBodyCoveringType(os.getBody()), false);
//								System.out.println(os.getName()+": "+os.getBody().getRaceStage().getName()+" "+os.getRace().getName(os.getBody(), os.isFeral()));
//								System.out.println("Primary: "+covering.getPrimaryColour().getName());
//								System.out.println("Secondary: "+covering.getPrimaryColour().getName());
//								
//								covering = os.getBody().getCovering(os.getBody().getEyeType().getBodyCoveringType(os.getBody()), false);
//								System.out.println("Eyes: "+covering.getPrimaryColour().getName());
//								
//								covering = os.getBody().getCovering(os.getBody().getHairType().getBodyCoveringType(os.getBody()), false);
//								System.out.println("Hair: "+covering.getPrimaryColour().getName());
//							}
							
//							GameState.game.getCharactersPresent().get(0).incrementAttribute(Attribute.MAJOR_CORRUPTION, 100);
//							GameState.game.getCharactersPresent().get(0).addFetish(Fetish.FETISH_BONDAGE_APPLIER);
							
//							GameState.game.getCharactersPresent().get(0).addClothing(GameState.game.getItemGen().generateClothing("innoxia_penis_condom", PresetColour.CLOTHING_PINK, false), 2, false, false);
//							GameState.game.getCharactersPresent().get(0).removeFetish(Fetish.FETISH_CUM_STUD);
//							GameState.game.getCharactersPresent().get(0).setFetishDesire(Fetish.FETISH_CUM_STUD, FetishDesire.TWO_NEUTRAL);
							
//							GameState.game.getPlayer().incrementFetishExperience(Fetish.FETISH_DOMINANT, 200);
//							if(GameState.game.isInSex()) {
//								for(GameCharacter c : GameState.sex.getSubmissiveSpectators()) {
//									c.setArousal(98);
//									c.setLust(98);
//								}
//							}
//							System.out.println("START");
//							Util.addDrunkSlur(Util.addMuffle(Util.addBimbo(
//									"Hello, my name is [rose.name], would you like to see my [rose.hands(true)]? #IF(nyan.isFeminine())Or my [rose.breasts+]?#ENDIF Or should I call [lilaya.name]? Answer me now.",
//									4), 4), 4);
//							System.out.println("END");
							
//							System.out.println("-----");
//							for(NameTriplet nameTriplet : Name.getAllNameTriplets(Subspecies.getSubspeciesFromId("innoxia_hyena_subspecies_spotted"))) {
//								System.out.println(nameTriplet.getMasculine()+", "+nameTriplet.getAndrogynous()+", "+nameTriplet.getFeminine());
//							}
//							System.out.println("-----");
//							for(NameTriplet nameTriplet : Name.getAllNameTriplets(Subspecies.getSubspeciesFromId("innoxia_hyena_subspecies_striped"))) {
//								System.out.println(nameTriplet.getMasculine()+", "+nameTriplet.getAndrogynous()+", "+nameTriplet.getFeminine());
//							}
							
//							System.out.println("------");
//							for(AbstractCombatMove move : GameState.game.getPlayer().getEquippedMoves()) {
//								System.out.println(move.getName(0, GameState.game.getPlayer()));
//							}
							
//							System.out.println("--- Penis diameters ---");
//							for(int i=10; i<75; i+=5) {
//								System.out.println(i+" : "+Penis.getGenericDiameter(i, PenetrationGirth.THREE_AVERAGE));
//							}
							
//							if(GameState.game.isInSex()) {
//								System.out.println("----");
//								for(GameCharacter character : GameState.sex.getAllParticipants()) {
//									System.out.println(character.getName()+" -> "+GameState.sex.getTargetedPartner(character).getName());
//								}
//							}

//							for(NPC npc : GameState.game.getAllNPCs()) {
//								if(npc.isUnique() && !npc.hasArtwork()
////										&& (npc.getWorldLocation().getWorldRegion()==WorldRegion.DOMINION)
////										&& npc.isFeminine()
//										&& npc.getFaceType().getBodyCoveringType(npc).getCategory()==BodyCoveringCategory.MAIN_SKIN
////										&& npc.isAbleToBeImpregnated()
////										&& npc.isFeminine()
////										&& (npc.getClass().getName().contains("dominion.") || npc.getClass().getName().contains("submission."))
//										) {
//									System.out.println(UtilText.parse(npc, "[npc.Race] ([npc.raceStage]) [npc.name(true)] [npc.surname]")+" | "+npc.getClass().getName().split(".npc.")[1]+" ("+npc.getHomeWorldLocation().getName()+")");
//								}
//							}
							
//							Map<AbstractSubspecies, Integer> subCountMap = new HashMap<>();
//							Map<AbstractSubspecies, Integer> subHDCountMap = new HashMap<>();
//							for(NPC npc : GameState.game.getAllNPCs()) {
//								if(npc.isUnique()) {
//									if(npc.getSubspecies()==Subspecies.HALF_DEMON) {
//										AbstractSubspecies ss = npc.getHalfDemonSubspecies();
//										subHDCountMap.putIfAbsent(ss, 0);
//										subHDCountMap.put(ss, subHDCountMap.get(ss)+1);
//										
//									} else {
//										AbstractSubspecies ss = npc.getTrueSubspecies();
//										subCountMap.putIfAbsent(ss, 0);
//										subCountMap.put(ss, subCountMap.get(ss)+1);
//									}
//								}
//							}
//							for(AbstractSubspecies ss : Subspecies.getAllSubspecies()) {
//								subCountMap.putIfAbsent(ss, 0);
//								subHDCountMap.putIfAbsent(ss, 0);
//								System.out.println(ss.getName(null)+": "+subCountMap.get(ss)+" "+subHDCountMap.get(ss));
//							}
							
//							GameState.game.getPlayer().incrementPerkCategoryPoints(PerkCategory.PHYSICAL, 1);
//							GameState.game.getPlayer().incrementPerkCategoryPoints(PerkCategory.ARCANE, 1);
//							GameState.game.getPlayer().incrementPerkCategoryPoints(PerkCategory.LUST, 1);
//							for(NPC npc : GameState.game.getCharactersTreatingCellAsHome()) {
//								System.out.println(npc.getNameIgnoresPlayerKnowledge()+npc.getClass().getName());
//							}
//							GameState.sex.getTargetedPartner(GameState.game.getPlayer()).generateSexChoices(true, GameState.game.getPlayer());
//							GameState.game.getPlayer().setFeral(Subspecies.HORSE_MORPH);
//							UtilText.addSpecialParsingString("true", true);
//							System.out.println(UtilText.parse("#IF([#SPECIAL_PARSE_0]):3#ELSE:(#ENDIF"));
//							System.out.println(GameState.sex.isSexStarted());
						}
						

						// Escape Menu:
						if (keyEventMatchesBindings(KeyboardAction.MENU, event)) {
							openOptions();
						}
						
						// Misc.:
						if (keyEventMatchesBindings(KeyboardAction.FULL_SCREEN, event)) {
							Main.primaryStage.setFullScreenExitHint("Press 'Esc' or '"+KeyboardAction.FULL_SCREEN.getPrimaryDefault().getFullName()+"' to exit fullscreen mode.");
							Main.primaryStage.setFullScreen(!Main.primaryStage.isFullScreen());
						}
						
						// Movement:
						if (keyEventMatchesBindings(KeyboardAction.MOVE_NORTH, event)) {
							if (!GameState.game.getCurrentDialogueNode().isTravelDisabled()) {
								moveNorth();
							} else {
								GameState.game.responseNavigationUp();
							}
						}
						if (keyEventMatchesBindings(KeyboardAction.MOVE_WEST, event)) {
							if (!GameState.game.getCurrentDialogueNode().isTravelDisabled()) {
								moveWest();
							} else {
								GameState.game.responseNavigationLeft();
							}
						}
						if (keyEventMatchesBindings(KeyboardAction.MOVE_SOUTH, event)) {
							if (!GameState.game.getCurrentDialogueNode().isTravelDisabled()) {
								moveSouth();
							} else {
								GameState.game.responseNavigationDown();
							}
						}
						if (keyEventMatchesBindings(KeyboardAction.MOVE_EAST, event)) {
							if (!GameState.game.getCurrentDialogueNode().isTravelDisabled()) {
								moveEast();
							} else {
								GameState.game.responseNavigationRight();
							}
						}
						if (keyEventMatchesBindings(KeyboardAction.MOVE_RESPONSE_CURSOR_NORTH, event)) {
							GameState.game.responseNavigationUp();
						}
						if (keyEventMatchesBindings(KeyboardAction.MOVE_RESPONSE_CURSOR_WEST, event)) {
							GameState.game.responseNavigationLeft();
						}
						if (keyEventMatchesBindings(KeyboardAction.MOVE_RESPONSE_CURSOR_SOUTH, event)) {
							GameState.game.responseNavigationDown();
						}
						if (keyEventMatchesBindings(KeyboardAction.MOVE_RESPONSE_CURSOR_EAST, event)) {
							GameState.game.responseNavigationRight();
						}

						// Game stuff:
						if (keyEventMatchesBindings(KeyboardAction.QUICKSAVE, event)) {
							GameState.quickSaveGame();
						}
						if (keyEventMatchesBindings(KeyboardAction.QUICKLOAD, event)) {
							GameState.quickLoadGame();
						}
						
						boolean allowInput = true;
						boolean enterConsumed = false;
						
						// Name selections:
						if(GameState.game.getCurrentDialogueNode() == CharacterCreation.CHOOSE_NAME || GameState.game.getCurrentDialogueNode() == CityHallDemographics.NAME_CHANGE){
							if((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('nameMasculineInput') === document.activeElement")
									|| (boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('nameAndrogynousInput') === document.activeElement")
									|| (boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('nameFeminineInput') === document.activeElement")) {
								allowInput = false;
								if (event.getCode() == KeyCode.ENTER) {
									enterConsumed = true;
									GameState.game.setContent(1);
								}
							}
							if((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('surnameInput') === document.activeElement")) {
								allowInput = false;
								if (event.getCode() == KeyCode.ENTER) {
									enterConsumed = true;
									GameState.game.setContent(1);
								}
							}
						}
						if(GameState.game.getCurrentDialogueNode() == OccupantManagementDialogue.ROOM_UPGRADES){
							if((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('nameInput') === document.activeElement")) {
								allowInput = false;
								if (event.getCode() == KeyCode.ENTER) {
									enterConsumed = true;
									boolean unsuitableName = false;
									if(Main.mainController.getWebEngine().executeScript("document.getElementById('nameInput')")!=null) {
										
										Main.mainController.getWebEngine().executeScript("document.getElementById('hiddenFieldName').innerHTML=document.getElementById('nameInput').value;");
										if(Main.mainController.getWebEngine().getDocument()!=null) {
											unsuitableName = Main.mainController.getWebEngine().getDocument().getElementById("hiddenFieldName").getTextContent().length() < 1
															|| Main.mainController.getWebEngine().getDocument().getElementById("hiddenFieldName").getTextContent().length() > 32;
										}
										
										if (!unsuitableName) {
											GameState.game.setContent(new Response("Rename Room", "Rename this room to whatever you've entered in the text box.", GameState.game.getCurrentDialogueNode()){
												@Override
												public void effects() {
													GameState.game.getPlayerCell().getPlace().setName(Main.mainController.getWebEngine().getDocument().getElementById("hiddenFieldName").getTextContent());
												}
											});
										} else {
											GameState.game.setContent(new Response("Rename Room", "", GameState.game.getCurrentDialogueNode()));
										}
									}
								}
							}
						}
						if(GameState.game.getCurrentDialogueNode() == OptionsDialogue.SAVE_LOAD){
							if((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('new_save_name') === document.activeElement")) {
								allowInput = false;
								if (event.getCode() == KeyCode.ENTER) {
									enterConsumed = true;
									Main.mainController.getWebEngine().executeScript("document.getElementById('hiddenPField').innerHTML=document.getElementById('new_save_name').value;");
									if(GameState.isSaveGameAvailable()) {
										GameState.saveGame(Main.mainController.getWebEngine().getDocument().getElementById("hiddenPField").getTextContent(), false, false);
									}
									GameState.game.setContent(new Response("Save", "", GameState.game.getCurrentDialogueNode()));
								}
							}
						}
						if(GameState.game.getCurrentDialogueNode() == EnchantmentDialogue.ENCHANTMENT_MENU){
							if((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('output_name') === document.activeElement")) {
								allowInput = false;
								if (event.getCode() == KeyCode.ENTER) {
									enterConsumed = true;
									Main.mainController.getWebEngine().executeScript("document.getElementById('hiddenPField').innerHTML=document.getElementById('output_name').value;");
									EnchantmentDialogue.setOutputName(Main.mainController.getWebEngine().getDocument().getElementById("hiddenPField").getTextContent());
									GameState.game.setContent(new Response("", "", GameState.game.getCurrentDialogueNode()));
								}
							}
						}
						if(GameState.game.getCurrentDialogueNode() == EnchantmentDialogue.ENCHANTMENT_SAVE_LOAD){
							if((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('new_save_name') === document.activeElement")) {
								allowInput = false;
								if (event.getCode() == KeyCode.ENTER) {
									enterConsumed = true;
									Main.mainController.getWebEngine().executeScript("document.getElementById('hiddenPField').innerHTML=document.getElementById('new_save_name').value;");
									EnchantmentDialogue.saveEnchant(Main.mainController.getWebEngine().getDocument().getElementById("hiddenPField").getTextContent(), false, EnchantmentDialogue.ENCHANTMENT_SAVE_LOAD);
									GameState.game.setContent(new Response("Save", "", GameState.game.getCurrentDialogueNode()));
								}
							}
						}
						if(GameState.game.getCurrentDialogueNode() == BodyChanging.BODY_CHANGING_SAVE_LOAD){
							if((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('new_save_name') === document.activeElement")) {
								allowInput = false;
								if (event.getCode() == KeyCode.ENTER) {
									enterConsumed = true;
									Main.mainController.getWebEngine().executeScript("document.getElementById('hiddenPField').innerHTML=document.getElementById('new_save_name').value;");
									BodyChanging.saveBody(Main.mainController.getWebEngine().getDocument().getElementById("hiddenPField").getTextContent(), false);
									GameState.game.setContent(new Response("Save", "", GameState.game.getCurrentDialogueNode()));
								}
							}
						}
						if(GameState.game.getCurrentDialogueNode() == CosmeticsDialogue.TATTOO_SAVE_LOAD){
							if((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('new_save_name') === document.activeElement")) {
								allowInput = false;
								if (event.getCode() == KeyCode.ENTER) {
									enterConsumed = true;
									Main.mainController.getWebEngine().executeScript("document.getElementById('hiddenPField').innerHTML=document.getElementById('new_save_name').value;");
									EnchantmentDialogue.saveEnchant(Main.mainController.getWebEngine().getDocument().getElementById("hiddenPField").getTextContent(), false, CosmeticsDialogue.TATTOO_SAVE_LOAD);
									GameState.game.setContent(new Response("Save", "", GameState.game.getCurrentDialogueNode()));
								}
							}
						}
						if(GameState.game.getCurrentDialogueNode() == SuccubisSecrets.SHOP_BEAUTY_SALON_TATTOOS_ADD
								|| GameState.game.getCurrentDialogueNode() == CompanionManagement.SLAVE_MANAGEMENT_TATTOOS_ADD
								|| GameState.game.getCurrentDialogueNode() == CharacterCreation.CHOOSE_ADVANCED_APPEARANCE_TATTOOS_ADD
								|| GameState.game.getCurrentDialogueNode() == CosmeticsDialogue.BEAUTICIAN_TATTOOS_ADD){
							if((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('tattoo_name') === document.activeElement")) {
								allowInput = false;
								if (event.getCode() == KeyCode.ENTER) {
									enterConsumed = true;
									Main.mainController.getWebEngine().executeScript("document.getElementById('hiddenPField').innerHTML=document.getElementById('tattoo_name').value;");
									CharacterModificationUtils.tattoo.getWriting().setText(Main.mainController.getWebEngine().getDocument().getElementById("hiddenPField").getTextContent());
									GameState.game.setContent(new Response("", "", GameState.game.getCurrentDialogueNode()));
								}
							}
						}
						if(GameState.game.getCurrentDialogueNode() == CompanionManagement.OCCUPANT_CHOOSE_NAME
								|| GameState.game.getCurrentDialogueNode() == ScarlettsShop.HELENAS_SHOP_CUSTOM_SLAVE_PERSONALITY
								|| GameState.game.getCurrentDialogueNode() == KaysWarehouse.KAY_OFFICE_DOMINATE_NAMING
								|| GameState.game.getCurrentDialogueNode() == ElementalDialogue.ELEMENTAL_CHOOSE_NAME) {
							
							GameCharacter slave = GameState.game.getDialogueFlags().getManagementCompanion();
							if(GameState.game.getCurrentDialogueNode() == ScarlettsShop.HELENAS_SHOP_CUSTOM_SLAVE_PERSONALITY) {
								slave = BodyChanging.getTarget();
							} else if(GameState.game.getCurrentDialogueNode() == KaysWarehouse.KAY_OFFICE_DOMINATE_NAMING) {
								slave = GameState.game.getNpc(Kay.class);
							} else if(GameState.game.getCurrentDialogueNode() == ElementalDialogue.ELEMENTAL_CHOOSE_NAME) {
								slave = GameState.game.getPlayer().getElemental();
							}
							GameCharacter slaveFinal = slave;
							
							if((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('slaveToPlayerNameInput') === document.activeElement")) {
								allowInput = false;
								if(event.getCode() == KeyCode.ENTER) {
									enterConsumed = true;
									boolean unsuitableName = false;
								 	if(Main.mainController.getWebEngine().executeScript("document.getElementById('slaveToPlayerNameInput')")!=null) {
									 
										Main.mainController.getWebEngine().executeScript("document.getElementById('hiddenFieldName').innerHTML=document.getElementById('slaveToPlayerNameInput').value;");
										if(Main.mainController.getWebEngine().getDocument()!=null) {
											unsuitableName = Main.mainController.getWebEngine().getDocument().getElementById("hiddenFieldName").getTextContent().length() < 1
															|| Main.mainController.getWebEngine().getDocument().getElementById("hiddenFieldName").getTextContent().length() > 32;
										}
										
										if(!unsuitableName) {
											GameState.game.setContent(new Response("Rename", "", GameState.game.getCurrentDialogueNode()){
												@Override
												public void effects() {
													slaveFinal.setPetName(GameState.game.getPlayer(), Main.mainController.getWebEngine().getDocument().getElementById("hiddenFieldName").getTextContent());
												}
											});
										} else {
											GameState.game.setContent(new Response("Rename", "", GameState.game.getCurrentDialogueNode()));
										}
										
									}
								}
							}
							if(((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('slaveNameInput') === document.activeElement"))) {
								allowInput = false;
								if (event.getCode() == KeyCode.ENTER) {
									enterConsumed = true;
									boolean unsuitableName = false;
								 	if(Main.mainController.getWebEngine().executeScript("document.getElementById('slaveNameInput')")!=null) {
									 
										Main.mainController.getWebEngine().executeScript("document.getElementById('hiddenFieldName').innerHTML=document.getElementById('slaveNameInput').value;");
										if(Main.mainController.getWebEngine().getDocument()!=null) {
											unsuitableName = Main.mainController.getWebEngine().getDocument().getElementById("hiddenFieldName").getTextContent().length() < 1
															|| Main.mainController.getWebEngine().getDocument().getElementById("hiddenFieldName").getTextContent().length() > 32;
										}
										
										if (!unsuitableName) {
											GameState.game.setContent(new Response("Rename", "", GameState.game.getCurrentDialogueNode()){
												@Override
												public void effects() {
													slaveFinal.setName(new NameTriplet(Main.mainController.getWebEngine().getDocument().getElementById("hiddenFieldName").getTextContent()));
												}
											});
										} else {
											GameState.game.setContent(new Response("Rename", "", GameState.game.getCurrentDialogueNode()));
										}
									}
								}
							}
							if(((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('slaveSurnameInput') === document.activeElement"))) {
								allowInput = false;
								if (event.getCode() == KeyCode.ENTER) {
									enterConsumed = true;
									boolean unsuitableName = false;
								 	if(Main.mainController.getWebEngine().executeScript("document.getElementById('slaveSurnameInput')")!=null) {
									 
										Main.mainController.getWebEngine().executeScript("document.getElementById('hiddenFieldName').innerHTML=document.getElementById('slaveSurnameInput').value;");
										if(Main.mainController.getWebEngine().getDocument()!=null) {
											unsuitableName = Main.mainController.getWebEngine().getDocument().getElementById("hiddenFieldName").getTextContent().length() < 1
															|| Main.mainController.getWebEngine().getDocument().getElementById("hiddenFieldName").getTextContent().length() > 32;
										}
										
										if (!unsuitableName) {
											GameState.game.setContent(new Response("Rename", "", GameState.game.getCurrentDialogueNode()){
												@Override
												public void effects() {
													slaveFinal.setSurname(Main.mainController.getWebEngine().getDocument().getElementById("hiddenFieldName").getTextContent());
												}
											});
										} else {
											GameState.game.setContent(new Response("Rename", "", GameState.game.getCurrentDialogueNode()));
										}
									}
								}
							}
						}
						
						//TODO for some reason, if this catches a ResponseEffectsOnly and sets content using that, it blanks the main page. I have absolutely no idea why.
						if(GameState.game.getCurrentDialogueNode() == OptionsDialogue.OPTIONS_PRONOUNS) {
							allowInput = false;
							if (event.getCode() == KeyCode.ENTER) {
								enterConsumed = true;
								boolean found = false;
								for(GenderPronoun gp : GenderPronoun.values()) {
									if((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('feminine_" + gp + "') === document.activeElement")
										|| (boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('masculine_" + gp + "') === document.activeElement")) {
										GameState.game.setContent(
												new Response("Save", "Save all the pronouns that are currently displayed.", OptionsDialogue.OPTIONS_PRONOUNS) {
													@Override
													public void effects() {
														OptionsDialogue.OPTIONS_PRONOUNS.getResponse(0, 1).effects();
													}
												});
										found = true;
										break;
									}
								}
								if(!found) {
									for(GenderNames genderName : GenderNames.values()) {
										if((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('GENDER_NAME_MASCULINE_" + genderName + "') === document.activeElement")
											|| (boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('GENDER_NAME_ANDROGYNOUS_" + genderName + "') === document.activeElement")
											|| (boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('GENDER_NAME_FEMININE_" + genderName + "') === document.activeElement")) {
											GameState.game.setContent(
													new Response("Save", "Save all the pronouns that are currently displayed.", OptionsDialogue.OPTIONS_PRONOUNS) {
														@Override
														public void effects() {
															OptionsDialogue.OPTIONS_PRONOUNS.getResponse(0, 1).effects();
														}
													});
										}
									}
								}
							}
						}
						
						if(((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('offspringPetNameInput') === document.activeElement"))) {
							allowInput = false;
							if (event.getCode() == KeyCode.ENTER) {
								enterConsumed = true;
								boolean unsuitableName = false;
							 	if(Main.mainController.getWebEngine().executeScript("document.getElementById('offspringPetNameInput')")!=null) {
								 
									Main.mainController.getWebEngine().executeScript("document.getElementById('hiddenFieldName').innerHTML=document.getElementById('offspringPetNameInput').value;");
									if(Main.mainController.getWebEngine().getDocument()!=null) {
										if (Main.mainController.getWebEngine().getDocument().getElementById("hiddenFieldName").getTextContent().length() < 2
												|| Main.mainController.getWebEngine().getDocument().getElementById("hiddenFieldName").getTextContent().length() > 32)
											unsuitableName = true;
										else {
											unsuitableName = false;
										}
									}
									
									if (!unsuitableName) {
										GameState.game.setContent(new Response("Rename", "", GameState.game.getCurrentDialogueNode()){
											@Override
											public void effects() {
												GameState.game.getActiveNPC().setPetName(GameState.game.getPlayer(), Main.mainController.getWebEngine().getDocument().getElementById("hiddenFieldName").getTextContent());
											}
										});
									} else {
										GameState.game.setContent(new Response("Rename", "", GameState.game.getCurrentDialogueNode()));
									}
									
								}
							}
						}

						if(GameState.game.getCurrentDialogueNode() == LilayaDressingRoomDialogue.OUTFIT_EDITOR){
							if((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('outfit_name') === document.activeElement")) {
								allowInput = false;
								if (event.getCode() == KeyCode.ENTER) {
									enterConsumed = true;
									Main.mainController.getWebEngine().executeScript("document.getElementById('hiddenPField').innerHTML=document.getElementById('outfit_name').value;");
									LilayaDressingRoomDialogue.setOutfitName(Main.mainController.getWebEngine().getDocument().getElementById("hiddenPField").getTextContent());
									GameState.game.setContent(new Response("Outfit editor", "", GameState.game.getCurrentDialogueNode()));
								}
							}
						}
						if(GameState.game.getCurrentDialogueNode() == LilayaDressingRoomDialogue.OUTFIT_EDITOR_ITEM_ENCHANT){
							if((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('output_name') === document.activeElement")) {
								allowInput = false;
								if (event.getCode() == KeyCode.ENTER) {
									enterConsumed = true;
									Main.mainController.getWebEngine().executeScript("document.getElementById('hiddenPField').innerHTML=document.getElementById('output_name').value;");
									LilayaDressingRoomDialogue.setOutputName(Main.mainController.getWebEngine().getDocument().getElementById("hiddenPField").getTextContent());
									GameState.game.setContent(new Response("", "", GameState.game.getCurrentDialogueNode()));
								}
							}
						}
						
						if(GameState.game.getCurrentDialogueNode() == DebugDialogue.PARSER){
							if((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('parseInput') === document.activeElement")
									|| (boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('xmlTest') === document.activeElement")) {
								allowInput = false;
							}
						}

						// Allow users to type in slave metadata without getting whisked away.
						if((boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('SET_SLAVE_NOTES') === document.activeElement")
						|| (boolean) Main.mainController.getWebEngine().executeScript("document.getElementById('SET_SLAVE_CATEGORY') === document.activeElement")) {
							allowInput = false;
						}
						
						if(allowInput){
							if (keyEventMatchesBindings(KeyboardAction.INVENTORY, event)) {
								openInventory();
							}
							if (keyEventMatchesBindings(KeyboardAction.JOURNAL, event)) {
								openPhone();
							}
							if (keyEventMatchesBindings(KeyboardAction.MAP, event)) {
								openPhone(PhoneDialogue.MAP);
							}
							if (keyEventMatchesBindings(KeyboardAction.CHARACTERS, event)) {
								openCharactersPresent(null);
							}
							if (keyEventMatchesBindings(KeyboardAction.ZOOM, event)) {
								zoomMap();
							}
	
							if (keyEventMatchesBindings(KeyboardAction.SCROLL_UP, event))
								Main.mainController.getWebEngine().executeScript("document.getElementById('main-content').scrollTop -= 50");
							if (keyEventMatchesBindings(KeyboardAction.SCROLL_DOWN, event))
								Main.mainController.getWebEngine().executeScript("document.getElementById('main-content').scrollTop += 50");
							
							// Responses:
							KeyboardAction[] keyboardActionsForResponses =
								{
										KeyboardAction.RESPOND_0, KeyboardAction.RESPOND_1, KeyboardAction.RESPOND_2, KeyboardAction.RESPOND_3, KeyboardAction.RESPOND_4,
										KeyboardAction.RESPOND_5, KeyboardAction.RESPOND_6, KeyboardAction.RESPOND_7, KeyboardAction.RESPOND_8, KeyboardAction.RESPOND_9,
										KeyboardAction.RESPOND_10, KeyboardAction.RESPOND_11, KeyboardAction.RESPOND_12, KeyboardAction.RESPOND_13, KeyboardAction.RESPOND_14
								};
							
							for (int i = 0; i < keyboardActionsForResponses.length; i++) {
								if (keyEventMatchesBindings(keyboardActionsForResponses[i], event)) {
									processResponse(i);
								}
							}
							
							if (keyEventMatchesBindings(KeyboardAction.MENU_SELECT, event)) {
								if(event.getCode() == KeyCode.ENTER) {
									if(!enterConsumed) {
										GameState.game.setContent(GameState.game.getResponsePointer());
									}
								} else {
									GameState.game.setContent(GameState.game.getResponsePointer());
								}
							}
							
						}
						
						// Next/Previous response tab:
						if (keyEventMatchesBindings(KeyboardAction.RESPOND_NEXT_TAB, event)) {
							if (GameState.game.incrementResponseTab()) {
								GameState.game.updateResponses();
							}
						}
						if (keyEventMatchesBindings(KeyboardAction.RESPOND_PREVIOUS_TAB, event)) {
							if (GameState.game.decrementResponseTab()) {
								GameState.game.updateResponses();
							}
						}
						
						// Next/Previous response page:
						if (keyEventMatchesBindings(KeyboardAction.RESPOND_NEXT_PAGE, event)) {
							if (GameState.game.isHasNextResponsePage()) {
								GameState.game.setResponsePage(GameState.game.getResponsePage() + 1);
								GameState.game.updateResponses();
							}
						}
						if (keyEventMatchesBindings(KeyboardAction.RESPOND_PREVIOUS_PAGE, event)) {
							if (GameState.game.getResponsePage() != 0) {
								GameState.game.setResponsePage(GameState.game.getResponsePage() - 1);
								GameState.game.updateResponses();
							}
						}
					}
				}
			}
		};

		actionKeyReleased = new EventHandler<KeyEvent>() {
			public void handle(KeyEvent event) {
				if (buttonsPressed.contains(event.getCode())) {
					buttonsPressed.remove(event.getCode());
				}
			}
		};

		Main.primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, actionKeyPressed);
		Main.primaryStage.addEventFilter(KeyEvent.KEY_RELEASED, actionKeyReleased);
	}

	private void processResponse(int index) {
		if (GameState.game.getResponsePage() == 0) {
			GameState.game.setContent(GameState.game.getResponsePage() * RESPONSE_COUNT + index);
			
		} else {
			if (index != 0) {
				GameState.game.setContent(GameState.game.getResponsePage() * RESPONSE_COUNT + index - 1);
			} else {
				GameState.game.setContent(GameState.game.getResponsePage() * RESPONSE_COUNT + (RESPONSE_COUNT-1));
			}
		}
	}

	// Event listeners:
	
	// General tooltips:
	static TooltipMoveEventListener moveTooltipListener = new TooltipMoveEventListener();
	static TooltipHideEventListener hideTooltipListener = new TooltipHideEventListener();
	
	// Buttons:
	static ButtonCopyDialogueEventListener copyDialogueButtonListener = new ButtonCopyDialogueEventListener();
	
	// Map movement:
	private ButtonMoveNorthEventListener moveNorthListener = new ButtonMoveNorthEventListener();
	private ButtonMoveSouthEventListener moveSouthListener = new ButtonMoveSouthEventListener();
	private ButtonMoveEastEventListener moveEastListener = new ButtonMoveEastEventListener();
	private ButtonMoveWestEventListener moveWestListener = new ButtonMoveWestEventListener();
	
	// Responses:
	static TooltipResponseMoveEventListener responseTooltipListener = new TooltipResponseMoveEventListener();
	static SetContentEventListener nextResponsePageListener = new SetContentEventListener().nextPage();
	static SetContentEventListener previousResponsePageListener = new SetContentEventListener().previousPage();
	
	// Temporary ones to clear:
	static Map<Document, List<EventListenerData>> EventListenerDataMap = new HashMap<>();
	
	private void unbindListeners(Document document) {
		cookieManager.getCookieStore().removeAll();
		if(document!=null) {
			for(EventListenerData data : EventListenerDataMap.get(document)) {
				((EventTarget) document.getElementById(data.ID)).removeEventListener(data.type, data.listener, data.useCapture);
			}
			EventListenerDataMap.get(document).clear();
			
			EventListenerDataMap.remove(document);
		}
	}
	
	static void addEventListener(Document document, String ID, String type, EventListener listener, boolean useCapture) {
		((EventTarget) document.getElementById(ID)).addEventListener(type, listener, useCapture);
		EventListenerDataMap.get(document).add(new EventListenerData(ID, type, listener, useCapture));
	}
	
	public static void addTooltipListeners(String id, EventListener tooltip) {
		addTooltipListeners(id, tooltip, null, false);
	}
	
	public static void addTooltipListeners(String id, EventListener tooltip, EventListener click, boolean capture) {
		addEventListener(document, id, "mousemove", moveTooltipListener, false);
		addEventListener(document, id, "mouseleave", hideTooltipListener, false);
		addEventListener(document, id, "mouseenter", tooltip, false);
		if(click !=null) {
			addEventListener(document, id, "click", click, capture);
		}
	}
	
	public static Document document, documentButtonsLeft, documentButtonsRight, documentAttributes, documentRight, documentInventory, documentMap, documentMapTitle;
	private boolean debugAllowListeners = true;
	/**
	 * Sets up all WebView EventListeners and WebEngines.
	 */
	private void setUpWebViews() {

		java.net.CookieHandler.setDefault(cookieManager);
		
		// Tooltip WebView:
		webviewTooltip.setContextMenuEnabled(false);
		webEngineTooltip = webviewTooltip.getEngine();
		webEngineTooltip.setJavaScriptEnabled(false);
		webEngineTooltip.getHistory().setMaxSize(0);
		
		if (GameState.getProperties().hasValue(PropertyValue.lightTheme)) {
			webEngineTooltip.setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewTooltip_stylesheet_light.css").toExternalForm());
		} else {
			webEngineTooltip.setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewTooltip_stylesheet.css").toExternalForm());
		}
		
		// Main WebView:
		webViewMain.setContextMenuEnabled(false);
		webEngine = webViewMain.getEngine();
//		webEngine.setJavaScriptEnabled(false);
		webEngine.getHistory().setMaxSize(0);
		
		if (GameState.getProperties().hasValue(PropertyValue.lightTheme)) {
			webEngine.setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webView_stylesheet_light.css").toExternalForm());
		} else {
			webEngine.setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webView_stylesheet.css").toExternalForm());
		}
		
		if(debugAllowListeners) {
			webEngine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> ov, State oldState, State newState) -> {
				if (newState == State.SUCCEEDED) {
					unbindListeners(document);
					manageMainListeners();
				}
			});
		}
		webEngine.executeScript("var timer = false;");

		// Buttons webview:

		webViewButtonsLeft.setContextMenuEnabled(false);
		webEngineButtonsLeft = webViewButtonsLeft.getEngine();
		webEngineButtonsLeft.setJavaScriptEnabled(false);
		webEngineButtonsLeft.getHistory().setMaxSize(0);
		
		if (GameState.getProperties().hasValue(PropertyValue.lightTheme)) {
			webEngineButtonsLeft.setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewButtons_stylesheet_light.css").toExternalForm());
		} else {
			webEngineButtonsLeft.setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewButtons_stylesheet.css").toExternalForm());
		}
		
		if(debugAllowListeners)
		webEngineButtonsLeft.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> ov, State oldState, State newState) -> {
			if (newState == State.SUCCEEDED) {
				unbindListeners(documentButtonsLeft);
				manageButtonLeftListeners();
			}
		});

		webViewButtonsRight.setContextMenuEnabled(false);
		webEngineButtonsRight = webViewButtonsRight.getEngine();
		webEngineButtonsRight.setJavaScriptEnabled(false);
		webEngineButtonsRight.getHistory().setMaxSize(0);
		
		if (GameState.getProperties().hasValue(PropertyValue.lightTheme)) {
			webEngineButtonsRight.setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewButtons_stylesheet_light.css").toExternalForm());
		} else {
			webEngineButtonsRight.setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewButtons_stylesheet.css").toExternalForm());
		}
		
		if(debugAllowListeners)
		webEngineButtonsRight.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> ov, State oldState, State newState) -> {
			if (newState == State.SUCCEEDED) {
				unbindListeners(documentButtonsRight);
				manageButtonRightListeners();
			}
		});
		
		
		// Attributes WebView:
		webViewAttributes.setContextMenuEnabled(false);
		webEngineAttributes = webViewAttributes.getEngine();
		webEngineAttributes.setJavaScriptEnabled(false);
		webEngineAttributes.getHistory().setMaxSize(0);
		
		if (GameState.getProperties().hasValue(PropertyValue.lightTheme)) {
			webEngineAttributes.setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewAttributes_stylesheet_light.css").toExternalForm());
		} else {
			webEngineAttributes.setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewAttributes_stylesheet.css").toExternalForm());
		}
		
		if(debugAllowListeners){
			webEngineAttributes.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> ov, State oldState, State newState) -> {
				if (newState == State.SUCCEEDED) {
					unbindListeners(documentAttributes);
					manageAttributeListeners();
				}
			});
		}
		
		// Attributes WebView:
		webViewRight.setContextMenuEnabled(false);
		webEngineRight = webViewRight.getEngine();
		webEngineRight.setJavaScriptEnabled(false);
		webEngineRight.getHistory().setMaxSize(0);
		
		if (GameState.getProperties().hasValue(PropertyValue.lightTheme)) {
			webEngineRight.setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewAttributes_stylesheet_light.css").toExternalForm());
		} else {
			webEngineRight.setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewAttributes_stylesheet.css").toExternalForm());
		}
		
		if(debugAllowListeners) {
			webEngineRight.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> ov, State oldState, State newState) -> {
				if (newState == State.SUCCEEDED) {
					unbindListeners(documentRight);
					manageRightListeners();
				}
			});
		}
		
	}
	
	private void manageMainListeners() {
		document = (Document) webEngine.executeScript("document");
		EventListenerDataMap.put(document, new ArrayList<>());
		
		if (flashMessageColour != null && flashMessageText != null) {
			GameState.game.flashMessage(flashMessageColour, flashMessageText);
			flashMessageColour = null;
			flashMessageText = null;
		}
		
		// Initialise tooltip listeners if there are tooltips to be initialised
		if (Game.informationTooltips.size() > 0) {
			MiscController.initInfoTooltipListeners();
		}
		
		// There are too many dialogue nodes that require CoveringChangeListeners so better to check by flag
		if (GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.coveringChangeListenersRequired)) {
			CoveringController.initCoveringChangeListeners();
		}
		
		// Initialise dice listeners if there are dice to be initialised
		if (GameState.game.isStarted() && DicePoker.progress>0) {
			for (int i = 0; i<DicePoker.getPlayerDice().size(); i++) {
				String id = "DICE_PLAYER_"+i;
				if ((MainController.document.getElementById(id)) != null) {
					int finalI = i;
					((EventTarget) MainController.document.getElementById(id)).addEventListener("click", e -> {
						if(DicePoker.isAbleToSelectReroll()) {
							DicePoker.setReroll(DicePoker.getPlayerDice().get(finalI));
						}
					}, false);
					MainController.addTooltipListeners(id, new TooltipInformationEventListener().setInformation(
							"Mark for reroll",
							DicePoker.isAbleToSelectReroll()?"":"[style.italicsBad(You cannot mark your dice for reroll in this scene!)]"));
				}
			}
		}
		
		DialogueNode currentNode = GameState.game.getCurrentDialogueNode();
		// Init combat listeners separately, as otherwise no menu buttons work
		if (GameState.game.isInCombat()) {
			MiscController.initCombatListeners();
			if(currentNode.getDialogueNodeType() == DialogueNodeType.INVENTORY) {
				InventoryController.initInventoryListeners();
			}
		}
		
		if(currentNode.equals(MiscDialogue.DOLL_BROCHURE)
				|| currentNode.equals(MiscDialogue.DOLL_BROCHURE_INTERNAL)) {
			MiscController.initDollBrochureListeners();
			
		} else if (currentNode.equals(BodyChanging.BODY_CHANGING_ASS)
				|| currentNode.equals(ScarlettsShop.HELENAS_SHOP_CUSTOM_SLAVE_BODY_ASS)
				|| currentNode.equals(MiscDialogue.SAELLATRIX_DOLL_ASS)) {
			CreationController.initAnusCapacityListeners();
			CreationController.initAnusDepthListeners();
			CreationController.initAnusElasticityListeners();
			CreationController.initAnusModifierListeners();
			CreationController.initAnusPlasticityListeners();
			CreationController.initAnusWetnessListeners();
			CreationController.initAssTypeListeners();
			CreationController.initAssSizeListeners(false);
			CreationController.initHipSizeListeners(false);
			if (GameState.game.isAssHairEnabled()) {
				CoveringController.initAssHairListeners();
			}
		} else if (currentNode.equals(BodyChanging.BODY_CHANGING_BREASTS)
				|| currentNode.equals(ScarlettsShop.HELENAS_SHOP_CUSTOM_SLAVE_BODY_BREASTS)) {
			CreationController.initAreolaeSizeListeners();
			CreationController.initBreastCountListeners();
			CreationController.initBreastShapeListeners();
			CreationController.initBreastSizeListeners(false);
			CreationController.initBreastTypeListeners();
			CreationController.initLactationCapacityListeners();
			CreationController.initLactationFlavourListeners();
			CreationController.initLactationModifierListeners();
			CreationController.initLactationRegenListeners();
			CreationController.initNippleCountListeners();
			CreationController.initNippleModifierListeners();
			CreationController.initNippleShapeListeners();
			CreationController.initNippleSizeListeners();
			if (BodyChanging.isDebugMenu() || GameState.game.isNipplePenEnabled()) {
				CreationController.initNippleCapacityListeners();
				CreationController.initNippleDepthListeners();
				CreationController.initNippleElasticityListeners();
				CreationController.initNipplePlasticityListeners();
			}
		} else if (currentNode.equals(BodyChanging.BODY_CHANGING_BREASTS_CROTCH)
				|| currentNode.equals(ScarlettsShop.HELENAS_SHOP_CUSTOM_SLAVE_BODY_BREASTS_CROTCH)) {
			CreationController.initCrotchAreolaeSizeListeners();
			CreationController.initCrotchBreastCountListeners();
			CreationController.initCrotchBreastShapeListeners();
			CreationController.initCrotchBreastSizeListeners();
			CreationController.initCrotchBreastTypeListeners();
			CreationController.initCrotchLactationCapacityListeners();
			CreationController.initCrotchLactationFlavourListeners();
			CreationController.initCrotchLactationModifierListeners();
			CreationController.initCrotchLactationRegenListeners();
			CreationController.initCrotchNippleCountListeners();
			CreationController.initCrotchNippleModifierListeners();
			CreationController.initCrotchNippleShapeListeners();
			CreationController.initCrotchNippleSizeListeners();
			if (BodyChanging.isDebugMenu() || GameState.game.isNipplePenEnabled()) {
				CreationController.initCrotchNippleCapacityListeners();
				CreationController.initCrotchNippleDepthListeners();
				CreationController.initCrotchNippleElasticityListeners();
				CreationController.initCrotchNipplePlasticityListeners();
			}
		} else if (currentNode.equals(BodyChanging.BODY_CHANGING_CORE)
				|| currentNode.equals(ScarlettsShop.HELENAS_SHOP_CUSTOM_SLAVE_BODY_CORE)) {
			CreationController.initAgeAppearanceListeners();
			CreationController.initArmCountListeners();
			CreationController.initArmTypeListeners();
			CreationController.initBodySizeListeners();
			CreationController.initFaceTypeListeners();
			CreationController.initFemininityListeners();
			CreationController.initFootStructureListeners();
			CreationController.initGenitalArrangementListeners();
			CreationController.initHeightListeners();
			CreationController.initLegConfigListeners();
			CreationController.initLegTypeListeners();
			CreationController.initMuscleListeners();
			CreationController.initTailCountListeners();
			CreationController.initTailGirthListeners();
			CreationController.initTailLengthListeners();
			CreationController.initTailTypeListeners();
			CreationController.initTentacleGirthListeners();
			CreationController.initTentacleLengthListeners();
			CreationController.initTorsoTypeListeners();
			CreationController.initWingSizeListeners();
			CreationController.initWingTypeListeners();
			if (BodyChanging.getTarget().isElemental()) {
				CreationController.initBodyMaterialListeners();
			}
			if (GameState.game.isBodyHairEnabled()) {
				CoveringController.initUnderarmHairListeners();
			}
		} else if (currentNode.equals(BodyChanging.BODY_CHANGING_EYES)
				|| currentNode.equals(ScarlettsShop.HELENAS_SHOP_CUSTOM_SLAVE_BODY_EYES)) {
			CreationController.initEyeCountListeners();
			CreationController.initEyeShapeListeners();
			CreationController.initEyeTypeListeners();
		} else if (currentNode.equals(BodyChanging.BODY_CHANGING_HAIR)
				|| currentNode.equals(ScarlettsShop.HELENAS_SHOP_CUSTOM_SLAVE_BODY_HAIR)
				|| currentNode.equals(CompanionManagement.SLAVE_MANAGEMENT_COSMETICS_HAIR)
				|| currentNode.equals(RoomPlayer.AUNT_HOME_PLAYERS_ROOM_MAKEUP)) {
			CoveringController.initHairLengthListeners();
			CoveringController.initHairStyleListeners();
			CreationController.initHairTypeListeners();
			CreationController.initNeckFluffListeners();
		} else if (currentNode.equals(BodyChanging.BODY_CHANGING_HEAD)
				|| currentNode.equals(ScarlettsShop.HELENAS_SHOP_CUSTOM_SLAVE_BODY_HEAD)) {
			CreationController.initAntennaCountListeners();
			CreationController.initAntennaLengthListeners();
			CreationController.initAntennaRowsListeners();
			CreationController.initAntennaTypeListeners();
			CreationController.initEarTypeListeners();
			CreationController.initHornCountListeners();
			CreationController.initHornLengthListeners();
			CreationController.initHornRowsListeners();
			CreationController.initHornTypeListeners();
			CreationController.initLipSizeListeners();
			CreationController.initThroatCapacityListeners();
			CreationController.initThroatDepthListeners();
			CreationController.initThroatElasticityListeners();
			CreationController.initThroatModifierListeners();
			CreationController.initThroatPlasticityListeners();
			CreationController.initThroatWetnessListeners();
			CreationController.initTongueLengthListeners();
			CreationController.initTongueModifierListeners();
			if (GameState.game.isFacialHairEnabled() && (!BodyChanging.getTarget().isFeminine() || GameState.game.isFemaleFacialHairEnabled())) {
				CoveringController.initFacialHairListeners();
			}
		} else if (currentNode.equals(BodyChanging.BODY_CHANGING_PENIS)
				|| currentNode.equals(ScarlettsShop.HELENAS_SHOP_CUSTOM_SLAVE_BODY_PENIS)) {
			CreationController.initPenisGirthListeners();
			CreationController.initPenisModifierListeners();
			CreationController.initPenisSizeListeners(false);
			CreationController.initPenisTypeListeners();
			CreationController.initPenisCumExpulsionListeners();
			CreationController.initPenisCumFlavourListeners();
			CreationController.initPenisCumModifierListeners();
			CreationController.initPenisCumProductionListeners();
			CreationController.initPenisCumRegenListeners();
			CreationController.initTesticleCountListeners();
			CreationController.initTesticleInternalListeners();
			CreationController.initTesticleSizeListeners(false);
			CreationController.initUrethraCapacityListeners();
			CreationController.initUrethraDepthListeners();
			CreationController.initUrethraElasticityListeners();
			CreationController.initUrethraModifierListeners();
			CreationController.initUrethraPlasticityListeners();
			if (GameState.game.isPubicHairEnabled()) {
				CoveringController.initPubicHairListeners();
			}
		} else if (currentNode.equals(BodyChanging.BODY_CHANGING_SPINNERET)
				|| currentNode.equals(ScarlettsShop.HELENAS_SHOP_CUSTOM_SLAVE_BODY_SPINNERET)) {
			CreationController.initSpinneretCapacityListeners();
			CreationController.initSpinneretDepthListeners();
			CreationController.initSpinneretElasticityListeners();
			CreationController.initSpinneretModifierListeners();
			CreationController.initSpinneretPlasticityListeners();
			CreationController.initSpinneretWetnessListeners();
		} else if (currentNode.equals(BodyChanging.BODY_CHANGING_VAGINA)
				|| currentNode.equals(ScarlettsShop.HELENAS_SHOP_CUSTOM_SLAVE_BODY_VAGINA)
				|| currentNode.equals(MiscDialogue.SAELLATRIX_DOLL_PUSSY)) {
			CreationController.initVaginaCapacityListeners();
			CreationController.initVaginaDepthListeners();
			CreationController.initVaginaEggLayerListeners();
			CreationController.initVaginaElasticityListeners();
			CreationController.initVaginaHymenListeners();
			CreationController.initVaginaModifierListeners();
			CreationController.initVaginaPlasticityListeners();
			CreationController.initVaginaSquinterListeners();
			CreationController.initVaginaTypeListeners();
			CreationController.initVaginaWetnessListeners();
			CreationController.initVaginaCumFlavourListeners();
			CreationController.initVaginaCumModifierListeners();
			CreationController.initClitorisGirthListeners();
			CreationController.initClitorisModifierListeners();
			CreationController.initClitorisSizeListeners(false);
			CreationController.initLabiaSizeListeners();
			CreationController.initVaginaUrethraCapacityListeners();
			CreationController.initVaginaUrethraDepthListeners();
			CreationController.initVaginaUrethraElasticityListeners();
			CreationController.initVaginaUrethraModifierListeners();
			CreationController.initVaginaUrethraPlasticityListeners();
			if (GameState.game.isPubicHairEnabled()) {
				CoveringController.initPubicHairListeners();
			}
		} else if (currentNode.equals(BodyChanging.BODY_CHANGING_SAVE_LOAD)) {
			FileController.initBodySaveLoadListeners();
		} else if(currentNode.equals(LilayaDressingRoomDialogue.OUTFITS)) {
			FileController.initOutfitListeners();
		} else if(currentNode.equals(LilayaDressingRoomDialogue.OUTFIT_EDITOR)
				|| currentNode.equals(LilayaDressingRoomDialogue.OUTFIT_EDITOR_ITEM_CHOICE)) {
			MiscController.initDressingRoomListeners();
		} else if(currentNode.equals(LilayaDressingRoomDialogue.OUTFIT_EDITOR_ITEM_DYE)) {
			MiscController.initDressingRoomDyeListeners();
		} else if(currentNode.equals(LilayaDressingRoomDialogue.OUTFIT_EDITOR_ITEM_ENCHANT)) {
			MiscController.initDressingRoomEnchantmentListeners();
		} else if (currentNode.equals(CharacterCreation.BACKGROUND_SELECTION_MENU)) {
			CreationController.initBackgroundSelectionListeners();
		} else if (currentNode.equals(CompanionManagement.SLAVE_MANAGEMENT_COSMETICS_OTHER)) {
			CoveringController.initBleachingListeners();
			CoveringController.initAssHairListeners();
			CoveringController.initFacialHairListeners();
			CoveringController.initPubicHairListeners();
			CoveringController.initUnderarmHairListeners();
		} else if (currentNode.equals(CharacterCreation.CHOOSE_ADVANCED_APPEARANCE_ASS)) {
			CoveringController.initBleachingListeners();
			CreationController.initAssSizeListeners(true);
			CreationController.initHipSizeListeners(true);
		} else if (currentNode.equals(CharacterCreation.CHOOSE_ADVANCED_APPEARANCE_BREASTS)) {
			CreationController.initAreolaeSizeListeners();
			CreationController.initBreastShapeListeners();
			CreationController.initBreastSizeListeners(true);
			CreationController.initNipplePuffinessListeners();
			CreationController.initNippleSizeListeners();
			CreationController.initLactationCapacityListeners();
		} else if (currentNode.equals(CharacterCreation.CHOOSE_ADVANCED_APPEARANCE_BODY_HAIR)) {
			CoveringController.initAssHairListeners();
			CoveringController.initFacialHairListeners();
			CoveringController.initPubicHairListeners();
			CoveringController.initUnderarmHairListeners();
		} else if (currentNode.equals(CharacterCreation.CHOOSE_ADVANCED_APPEARANCE_CORE)) {
			CreationController.initBodySizeListeners();
			CreationController.initHeightListeners();
			CreationController.initMuscleListeners();
		} else if (currentNode.equals(CharacterCreation.CHOOSE_ADVANCED_APPEARANCE_FACE)) {
			CreationController.initLipSizeListeners();
			CreationController.initLipPuffinessListeners();
		} else if (currentNode.equals(CharacterCreation.CHOOSE_ADVANCED_APPEARANCE_GENITALS)) {
			if (GameState.game.getPlayer().hasPenis()) {
				CreationController.initPenisSizeListeners(true);
				CreationController.initTesticleSizeListeners(true);
				CreationController.initPenisCumProductionListeners();
			} else {
				CreationController.initClitorisSizeListeners(true);
				CreationController.initLabiaSizeListeners();
				CreationController.initVaginaCapacityListeners();
			}
		} else if (currentNode.equals(CharacterCreation.CHOOSE_ADVANCED_APPEARANCE_HAIR)) {
			CoveringController.initHairLengthListeners();
			CoveringController.initHairStyleListeners();
		} else if (currentNode.equals(CharacterCreation.CHOOSE_ADVANCED_APPEARANCE_PIERCINGS)
				|| currentNode.equals(ScarlettsShop.HELENAS_SHOP_CUSTOM_SLAVE_BODY_PIERCINGS)
				|| currentNode.equals(CompanionManagement.SLAVE_MANAGEMENT_COSMETICS_PIERCINGS)
				|| currentNode.equals(SuccubisSecrets.SHOP_BEAUTY_SALON_PIERCINGS)) {
			CoveringController.initPiercingsListeners();
		} else if (currentNode.equals(CharacterCreation.CHOOSE_ADVANCED_APPEARANCE_TATTOOS)
				|| currentNode.equals(CompanionManagement.SLAVE_MANAGEMENT_TATTOOS)
				|| currentNode.equals(CosmeticsDialogue.BEAUTICIAN_TATTOOS)
				|| currentNode.equals(SuccubisSecrets.SHOP_BEAUTY_SALON_TATTOOS)) {
			CreationController.initTattooInfoListeners(currentNode);
		} else if (currentNode.equals(CharacterCreation.CHOOSE_ADVANCED_APPEARANCE_TATTOOS_ADD)
				|| currentNode.equals(CompanionManagement.SLAVE_MANAGEMENT_TATTOOS_ADD)
				|| currentNode.equals(CosmeticsDialogue.BEAUTICIAN_TATTOOS_ADD)
				|| currentNode.equals(SuccubisSecrets.SHOP_BEAUTY_SALON_TATTOOS_ADD)) {
			CreationController.initTattooAddListeners();
		} else if(currentNode.equals(CosmeticsDialogue.TATTOO_SAVE_LOAD)) {
			FileController.initTattooSaveLoadListeners();
		} else if (currentNode.equals(CharacterCreation.CHOOSE_APPEARANCE)) {
			CreationController.initAgeListeners();
			CreationController.initBirthdayListeners();
			CreationController.initFemininityPresetListeners();
			CreationController.initGenderListeners();
			CreationController.initOrientationListeners();
			CreationController.initPersonalityListeners();
		} else if (currentNode.equals(CharacterCreation.CHOOSE_SEX_EXPERIENCE)
				|| currentNode.equals(ScarlettsShop.HELENAS_SHOP_CUSTOM_SLAVE_FINISH)) {
			CreationController.initSexExperienceListeners();
			CreationController.initFetishListeners();
		} else if (currentNode.equals(CharacterCreation.IMPORT_CHOOSE)) {
			FileController.initPlayerImportListeners();
		} else if (currentNode.equals(CharactersPresentDialogue.MENU)) {
			FileController.initArtworkListeners();
			MiscController.initPerkListeners(currentNode);
		} else if (currentNode.equals(CityHall.LODGER_IMPORT)) {
			FileController.initLodgerImportListeners();
		} else if (currentNode.equals(CityHall.CITY_HALL_WAITING_AREA_LODGER_LIST)) {
			FileController.initLodgerWaitingListeners();
		} else if (currentNode.equals(NightlifeDistrict.WATERING_HOLE_IMPORT)) {
			FileController.initClubberImportListeners();
		} else if (currentNode.equals(CombatMovesSetup.COMBAT_MOVES_CORE)) {
			MiscController.initCombatMoveListeners();
		} else if (currentNode.equals(CompanionManagement.OCCUPANT_CHOOSE_NAME)
				|| currentNode.equals(ElementalDialogue.ELEMENTAL_CHOOSE_NAME)) {
			MiscController.initRenameListeners();
		} else if (currentNode.equals(CompanionManagement.SLAVE_MANAGEMENT_INSPECT)
				|| currentNode.equals(PhoneDialogue.CONTACTS_CHARACTER)
				|| currentNode.equals(PhoneDialogue.CHARACTER_APPEARANCE)) {
			FileController.initArtworkListeners();
			MiscController.initPerkListeners(currentNode);
		} else if (currentNode.equals(CompanionManagement.SLAVE_MANAGEMENT_JOBS)) {
			OccupantController.initSlaveJobListeners();
		} else if (currentNode.equals(CompanionManagement.SLAVE_MANAGEMENT_PERMISSIONS)) {
			OccupantController.initSlavePermissionsListeners();
		} else if (currentNode.equals(CharactersPresentDialogue.PERKS)
				|| currentNode.equals(CompanionManagement.SLAVE_MANAGEMENT_PERKS)
				|| currentNode.equals(ElementalDialogue.ELEMENTAL_PERKS)
				|| currentNode.equals(PhoneDialogue.CHARACTER_APPEARANCE)
				|| currentNode.equals(PhoneDialogue.CHARACTER_PERK_TREE)) {
			MiscController.initPerkListeners(currentNode);
		} else if (currentNode.equals(DebugDialogue.SPAWN_MENU)
				|| currentNode.equals(DebugDialogue.ITEM_VIEWER)) {
			DebugController.initSpawnItemListeners();
		} else if (currentNode.equals(DebugDialogue.SPAWN_MENU_SET)) {
			DebugController.initSpawnSetListeners();
		} else if (currentNode.equals(DebugDialogue.OUTFIT_VIEWER)) {
			DebugController.initApplyOutfitListeners();
		} else if (currentNode.equals(ElementalDialogue.ELEMENTAL_FETISHES)
				|| currentNode.equals(PhoneDialogue.CHARACTER_FETISHES)) {
			MiscController.initFetishListeners();
		} else if(currentNode.equals(PhoneDialogue.RACES)) {
			MiscController.initEncyclopediaRaceListeners();
		} else if (currentNode.equals(EnchantmentDialogue.ENCHANTMENT_MENU)) {
			EnchantmentController.initEnchantmentMenuListeners();
		} else if (currentNode.equals(EnchantmentDialogue.ENCHANTMENT_SAVE_LOAD)) {
			FileController.initEnchantmentSaveLoadListeners();
		} else if (currentNode.equals(GenericOffspringDialogue.OFFSPRING_ENCOUNTER_CHOOSE_NAME)) {
			MiscController.initFamilyRenameListeners();
		} else if (currentNode.equals(GiftDialogue.GIFT_DIALOGUE)) {
			MiscController.initGiftListeners();
		} else if (currentNode.getDialogueNodeType() == DialogueNodeType.INVENTORY) {
			if (currentNode.equals(InventoryDialogue.DYE_CLOTHING)
					|| currentNode.equals(InventoryDialogue.DYE_CLOTHING_CHARACTER_CREATION)
					|| currentNode.equals(InventoryDialogue.DYE_EQUIPPED_CLOTHING)
					|| currentNode.equals(InventoryDialogue.DYE_EQUIPPED_CLOTHING_CHARACTER_CREATION)) {
				InventoryController.initClothingDyeListeners();
				InventoryController.initPatternListeners();
				InventoryController.initPatternRecolourListeners();
				InventoryController.initStickerListeners();
			} else if (currentNode.equals(InventoryDialogue.DYE_WEAPON)
					|| currentNode.equals(InventoryDialogue.DYE_EQUIPPED_WEAPON)) {
				InventoryController.initWeaponDyeListeners();
				InventoryController.initDamageTypeListeners();
			} else {
				InventoryController.initInventoryListeners();
			}
		} else if (currentNode.equals(Library.DOMINION_MAP)) {
			MiscController.initMapListeners(WorldType.DOMINION, false);
		} else if (currentNode.equals(LilayaMilkingRoomDialogue.MILKING_ROOM)) {
			MilkingRoom room = GameState.game.getOccupancyUtil().getMilkingRoom(GameState.game.getPlayerCell().getType(), GameState.game.getPlayerCell().getLocation());
			for (FluidStored fluid : room.getFluidsStored()) {
				OccupantController.fluidHandler(room, fluid);
			}
		} else if (currentNode.equals(OccupantManagementDialogue.ROOM_MANAGEMENT)) {
			OccupantController.initRoomManagerListeners();
		} else if (currentNode.equals(OccupantManagementDialogue.ROOM_UPGRADES)) {
			OccupantController.initRoomUpgradesListeners();
		} else if (currentNode.equals(OccupantManagementDialogue.SLAVE_LIST)
				|| currentNode.equals(OccupantManagementDialogue.SLAVE_LIST_MANAGEMENT)) {
			OccupantController.initOccupantListListeners();
			if (GameState.game.getDialogueFlags().getSlaveTrader() != null) {
				OccupantController.initSlaveTraderListeners();
			}
		} else if (currentNode.equals(OccupantManagementDialogue.OCCUPANT_OVERVIEW)) {
			OccupantController.initOverviewListeners();
		} else if (currentNode.equals(OptionsDialogue.AGE_PREFERENCE)) {
			OptionsController.initAgeListeners();
		} else if (currentNode.equals(OptionsDialogue.BODIES)) {
			OptionsController.initBodiesListeners();
		} else if (currentNode.equals(OptionsDialogue.GAMEPLAY)) {
			OptionsController.initGameplayListeners();
		} else if (currentNode.equals(OptionsDialogue.GENDER_PREFERENCE)) {
			OptionsController.initGenderListeners();
		} else if (currentNode.equals(OptionsDialogue.FETISH_PREFERENCE)) {
			OptionsController.initFetishListeners();
		} else if (currentNode.equals(OptionsDialogue.FURRY_PREFERENCE)) {
			OptionsController.initFurryListeners();
		} else if (currentNode.equals(OptionsDialogue.IMPORT_EXPORT)) {
			FileController.initImportExportListeners();
		} else if (currentNode.equals(OptionsDialogue.KEYBINDS)) {
			OptionsController.initKeybindListeners();
		} else if (currentNode.equals(OptionsDialogue.MISCELLANEOUS)) {
			OptionsController.initMiscellaneousListeners();
		} else if (currentNode.equals(OptionsDialogue.ORIENTATION_PREFERENCE)) {
			OptionsController.initOrientationListeners();
		} else if (currentNode.equals(OptionsDialogue.SAVE_LOAD)) {
			FileController.initSaveLoadListeners();
		} else if (currentNode.equals(OptionsDialogue.SEX)) {
			OptionsController.initSexListeners();
		} else if (currentNode.equals(OptionsDialogue.UNIT_PREFERENCE)) {
			OptionsController.initUnitListeners();
		} else if (currentNode.equals(PhoneDialogue.CLOTHING_CATALOGUE)) {
			MiscController.initEncyclopediaClothingListeners();
		} else if (currentNode.equals(PhoneDialogue.ITEM_CATALOGUE)) {
			MiscController.initEncyclopediaItemListeners();
		} else if (currentNode.equals(PhoneDialogue.MAP)) {
			MiscController.initMapListeners(PhoneDialogue.worldTypeMap, true);
		} else if (currentNode.equals(PhoneDialogue.WEAPON_CATALOGUE)) {
			MiscController.initEncyclopediaWeaponListeners();
		} else if (currentNode.equals(RoomPlayer.ROOM_SET_ALARM)) {
			MiscController.initAlarmListeners();
		} else if (currentNode.equals(ScarlettsShop.HELENAS_SHOP_CUSTOM_SLAVE_PERSONALITY)) {
			CreationController.initAffectionListeners();
			CreationController.initAgeListeners();
			CreationController.initFetishListeners();
			CreationController.initObedienceListeners();
			CreationController.initOrientationListeners();
			CreationController.initPersonalityListeners();
			MiscController.initRenameListeners();
		} else if (currentNode.equals(SlaverAlleyDialogue.AUCTION_BLOCK_LIST)) {
			FileController.initAuctionListeners();
		} else if (currentNode.equals(SlaverAlleyDialogue.AUCTION_IMPORT)) {
			FileController.initSlaveImportListeners();
		} else if (currentNode.equals(SpellManagement.CHARACTER_SPELLS_ARCANE)) {
			MiscController.initSpellListeners(SpellSchool.ARCANE);
		} else if (currentNode.equals(SpellManagement.CHARACTER_SPELLS_AIR)) {
			MiscController.initSpellListeners(SpellSchool.AIR);
		} else if (currentNode.equals(SpellManagement.CHARACTER_SPELLS_EARTH)) {
			MiscController.initSpellListeners(SpellSchool.EARTH);
		} else if (currentNode.equals(SpellManagement.CHARACTER_SPELLS_FIRE)) {
			MiscController.initSpellListeners(SpellSchool.FIRE);
		} else if (currentNode.equals(SpellManagement.CHARACTER_SPELLS_MISC)) {
			MiscController.initSpellListeners(null);
		} else if (currentNode.equals(SpellManagement.CHARACTER_SPELLS_WATER)) {
			MiscController.initSpellListeners(SpellSchool.WATER);
		} else if (currentNode.equals(SuccubisSecrets.SHOP_BEAUTY_SALON_OTHER)) {
			CoveringController.initAssHairListeners();
			CoveringController.initBleachingListeners();
			CoveringController.initFacialHairListeners();
			CoveringController.initPubicHairListeners();
			CoveringController.initUnderarmHairListeners();
		} else if (currentNode.equals(SuccubisSecrets.SHOP_BEAUTY_SALON_HAIR)) {
			CoveringController.initHairLengthListeners();
			CoveringController.initHairStyleListeners();
		}
		setResponseEventListenersStatic();
	}
	
	@Override
	public void setResponseEventListeners() {
		setResponseEventListenersStatic();
	}

	public static void setResponseEventListenersStatic() {
		
		if(GameState.game.getCurrentDialogueNode().getResponseTabTitle(0) != null && !GameState.game.getCurrentDialogueNode().getResponseTabTitle(0).isEmpty()) {
			int responsePageCounter = 0;
			while (GameState.game.getCurrentDialogueNode().getResponseTabTitle(responsePageCounter) != null){
				setResponseTabListeners(responsePageCounter);
				responsePageCounter++;
			}
		}
		
		// Responses:
		for (int i = 0; i < RESPONSE_COUNT; i++) {
			String id = "option_" + i;
			if (((EventTarget) document.getElementById(id)) != null) {
				SetContentEventListener el = new SetContentEventListener().setIndex(i);
				((EventTarget) document.getElementById(id)).addEventListener("click", el, false);
				
				addEventListener(document, id, "mousemove", responseTooltipListener, false);
				addEventListener(document, id, "mouseleave", hideTooltipListener, false);
				TooltipResponseDescriptionEventListener el2 = new TooltipResponseDescriptionEventListener().setIndex(i);
				addEventListener(document, id, "mouseenter", el2, false);
			}
		}
		if (((EventTarget) document.getElementById("switch_right")) != null) {
			addEventListener(document, "switch_right", "click", nextResponsePageListener, false);
		}
		if (((EventTarget) document.getElementById("switch_left")) != null) {
			addEventListener(document, "switch_left", "click", previousResponsePageListener, false);
		}
	}
	
	private static void setResponseTabListeners(int responsePageCounter) {
		String id = "tab_" + responsePageCounter;

		((EventTarget) document.getElementById(id)).addEventListener("click", e -> {
				GameState.game.setResponseTab(responsePageCounter);
				GameState.game.updateResponses();
			}, false);
	}
	
	static void setInventoryPageLeft(int i) {
		String id = "INV_PAGE_LEFT_"+i;
		if (((EventTarget) document.getElementById(id)) != null) {
			if(i!=5 || GameState.game.getPlayer().isCarryingQuestItems()) {
				((EventTarget) document.getElementById(id)).addEventListener("click", e -> {
					RenderingEngine.setPageLeft(i);
					GameState.game.setContent(new Response("", "", GameState.game.getCurrentDialogueNode()));
				}, false);
			}
			if(i==5) {
				MainController.addEventListener(MainController.document, id, "mousemove", MainController.moveTooltipListener, false);
				MainController.addEventListener(MainController.document, id, "mouseleave", MainController.hideTooltipListener, false);
				TooltipInformationEventListener el2 =  new TooltipInformationEventListener().setInformation("Unique Items", "");
				MainController.addEventListener(MainController.document, id, "mouseenter", el2, false);
			}
		}
	}
	
	static void setInventoryPageRight(int i) {
		String id = "INV_PAGE_RIGHT_"+i;
		if (((EventTarget) document.getElementById(id)) != null) {
			if(i!=5
				|| (InventoryDialogue.getInventoryNPC()==null
					?GameState.game.getPlayer().getCell().getInventory().isAnyQuestItemPresent()
					:InventoryDialogue.getInventoryNPC().isCarryingQuestItems())) {
				((EventTarget) document.getElementById(id)).addEventListener("click", e -> {
					RenderingEngine.setPageRight(i);
					GameState.game.setContent(new Response("", "", GameState.game.getCurrentDialogueNode()));
				}, false);
			}
			if(i==5) {
				MainController.addEventListener(MainController.document, id, "mousemove", MainController.moveTooltipListener, false);
				MainController.addEventListener(MainController.document, id, "mouseleave", MainController.hideTooltipListener, false);
				TooltipInformationEventListener el2 =  new TooltipInformationEventListener().setInformation("Unique Items", "");
				MainController.addEventListener(MainController.document, id, "mouseenter", el2, false);
			}
		}
	}
	
	private void manageButtonLeftListeners() {
		documentButtonsLeft = (Document) webEngineButtonsLeft.executeScript("document");
		EventListenerDataMap.put(documentButtonsLeft, new ArrayList<>());
		// Only captures the fresh document. What each button does lives in core (ChromeButtons),
		// which registers after this and calls back into bindingsChanged to attach the listeners.
	}

	private void manageButtonRightListeners() {
		documentButtonsRight = (Document) webEngineButtonsRight.executeScript("document");
		EventListenerDataMap.put(documentButtonsRight, new ArrayList<>());
	}

	/**
	 * Attaches real DOM listeners for a panel's core-registered bindings.
	 *
	 * Deliberately mirrors what the hand-written registration used to do, including the
	 * "does this element actually exist in the current content" guard and the capture flag,
	 * both of which are load-bearing.
	 */
	@Override
	public void bindingsChanged(Panel panel) {
		Document doc = documentFor(panel);
		if (doc == null) {
			return;
		}
		for (Ui.Binding binding : Ui.bindings(panel)) {
			String id = binding.getId();
			if (doc.getElementById(id) == null) {
				continue;
			}
			if (binding.getClick() != null) {
				ClickAction action = binding.getClick();
				addEventListener(doc, id, "click", e -> action.run(), binding.isUseCapture());
			}
			InformationTooltip tooltip = binding.getTooltip();
			if (tooltip != null) {
				addEventListener(doc, id, "mousemove", MainController.moveTooltipListener, false);
				addEventListener(doc, id, "mouseleave", MainController.hideTooltipListener, false);
				addEventListener(doc, id, "mouseenter",
						new TooltipInformationEventListener().setInformation(
								tooltip.getTitle(), tooltip.getDescription(), tooltip.getDescriptionHeightOverride()),
						false);
			}
		}
	}

	private Document documentFor(Panel panel) {
		switch (panel) {
			case MAIN: return document;
			case ATTRIBUTES: return documentAttributes;
			case RIGHT: return documentRight;
			case BUTTONS_LEFT: return documentButtonsLeft;
			case BUTTONS_RIGHT: return documentButtonsRight;
			default: return null;
		}
	}

	private void manageAttributeListeners() {
		documentAttributes = (Document) webEngineAttributes.executeScript("document");
		EventListenerDataMap.put(documentAttributes, new ArrayList<>());
		
		// Map:
		if (((EventTarget) documentAttributes.getElementById("upButton")) != null) {
			addEventListener(documentAttributes, "upButton", "click", moveNorthListener, true);
		}
		if (((EventTarget) documentAttributes.getElementById("downButton")) != null) {
			addEventListener(documentAttributes, "downButton", "click", moveSouthListener, true);
		}
		if (((EventTarget) documentAttributes.getElementById("leftButton")) != null) {
			addEventListener(documentAttributes, "leftButton", "click", moveWestListener, true);
		}
		if (((EventTarget) documentAttributes.getElementById("rightButton")) != null) {
			addEventListener(documentAttributes, "rightButton", "click", moveEastListener, true);
		}
		
		// Inventory:
		// For all equipped clothing slots:
		String id;
		for (InventorySlot invSlot : InventorySlot.values()) {
			id = invSlot.toString() + "Slot";
			if (!invSlot.isWeapon()) {
				if (((EventTarget) documentAttributes.getElementById(id)) != null) {
					if(!RenderingEngine.ENGINE.isRenderingTattoosLeft() || invSlot.isJewellery()) {
						InventorySelectedItemEventListener el = new InventorySelectedItemEventListener().setClothingEquipped(GameState.game.getPlayer(), invSlot);
						addEventListener(documentAttributes, id, "click", el, false);
					}
					addEventListener(documentAttributes, id, "mousemove", moveTooltipListener, false);
					addEventListener(documentAttributes, id, "mouseleave", hideTooltipListener, false);
					TooltipInventoryEventListener el2 = new TooltipInventoryEventListener().setInventorySlot(invSlot, GameState.game.getPlayer());
					addEventListener(documentAttributes, id, "mouseenter", el2, false);
				}
			} else {
				if (((EventTarget) documentAttributes.getElementById(id)) != null) {
					InventorySelectedItemEventListener el = new InventorySelectedItemEventListener().setWeaponEquipped(GameState.game.getPlayer(),invSlot);
					addEventListener(documentAttributes, id, "click", el, false);
					addEventListener(documentAttributes, id, "mousemove", moveTooltipListener, false);
					addEventListener(documentAttributes, id, "mouseleave", hideTooltipListener, false);
					TooltipInventoryEventListener el2 = new TooltipInventoryEventListener().setInventorySlot(invSlot, GameState.game.getPlayer());
					addEventListener(documentAttributes, id, "mouseenter", el2, false);
				}
			}
		}
		
		id = "TATTOO_SWITCH_LEFT";
		if (((EventTarget) documentAttributes.getElementById(id)) != null) {
			((EventTarget) documentAttributes.getElementById(id)).addEventListener("click", e -> {
				RenderingEngine.ENGINE.setRenderingTattoosLeft(!RenderingEngine.ENGINE.isRenderingTattoosLeft());
				this.updateUILeftPanel();
			}, false);
			addEventListener(documentAttributes, id, "mousemove", moveTooltipListener, false);
			addEventListener(documentAttributes, id, "mouseleave", hideTooltipListener, false);
			TooltipInformationEventListener el2 = new TooltipInformationEventListener().setInformation(
					!RenderingEngine.ENGINE.isRenderingTattoosLeft()
						?"Switch to tattoos"
						:"Switch to clothing",
					"");
			addEventListener(documentAttributes, id, "mouseenter", el2, false);
		}
		
		id = "INVENTORY_ENCHANTMENT_LIMIT";
		if (((EventTarget) documentAttributes.getElementById(id)) != null) {
			addEventListener(documentAttributes, id, "mousemove", moveTooltipListener, false);
			addEventListener(documentAttributes, id, "mouseleave", hideTooltipListener, false);
			TooltipInformationEventListener el2 = new TooltipInformationEventListener().setInformation(
					Util.capitaliseSentence(Attribute.ENCHANTMENT_LIMIT.getName()),
					"The total amount of weapon, clothing, and tattoo attribute enchantments you're able to handle without incurring massive penalties."
					+" Your limit is calculated from: <i>10 + (level) + (perk gains)</i>");
			addEventListener(documentAttributes, id, "mouseenter", el2, false);
		}

		boolean dateKnown = GameState.game.getDialogueFlags().hasFlag(DialogueFlagValue.knowsDate) || !GameState.game.isInNewWorld();
		
		id = "DATE_DISPLAY_TOGGLE";
		if (((EventTarget) documentAttributes.getElementById(id)) != null) {
			((EventTarget) documentAttributes.getElementById(id)).addEventListener("click", e -> {
				if(!GameState.game.isBadEnd()) {
					GameState.getProperties().setValue(PropertyValue.calendarDisplay, !GameState.getProperties().hasValue(PropertyValue.calendarDisplay));
					GameState.saveProperties();
					MainController.updateUIStatic();
				}
			}, false);
			
			addEventListener(documentAttributes, id, "mousemove", moveTooltipListener, false);
			addEventListener(documentAttributes, id, "mouseleave", hideTooltipListener, false);
			String day = GameState.game.getDateNow().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
			StringBuilder dateSB = new StringBuilder();
			if(GameState.game.isBadEnd()) {
				dateSB.append("[style.italicsBad(Given your current situation, you've lost track of what day it is...)]");
			} else {
				if(!dateKnown) {
					dateSB.append("[style.italicsMinorBad(Look at the calendar in your room to reveal the date!)]");
				} else {
					dateSB.append("The current date is: [style.colourGood("+ GameState.game.getDisplayDate(true)+")]");
				}
				dateSB.append("<br/>");
				dateSB.append("It is currently "+UtilText.generateSingularDeterminer(day)+" [style.colourBlueLight("+day+")].<br/>"
						+ "You've been in this new world for: [style.colourExcellent("+GameState.game.getDayNumber()+" day"+(GameState.game.getDayNumber()>1?"s":"")+")]");
				dateSB.append("<br/><i>Click to toggle between a date and day count.</i>");
			}
			TooltipInformationEventListener el2 = new TooltipInformationEventListener().setInformation("Toggle Calendar Display",
						dateSB.toString());
			addEventListener(documentAttributes, id, "mouseenter", el2, false);
		}
		
		id = "TWENTY_FOUR_HOUR_TIME_TOGGLE";
		if (((EventTarget) documentAttributes.getElementById(id)) != null) {
			((EventTarget) documentAttributes.getElementById(id)).addEventListener("click", e -> {
				if(!GameState.game.isBadEnd()) {
				    overrideAutoLocale();
					GameState.getProperties().setValue(PropertyValue.twentyFourHourTime, !GameState.getProperties().hasValue(PropertyValue.twentyFourHourTime));
					GameState.saveProperties();
					Units.FORMATTER.updateTimeFormat(GameState.getProperties().hasValue(PropertyValue.autoLocale));
					MainController.updateUIStatic();
				}
			}, false);
			
			DayPeriod dp = DateAndTime.getDayPeriod(GameState.game.getDateNow(), Game.DOMINION_LATITUDE, Game.DOMINION_LONGITUDE);
			LocalDateTime[] ldt = DateAndTime.getTimeOfSolarElevationChange(GameState.game.getDateNow(), SolarElevationAngle.SUN_ALTITUDE_SUNRISE_SUNSET, Game.DOMINION_LATITUDE, Game.DOMINION_LONGITUDE);
			
			
			addEventListener(documentAttributes, id, "mousemove", moveTooltipListener, false);
			addEventListener(documentAttributes, id, "mouseleave", hideTooltipListener, false);
			StringBuilder timeSB = new StringBuilder();
			if(GameState.game.isBadEnd()) {
				timeSB.append("[style.italicsBad(Given your current situation, you've lost track of what time it is...)]");
			} else {
				timeSB.append("Current time: "+Units.time(GameState.game.getDateNow())+" (<span style='color:"+dp.getColour().toWebHexString()+";'>"+Util.capitaliseSentence(dp.getName())+"</span>)<br/>"
					+ "Sunrise: "+(!dateKnown?"[style.colourMinorBad(??/??/??)] [style.colourYellow("+Units.time(ldt[0]):"[style.colourYellow("+Units.dateTime(ldt[0]))+")]<br/>"
					+ "Sunset: "+(!dateKnown?"[style.colourMinorBad(??/??/??)] [style.colourOrange("+Units.time(ldt[1]):"[style.colourOrange("+Units.dateTime(ldt[1]))+")]<br/>"
					+ "<i>Click to toggle the time display between a 24-hour and 12-hour clock.</i>");
			}
			TooltipInformationEventListener el2 = new TooltipInformationEventListener().setInformation(
					(GameState.game.isDayTime()?"Day-time":"Night-time"),
					timeSB.toString());
			addEventListener(documentAttributes, id, "mouseenter", el2, false);
		}

		id = "ESSENCE_ICON";
		if (((EventTarget) documentAttributes.getElementById(id)) != null) {
			addEventListener(documentAttributes, id, "mousemove", moveTooltipListener, false);
			addEventListener(documentAttributes, id, "mouseleave", hideTooltipListener, false);
			
			TooltipInformationEventListener el2 = new TooltipInformationEventListener().setInformation("[style.boldArcane(Arcane Essences)]", "");
			addEventListener(documentAttributes, id, "mouseenter", el2, false);
		}
		
		AbstractAttribute[] attributes = {
				Attribute.HEALTH_MAXIMUM,
				Attribute.MANA_MAXIMUM,
				Attribute.EXPERIENCE,
				Attribute.MAJOR_PHYSIQUE,
				Attribute.MAJOR_ARCANE,
				Attribute.MAJOR_CORRUPTION,
				Attribute.AROUSAL,
				Attribute.LUST };
		
		List<GameCharacter> charactersBeingRendered = new ArrayList<>();
		if(GameState.game.isInSex()) {
			charactersBeingRendered.addAll(GameState.sex.getDominantParticipants(true).keySet());
			charactersBeingRendered.addAll(GameState.sex.getSubmissiveParticipants(true).keySet());
		} else if(GameState.game.isInCombat()) {
			charactersBeingRendered.add(GameState.game.getPlayer());
			charactersBeingRendered.addAll(GameState.combat.getAllies(GameState.game.getPlayer()));
		} else {
			if(GameState.game.getPlayer()!=null) {
				charactersBeingRendered.add(GameState.game.getPlayer());
				charactersBeingRendered.addAll(GameState.game.getPlayer().getCompanions());
			}
		}
		
		for(GameCharacter character : charactersBeingRendered) {
			String idModifier = (character.isPlayer()?"PLAYER_":"NPC_"+character.getId()+"_");
			
			for (AbstractAttribute a : attributes) {
				if (((EventTarget) documentAttributes.getElementById(idModifier+a.getName())) != null) {
					if(a == Attribute.EXPERIENCE) {
						((EventTarget) documentAttributes.getElementById(idModifier+a.getName())).addEventListener("click", e -> {
							if(character.isPlayer()) {
								// block when in character creation
								if(GameState.game.isInNewWorld()) {
									if (GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.PHONE) {
										if(GameState.game.getCurrentDialogueNode() == PhoneDialogue.CHARACTER_APPEARANCE) {
											openPhone();
										} else {
											GameState.game.setContent(new Response("", "", PhoneDialogue.CHARACTER_APPEARANCE));
										}
										
									} else {
										if (GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.NORMAL
//												|| GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.OCCUPANT_MANAGEMENT
												) {
											GameState.game.saveDialogueNode();
										}
										
										GameState.game.setContent(new Response("", "", PhoneDialogue.CHARACTER_APPEARANCE));
									}
								}
								
							} else {
								openCharactersPresent(character);
							}
						}, false);
					}
					addEventListener(documentAttributes, idModifier+a.getName(), "mousemove", moveTooltipListener, false);
					addEventListener(documentAttributes, idModifier+a.getName(), "mouseleave", hideTooltipListener, false);
					
					TooltipInformationEventListener el = new TooltipInformationEventListener().setAttribute(a, character);
					addEventListener(documentAttributes, idModifier+a.getName(), "mouseenter", el, false);
				}
			}
			
			if(((EventTarget) documentAttributes.getElementById(idModifier+"ATTRIBUTES"))!=null){
				((EventTarget) documentAttributes.getElementById(idModifier+"ATTRIBUTES")).addEventListener("click", e -> {
					if(character.isPlayer()) {
						// block when in character creation
						if(GameState.game.isInNewWorld()) {
							if(GameState.game.isInCombat()) {
								GameState.combat.setTargetedCombatant(character);
								updateUIStatic();
								GameState.game.updateResponses();
								
							} else if (GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.PHONE) {
								if(GameState.game.getCurrentDialogueNode() == PhoneDialogue.CHARACTER_PERK_TREE) {
									openPhone();
								} else {
									GameState.game.setContent(new Response("", "", PhoneDialogue.CHARACTER_PERK_TREE));
								}
								
							} else {
								if (GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.NORMAL
//										|| GameState.game.getCurrentDialogueNode().getDialogueNodeType() == DialogueNodeType.OCCUPANT_MANAGEMENT
										) {
									GameState.game.saveDialogueNode();
								}
								
								GameState.game.setContent(new Response("", "", PhoneDialogue.CHARACTER_PERK_TREE));
							}
						}
						
					} else { //TODO display NPC perk tree
						if(GameState.game.isInSex()) {
							GameState.sex.setTargetedPartner(GameState.game.getPlayer(), character);
							GameState.sex.recalculateSexActions();
							updateUIStatic();
							GameState.game.updateResponses();
							
						} else if(GameState.game.isInCombat()) {
							GameState.combat.setTargetedCombatant((NPC) character);
							updateUIStatic();
							GameState.game.updateResponses();
							
						} else {
							openCharactersPresent(character);
						}
					}
				}, false);
				addEventListener(documentAttributes, idModifier+"ATTRIBUTES", "mousemove", moveTooltipListener, false);
				addEventListener(documentAttributes, idModifier+"ATTRIBUTES", "mouseleave", hideTooltipListener, false);
	
				TooltipInformationEventListener el = new TooltipInformationEventListener().setExtraAttributes(character);
				addEventListener(documentAttributes, idModifier+"ATTRIBUTES", "mouseenter", el, false);
			}
			
			// Elemental:
			id = idModifier+"ELEMENTAL_"+Attribute.EXPERIENCE.getName();
			if (((EventTarget) documentAttributes.getElementById(id)) != null) {
				((EventTarget) documentAttributes.getElementById(id)).addEventListener("click", e -> {
					if(character.isPlayer() && GameState.game.isInNeutralDialogue()) {
						GameState.game.setContent(new Response("", "", ElementalDialogue.ELEMENTAL_START));
					}
				}, false);
				addEventListener(documentAttributes, id, "mousemove", moveTooltipListener, false);
				addEventListener(documentAttributes, id, "mouseleave", hideTooltipListener, false);
				TooltipInformationEventListener el = new TooltipInformationEventListener().setAttribute(Attribute.EXPERIENCE, character.getElemental());
				addEventListener(documentAttributes, id, "mouseenter", el, false);
			}
			
			id = idModifier+"ELEMENTAL_ATTRIBUTES";
			if(((EventTarget) documentAttributes.getElementById(id))!=null){
				((EventTarget) documentAttributes.getElementById(id)).addEventListener("click", e -> {
					if(character.isPlayer() && GameState.game.isInNeutralDialogue()) {
						GameState.game.setContent(new Response("", "", ElementalDialogue.ELEMENTAL_START));
					}
				}, false);
				addEventListener(documentAttributes, id, "mousemove", moveTooltipListener, false);
				addEventListener(documentAttributes, id, "mouseleave", hideTooltipListener, false);
				TooltipInformationEventListener el = new TooltipInformationEventListener().setExtraAttributes(character.getElemental());
				addEventListener(documentAttributes, id, "mouseenter", el, false);
			}
			
			
			// For status effect slots:
			for (AbstractStatusEffect se : character.getStatusEffects()) {
				id = "SE_"+idModifier + se;
				if (((EventTarget) documentAttributes.getElementById(id)) != null) {
					addEventListener(documentAttributes, id, "mousemove", moveTooltipListener, false);
					addEventListener(documentAttributes, id, "mouseleave", hideTooltipListener, false);
					
					// Set target to whoever is interacting with this area:
					if(GameState.game.isInSex()) { //TODO add click helper text //TODO add border or something to show that it's clickable
						SexAreaInterface si = null;
						if(se==StatusEffect.PENIS_STATUS) {
							si = SexAreaPenetration.PENIS;
						} else if(se==StatusEffect.ANUS_STATUS) {
							si = SexAreaOrifice.ANUS;
						} else if(se==StatusEffect.ASS_STATUS) {
							si = SexAreaOrifice.ASS;
						} else if(se==StatusEffect.MOUTH_STATUS) {
							si = SexAreaOrifice.MOUTH;
						} else if(se==StatusEffect.BREAST_STATUS) {
							si = SexAreaOrifice.BREAST;
						} else if(se==StatusEffect.BREAST_CROTCH_STATUS) {
							si = SexAreaOrifice.BREAST_CROTCH;
						} else if(se==StatusEffect.NIPPLE_STATUS) {
							si = SexAreaOrifice.NIPPLE;
						} else if(se==StatusEffect.NIPPLE_CROTCH_STATUS) {
							si = SexAreaOrifice.NIPPLE_CROTCH;
						} else if(se==StatusEffect.THIGH_STATUS) {
							si = SexAreaOrifice.THIGHS;
						} else if(se==StatusEffect.URETHRA_PENIS_STATUS) {
							si = SexAreaOrifice.URETHRA_PENIS;
						} else if(se==StatusEffect.URETHRA_VAGINA_STATUS) {
							si = SexAreaOrifice.URETHRA_VAGINA;
						} else if(se==StatusEffect.VAGINA_STATUS) {
							si = SexAreaOrifice.VAGINA;
						}
						if(si!=null) {
							setStatusEffectSexTargetChangeListener(documentAttributes, id, character, si);
						}
					}
					
					TooltipInformationEventListener el = new TooltipInformationEventListener().setStatusEffect(se, character);
					addEventListener(documentAttributes, id, "mouseenter", el, false);
				}
			}
			if(character.isElementalSummoned() && !character.isElementalActive()) {
				for (AbstractStatusEffect se : character.getElemental().getStatusEffects()) {
					id = "SE_ELEMENTAL_"+idModifier + se;
					if (((EventTarget) documentAttributes.getElementById(id)) != null) {
						addEventListener(documentAttributes, id, "mousemove", moveTooltipListener, false);
						addEventListener(documentAttributes, id, "mouseleave", hideTooltipListener, false);
						
						TooltipInformationEventListener el = new TooltipInformationEventListener().setStatusEffect(se, character.getElemental());
						addEventListener(documentAttributes, id, "mouseenter", el, false);
					}
				}
			}
			
			for (AbstractPerk trait : character.getTraits()) {
				id = "TRAIT_" + idModifier + Perk.getIdFromPerk(trait);
				if (((EventTarget) documentAttributes.getElementById(id)) != null) {
					addEventListener(documentAttributes, id, "mousemove", moveTooltipListener, false);
					addEventListener(documentAttributes, id, "mouseleave", hideTooltipListener, false);
					TooltipInformationEventListener el = new TooltipInformationEventListener().setPerk(trait, character);
					addEventListener(documentAttributes, id, "mouseenter", el, false);
				}
			}
			for (AbstractFetish f : character.getFetishes(true)) {
				if (((EventTarget) documentAttributes.getElementById("FETISH_"+idModifier + Fetish.getIdFromFetish(f))) != null) {
					addEventListener(documentAttributes, "FETISH_"+idModifier + Fetish.getIdFromFetish(f), "mousemove", moveTooltipListener, false);
					addEventListener(documentAttributes, "FETISH_"+idModifier + Fetish.getIdFromFetish(f), "mouseleave", hideTooltipListener, false);

					TooltipInformationEventListener el = new TooltipInformationEventListener().setFetish(f, character, false);
					addEventListener(documentAttributes, "FETISH_"+idModifier + Fetish.getIdFromFetish(f), "mouseenter", el, false);
				}
			}
			for (AbstractCombatMove combatMove : character.getAvailableMoves()) {
				id = "CM_"+idModifier + combatMove.getIdentifier();
				if (((EventTarget) documentAttributes.getElementById(id)) != null) {
					addEventListener(documentAttributes, id, "mousemove", moveTooltipListener, false);
					addEventListener(documentAttributes, id, "mouseleave", hideTooltipListener, false);
	
					TooltipInformationEventListener el = new TooltipInformationEventListener().setCombatMove(combatMove, character);
					addEventListener(documentAttributes, id, "mouseenter", el, false);
				}
			}
			for (Spell s : character.getAllSpells()) {
				if (((EventTarget) documentAttributes.getElementById("SPELL_"+idModifier + s)) != null) {
					addEventListener(documentAttributes, "SPELL_"+idModifier + s, "mousemove", moveTooltipListener, false);
					addEventListener(documentAttributes, "SPELL_"+idModifier + s, "mouseleave", hideTooltipListener, false);

					TooltipInformationEventListener el = new TooltipInformationEventListener().setSpell(s, character);
					addEventListener(documentAttributes, "SPELL_"+idModifier + s, "mouseenter", el, false);
				}
			}
		}
		
	}

	public static void overrideAutoLocale() {
		if (GameState.getProperties().hasValue(PropertyValue.autoLocale)) {
			GameState.getProperties().setValue(PropertyValue.autoLocale, false);
			Units.FORMATTER.updateNumberFormat(false);
		}
	}

	private static void setStatusEffectSexTargetChangeListener(Document document, String id, GameCharacter character, SexAreaInterface si) {
		((EventTarget) document.getElementById(id)).addEventListener("click", e -> {
			GameCharacter target = GameState.sex.getCharactersHavingOngoingActionWith(character, si).isEmpty()
					?null
					:GameState.sex.getCharactersHavingOngoingActionWith(character, si).get(0);
			if(target!=null && target instanceof NPC) {
				GameState.sex.setTargetedPartner(GameState.game.getPlayer(), target);
				GameState.sex.recalculateSexActions();
				updateUIStatic();
				GameState.game.updateResponses();
			}
		}, false);
	}
	
	private void manageRightListeners() {
		documentRight = (Document) webEngineRight.executeScript("document");
		EventListenerDataMap.put(documentRight, new ArrayList<>());

		Map<InventorySlot, List<AbstractClothing>> concealedSlots = new HashMap<>();
		
		if(RenderingEngine.getCharacterToRender()!=null) {
			concealedSlots = RenderingEngine.getCharacterToRender().getInventorySlotsConcealed(GameState.game.getPlayer());
		}
		
		// Inventory:
		String id;
		for (InventorySlot invSlot : InventorySlot.values()) {
			id = invSlot.toString() + "Slot";
			if (!invSlot.isWeapon()) {
				if (((EventTarget) documentRight.getElementById(id)) != null) {
					if(concealedSlots.keySet().contains(invSlot)) {
						addEventListener(documentRight, id, "mousemove", moveTooltipListener, false);
						addEventListener(documentRight, id, "mouseleave", hideTooltipListener, false);
						TooltipInformationEventListener el2 = new TooltipInformationEventListener().setConcealedSlot(invSlot);
						addEventListener(documentRight, id, "mouseenter", el2, false);
						
					} else {
						if(!RenderingEngine.ENGINE.isRenderingTattoosRight() || invSlot.isJewellery()) {
							InventorySelectedItemEventListener el = new InventorySelectedItemEventListener().setClothingEquipped(RenderingEngine.getCharacterToRender(), invSlot);
							addEventListener(documentRight, id, "click", el, false);
						}
						
						addEventListener(documentRight, id, "mousemove", moveTooltipListener, false);
						addEventListener(documentRight, id, "mouseleave", hideTooltipListener, false);
						TooltipInventoryEventListener el2 = new TooltipInventoryEventListener().setInventorySlot(invSlot, RenderingEngine.getCharacterToRender());
						addEventListener(documentRight, id, "mouseenter", el2, false);
					}
				}
			} else {
				if (((EventTarget) documentRight.getElementById(id)) != null) {
					
					InventorySelectedItemEventListener el = new InventorySelectedItemEventListener().setWeaponEquipped(RenderingEngine.getCharacterToRender(), invSlot);
					addEventListener(documentRight, id, "click", el, false);
					
					addEventListener(documentRight, id, "mousemove", moveTooltipListener, false);
					addEventListener(documentRight, id, "mouseleave", hideTooltipListener, false);
					TooltipInventoryEventListener el2 = new TooltipInventoryEventListener().setInventorySlot(invSlot, RenderingEngine.getCharacterToRender());
					addEventListener(documentRight, id, "mouseenter", el2, false);
				}
			}
		}
		
		id = "TATTOO_SWITCH_RIGHT";
		if (((EventTarget) documentRight.getElementById(id)) != null) {
			((EventTarget) documentRight.getElementById(id)).addEventListener("click", e -> {
				RenderingEngine.ENGINE.setRenderingTattoosRight(!RenderingEngine.ENGINE.isRenderingTattoosRight());
				updateUIRightPanel();
			}, false);
			addEventListener(documentRight, id, "mousemove", moveTooltipListener, false);
			addEventListener(documentRight, id, "mouseleave", hideTooltipListener, false);
			TooltipInformationEventListener el2 = new TooltipInformationEventListener().setInformation(
					!RenderingEngine.ENGINE.isRenderingTattoosRight()
						?"Switch to tattoos"
						:"Switch to clothing",
					"");
			addEventListener(documentRight, id, "mouseenter", el2, false);
		}
		
		id = "INVENTORY_ENCHANTMENT_LIMIT_NPC";
		if (((EventTarget) documentRight.getElementById(id)) != null) {
			addEventListener(documentRight, id, "mousemove", moveTooltipListener, false);
			addEventListener(documentRight, id, "mouseleave", hideTooltipListener, false);
			TooltipInformationEventListener el2 = new TooltipInformationEventListener().setInformation(
					Util.capitaliseSentence(Attribute.ENCHANTMENT_LIMIT.getName()),
					UtilText.parse(RenderingEngine.getCharacterToRender(),
							"The total amount of weapon, clothing, and tattoo attribute enchantments [npc.nameIsFull] able to handle without incurring massive penalties."
								+" [npc.Her] maximum is calculated from: <i>10 + (level) + (perk gains)</i>"));
			addEventListener(documentRight, id, "mouseenter", el2, false);
		}
		
		if(GameState.game.getPlayer()!=null) {
			// Money on floor:
			id = "MONEY_ON_FLOOR";
			if (((EventTarget) documentRight.getElementById(id)) != null) {
				if(!GameState.game.getCurrentDialogueNode().isInventoryDisabled() || GameState.game.getCurrentDialogueNode().getDialogueNodeType()==DialogueNodeType.INVENTORY) {
					addEventListener(documentRight, id, "click", e -> {
						Main.mainController.openInventory();
					}, false);
				}
			}

			// Weapons on floor:
			for (Entry<AbstractWeapon, Integer> entry : GameState.game.getPlayerCell().getInventory().getAllWeaponsInInventory().entrySet()) {
				id = "WEAPON_FLOOR_" + entry.getKey().hashCode();
				if (((EventTarget) documentRight.getElementById(id)) != null) {
					if(!GameState.game.getCurrentDialogueNode().isInventoryDisabled() || GameState.game.getCurrentDialogueNode().getDialogueNodeType()==DialogueNodeType.INVENTORY) {
						InventorySelectedItemEventListener el = new InventorySelectedItemEventListener().setWeaponInventory(entry.getKey(), null);
						addEventListener(documentRight, id, "click", el, false);
					}
					addEventListener(documentRight, id, "mousemove", moveTooltipListener, false);
					addEventListener(documentRight, id, "mouseleave", hideTooltipListener, false);
					TooltipInventoryEventListener el2 = new TooltipInventoryEventListener().setWeapon(entry.getKey(), null, false);
					addEventListener(documentRight, id, "mouseenter", el2, false);
				}
			}
			
			// Clothing on floor:
			for (Entry<AbstractClothing, Integer> entry : GameState.game.getPlayerCell().getInventory().getAllClothingInInventory().entrySet()) {
				id = "CLOTHING_FLOOR_" + entry.getKey().hashCode();
				if (((EventTarget) documentRight.getElementById(id)) != null) {
					if(!GameState.game.getCurrentDialogueNode().isInventoryDisabled() || GameState.game.getCurrentDialogueNode().getDialogueNodeType()==DialogueNodeType.INVENTORY) {
						InventorySelectedItemEventListener el = new InventorySelectedItemEventListener().setClothingInventory(entry.getKey(), null);
						addEventListener(documentRight, id, "click", el, false);
					}
					addEventListener(documentRight, id, "mousemove", moveTooltipListener, false);
					addEventListener(documentRight, id, "mouseleave", hideTooltipListener, false);
					TooltipInventoryEventListener el2 = new TooltipInventoryEventListener().setClothing(entry.getKey(), null, null);
					addEventListener(documentRight, id, "mouseenter", el2, false);
				}
			}
			
			// Items on floor:
			for (Entry<AbstractItem, Integer> entry : GameState.game.getPlayerCell().getInventory().getAllItemsInInventory().entrySet()) {
				id = "ITEM_FLOOR_" + entry.getKey().hashCode();
				if (((EventTarget) documentRight.getElementById(id)) != null) {
					if(!GameState.game.getCurrentDialogueNode().isInventoryDisabled() || GameState.game.getCurrentDialogueNode().getDialogueNodeType()==DialogueNodeType.INVENTORY) {
						InventorySelectedItemEventListener el = new InventorySelectedItemEventListener().setItemInventory(entry.getKey(), null);
						addEventListener(documentRight, id, "click", el, false);
					}
					addEventListener(documentRight, id, "mousemove", moveTooltipListener, false);
					addEventListener(documentRight, id, "mouseleave", hideTooltipListener, false);
					TooltipInventoryEventListener el2 = new TooltipInventoryEventListener().setItem(entry.getKey(), null, null);
					addEventListener(documentRight, id, "mouseenter", el2, false);
				}
			}
		}
		
		AbstractAttribute[] attributes = {
				Attribute.HEALTH_MAXIMUM,
				Attribute.MANA_MAXIMUM,
				Attribute.EXPERIENCE,
				Attribute.MAJOR_PHYSIQUE,
				Attribute.MAJOR_ARCANE,
				Attribute.MAJOR_CORRUPTION,
				Attribute.AROUSAL,
				Attribute.LUST };
		if(!RenderingEngine.ENGINE.isRenderingCharactersRightPanel()) {
			attributes = new AbstractAttribute[] {Attribute.EXPERIENCE};
		}
		
		List<GameCharacter> charactersBeingRendered = new ArrayList<>();
		if(GameState.game.isInSex()) {
			charactersBeingRendered.addAll(GameState.sex.getDominantParticipants(true).keySet());
			charactersBeingRendered.addAll(GameState.sex.getSubmissiveParticipants(true).keySet());
			
		} else if(GameState.game.isInCombat()) {
			charactersBeingRendered.addAll(GameState.combat.getEnemies(GameState.game.getPlayer()));
			
		} else if(RenderingEngine.ENGINE.isRenderingCharactersRightPanel()) {
			charactersBeingRendered.add(RenderingEngine.getCharacterToRender());
			
		} else {
			charactersBeingRendered.addAll(GameState.game.getCharactersPresent());
			for(GameCharacter c : GameState.game.getCharactersPresent()) {
				if(c.isElementalSummoned() && !c.getElemental().isActive()) {
					charactersBeingRendered.add(c.getElemental());
				}
			}
			
			if(GameState.game.isStarted()) {
				int i=0;
				for(Population pop : GameState.game.getPlayer().getLocationPlace().getPlaceType().getPopulation()) {
					id = "PLACE_POPULATION_"+i;
					if (((EventTarget) documentRight.getElementById(id)) != null) {
						addEventListener(documentRight, id, "mousemove", moveTooltipListener, false);
						addEventListener(documentRight, id, "mouseleave", hideTooltipListener, false);
						
						Set<AbstractSubspecies> subspecies = new HashSet<>();
						subspecies.addAll(pop.getSpecies().keySet());
						TooltipInformationEventListener el;
						if(subspecies.isEmpty()) {
							el = new TooltipInformationEventListener().setInformation("Races Present", "[style.colourDisabled(Unknown)]");
						} else {
							el = new TooltipInformationEventListener().setInformation(
									"Races Present",
									Util.subspeciesToStringList(subspecies, true)+".",
									16 + ((subspecies.size()/3)*16));
						}
						addEventListener(documentRight, id, "mouseenter", el, false);
					}
					i++;
				}
			}
		}
		
		charactersBeingRendered.remove(GameState.game.getPlayer());
		
		for(GameCharacter character : charactersBeingRendered) {
			String idModifier = character.getId()+"_";
			
			for (AbstractAttribute a : attributes) {
				if (((EventTarget) documentRight.getElementById("NPC_"+idModifier+a.getName())) != null) {
					if(a == Attribute.EXPERIENCE && (!character.isElemental() || ((Elemental)character).isActive())) {
						((EventTarget) documentRight.getElementById("NPC_"+idModifier+a.getName())).addEventListener("click", e -> {
							openCharactersPresent(character);
						}, false);
					}
					addEventListener(documentRight, "NPC_"+idModifier+a.getName(), "mousemove", moveTooltipListener, false);
					addEventListener(documentRight, "NPC_"+idModifier+a.getName(), "mouseleave", hideTooltipListener, false);
					
					TooltipInformationEventListener el = new TooltipInformationEventListener().setAttribute(a, character);
					addEventListener(documentRight, "NPC_"+idModifier+a.getName(), "mouseenter", el, false);
				}
			}
			
			// Extra attribute info:
			if(((EventTarget) documentRight.getElementById("NPC_"+idModifier+"ATTRIBUTES"))!=null){
				if(!RenderingEngine.ENGINE.isRenderingCharactersRightPanel() && (!character.isElemental() || ((Elemental)character).isActive())) {
					((EventTarget) documentRight.getElementById("NPC_"+idModifier+"ATTRIBUTES")).addEventListener("click", e -> {
						openCharactersPresent(character);
					}, false);
					
				} else if(GameState.game.isInSex()) {
					((EventTarget) documentRight.getElementById("NPC_"+idModifier+"ATTRIBUTES")).addEventListener("click", e -> {
						GameState.sex.setTargetedPartner(GameState.game.getPlayer(), character);
						GameState.sex.recalculateSexActions();
						updateUIStatic();
						GameState.game.updateResponses();
					}, false);
					
				} else if(GameState.game.isInCombat()) {
					((EventTarget) documentRight.getElementById("NPC_"+idModifier+"ATTRIBUTES")).addEventListener("click", e -> {
						GameState.combat.setTargetedCombatant((NPC) character);
						updateUIStatic();
						GameState.game.updateResponses();
					}, false);
				}
				addEventListener(documentRight, "NPC_"+idModifier+"ATTRIBUTES", "mousemove", moveTooltipListener, false);
				addEventListener(documentRight, "NPC_"+idModifier+"ATTRIBUTES", "mouseleave", hideTooltipListener, false);
	
				TooltipInformationEventListener el = new TooltipInformationEventListener().setExtraAttributes(character);
				addEventListener(documentRight, "NPC_"+idModifier+"ATTRIBUTES", "mouseenter", el, false);
			}
			
			// Elemental:
			id = "NPC_"+idModifier+"ELEMENTAL_"+Attribute.EXPERIENCE.getName();
			if (((EventTarget) documentRight.getElementById(id)) != null) {
				addEventListener(documentRight, id, "mousemove", moveTooltipListener, false);
				addEventListener(documentRight, id, "mouseleave", hideTooltipListener, false);
				TooltipInformationEventListener el = new TooltipInformationEventListener().setAttribute(Attribute.EXPERIENCE, character.getElemental());
				addEventListener(documentRight, id, "mouseenter", el, false);
			}
			
			id = "NPC_"+idModifier+"ELEMENTAL_ATTRIBUTES";
			if(((EventTarget) documentRight.getElementById(id))!=null){
				addEventListener(documentRight, id, "mousemove", moveTooltipListener, false);
				addEventListener(documentRight, id, "mouseleave", hideTooltipListener, false);
				TooltipInformationEventListener el = new TooltipInformationEventListener().setExtraAttributes(character.getElemental());
				addEventListener(documentRight, id, "mouseenter", el, false);
			}
			
			if(RenderingEngine.ENGINE.isRenderingCharactersRightPanel()) {
				// For status effect slots:
				for (AbstractStatusEffect se : character.getStatusEffects()) {
					id = "SE_NPC_"+idModifier + se;
					if (((EventTarget) documentRight.getElementById(id)) != null) {
						addEventListener(documentRight, id, "mousemove", moveTooltipListener, false);
						addEventListener(documentRight, id, "mouseleave", hideTooltipListener, false);
		
						// Set target to whoever is interacting with this area:
						if(GameState.game.isInSex()) { //TODO add click helper text
							SexAreaInterface si = null;
							if(se==StatusEffect.PENIS_STATUS) {
								si = SexAreaPenetration.PENIS;
							} else if(se==StatusEffect.ANUS_STATUS) {
								si = SexAreaOrifice.ANUS;
							} else if(se==StatusEffect.ASS_STATUS) {
								si = SexAreaOrifice.ASS;
							} else if(se==StatusEffect.MOUTH_STATUS) {
								si = SexAreaOrifice.MOUTH;
							} else if(se==StatusEffect.BREAST_STATUS) {
								si = SexAreaOrifice.BREAST;
							} else if(se==StatusEffect.NIPPLE_STATUS) {
								si = SexAreaOrifice.NIPPLE;
							} else if(se==StatusEffect.THIGH_STATUS) {
								si = SexAreaOrifice.THIGHS;
							} else if(se==StatusEffect.URETHRA_PENIS_STATUS) {
								si = SexAreaOrifice.URETHRA_PENIS;
							} else if(se==StatusEffect.URETHRA_VAGINA_STATUS) {
								si = SexAreaOrifice.URETHRA_VAGINA;
							} else if(se==StatusEffect.VAGINA_STATUS) {
								si = SexAreaOrifice.VAGINA;
							}
							if(si!=null) {
								setStatusEffectSexTargetChangeListener(documentRight, id, character, si);
							}
						}
						
						TooltipInformationEventListener el = new TooltipInformationEventListener().setStatusEffect(se, character);
						addEventListener(documentRight, id, "mouseenter", el, false);
					}
				}
				if(character.isElementalSummoned() && !character.isElementalActive()) {
					for (AbstractStatusEffect se : character.getElemental().getStatusEffects()) {
						id = "SE_ELEMENTAL_NPC_"+idModifier + se;
						if (((EventTarget) documentRight.getElementById(id)) != null) {
							addEventListener(documentRight, id, "mousemove", moveTooltipListener, false);
							addEventListener(documentRight, id, "mouseleave", hideTooltipListener, false);
							
							TooltipInformationEventListener el = new TooltipInformationEventListener().setStatusEffect(se, character.getElemental());
							addEventListener(documentRight, id, "mouseenter", el, false);
						}
					}
				}
				
				// For perk slots:
				for (AbstractPerk p : character.getMajorPerks()) {
					id = "PERK_NPC_"+idModifier + Perk.getIdFromPerk(p);
					if (((EventTarget) documentRight.getElementById(id)) != null) {
						addEventListener(documentRight, id, "mousemove", moveTooltipListener, false);
						addEventListener(documentRight, id, "mouseleave", hideTooltipListener, false);
		
						TooltipInformationEventListener el = new TooltipInformationEventListener().setPerk(p, character);
						addEventListener(documentRight, id, "mouseenter", el, false);
					}
				}
				for (AbstractFetish f : character.getFetishes(true)) {
					if (((EventTarget) documentRight.getElementById("FETISH_NPC_"+idModifier + Fetish.getIdFromFetish(f))) != null) {
						addEventListener(documentRight, "FETISH_NPC_"+idModifier + Fetish.getIdFromFetish(f), "mousemove", moveTooltipListener, false);
						addEventListener(documentRight, "FETISH_NPC_"+idModifier + Fetish.getIdFromFetish(f), "mouseleave", hideTooltipListener, false);
	
						TooltipInformationEventListener el = new TooltipInformationEventListener().setFetish(f, character, false);
						addEventListener(documentRight, "FETISH_NPC_"+idModifier + Fetish.getIdFromFetish(f), "mouseenter", el, false);
					}
				}
				for (AbstractCombatMove combatMove : character.getAvailableMoves()) {
					id = "CM_NPC_"+idModifier + combatMove.getIdentifier();
					if (((EventTarget) documentRight.getElementById(id)) != null) {
						addEventListener(documentRight, id, "mousemove", moveTooltipListener, false);
						addEventListener(documentRight, id, "mouseleave", hideTooltipListener, false);
		
						TooltipInformationEventListener el = new TooltipInformationEventListener().setCombatMove(combatMove, character);
						addEventListener(documentRight, id, "mouseenter", el, false);
					}
				}
				for (Spell s : character.getAllSpells()) {
					if (((EventTarget) documentRight.getElementById("SPELL_"+idModifier + s)) != null) {
						addEventListener(documentRight, "SPELL_"+idModifier + s, "mousemove", moveTooltipListener, false);
						addEventListener(documentRight, "SPELL_"+idModifier + s, "mouseleave", hideTooltipListener, false);
	
						TooltipInformationEventListener el = new TooltipInformationEventListener().setSpell(s, character);
						addEventListener(documentRight, "SPELL_"+idModifier + s, "mouseenter", el, false);
					}
				}
			}
		}
	}
	
	
	private boolean useJavascriptToSetContent = true;
	
	private void setWebEngineContent(WebEngine engine, String content) {
		content=content.replaceAll("[\r\n]", "");
		content=content.replaceAll("\"", "'");
		
		engine.executeScript(
			"document.open('text/html');"
			+ "document.write(\""+content+"\");"
			+"document.close();");
	}
	
	public void setMainContent(String content) {
		if(useJavascriptToSetContent
				 // For rendering images from file:
				&& !GameState.game.getCurrentDialogueNode().equals(CharactersPresentDialogue.MENU)
				&& !GameState.game.getCurrentDialogueNode().equals(PhoneDialogue.CONTACTS_CHARACTER)
				&& !GameState.game.getCurrentDialogueNode().equals(CompanionManagement.SLAVE_MANAGEMENT_INSPECT)) {
			unbindListeners(document);
			setWebEngineContent(webEngine, content);
			manageMainListeners();
		} else {
			webEngine.loadContent(content);
		}
	}

	/**
	 * Sets the tooltip content, and returns the pixel height
	 * of the resultant tooltip content.
	 *
	 * To calculate height, wraps the content in a #sizing-box
	 * div, and checks that element's height.
	 *
	 * This does not set the tooltip height, but only measures
	 * the content height. The tooltip must be manually
	 * resized as needed.
	 *
	 * Additionally, this should be called before setting the
	 * tooltip position, if that requires knowledge of the
	 * final height.
	 *
	 * @param content HTML content to display in the tooltip
	 * @return the calculated height of the tooltip content
	 */
	public int setTooltipContent(String content) {
		if (GameState.getProperties().hasValue(PropertyValue.fadeInText)) {
			content = "<div class='tooltip-animation' style='width: 100%;'>" + content + "</div>";
		}
		content = "<div id='sizing-box' style='width: 100%;'>" + content + "</div>";
		if(useJavascriptToSetContent) {
			setWebEngineContent(webEngineTooltip, content);
		} else {
			webEngineTooltip.loadContent(content);
		}
		int height = 0;
		try {
			// add 8 + 8 to account for the top + bottom margins
			height = 16 + (int)Main.mainController.getWebEngineTooltip().executeScript("document.getElementById('sizing-box').scrollHeight");
		} catch(Exception e) {
			System.err.println("Failed to locate the tooltip sizing box!");
			e.printStackTrace();
		}
		TooltipUpdateThread.cancelThreads = true;
		Main.mainController.getTooltip().hide();
		return height;
	}
	
	public void setAttributePanelContent(String content) {
		if(useJavascriptToSetContent) {
			unbindListeners(documentAttributes);
			setWebEngineContent(webEngineAttributes, content);
			manageAttributeListeners();
		} else {
			webEngineAttributes.loadContent(content);
		}
	}
	
	public void setRightPanelContent(String content) {
		if(useJavascriptToSetContent) {
			unbindListeners(documentRight);
			setWebEngineContent(webEngineRight, content);
			manageRightListeners();
		} else {
			webEngineRight.loadContent(content);
		}
	}
	
	public void setButtonsLeftContent(String content) {
		if(useJavascriptToSetContent) {
			unbindListeners(documentButtonsLeft);
			setWebEngineContent(webEngineButtonsLeft, content);
			manageButtonLeftListeners();
		} else {
			webEngineButtonsLeft.loadContent(content);
		}
	}
	
	public void setButtonsRightContent(String content) {
		if(useJavascriptToSetContent) {
			unbindListeners(documentButtonsRight);
			setWebEngineContent(webEngineButtonsRight, content);
			manageButtonRightListeners();
		} else {
			webEngineButtonsRight.loadContent(content);
		}
	}
	
	private boolean keyEventMatchesBindings(KeyboardAction binding, KeyEvent keyEvent) {
		KeyCodeWithModifiers primary = GameState.getProperties().hotkeyMapPrimary.get(binding);
		KeyCodeWithModifiers secondary = GameState.getProperties().hotkeyMapSecondary.get(binding);
		return (primary != null && KeyCodes.matches(primary, keyEvent)) || (secondary != null && KeyCodes.matches(secondary, keyEvent));
	}
	
	/**
	 * For cheat codes. debug: toggle debug mode spawn: open enemy spawn menu
	 */
	private void checkLastKeys() {
		if (lastKeysEqual(KeyCode.B, KeyCode.U, KeyCode.G, KeyCode.G, KeyCode.Y)) {
			if(GameState.game!=null) {
				if(GameState.game.isStarted() && GameState.game.isInNewWorld() && GameState.game.isPrologueFinished()) {
					GameState.game.setContent(new Response("", "", GameState.game.getDefaultDialogue(false)));
					GameState.game.saveDialogueNode();
					GameState.game.setContent(new Response("", "", DebugDialogue.DEBUG_MENU));
				} else {
					GameState.game.flashMessage(PresetColour.GENERIC_BAD, "Unavailable in prologue!");
				}
			}
		}
//		if (lastKeysEqual(KeyCode.N, KeyCode.O, KeyCode.X, KeyCode.X, KeyCode.X)) {
//			if(GameState.game.getPlayer().getLocationPlace().getPlaceType().equals(PlaceType.SHOPPING_ARCADE_GENERIC_SHOP) && !GameState.game.getNpc(TestNPC.class).isSlave()) {
//				GameState.game.setActiveNPC(GameState.game.getNpc(TestNPC.class));
//				GameState.game.setContent(new Response("", "", TestNPC.TEST_DIALOGUE) {
//					@Override
//					public void effects() {
//						GameState.game.getNpc(TestNPC.class).setLocation(WorldType.SHOPPING_ARCADE, GameState.game.getPlayer().getLocation(), true);
//					}
//				});
//			}
//		}
		if (lastKeysEqual(KeyCode.D, KeyCode.K, KeyCode.O, KeyCode.M, KeyCode.A)) {
			if(GameState.game!=null) {
				if(GameState.game.isStarted()
						&& GameState.game.isInNewWorld()
						&& GameState.game.isInCombat()
						&& GameState.combat.getAllCombatants(false).size()==1
						&& !GameState.combat.getEnemies(GameState.game.getPlayer()).get(0).isUnique()
						&& GameState.game.getPlayer().hasPenis()
						&& GameState.game.getPlayer().isAbleToAccessCoverableArea(CoverableArea.PENIS, true)
						&& GameState.combat.getEnemies(GameState.game.getPlayer()).get(0).hasVagina()
						&& GameState.combat.getEnemies(GameState.game.getPlayer()).get(0).isAbleToAccessCoverableArea(CoverableArea.VAGINA, true)) {
					GameCharacter target = GameState.combat.getEnemies(GameState.game.getPlayer()).get(0);
					GameState.combat.endCombat(true);
//					GameState.game.setContent(new Response("", "", GameState.game.getDefaultDialogue(false)));
					GameState.game.setContent(new ResponseSex(
							"Dominate",
							"",
							false,
							false,
							new SexManagerDefault(
									true,
									SexPosition.LYING_DOWN,
									Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotLyingDown.MATING_PRESS)),
									Util.newHashMapOfValues(new Value<>(target, SexSlotLyingDown.LYING_DOWN))) {
								@Override
								public boolean isPositionChangingAllowed(GameCharacter character) {
									return false;
								}
								@Override
								public boolean isSwapPositionAllowed(GameCharacter character, GameCharacter target) {
									return false;
								}
								@Override
								public Map<GameCharacter, List<CoverableArea>> exposeAtStartOfSexMap() {
									return Util.newHashMapOfValues(
											new Value<>(GameState.game.getPlayer(), Util.newArrayListOfValues(CoverableArea.PENIS)),
											new Value<>(target, Util.newArrayListOfValues(CoverableArea.VAGINA)));
								}
								@Override
								public void initStartingLustAndArousal(GameCharacter character) {
									if(!character.isPlayer()) {
										character.setLustNoText(100);
										character.setArousal(25);
									} else {
										super.initStartingLustAndArousal(character);
									}
								}
								@Override
								public SexPace getForcedSexPace(GameCharacter character) {
									if(!character.isPlayer()) {
										return SexPace.SUB_EAGER;
									}
									return null;
								}
								@Override
								public boolean isPartnerWantingToStopSex(GameCharacter partner) {
									return false;
								}
								@Override
								public boolean isAppendStartingExposedDescriptions(GameCharacter character) {
									return false;
								}
							},
							null,
							null,
							DebugDialogue.POST_SEX_2KOMA,
							UtilText.parseFromXMLFile("misc/misc", "START_SEX_2KOMA", GameState.game.getPlayer(), target)) {
						@Override
						public List<InitialSexActionInformation> getInitialSexActions() {
							return Util.newArrayListOfValues(new InitialSexActionInformation(GameState.game.getPlayer(), target, PenisVagina.PENIS_FUCKING_START, true, true));
						}
					});
					
				} else {
					GameState.game.flashMessage(PresetColour.GENERIC_BAD, "Cannot dominate!");
				}
			}
		}

		if (lastKeysEqual(KeyCode.S, KeyCode.K, KeyCode.O, KeyCode.M, KeyCode.A)) {
			if(GameState.game!=null) {
				if(GameState.game.isStarted()
						&& GameState.game.isInNewWorld()
						&& GameState.game.isInCombat()
						&& GameState.combat.getAllCombatants(false).size()==1
						&& !GameState.combat.getEnemies(GameState.game.getPlayer()).get(0).isUnique()
						&& GameState.combat.getEnemies(GameState.game.getPlayer()).get(0).hasPenis()
						&& GameState.combat.getEnemies(GameState.game.getPlayer()).get(0).isAbleToAccessCoverableArea(CoverableArea.PENIS, true)
						&& GameState.game.getPlayer().hasVagina()
						&& GameState.game.getPlayer().isAbleToAccessCoverableArea(CoverableArea.VAGINA, true)) {
					GameCharacter target = GameState.combat.getEnemies(GameState.game.getPlayer()).get(0);
					GameState.combat.endCombat(false);
//					GameState.game.setContent(new Response("", "", GameState.game.getDefaultDialogue(false)));
					GameState.game.setContent(new ResponseSex(
							"Dominated",
							"",
							false,
							false,
							new SexManagerDefault(
									true,
									SexPosition.LYING_DOWN,
									Util.newHashMapOfValues(new Value<>(target, SexSlotLyingDown.MATING_PRESS)),
									Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotLyingDown.LYING_DOWN))) {
								@Override
								public boolean isPositionChangingAllowed(GameCharacter character) {
									return false;
								}
								@Override
								public boolean isSwapPositionAllowed(GameCharacter character, GameCharacter target) {
									return false;
								}
								@Override
								public Map<GameCharacter, List<CoverableArea>> exposeAtStartOfSexMap() {
									return Util.newHashMapOfValues(
											new Value<>(GameState.game.getPlayer(), Util.newArrayListOfValues(CoverableArea.VAGINA)),
											new Value<>(target, Util.newArrayListOfValues(CoverableArea.PENIS)));
								}
								@Override
								public void initStartingLustAndArousal(GameCharacter character) {
									character.setLustNoText(100);
									character.setArousal(25);
								}
								@Override
								public SexPace getForcedSexPace(GameCharacter character) {
									if(!character.isPlayer()) {
										if(character.getFetishDesire(Fetish.FETISH_DOMINANT).isNegative()) {
											return SexPace.DOM_NORMAL;
										}
										return SexPace.DOM_ROUGH;
									}
									return null;
								}
								@Override
								public boolean isAppendStartingExposedDescriptions(GameCharacter character) {
									return false;
								}
								@Override
								public boolean isAppendStartingWetDescriptions() {
									return false;
								}
								@Override
								public SexType getForeplayPreference(GameCharacter character, GameCharacter targetedCharacter) {
									return new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, SexAreaOrifice.VAGINA);
								}
								@Override
								public SexType getMainSexPreference(GameCharacter character, GameCharacter targetedCharacter) {
									return new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, SexAreaOrifice.VAGINA);
								}
							},
							null,
							null,
							DebugDialogue.POST_SEX_2KOMA,
							UtilText.parseFromXMLFile("misc/misc", "START_SEX_2KOMA", target, GameState.game.getPlayer())) {
						@Override
						public List<InitialSexActionInformation> getInitialSexActions() {
							return Util.newArrayListOfValues(new InitialSexActionInformation(target, GameState.game.getPlayer(), PenisVagina.PENIS_FUCKING_START, true, true));
						}
					});
					
				} else {
					GameState.game.flashMessage(PresetColour.GENERIC_BAD, "Cannot dominate!");
				}
			}
		}
	}

	private boolean lastKeysEqual(KeyCode one, KeyCode two, KeyCode three, KeyCode four, KeyCode five) {
		return lastKeys[0] == five && lastKeys[1] == four && lastKeys[2] == three && lastKeys[3] == two && lastKeys[4] == one;
	}

	/**
	 * Updates every element of the UI.
	 */
	@Override
	public void updateUI() {
		updateUIStatic();
	}

	public static void updateUIStatic() {
		if (GameState.game.isRenderAttributesSection()) {
			RenderingEngine.ENGINE.renderAttributesPanelLeft();
			RenderingEngine.ENGINE.renderAttributesPanelRight();
		}
		updateUIButtons();
	}
	
	public static void updateUIButtons() {
		RenderingEngine.ENGINE.renderButtonsLeft();
		RenderingEngine.ENGINE.renderButtonsRight();
	}
	
	public void updateUILeftPanel() {
		RenderingEngine.ENGINE.renderAttributesPanelLeft();
	}
	
	public static void updateUIRightPanel() {
		RenderingEngine.ENGINE.renderAttributesPanelRight();
	}


	private void moveTile(int xOffset, int yOffset) {
		Vector2i location = GameState.game.getPlayer().getLocation();
		if (location.getY() + yOffset < GameState.game.getActiveWorld().WORLD_HEIGHT
				&& location.getY() + yOffset >= 0
				&& location.getX() + xOffset < GameState.game.getActiveWorld().WORLD_WIDTH
				&& location.getX() + xOffset >= 0) {
			
			Cell placeTypeTarget = GameState.game.getActiveWorld().getCell(location.getX() + xOffset, location.getY() + yOffset);
			
			if(!placeTypeTarget.getPlace().getPlaceType().equals(PlaceType.GENERIC_IMPASSABLE)) {
				if(GameState.game.isInGlobalMap() && placeTypeTarget.getDialogue(false)==null) {
					GameState.game.flashMessage(PresetColour.GENERIC_BAD, "Cannot travel here!");
					
				} else {
					if(GameState.game.isInGlobalMap()) {
						GameState.game.getPlayer().setGlobalLocation(new Vector2i(location.getX() + xOffset, location.getY() + yOffset));
					} else {
						GameState.game.getPlayer().setGlobalLocation(GameState.game.getWorlds().get(WorldType.WORLD_MAP).getCell(GameState.game.getPlayerCell().getType().getGlobalMapLocation()).getLocation());
					}
					GameState.game.getPlayer().setLocation(new Vector2i(location.getX() + xOffset, location.getY() + yOffset));
					
					DialogueNode dn = GameState.game.getPlayerCell().getDialogue(true);
					
					GameState.game.setContent(new Response("", "", dn) {
						@Override
						public int getSecondsPassed() {
							return GameState.game.getModifierTravelTime(GameState.game.getPlayer().getLocationPlace().getPlaceType().isLand(), dn.getSecondsPassed());
						}
					});
				}
			}
		}
	}
	
	public void moveNorth() {
		moveTile(0, 1);
	}

	public void moveSouth() {
		moveTile(0, -1);
	}

	public void moveEast() {
		moveTile(1, 0);
	}

	public void moveWest() {
		moveTile(-1, 0);
	}

	
	// Getters & Setters:

	// Rendering related:
	public WebView getWebViewMain() {
		return webViewMain;
	}

	public WebView getWebViewAttributes() {
		return webViewAttributes;
	}

	// ------------------------------------------------------------------
	// RenderSink implementation
	// ------------------------------------------------------------------

	@Override
	public void hideTooltip() {
		getTooltip().hide();
	}

	@Override
	public Object executeScript(String script) {
		return webEngine.executeScript(script);
	}

	@Override
	public org.w3c.dom.Document getDocument() {
		return webEngine.getDocument();
	}

	@Override
	public void cancelTooltipThreads() {
		TooltipUpdateThread.cancelThreads = true;
	}

	@Override
	public void onNewGameStarted() {
		Main.rebuildSceneForNewGame();
	}

	public WebEngine getWebEngine() {
		return webEngine;
	}

	public WebEngine getWebEngineTooltip() {
		return webEngineTooltip;
	}
	
	public WebEngine getWebEngineAttributes() {
		return webEngineAttributes;
	}
	
	public WebEngine getWebEngineRight() {
		return webEngineRight;
	}
	
	public WebEngine getWebEngineButtonsLeft() {
		return webEngineButtonsLeft;
	}
	
	public WebEngine getWebEngineButtonsRight() {
		return webEngineButtonsRight;
	}

	// UI related:
	public Tooltip getTooltip() {
		return tooltip;
	}

	public WebView getWebviewTooltip() {
		return webviewTooltip;
	}

	public void setTooltipSize(int width, int height) {
		webviewTooltip.setMaxWidth(width);
		webviewTooltip.setMaxHeight(height);
		webviewTooltip.setPrefHeight(height);
		tooltip.setMaxWidth(width);
		tooltip.setPrefWidth(width);
		tooltip.setMaxHeight(height);
		tooltip.setPrefHeight(height);
		tooltipWidth = width;
		tooltipHeight = height;
	}
	
	public int[] getTooltipSize() {
		return new int[] {tooltipWidth, tooltipHeight};
	}

	public KeyboardAction getActionToBind() {
		return actionToBind;
	}

	public boolean isPrimaryBinding() {
		return primaryBinding;
	}
	
	public void switchTheme() {
		if (GameState.getProperties().hasValue(PropertyValue.lightTheme)) {
			getWebEngineTooltip().setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewTooltip_stylesheet.css").toExternalForm());
			getWebEngine().setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webView_stylesheet.css").toExternalForm());
			getWebEngineButtonsLeft().setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewButtons_stylesheet.css").toExternalForm());
			getWebEngineButtonsRight().setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewButtons_stylesheet.css").toExternalForm());
			getWebEngineAttributes().setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewAttributes_stylesheet.css").toExternalForm());
			getWebEngineRight().setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewAttributes_stylesheet.css").toExternalForm());
	
			Main.mainScene.getStylesheets().clear();
			Main.mainScene.getStylesheets().add("/com/lilithsthrone/res/css/stylesheet.css");
			Main.primaryStage.setScene(Main.mainScene);
		} else {
			getWebEngineTooltip().setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewTooltip_stylesheet_light.css").toExternalForm());
			getWebEngine().setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webView_stylesheet_light.css").toExternalForm());
			getWebEngineButtonsLeft().setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewButtons_stylesheet_light.css").toExternalForm());
			getWebEngineButtonsRight().setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewButtons_stylesheet_light.css").toExternalForm());
			getWebEngineAttributes().setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewAttributes_stylesheet_light.css").toExternalForm());
			getWebEngineRight().setUserStyleSheetLocation(getClass().getResource("/com/lilithsthrone/res/css/webViewAttributes_stylesheet_light.css").toExternalForm());
	
			Main.mainScene.getStylesheets().clear();
			Main.mainScene.getStylesheets().add("/com/lilithsthrone/res/css/stylesheet_light.css");
			Main.primaryStage.setScene(Main.mainScene);
		}
		GameState.getProperties().setValue(PropertyValue.lightTheme, !GameState.getProperties().hasValue(PropertyValue.lightTheme));
		GameState.saveProperties();
	}

	public Colour getFlashMessageColour() {
		return flashMessageColour;
	}

	public void setFlashMessageColour(Colour flashMessageColour) {
		MainController.flashMessageColour = flashMessageColour;
	}

	public String getFlashMessageText() {
		return flashMessageText;
	}

	public void setFlashMessageText(String flashMessageText) {
		MainController.flashMessageText = flashMessageText;
	}

}
