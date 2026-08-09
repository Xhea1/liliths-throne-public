package com.lilithsthrone.game.sex.managers.submission;

import java.util.Map;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.quests.Quest;
import com.lilithsthrone.game.character.quests.QuestLine;
import com.lilithsthrone.game.character.race.Race;
import com.lilithsthrone.game.sex.SexPace;
import com.lilithsthrone.game.sex.managers.SexManagerDefault;
import com.lilithsthrone.game.sex.positions.AbstractSexPosition;
import com.lilithsthrone.game.sex.positions.slots.SexSlot;
import com.lilithsthrone.main.GameState;

/**
 * @since 0.3
 * @version 0.4.1
 * @author Innoxia
 */
public class SMLyssiethSex extends SexManagerDefault {

	public SMLyssiethSex(AbstractSexPosition position, Map<GameCharacter, SexSlot> dominants, Map<GameCharacter, SexSlot> submissives) {
		super(position,
				dominants,
				submissives);
	}

	@Override
	public boolean isPartnerWantingToStopSex(GameCharacter partner) {
		if(!GameState.game.getPlayer().isQuestProgressGreaterThan(QuestLine.MAIN, Quest.MAIN_2_D_MEETING_A_LILIN)) {
			return false; // Lyssieth orgasm ends sex, so this is fine
		}
		return super.isPartnerWantingToStopSex(partner);
	}
	
	@Override
	public boolean isPlayerAbleToStopSex() {
		return GameState.game.getPlayer().isQuestProgressGreaterThan(QuestLine.MAIN, Quest.MAIN_2_D_MEETING_A_LILIN);
	}
	
	@Override
	public boolean isSwapPositionAllowed(GameCharacter character, GameCharacter target) {
		return true;
	}
	
	@Override
	public boolean isPositionChangingAllowed(GameCharacter character) {
		return true;
	}
	
	@Override
	public SexPace getForcedSexPace(GameCharacter character) {
		if(!character.isPlayer()) {
			if(GameState.game.getPlayer().getRace()==Race.HUMAN) {
				if(GameState.sex.isDom(character)) {
					return SexPace.DOM_NORMAL;
				} else {
					return SexPace.SUB_EAGER;
				}
				
			} else {
				if(GameState.sex.isDom(character)) {
					return SexPace.DOM_NORMAL;
				} else {
					return SexPace.SUB_NORMAL;
				}
			}
		}
		return null;
	}
}
