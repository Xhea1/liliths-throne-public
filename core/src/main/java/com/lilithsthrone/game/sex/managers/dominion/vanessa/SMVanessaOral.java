package com.lilithsthrone.game.sex.managers.dominion.vanessa;

import java.util.List;
import java.util.Map;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.body.CoverableArea;
import com.lilithsthrone.game.character.npc.dominion.Vanessa;
import com.lilithsthrone.game.inventory.clothing.AbstractClothing;
import com.lilithsthrone.game.sex.SexControl;
import com.lilithsthrone.game.sex.managers.SexManagerDefault;
import com.lilithsthrone.game.sex.positions.AbstractSexPosition;
import com.lilithsthrone.game.sex.positions.slots.SexSlot;
import com.lilithsthrone.main.GameState;
import com.lilithsthrone.utils.Util;
import com.lilithsthrone.utils.Util.Value;

/**
 * @since 0.3.2
 * @version 0.3.4
 * @author Innoxia
 */
public class SMVanessaOral extends SexManagerDefault {
	
	public SMVanessaOral(AbstractSexPosition position, Map<GameCharacter, SexSlot> dominants, Map<GameCharacter, SexSlot> submissives) {
		super(position,
				dominants,
				submissives);
	}
	
	@Override
	public boolean isPlayerAbleToStopSex() {
		return false;
	}
	
	@Override
	public Map<GameCharacter, List<CoverableArea>> exposeAtStartOfSexMap() {
		if(GameState.sex.isDom(GameState.game.getPlayer())) {
			return Util.newHashMapOfValues(new Value<>(GameState.game.getPlayer(), Util.newArrayListOfValues(CoverableArea.VAGINA)));
		}
		return Util.newHashMapOfValues(new Value<>(GameState.game.getNpc(Vanessa.class), Util.newArrayListOfValues(CoverableArea.VAGINA)));
	}
	
	@Override
	public SexControl getSexControl(GameCharacter character) {
		if(character.isPlayer() && !GameState.sex.isDom(character)) {
			return SexControl.ONGOING_ONLY;
		}
		return super.getSexControl(character);
	}
	
	@Override
	public boolean isAbleToRemoveOthersClothing(GameCharacter character, AbstractClothing clothing){
		return false;
	}
	
	@Override
	public boolean isPositionChangingAllowed(GameCharacter character) {
		return false;
	}
	
	@Override
	public boolean isPartnerWantingToStopSex(GameCharacter partner) {
		// When player is performing cunnilingus, she stops after orgasming once:
		if(GameState.sex.isDom(partner)) {
			return GameState.sex.getNumberOfOrgasms(partner)>=1;
		}

		// When player is receiving cunnilingus, or fucking Vanessa, she stops after both of you are satisfied:
		return GameState.sex.isSatisfiedFromOrgasms(GameState.game.getPlayer(), true) && GameState.sex.isSatisfiedFromOrgasms(GameState.game.getNpc(Vanessa.class), true);
	}
	
}
