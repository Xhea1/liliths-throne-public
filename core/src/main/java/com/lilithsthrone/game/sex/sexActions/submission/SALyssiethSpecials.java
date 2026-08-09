package com.lilithsthrone.game.sex.sexActions.submission;

import com.lilithsthrone.game.character.PlayerCharacter;
import com.lilithsthrone.game.character.attributes.CorruptionLevel;
import com.lilithsthrone.game.character.body.coverings.BodyCoveringType;
import com.lilithsthrone.game.character.body.coverings.Covering;
import com.lilithsthrone.game.character.body.types.AssType;
import com.lilithsthrone.game.character.body.types.BreastType;
import com.lilithsthrone.game.character.body.types.PenisType;
import com.lilithsthrone.game.character.body.types.VaginaType;
import com.lilithsthrone.game.character.body.valueEnums.AssSize;
import com.lilithsthrone.game.character.body.valueEnums.BodySize;
import com.lilithsthrone.game.character.body.valueEnums.Capacity;
import com.lilithsthrone.game.character.body.valueEnums.CupSize;
import com.lilithsthrone.game.character.body.valueEnums.Femininity;
import com.lilithsthrone.game.character.body.valueEnums.FluidExpulsion;
import com.lilithsthrone.game.character.body.valueEnums.HairLength;
import com.lilithsthrone.game.character.body.valueEnums.HipSize;
import com.lilithsthrone.game.character.body.valueEnums.LabiaSize;
import com.lilithsthrone.game.character.body.valueEnums.LipSize;
import com.lilithsthrone.game.character.body.valueEnums.Muscle;
import com.lilithsthrone.game.character.body.valueEnums.OrificeElasticity;
import com.lilithsthrone.game.character.body.valueEnums.OrificePlasticity;
import com.lilithsthrone.game.character.body.valueEnums.PenetrationGirth;
import com.lilithsthrone.game.character.body.valueEnums.TesticleSize;
import com.lilithsthrone.game.character.body.valueEnums.Wetness;
import com.lilithsthrone.game.character.npc.submission.Lyssieth;
import com.lilithsthrone.game.character.race.Race;
import com.lilithsthrone.game.dialogue.utils.ParserTag;
import com.lilithsthrone.game.dialogue.utils.UtilText;
import com.lilithsthrone.game.inventory.clothing.AbstractClothing;
import com.lilithsthrone.game.inventory.enchanting.ItemEffect;
import com.lilithsthrone.game.inventory.enchanting.ItemEffectType;
import com.lilithsthrone.game.inventory.enchanting.TFModifier;
import com.lilithsthrone.game.inventory.enchanting.TFPotency;
import com.lilithsthrone.game.sex.ArousalIncrease;
import com.lilithsthrone.game.sex.SexAreaOrifice;
import com.lilithsthrone.game.sex.SexAreaPenetration;
import com.lilithsthrone.game.sex.SexParticipantType;
import com.lilithsthrone.game.sex.SexType;
import com.lilithsthrone.game.sex.managers.submission.SMLyssiethDemonTF;
import com.lilithsthrone.game.sex.positions.SexPosition;
import com.lilithsthrone.game.sex.positions.slots.SexSlotAllFours;
import com.lilithsthrone.game.sex.positions.slots.SexSlotDesk;
import com.lilithsthrone.game.sex.positions.slots.SexSlotLyingDown;
import com.lilithsthrone.game.sex.positions.slots.SexSlotSitting;
import com.lilithsthrone.game.sex.sexActions.SexAction;
import com.lilithsthrone.game.sex.sexActions.SexActionCategory;
import com.lilithsthrone.game.sex.sexActions.SexActionPriority;
import com.lilithsthrone.game.sex.sexActions.SexActionType;
import com.lilithsthrone.main.GameState;
import com.lilithsthrone.utils.Util;
import com.lilithsthrone.utils.Util.Value;
import com.lilithsthrone.utils.colours.PresetColour;

/**
 * @since 0.3
 * @version 0.3.7
 * @author Innoxia
 */
public class SALyssiethSpecials {

	public static final void playerGrowDemonicVagina() {
		GameState.game.getPlayer().setVaginaType(VaginaType.DEMON_COMMON, true);
		GameState.game.getPlayer().setVaginaLabiaSize(LabiaSize.ZERO_TINY);
		GameState.game.getPlayer().setVaginaWetness(Wetness.SEVEN_DROOLING);
		GameState.game.getPlayer().setVaginaCapacity(Capacity.ONE_EXTREMELY_TIGHT.getMinimumValue(), true);
		GameState.game.getPlayer().setVaginaElasticity(OrificeElasticity.FOUR_LIMBER.getValue());
		GameState.game.getPlayer().setVaginaPlasticity(OrificePlasticity.THREE_RESILIENT.getValue());

		GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.VAGINA, PresetColour.SKIN_RED_DARK), false);
	}

	public static final void playerGrowDemonicPenis() {
		GameState.game.getPlayer().setPenisType(PenisType.DEMON_COMMON);
		GameState.game.getPlayer().setPenisSize(20);
		GameState.game.getPlayer().setPenisGirth(PenetrationGirth.FOUR_GIRTHY.getValue());
		GameState.game.getPlayer().setTesticleSize(TesticleSize.THREE_LARGE);
		GameState.game.getPlayer().setPenisCumStorage(750);
		GameState.game.getPlayer().setPenisCumExpulsion(FluidExpulsion.FOUR_HUGE.getMedianValue());
		GameState.game.getPlayer().fillCumToMaxStorage();
		
		GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.PENIS, PresetColour.SKIN_RED), false);
	}

	public static final void playerGrowDemonicBreasts(CupSize breastCup, CupSize crotchBreastCup) {
		PlayerCharacter player = GameState.game.getPlayer();
		if (player.getBreastCrotchType() != BreastType.NONE) {
			player.setBreastCrotchType(BreastType.DEMON_COMMON);
			if (crotchBreastCup != null) {
				player.setMinimumBreastCrotchSize(crotchBreastCup);
			}
		}
		player.setBreastType(BreastType.DEMON_COMMON);
		if (breastCup != null) {
			player.setMinimumBreastSize(breastCup);
		}
		
		GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.NIPPLES, PresetColour.SKIN_RED_DARK), false);
		GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.NIPPLES_CROTCH, PresetColour.SKIN_RED_DARK), false);
	}

	public static final void playerGrowDemonicBreasts(CupSize breastCup) {
		playerGrowDemonicBreasts(breastCup, null);
	}

	public static final void playerGrowDemonicBreasts() {
		playerGrowDemonicBreasts(null, null);
	}

	public static final SexAction DEMON_TF_STAGE_1_CHOICE_GET_FUCKED = new SexAction(
			SexActionType.SPECIAL,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ZERO_PURE,
			null,
			SexParticipantType.NORMAL) {

		@Override
		public boolean isBaseRequirementsMet() {
			return GameState.sex.getSexManager() instanceof SMLyssiethDemonTF
					&& GameState.sex.getNumberOfOrgasms(GameState.game.getNpc(Lyssieth.class))==1
					&& GameState.sex.getPosition()==SexPosition.STANDING
					&& GameState.sex.getCharacterPerformingAction().isPlayer()
					&& GameState.game.getNpc(Lyssieth.class).hasPenis();
		}
		
		@Override
		public SexActionPriority getPriority() {
			return SexActionPriority.UNIQUE_MAX;
		}
		
		@Override
		public String getActionTitle() {
			return "Get fucked";
		}

		@Override
		public String getActionDescription() {
			return "Allow Lyssieth to push you down onto your back over her desk, and let her fuck you.<br/>"
								+ "[style.italicsFeminine(Lyssieth will feminise you and give you a demonic vagina!)]<br/>"
								+ (GameState.game.getPlayer().hasPenis()
										?"[style.italicsTfSexual(Lyssieth will also temporarily remove your cock!)]"
										:"");
		}

		@Override
		public String getDescription() {
			return UtilText.parseFromXMLFile(Util.newArrayListOfValues(ParserTag.SEX_ALLOW_MUFFLED_SPEECH), "characters/submission/lyssieth", "DEMON_TF_STAGE_1_CHOICE_GET_FUCKED");
		}

		@Override
		public void applyEffects() {
			specialActionTriggered = false;
			GameState.game.getNpc(Lyssieth.class).setForeplayPreference(GameState.game.getPlayer(), new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, SexAreaOrifice.VAGINA));
			
			if(GameState.game.getPlayer().getFemininityValue()<75) {
				GameState.game.getPlayer().setFemininity(75);
			} else {
				GameState.game.getPlayer().incrementFemininity(15);
			}
			GameState.game.getPlayer().setHairLength(HairLength.FOUR_MID_BACK.getMedianValue());
			GameState.game.getPlayer().setHipSize(HipSize.FIVE_VERY_WIDE);
			GameState.game.getPlayer().setAssSize(AssSize.FOUR_LARGE);
			GameState.game.getPlayer().setMuscle(Muscle.THREE_MUSCULAR.getMedianValue());
			GameState.game.getPlayer().setBodySize(BodySize.TWO_AVERAGE.getMedianValue());
			GameState.game.getPlayer().setHeight(168);
			GameState.game.getPlayer().setAssType(AssType.DEMON_COMMON);
			GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.ANUS, PresetColour.SKIN_RED_DARK), false);
			playerGrowDemonicBreasts(CupSize.DD);
			GameState.game.getPlayer().setPenisType(PenisType.NONE);
			playerGrowDemonicVagina();
			
			GameState.sex.setSexManager(
					new SMLyssiethDemonTF(
							SexPosition.OVER_DESK,
							Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Lyssieth.class), SexSlotDesk.BETWEEN_LEGS)),
							Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotDesk.OVER_DESK_ON_BACK))));
			
			GameState.game.getNpc(Lyssieth.class).fillCumToMaxStorage();
		}
	};

	public static final SexAction DEMON_TF_STAGE_1_CHOICE_GET_FUCKED_ANALLY = new SexAction(
			SexActionType.SPECIAL,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ZERO_PURE,
			null,
			SexParticipantType.NORMAL) {

		@Override
		public boolean isBaseRequirementsMet() {
			return GameState.sex.getSexManager() instanceof SMLyssiethDemonTF
					&& GameState.sex.getNumberOfOrgasms(GameState.game.getNpc(Lyssieth.class))==1
					&& GameState.sex.getPosition()==SexPosition.STANDING
					&& GameState.sex.getCharacterPerformingAction().isPlayer()
					&& GameState.game.isAnalContentEnabled()
					&& GameState.game.getNpc(Lyssieth.class).hasPenis();
		}
		
		@Override
		public SexActionPriority getPriority() {
			return SexActionPriority.UNIQUE_MAX;
		}
		
		@Override
		public String getActionTitle() {
			return "Ask for anal";
		}

		@Override
		public String getActionDescription() {
			return "Allow Lyssieth to push you down onto your back over her desk, and request that she use your ass.<br/>"
								+ "[style.italicsFeminine(Lyssieth will feminise you!)]<br/>"
								+ (GameState.game.getPlayer().hasPenis()
										?"[style.italicsFeminine(Lyssieth will sissify your cock!)]"
										:"");
		}

		@Override
		public String getDescription() {
			return UtilText.parseFromXMLFile(Util.newArrayListOfValues(ParserTag.SEX_ALLOW_MUFFLED_SPEECH), "characters/submission/lyssieth", "DEMON_TF_STAGE_1_CHOICE_GET_FUCKED_ANALLY");
		}

		@Override
		public void applyEffects() {
			specialActionTriggered = false;
			GameState.game.getNpc(Lyssieth.class).setForeplayPreference(GameState.game.getPlayer(), new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, SexAreaOrifice.ANUS));
			
			if(GameState.game.getPlayer().getFemininityValue()<=Femininity.MASCULINE.getMaximumFemininity()) {
				GameState.game.getPlayer().setFemininity(50);
				GameState.game.getPlayer().setHairLength(HairLength.THREE_SHOULDER_LENGTH.getMedianValue());
				GameState.game.getPlayer().setHipSize(HipSize.THREE_GIRLY);
				GameState.game.getPlayer().setAssSize(AssSize.THREE_NORMAL);
				GameState.game.getPlayer().setMuscle(Muscle.TWO_TONED.getMedianValue());
				GameState.game.getPlayer().setBodySize(BodySize.TWO_AVERAGE.getMedianValue());
				GameState.game.getPlayer().setHeight(168);
				GameState.game.getPlayer().setAssType(AssType.DEMON_COMMON);
				GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.ANUS, PresetColour.SKIN_RED_DARK), false);
				playerGrowDemonicBreasts(CupSize.AA);
			} else {
				GameState.game.getPlayer().incrementFemininity(15);
				GameState.game.getPlayer().setHairLength(HairLength.FOUR_MID_BACK.getMedianValue());
				GameState.game.getPlayer().setHipSize(HipSize.FIVE_VERY_WIDE);
				GameState.game.getPlayer().setAssSize(AssSize.FOUR_LARGE);
				GameState.game.getPlayer().setMuscle(Muscle.THREE_MUSCULAR.getMedianValue());
				GameState.game.getPlayer().setBodySize(BodySize.TWO_AVERAGE.getMedianValue());
				GameState.game.getPlayer().setHeight(168);
				GameState.game.getPlayer().setAssType(AssType.DEMON_COMMON);
				GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.ANUS, PresetColour.SKIN_RED_DARK), false);
				playerGrowDemonicBreasts(CupSize.DD);
			}
			
			if(GameState.game.getPlayer().hasPenis()) {
				GameState.game.getPlayer().setPenisType(PenisType.DEMON_COMMON);
				GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.PENIS, PresetColour.SKIN_RED), false);
				GameState.game.getPlayer().setPenisSize(8);
				GameState.game.getPlayer().setTesticleSize(TesticleSize.ONE_TINY);
				GameState.game.getPlayer().fillCumToMaxStorage();
			}
			if(GameState.game.getPlayer().hasVagina()) {
				playerGrowDemonicVagina();
			}
			
			GameState.sex.setSexManager(
					new SMLyssiethDemonTF(
							SexPosition.OVER_DESK,
							Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Lyssieth.class), SexSlotDesk.BETWEEN_LEGS)),
							Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotDesk.OVER_DESK_ON_BACK))));
			
			GameState.game.getNpc(Lyssieth.class).fillCumToMaxStorage();
		}
	};
	
	public static final SexAction DEMON_TF_STAGE_1_CHOICE_FUCK_LYSSIETH = new SexAction(
			SexActionType.SPECIAL,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ZERO_PURE,
			null,
			SexParticipantType.NORMAL) {

		@Override
		public boolean isBaseRequirementsMet() {
			return GameState.sex.getSexManager() instanceof SMLyssiethDemonTF
					&& GameState.sex.getNumberOfOrgasms(GameState.game.getNpc(Lyssieth.class))==1
					&& GameState.sex.getPosition()==SexPosition.STANDING
					&& GameState.sex.getCharacterPerformingAction().isPlayer();
		}
		
		@Override
		public SexActionPriority getPriority() {
			return SexActionPriority.UNIQUE_MAX;
		}
		
		@Override
		public String getActionTitle() {
			return "Fuck her";
		}

		@Override
		public String getActionDescription() {
			return "You never agreed to let Lyssieth fuck you. Push her down onto her desk and fuck her instead.<br/>"
								+ (!GameState.game.getPlayer().isFeminine()
										?"[style.italicsMasculine(Lyssieth will make you more masculine!)]"
										:"[style.italicsDisabled(This will have no extra feminising effect.)]")
								+ (!GameState.game.getPlayer().hasPenis()
										?"[style.italicsTfSexual(Lyssieth will give you a cock with which to fuck her!)]"
										:"");
		}

		@Override
		public String getDescription() {
			return UtilText.parseFromXMLFile(Util.newArrayListOfValues(ParserTag.SEX_ALLOW_MUFFLED_SPEECH), "characters/submission/lyssieth", "DEMON_TF_STAGE_1_CHOICE_FUCK_LYSSIETH");
		}

		@Override
		public void applyEffects() {
			specialActionTriggered = false;
			GameState.game.getNpc(Lyssieth.class).setForeplayPreference(GameState.game.getPlayer(), new SexType(SexParticipantType.NORMAL, SexAreaOrifice.VAGINA, SexAreaPenetration.PENIS));
			
			playerGrowDemonicPenis();
			GameState.game.getPlayer().setAssType(AssType.DEMON_COMMON);
			GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.ANUS, PresetColour.SKIN_RED_DARK), false);
			playerGrowDemonicBreasts();
			
			if(!GameState.game.getPlayer().isFeminine()) {
				GameState.game.getPlayer().setHeight(208);
				if(GameState.game.getPlayer().getFemininityValue()>25) {
					GameState.game.getPlayer().setFemininity(25);
				} else {
					GameState.game.getPlayer().incrementFemininity(-15);
				}
				GameState.game.getPlayer().setHairLength(HairLength.TWO_SHORT.getMedianValue());
				if(GameState.game.getPlayer().getMuscleValue()<Muscle.TWO_TONED.getMedianValue()) {
					GameState.game.getPlayer().setMuscle(Muscle.TWO_TONED.getMedianValue());
				} else {
					GameState.game.getPlayer().incrementMuscle(25);
				}
				if(GameState.game.getPlayer().getBodySizeValue()<BodySize.TWO_AVERAGE.getMedianValue()) {
					GameState.game.getPlayer().setBodySize(BodySize.TWO_AVERAGE.getMedianValue());
				} else {
					GameState.game.getPlayer().incrementBodySize(10);
				}
				GameState.game.getPlayer().setVaginaType(VaginaType.NONE);
			} else {
				if(GameState.game.getPlayer().hasVagina()) {
					playerGrowDemonicVagina();
				}
			}
			
			GameState.sex.setSexManager(
					new SMLyssiethDemonTF(
							SexPosition.OVER_DESK,
							Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotDesk.BETWEEN_LEGS)),
							Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Lyssieth.class), SexSlotDesk.OVER_DESK_ON_BACK))));

			GameState.game.getNpc(Lyssieth.class).fillCumToMaxStorage();
		}
	};
	
	public static final SexAction DEMON_TF_STAGE_1_CHOICE_SCISSOR_BOTTOM = new SexAction(
			SexActionType.SPECIAL,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ZERO_PURE,
			null,
			SexParticipantType.NORMAL) {
		@Override
		public boolean isBaseRequirementsMet() {
			return GameState.sex.getSexManager() instanceof SMLyssiethDemonTF
					&& GameState.sex.getNumberOfOrgasms(GameState.game.getNpc(Lyssieth.class))==1
					&& GameState.sex.getPosition()==SexPosition.STANDING
					&& GameState.sex.getCharacterPerformingAction().isPlayer()
					&& !GameState.game.getNpc(Lyssieth.class).hasPenis();
		}
		@Override
		public SexActionPriority getPriority() {
			return SexActionPriority.UNIQUE_MAX;
		}
		@Override
		public String getActionTitle() {
			return "Scissoring (bottom)";
		}
		@Override
		public String getActionDescription() {
			return "Allow Lyssieth to push you down onto your back on the office's sofa, and let her dominantly scissor you.<br/>"
								+ "[style.italicsFeminine(Lyssieth will feminise you and give you a demonic vagina!)]<br/>"
								+ (GameState.game.getPlayer().hasPenis()
										?"[style.italicsTfSexual(Lyssieth will also temporarily remove your cock!)]"
										:"");
		}
		@Override
		public String getDescription() {
			return UtilText.parseFromXMLFile(Util.newArrayListOfValues(ParserTag.SEX_ALLOW_MUFFLED_SPEECH), "characters/submission/lyssieth", "DEMON_TF_STAGE_1_CHOICE_SCISSOR_BOTTOM");
		}
		@Override
		public void applyEffects() {
			specialActionTriggered = false;
			GameState.game.getNpc(Lyssieth.class).setForeplayPreference(GameState.game.getPlayer(), new SexType(SexParticipantType.NORMAL, SexAreaPenetration.CLIT, SexAreaPenetration.CLIT));

			if(GameState.game.getPlayer().getFemininityValue()<75) {
				GameState.game.getPlayer().setFemininity(75);
			} else if(GameState.game.getPlayer().isFeminine()) {
				GameState.game.getPlayer().incrementFemininity(15);
			} else {
				GameState.game.getPlayer().setFemininity(Femininity.FEMININE.getMinimumFemininity());
			}
			GameState.game.getPlayer().setHairLength(HairLength.FOUR_MID_BACK.getMedianValue());
			GameState.game.getPlayer().setHipSize(HipSize.FIVE_VERY_WIDE);
			GameState.game.getPlayer().setAssSize(AssSize.FOUR_LARGE);
			GameState.game.getPlayer().setMuscle(Muscle.THREE_MUSCULAR.getMedianValue());
			GameState.game.getPlayer().setBodySize(BodySize.TWO_AVERAGE.getMedianValue());
			GameState.game.getPlayer().setHeight(168);
			GameState.game.getPlayer().setAssType(AssType.DEMON_COMMON);
			GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.ANUS, PresetColour.SKIN_RED_DARK), false);
			playerGrowDemonicBreasts(CupSize.DD);
			GameState.game.getPlayer().setPenisType(PenisType.NONE);
			playerGrowDemonicVagina();
			
			GameState.sex.setSexManager(
					new SMLyssiethDemonTF(
							SexPosition.LYING_DOWN,
							Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Lyssieth.class), SexSlotLyingDown.SCISSORING)),
							Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotLyingDown.LYING_DOWN))));
			
			GameState.game.getNpc(Lyssieth.class).fillCumToMaxStorage();
		}
	};
	
	public static final SexAction DEMON_TF_STAGE_1_CHOICE_SCISSOR_TOP = new SexAction(
			SexActionType.SPECIAL,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ZERO_PURE,
			null,
			SexParticipantType.NORMAL) {
		@Override
		public boolean isBaseRequirementsMet() {
			return GameState.sex.getSexManager() instanceof SMLyssiethDemonTF
					&& GameState.sex.getNumberOfOrgasms(GameState.game.getNpc(Lyssieth.class))==1
					&& GameState.sex.getPosition()==SexPosition.STANDING
					&& GameState.sex.getCharacterPerformingAction().isPlayer()
					&& !GameState.game.getNpc(Lyssieth.class).hasPenis();
		}
		@Override
		public SexActionPriority getPriority() {
			return SexActionPriority.UNIQUE_MAX;
		}
		@Override
		public String getActionTitle() {
			return "Scissoring (top)";
		}
		@Override
		public String getActionDescription() {
			return "Push Lyssieth down onto the office's sofa and dominantly scissor her.<br/>"
						+ " [style.italicsFeminine(Lyssieth will feminise you and give you a demonic vagina!)]<br/>"
						+ (GameState.game.getPlayer().hasPenis()
								?"[style.italicsTfSexual(Lyssieth will also temporarily remove your cock!)]"
								:"");
		}
		@Override
		public String getDescription() {
			return UtilText.parseFromXMLFile(Util.newArrayListOfValues(ParserTag.SEX_ALLOW_MUFFLED_SPEECH), "characters/submission/lyssieth", "DEMON_TF_STAGE_1_CHOICE_SCISSOR_TOP");
		}
		@Override
		public void applyEffects() {
			specialActionTriggered = false;
			GameState.game.getNpc(Lyssieth.class).setForeplayPreference(GameState.game.getPlayer(), new SexType(SexParticipantType.NORMAL, SexAreaPenetration.CLIT, SexAreaPenetration.CLIT));
			
			if(GameState.game.getPlayer().getFemininityValue()<75) {
				GameState.game.getPlayer().setFemininity(75);
			} else if(GameState.game.getPlayer().isFeminine()) {
				GameState.game.getPlayer().incrementFemininity(15);
			} else {
				GameState.game.getPlayer().setFemininity(Femininity.FEMININE.getMinimumFemininity());
			}
			GameState.game.getPlayer().setHairLength(HairLength.FOUR_MID_BACK.getMedianValue());
			GameState.game.getPlayer().setHipSize(HipSize.FIVE_VERY_WIDE);
			GameState.game.getPlayer().setAssSize(AssSize.FOUR_LARGE);
			GameState.game.getPlayer().setMuscle(Muscle.THREE_MUSCULAR.getMedianValue());
			GameState.game.getPlayer().setBodySize(BodySize.TWO_AVERAGE.getMedianValue());
			GameState.game.getPlayer().setHeight(168);
			GameState.game.getPlayer().setAssType(AssType.DEMON_COMMON);
			GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.ANUS, PresetColour.SKIN_RED_DARK), false);
			playerGrowDemonicBreasts(CupSize.DD);
			GameState.game.getPlayer().setPenisType(PenisType.NONE);
			playerGrowDemonicVagina();
			
			GameState.sex.setSexManager(
					new SMLyssiethDemonTF(
							SexPosition.LYING_DOWN,
							Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotLyingDown.SCISSORING)),
							Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Lyssieth.class), SexSlotLyingDown.LYING_DOWN))));
			
			GameState.game.getNpc(Lyssieth.class).fillCumToMaxStorage();
		}
	};
	
	
	// STAGE 2
	public static boolean specialActionTriggered = false; // OK to use this and not reset it, as the game can't be saved mid-sex, and this sex scene is only ever used once.
	
	public static final SexAction DEMON_TF_STAGE_2_CHOICE_FUCK_LYSSIETH = new SexAction(
			SexActionType.SPECIAL,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ZERO_PURE,
			null,
			SexParticipantType.NORMAL) {
		
		@Override
		public boolean isBaseRequirementsMet() {
			return GameState.sex.getSexManager() instanceof SMLyssiethDemonTF
					&& GameState.sex.getNumberOfOrgasms(GameState.game.getNpc(Lyssieth.class))==2
					&& !specialActionTriggered
					&& GameState.sex.getCharacterPerformingAction().isPlayer()
					&& GameState.sex.getSexTypeCount(GameState.sex.getCharacterPerformingAction(), GameState.game.getNpc(Lyssieth.class), new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, SexAreaOrifice.VAGINA))==0;
		}
		
		@Override
		public SexActionPriority getPriority() {
			return SexActionPriority.UNIQUE_MAX;
		}
		
		@Override
		public String getActionTitle() {
			return "Fuck her";
		}

		@Override
		public String getActionDescription() {
			return "Allow Lyssieth to get down on her hands and knees, and kneel down behind her to fuck her in the doggy-style position.<br/>"
								+ "[style.italicsDisabled(This will have no extra feminising effect.)]<br/>"
								+ (!GameState.game.getPlayer().hasPenis() || GameState.game.getPlayer().getPenisType().getRace()!=Race.DEMON
										?"[style.italicsTfSexual(Lyssieth will give you a demonic cock!)]"
										:"");
		}

		@Override
		public String getDescription() {
			if(GameState.game.getPlayer().hasPenis() && GameState.game.getPlayer().getPenisType().getRace()==Race.DEMON && GameState.game.getPlayer().getPenisRawSizeValue()<=4) {
				return UtilText.parseFromXMLFile(Util.newArrayListOfValues(ParserTag.SEX_ALLOW_MUFFLED_SPEECH), "characters/submission/lyssieth", "DEMON_TF_STAGE_2_CHOICE_FUCK_LYSSIETH_AS_SISSY");
			} else {
				return UtilText.parseFromXMLFile(Util.newArrayListOfValues(ParserTag.SEX_ALLOW_MUFFLED_SPEECH), "characters/submission/lyssieth", "DEMON_TF_STAGE_2_CHOICE_FUCK_LYSSIETH");
			}
		}

		@Override
		public void applyEffects() {
			GameState.game.getNpc(Lyssieth.class).setForeplayPreference(GameState.game.getPlayer(), new SexType(SexParticipantType.NORMAL, SexAreaOrifice.VAGINA, SexAreaPenetration.PENIS));
			specialActionTriggered = true;
			
			if(!GameState.game.getPlayer().hasPenis() || GameState.game.getPlayer().getPenisType().getRace()!=Race.DEMON) {
				playerGrowDemonicPenis();
			}

			GameState.sex.setSexManager(
					new SMLyssiethDemonTF(
							SexPosition.ALL_FOURS,
							Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotAllFours.BEHIND)),
							Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Lyssieth.class), SexSlotAllFours.ALL_FOURS))));

			GameState.game.getPlayer().fillCumToMaxStorage();
			GameState.game.getNpc(Lyssieth.class).fillCumToMaxStorage();
		}
	};
	
	public static final SexAction DEMON_TF_STAGE_2_CHOICE_LYSSIETH_ORAL = new SexAction(
			SexActionType.SPECIAL,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ZERO_PURE,
			null,
			SexParticipantType.NORMAL) {

		@Override
		public boolean isBaseRequirementsMet() {
			return GameState.sex.getSexManager() instanceof SMLyssiethDemonTF
					&& GameState.sex.getNumberOfOrgasms(GameState.game.getNpc(Lyssieth.class))==2
					&& !specialActionTriggered
					&& GameState.sex.getCharacterPerformingAction().isPlayer()
					&& GameState.sex.getSexTypeCount(GameState.sex.getCharacterPerformingAction(), GameState.game.getNpc(Lyssieth.class), new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, SexAreaOrifice.VAGINA))==0;
		}
		
		@Override
		public SexActionPriority getPriority() {
			return SexActionPriority.UNIQUE_MAX;
		}
		
		@Override
		public String getActionTitle() {
			return "Ask for oral";
		}

		@Override
		public String getActionDescription() {
			return "Ask Lyssieth to service you with her mouth instead.<br/>"
							+ "[style.italicsDisabled(This will have no extra feminising effect.)]";
		}

		@Override
		public String getDescription() {
			if(GameState.game.getPlayer().hasPenis() && GameState.game.getPlayer().getPenisType().getRace()==Race.DEMON && GameState.game.getPlayer().getPenisRawSizeValue()<=4) {
				return UtilText.parseFromXMLFile(Util.newArrayListOfValues(ParserTag.SEX_ALLOW_MUFFLED_SPEECH), "characters/submission/lyssieth", "DEMON_TF_STAGE_2_CHOICE_LYSSIETH_ORAL_AS_SISSY");
			} else {
				return UtilText.parseFromXMLFile(Util.newArrayListOfValues(ParserTag.SEX_ALLOW_MUFFLED_SPEECH), "characters/submission/lyssieth", "DEMON_TF_STAGE_2_CHOICE_LYSSIETH_ORAL");
			}
		}

		@Override
		public void applyEffects() {
			if(GameState.game.getPlayer().hasPenis()) {
				GameState.game.getNpc(Lyssieth.class).setForeplayPreference(GameState.game.getPlayer(), new SexType(SexParticipantType.NORMAL, SexAreaOrifice.MOUTH, SexAreaPenetration.PENIS));
			} else {
				GameState.game.getNpc(Lyssieth.class).setForeplayPreference(GameState.game.getPlayer(), new SexType(SexParticipantType.NORMAL, SexAreaPenetration.TONGUE, SexAreaOrifice.VAGINA));
			}
			specialActionTriggered = true;
			
			GameState.sex.setSexManager(
					new SMLyssiethDemonTF(
							SexPosition.SITTING,
							Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Lyssieth.class), SexSlotSitting.PERFORMING_ORAL)),
							Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotSitting.SITTING))));
			
			GameState.game.getNpc(Lyssieth.class).fillCumToMaxStorage();
		}
	};

	public static final SexAction DEMON_TF_STAGE_2_ASK_FOR_MORE_GETTING_FUCKED = new SexAction(
			SexActionType.SPECIAL,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ZERO_PURE,
			null,
			SexParticipantType.NORMAL) {

		@Override
		public boolean isBaseRequirementsMet() {
			return GameState.sex.getSexManager() instanceof SMLyssiethDemonTF
					&& GameState.sex.getNumberOfOrgasms(GameState.game.getNpc(Lyssieth.class))==2
					&& !specialActionTriggered
					&& GameState.sex.getCharacterPerformingAction().isPlayer()
					&& GameState.sex.getSexTypeCount(GameState.sex.getCharacterPerformingAction(), GameState.game.getNpc(Lyssieth.class), new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, SexAreaOrifice.VAGINA))==0
					&& GameState.game.getNpc(Lyssieth.class).hasPenis();
		}
		
		@Override
		public SexActionPriority getPriority() {
			return SexActionPriority.UNIQUE_MAX;
		}
		
		@Override
		public String getActionTitle() {
			if(GameState.game.getNpc(Lyssieth.class).getForeplayPreference(GameState.game.getPlayer()).getTargetedSexArea()==SexAreaOrifice.ANUS) {
				return "More anal";
			}
			return "Ask for more";
		}

		@Override
		public String getActionDescription() {
			if(GameState.game.getNpc(Lyssieth.class).getForeplayPreference(GameState.game.getPlayer()).getTargetedSexArea()==SexAreaOrifice.ANUS) {
				return "Ask Lyssieth to carry on fucking your ass instead.<br/>"
							+ "[style.italicsFeminine(Lyssieth will fully feminise you!)]"
							+ (GameState.game.getPlayer().hasPenis()
									?"<br/>[style.italicsTfSexual(Lyssieth will also fully sissify your cock!)]"
									:"");
			}
			return "Ask Lyssieth to carry on fucking your pussy instead.<br/>"
						+ "[style.italicsFeminine(Lyssieth will fully feminise you!)]";
		}

		@Override
		public String getDescription() {
			if(GameState.game.getNpc(Lyssieth.class).getForeplayPreference(GameState.game.getPlayer()).getTargetedSexArea()==SexAreaOrifice.ANUS) {
				return UtilText.parseFromXMLFile(Util.newArrayListOfValues(ParserTag.SEX_ALLOW_MUFFLED_SPEECH), "characters/submission/lyssieth", "DEMON_TF_STAGE_2_ASK_FOR_MORE_GETTING_FUCKED_ANAL");
			}
			return UtilText.parseFromXMLFile(Util.newArrayListOfValues(ParserTag.SEX_ALLOW_MUFFLED_SPEECH), "characters/submission/lyssieth", "DEMON_TF_STAGE_2_ASK_FOR_MORE_GETTING_FUCKED");
		}

		@Override
		public void applyEffects() {
			specialActionTriggered = true;
			
			GameState.game.getPlayer().setFemininity(100);
			GameState.game.getPlayer().setHairLength(HairLength.FOUR_MID_BACK.getMaximumValue());
			GameState.game.getPlayer().setHipSize(HipSize.FIVE_VERY_WIDE);
			GameState.game.getPlayer().setAssSize(AssSize.FOUR_LARGE);
			GameState.game.getPlayer().setLipSize(LipSize.THREE_PLUMP);
			GameState.game.getPlayer().setMuscle(Muscle.THREE_MUSCULAR.getMedianValue());
			GameState.game.getPlayer().setBodySize(BodySize.TWO_AVERAGE.getMedianValue());
			GameState.game.getPlayer().setHeight(168);
			GameState.game.getPlayer().setBreastSize(CupSize.F);
			
			if(GameState.game.getNpc(Lyssieth.class).getForeplayPreference(GameState.game.getPlayer()).getTargetedSexArea()==SexAreaOrifice.ANUS) {
				if(GameState.game.getPlayer().hasPenis()) {
					GameState.game.getPlayer().setPenisType(PenisType.DEMON_COMMON);
					GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.PENIS, PresetColour.SKIN_RED), false);
					GameState.game.getPlayer().setPenisSize(3);
					GameState.game.getPlayer().setTesticleSize(TesticleSize.ZERO_VESTIGIAL);
					GameState.game.getPlayer().fillCumToMaxStorage();
				}
				if(GameState.game.getPlayer().hasVagina()) {
					// Add anal-only vagina sticker:
					AbstractClothing pastie = GameState.game.getItemGen().generateClothing("innoxia_vagina_sticker_anal_only", PresetColour.CLOTHING_PINK_LIGHT, PresetColour.CLOTHING_PINK_HOT, PresetColour.CLOTHING_PURPLE_VERY_DARK, false);
					pastie.getEffects().clear();
					pastie.addEffect(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.CLOTHING_SPECIAL, TFModifier.CLOTHING_SEALING, TFPotency.DRAIN, 0));
					pastie.addEffect(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.TF_MOD_FETISH_BODY_PART, TFModifier.TF_MOD_FETISH_ANAL_RECEIVING, TFPotency.MAJOR_BOOST, 0));
					pastie.addEffect(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.TF_MOD_FETISH_BODY_PART, TFModifier.TF_MOD_FETISH_VAGINAL_RECEIVING, TFPotency.MAJOR_DRAIN, 0));
					pastie.addEffect(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.TF_MOD_FETISH_BEHAVIOUR, TFModifier.TF_MOD_FETISH_SUBMISSIVE, TFPotency.BOOST, 0));
					pastie.addEffect(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.TF_MOD_FETISH_BEHAVIOUR, TFModifier.TF_MOD_FETISH_DOMINANT, TFPotency.DRAIN, 0));
					pastie.setName("Lyssieth's buttslut pastie");
					GameState.game.getPlayer().equipClothingFromNowhere(pastie, true, GameState.game.getNpc(Lyssieth.class));
				}
				if(GameState.game.getPlayer().hasPenis()) {
					// Add chastity cage:
					AbstractClothing cage = GameState.game.getItemGen().generateClothing("innoxia_bdsm_ornate_chastity_cage", PresetColour.CLOTHING_PINK_LIGHT, PresetColour.CLOTHING_PINK_HOT, PresetColour.CLOTHING_GOLD, false);
					cage.getEffects().clear();
					cage.addEffect(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.CLOTHING_SPECIAL, TFModifier.CLOTHING_SEALING, TFPotency.DRAIN, 0));
					cage.addEffect(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.TF_MOD_FETISH_BODY_PART, TFModifier.TF_MOD_FETISH_PENIS_RECEIVING, TFPotency.MAJOR_BOOST, 0));
					cage.addEffect(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.TF_MOD_FETISH_BODY_PART, TFModifier.TF_MOD_FETISH_PENIS_GIVING, TFPotency.MAJOR_DRAIN, 0));
					cage.addEffect(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.TF_MOD_FETISH_BEHAVIOUR, TFModifier.TF_MOD_FETISH_CUM_ADDICT, TFPotency.BOOST, 0));
					cage.addEffect(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.TF_MOD_FETISH_BEHAVIOUR, TFModifier.TF_MOD_FETISH_CUM_STUD, TFPotency.DRAIN, 0));
					cage.addEffect(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.TF_PENIS, TFModifier.TF_MOD_SIZE, TFPotency.MAJOR_DRAIN, 1));
					cage.addEffect(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.TF_PENIS, TFModifier.TF_MOD_SIZE_SECONDARY, TFPotency.MAJOR_DRAIN, 0));
					cage.addEffect(new ItemEffect(ItemEffectType.CLOTHING, TFModifier.TF_MOD_FETISH_BEHAVIOUR, TFModifier.TF_MOD_FETISH_DENIAL_SELF, TFPotency.MAJOR_BOOST, 0));
					
					cage.setName("Lyssieth's sissy cage");
					GameState.game.getPlayer().equipClothingFromNowhere(cage, true, GameState.game.getNpc(Lyssieth.class));
				}
			}
			
			GameState.sex.setSexManager(
					new SMLyssiethDemonTF(
							SexPosition.ALL_FOURS,
							Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Lyssieth.class), SexSlotAllFours.BEHIND)),
							Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotAllFours.ALL_FOURS))));
			
			GameState.game.getNpc(Lyssieth.class).fillCumToMaxStorage();
		}
	};
	
	// These are for if the player has already fucked Lyssieth:
	
	public static final SexAction DEMON_TF_STAGE_2_CHOICE_GET_FUCKED = new SexAction(
			SexActionType.SPECIAL,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ZERO_PURE,
			null,
			SexParticipantType.NORMAL) {

		@Override
		public boolean isBaseRequirementsMet() {
			return GameState.sex.getSexManager() instanceof SMLyssiethDemonTF
					&& GameState.sex.getNumberOfOrgasms(GameState.game.getNpc(Lyssieth.class))==2
					&& !specialActionTriggered
					&& GameState.sex.getCharacterPerformingAction().isPlayer()
					&& GameState.sex.getSexTypeCount(GameState.sex.getCharacterPerformingAction(), GameState.game.getNpc(Lyssieth.class), new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, SexAreaOrifice.VAGINA))>0
					&& GameState.game.getNpc(Lyssieth.class).hasPenis();
		}
		
		@Override
		public SexActionPriority getPriority() {
			return SexActionPriority.UNIQUE_MAX;
		}
		
		@Override
		public String getActionTitle() {
			return "Get fucked";
		}

		@Override
		public String getActionDescription() {
			return "Allow Lyssieth to push you down onto your back over her desk, and let her give you a demonic pussy for her to fuck.<br/>"
								+ "[style.italicsFeminine(Lyssieth will feminise you into a futa succubus!)]<br/>"
								+ (!GameState.game.getPlayer().hasVagina()
										?"[style.italicsTfSexual(Lyssieth will give you a demonic vagina!)]"
										:"");
		}

		@Override
		public String getDescription() {
			return UtilText.parseFromXMLFile(Util.newArrayListOfValues(ParserTag.SEX_ALLOW_MUFFLED_SPEECH), "characters/submission/lyssieth", "DEMON_TF_STAGE_2_CHOICE_GET_FUCKED");
		}

		@Override
		public void applyEffects() {
			specialActionTriggered = true;
			
			GameState.game.getNpc(Lyssieth.class).setForeplayPreference(GameState.game.getPlayer(), new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, SexAreaOrifice.VAGINA));
			
			GameState.game.getPlayer().setFemininity(75);
			GameState.game.getPlayer().setHairLength(HairLength.FOUR_MID_BACK.getMedianValue());
			GameState.game.getPlayer().setHipSize(HipSize.FIVE_VERY_WIDE);
			GameState.game.getPlayer().setAssSize(AssSize.FOUR_LARGE);
			GameState.game.getPlayer().setMuscle(Muscle.THREE_MUSCULAR.getMedianValue());
			GameState.game.getPlayer().setBodySize(BodySize.TWO_AVERAGE.getMedianValue());
			GameState.game.getPlayer().setHeight(168);
			GameState.game.getPlayer().setAssType(AssType.DEMON_COMMON);
			GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.ANUS, PresetColour.SKIN_RED_DARK), false);
			playerGrowDemonicBreasts(CupSize.F);
			playerGrowDemonicVagina();
			
			GameState.sex.setSexManager(
					new SMLyssiethDemonTF(
							SexPosition.OVER_DESK,
							Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Lyssieth.class), SexSlotDesk.BETWEEN_LEGS)),
							Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotDesk.OVER_DESK_ON_BACK))));
			
			GameState.game.getNpc(Lyssieth.class).fillCumToMaxStorage();
		}
	};

	public static final SexAction DEMON_TF_STAGE_2_CHOICE_GET_FUCKED_ANALLY = new SexAction(
			SexActionType.SPECIAL,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ZERO_PURE,
			null,
			SexParticipantType.NORMAL) {

		@Override
		public boolean isBaseRequirementsMet() {
			return GameState.sex.getSexManager() instanceof SMLyssiethDemonTF
					&& GameState.sex.getNumberOfOrgasms(GameState.game.getNpc(Lyssieth.class))==2
					&& !specialActionTriggered
					&& GameState.sex.getCharacterPerformingAction().isPlayer()
					&& GameState.sex.getSexTypeCount(GameState.sex.getCharacterPerformingAction(), GameState.game.getNpc(Lyssieth.class), new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, SexAreaOrifice.VAGINA))>0
					&& GameState.game.isAnalContentEnabled()
					&& GameState.game.getNpc(Lyssieth.class).hasPenis();
		}
		
		@Override
		public SexActionPriority getPriority() {
			return SexActionPriority.UNIQUE_MAX;
		}
		
		@Override
		public String getActionTitle() {
			return "Ask for anal";
		}

		@Override
		public String getActionDescription() {
			return "Allow Lyssieth to push you down onto your back over her desk, and request that she use your ass.<br/>"
						+ "[style.italicsFeminine(Lyssieth will feminise you!)]";
		}

		@Override
		public String getDescription() {
			return UtilText.parseFromXMLFile(Util.newArrayListOfValues(ParserTag.SEX_ALLOW_MUFFLED_SPEECH), "characters/submission/lyssieth", "DEMON_TF_STAGE_2_CHOICE_GET_FUCKED_ANALLY");
		}

		@Override
		public void applyEffects() {
			specialActionTriggered = true;
			
			GameState.game.getNpc(Lyssieth.class).setForeplayPreference(GameState.game.getPlayer(), new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, SexAreaOrifice.ANUS));
			
			if(GameState.game.getPlayer().isFeminine()) {
				GameState.game.getPlayer().incrementFemininity(25);
				GameState.game.getPlayer().setHairLength(HairLength.FOUR_MID_BACK.getMedianValue());
				GameState.game.getPlayer().setHipSize(HipSize.FIVE_VERY_WIDE);
				GameState.game.getPlayer().setAssSize(AssSize.FOUR_LARGE);
				GameState.game.getPlayer().setMuscle(Muscle.THREE_MUSCULAR.getMedianValue());
				GameState.game.getPlayer().setBodySize(BodySize.TWO_AVERAGE.getMedianValue());
				GameState.game.getPlayer().setHeight(168);
				GameState.game.getPlayer().setAssType(AssType.DEMON_COMMON);
				GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.ANUS, PresetColour.SKIN_RED_DARK), false);
				playerGrowDemonicBreasts(CupSize.F);
				
			} else {
				GameState.game.getPlayer().setFemininity(50);
				GameState.game.getPlayer().setHairLength(HairLength.THREE_SHOULDER_LENGTH.getMedianValue());
				GameState.game.getPlayer().setHipSize(HipSize.THREE_GIRLY);
				GameState.game.getPlayer().setAssSize(AssSize.THREE_NORMAL);
				GameState.game.getPlayer().setMuscle(Muscle.TWO_TONED.getMedianValue());
				GameState.game.getPlayer().setBodySize(BodySize.TWO_AVERAGE.getMedianValue());
				GameState.game.getPlayer().setHeight(168);
				GameState.game.getPlayer().setAssType(AssType.DEMON_COMMON);
				GameState.game.getPlayer().setSkinCovering(new Covering(BodyCoveringType.ANUS, PresetColour.SKIN_RED_DARK), false);
				playerGrowDemonicBreasts(CupSize.AA);
			}
			
			GameState.sex.setSexManager(
					new SMLyssiethDemonTF(
							SexPosition.OVER_DESK,
							Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Lyssieth.class), SexSlotDesk.BETWEEN_LEGS)),
							Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotDesk.OVER_DESK_ON_BACK))));
			
			GameState.game.getNpc(Lyssieth.class).fillCumToMaxStorage();
		}
	};
	
	public static final SexAction DEMON_TF_STAGE_2_MATING_PRESS = new SexAction(
			SexActionType.SPECIAL,
			ArousalIncrease.FOUR_HIGH,
			ArousalIncrease.FOUR_HIGH,
			CorruptionLevel.ONE_VANILLA,
			null,
			SexParticipantType.NORMAL) {

		@Override
		public SexActionCategory getCategory() {
			return SexActionCategory.MISCELLANEOUS;
		}

		@Override
		public boolean isBaseRequirementsMet() {
			return GameState.sex.getSexManager() instanceof SMLyssiethDemonTF
					&& GameState.sex.getNumberOfOrgasms(GameState.game.getNpc(Lyssieth.class))==2
					&& !specialActionTriggered
					&& GameState.sex.getCharacterPerformingAction().isPlayer()
					&& GameState.sex.getSexTypeCount(GameState.sex.getCharacterPerformingAction(), GameState.game.getNpc(Lyssieth.class), new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, SexAreaOrifice.VAGINA))>0;
		}
		
		@Override
		public SexActionPriority getPriority() {
			return SexActionPriority.UNIQUE_MAX;
		}
		
		@Override
		public String getActionTitle() {
			return "Breed her";
		}

		@Override
		public String getActionDescription() {
			return "There's no way you're going to let Lyssieth fuck you. Wrestle her into the 'mating press' position and give Lilaya a new sister or two.<br/>"
								+ (!GameState.game.getPlayer().isFeminine()
										?"[style.italicsMasculine(Lyssieth will turn you into her ideal incubus!)]"
										:"[style.italicsDisabled(This will have no extra feminising effect.)]");
		}

		@Override
		public String getDescription() {
			return UtilText.parseFromXMLFile(Util.newArrayListOfValues(ParserTag.SEX_ALLOW_MUFFLED_SPEECH), "characters/submission/lyssieth", "DEMON_TF_STAGE_2_MATING_PRESS");
		}

		@Override
		public void applyEffects() {
			specialActionTriggered = true;
			GameState.game.getNpc(Lyssieth.class).setForeplayPreference(GameState.game.getPlayer(), new SexType(SexParticipantType.NORMAL, SexAreaOrifice.VAGINA, SexAreaPenetration.PENIS));
			
			if(!GameState.game.getPlayer().isFeminine()) {
				GameState.game.getPlayer().setHairLength(HairLength.ONE_VERY_SHORT.getMedianValue());
				GameState.game.getPlayer().setVaginaType(VaginaType.NONE);
				GameState.game.getPlayer().setFemininity(0);
				GameState.game.getPlayer().setMuscle(100);
				GameState.game.getPlayer().setBodySize(BodySize.THREE_LARGE.getMedianValue());
				GameState.game.getPlayer().setPenisSize(35);
				GameState.game.getPlayer().setTesticleSize(TesticleSize.FOUR_HUGE);
				GameState.game.getPlayer().setPenisCumStorage(2000);
			}

			GameState.game.getNpc(Lyssieth.class).setPenisType(PenisType.NONE);
			GameState.game.getPlayer().fillCumToMaxStorage();
			
			GameState.sex.setSexManager(
					new SMLyssiethDemonTF(
							SexPosition.LYING_DOWN,
							Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotLyingDown.MATING_PRESS)),
							Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Lyssieth.class), SexSlotLyingDown.LYING_DOWN))));
			
			GameState.sex.applyOngoingAction(GameState.game.getPlayer(), SexAreaPenetration.PENIS, GameState.game.getNpc(Lyssieth.class), SexAreaOrifice.VAGINA, true);
		}
	};
	
	public static final SexAction DEMON_TF_STAGE_2_CHOICE_SIXTY_NINE_LYSSIETH_TOP = new SexAction(
			SexActionType.SPECIAL,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ZERO_PURE,
			null,
			SexParticipantType.NORMAL) {
		@Override
		public boolean isBaseRequirementsMet() {
			return GameState.sex.getSexManager() instanceof SMLyssiethDemonTF
					&& GameState.sex.getNumberOfOrgasms(GameState.game.getNpc(Lyssieth.class))==2
					&& !specialActionTriggered
					&& GameState.sex.getCharacterPerformingAction().isPlayer()
					&& !GameState.game.getNpc(Lyssieth.class).hasPenis();
		}
		@Override
		public SexActionPriority getPriority() {
			return SexActionPriority.UNIQUE_MAX;
		}
		@Override
		public String getActionTitle() {
			return "Sixty-nine (bottom)";
		}
		@Override
		public String getActionDescription() {
			return "Ask Lyssieth to take the dominant top position and sixty-nine with you.<br/>"
							+ "[style.italicsDisabled(This will have no extra feminising effect.)]";
		}
		@Override
		public String getDescription() {
			return UtilText.parseFromXMLFile(Util.newArrayListOfValues(ParserTag.SEX_ALLOW_MUFFLED_SPEECH), "characters/submission/lyssieth", "DEMON_TF_STAGE_2_CHOICE_SIXTY_NINE_LYSSIETH_TOP");
		}
		@Override
		public void applyEffects() {
			if(GameState.game.getPlayer().hasPenis()) {
				GameState.game.getNpc(Lyssieth.class).setForeplayPreference(GameState.game.getPlayer(), new SexType(SexParticipantType.NORMAL, SexAreaOrifice.MOUTH, SexAreaPenetration.PENIS));
			} else {
				GameState.game.getNpc(Lyssieth.class).setForeplayPreference(GameState.game.getPlayer(), new SexType(SexParticipantType.NORMAL, SexAreaPenetration.TONGUE, SexAreaOrifice.VAGINA));
			}
			specialActionTriggered = true;
			
			GameState.sex.setSexManager(
					new SMLyssiethDemonTF(
							SexPosition.LYING_DOWN,
							Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Lyssieth.class), SexSlotLyingDown.SIXTY_NINE)),
							Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotLyingDown.LYING_DOWN))));
			
			GameState.game.getNpc(Lyssieth.class).fillCumToMaxStorage();
		}
	};

	public static final SexAction DEMON_TF_STAGE_2_CHOICE_SIXTY_NINE_LYSSIETH_BOTTOM = new SexAction(
			SexActionType.SPECIAL,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ZERO_PURE,
			null,
			SexParticipantType.NORMAL) {
		@Override
		public boolean isBaseRequirementsMet() {
			return GameState.sex.getSexManager() instanceof SMLyssiethDemonTF
					&& GameState.sex.getNumberOfOrgasms(GameState.game.getNpc(Lyssieth.class))==2
					&& !specialActionTriggered
					&& GameState.sex.getCharacterPerformingAction().isPlayer()
					&& !GameState.game.getNpc(Lyssieth.class).hasPenis();
		}
		@Override
		public SexActionPriority getPriority() {
			return SexActionPriority.UNIQUE_MAX;
		}
		@Override
		public String getActionTitle() {
			return "Sixty-nine (top)";
		}
		@Override
		public String getActionDescription() {
			return "Ask Lyssieth to take the submissive bottom position and sixty-nine with her.<br/>"
							+ "[style.italicsDisabled(This will have no extra feminising effect.)]";
		}
		@Override
		public String getDescription() {
			return UtilText.parseFromXMLFile(Util.newArrayListOfValues(ParserTag.SEX_ALLOW_MUFFLED_SPEECH), "characters/submission/lyssieth", "DEMON_TF_STAGE_2_CHOICE_SIXTY_NINE_LYSSIETH_BOTTOM");
		}
		@Override
		public void applyEffects() {
			if(GameState.game.getPlayer().hasPenis()) {
				GameState.game.getNpc(Lyssieth.class).setForeplayPreference(GameState.game.getPlayer(), new SexType(SexParticipantType.NORMAL, SexAreaOrifice.MOUTH, SexAreaPenetration.PENIS));
			} else {
				GameState.game.getNpc(Lyssieth.class).setForeplayPreference(GameState.game.getPlayer(), new SexType(SexParticipantType.NORMAL, SexAreaPenetration.TONGUE, SexAreaOrifice.VAGINA));
			}
			specialActionTriggered = true;
			
			GameState.sex.setSexManager(
					new SMLyssiethDemonTF(
							SexPosition.LYING_DOWN,
							Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), SexSlotLyingDown.SIXTY_NINE)),
							Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Lyssieth.class), SexSlotLyingDown.LYING_DOWN))));
			
			GameState.game.getNpc(Lyssieth.class).fillCumToMaxStorage();
		}
	};
}
