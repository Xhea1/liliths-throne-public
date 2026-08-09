package com.lilithsthrone.game.sex.managers.submission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.body.CoverableArea;
import com.lilithsthrone.game.character.npc.submission.Shadow;
import com.lilithsthrone.game.character.npc.submission.Silence;
import com.lilithsthrone.game.character.npc.submission.Vengar;
import com.lilithsthrone.game.sex.SexAreaOrifice;
import com.lilithsthrone.game.sex.SexAreaPenetration;
import com.lilithsthrone.game.sex.SexParticipantType;
import com.lilithsthrone.game.sex.SexType;
import com.lilithsthrone.game.sex.managers.OrgasmBehaviour;
import com.lilithsthrone.game.sex.managers.SexManagerDefault;
import com.lilithsthrone.game.sex.positions.AbstractSexPosition;
import com.lilithsthrone.game.sex.positions.slots.SexSlot;
import com.lilithsthrone.main.GameState;
import com.lilithsthrone.utils.Util;

/**
 * @since 0.3.5.5
 * @version 0.3.5.5
 * @author Innoxia
 */
public class SMVengarDominantSex extends SexManagerDefault {

	public SMVengarDominantSex(AbstractSexPosition position, Map<GameCharacter, SexSlot> dominants, Map<GameCharacter, SexSlot> submissives) {
		super(position,
				dominants,
				submissives);
	}

	@Override
	public Map<GameCharacter, List<CoverableArea>> exposeAtStartOfSexMap() {
		Map<GameCharacter, List<CoverableArea>> map = new HashMap<>();
		
		for(GameCharacter dom : this.getDominants().keySet()) {
			if(dom.equals(GameState.game.getNpc(Vengar.class))) {
				map.put(GameState.game.getNpc(Vengar.class), Util.newArrayListOfValues(CoverableArea.PENIS));
				
			} else if(dom.equals(GameState.game.getNpc(Shadow.class))) {
				map.put(GameState.game.getNpc(Shadow.class), Util.newArrayListOfValues(CoverableArea.VAGINA));
				
			} else if(dom.equals(GameState.game.getNpc(Silence.class))) {
				map.put(GameState.game.getNpc(Silence.class), Util.newArrayListOfValues(CoverableArea.VAGINA));
				
			} else {
				map.put(dom, Util.newArrayListOfValues(CoverableArea.PENIS));
			}
		}
		
		if(this.getSubmissives().containsKey(GameState.game.getPlayer())) {
			map.put(GameState.game.getPlayer(), Util.newArrayListOfValues(CoverableArea.MOUTH));
			if(GameState.game.getPlayer().hasVagina() && GameState.game.getPlayer().isAbleToAccessCoverableArea(CoverableArea.VAGINA, true)) {
				map.get(GameState.game.getPlayer()).add(CoverableArea.VAGINA);
			} else {
				map.get(GameState.game.getPlayer()).add(CoverableArea.ANUS);
			}
		}
		
		GameCharacter companion = GameState.game.getPlayer().getMainCompanion();
		if(companion!=null && this.getSubmissives().containsKey(companion)) {
			map.put(companion, Util.newArrayListOfValues(CoverableArea.MOUTH));
			if(companion.hasVagina() && companion.isAbleToAccessCoverableArea(CoverableArea.VAGINA, true)) {
				map.get(companion).add(CoverableArea.VAGINA);
			} else {
				map.get(companion).add(CoverableArea.ANUS);
			}
		}
		
		return map;
	}
	
	@Override
	public boolean isPlayerAbleToStopSex() {
		return false;
	}
	
	@Override
	public boolean isSwapPositionAllowed(GameCharacter character, GameCharacter target) {
		return false;
	}
	
	@Override
	public boolean isPositionChangingAllowed(GameCharacter character) {
		return false;
	}
	
	@Override
	public SexType getForeplayPreference(GameCharacter character, GameCharacter targetedCharacter) {
		if(GameState.sex.isDom(character)) {
			if(targetedCharacter.hasVagina() && targetedCharacter.isAbleToAccessCoverableArea(CoverableArea.VAGINA, true)) {
				return new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, SexAreaOrifice.VAGINA);
				
			} else if(targetedCharacter.isAbleToAccessCoverableArea(CoverableArea.ANUS, true)) {
				return new SexType(SexParticipantType.NORMAL, SexAreaPenetration.PENIS, SexAreaOrifice.ANUS);
			}
		}
		return character.getForeplayPreference(targetedCharacter);
	}
	
	@Override
	public SexType getMainSexPreference(GameCharacter character, GameCharacter targetedCharacter) {
		if(GameState.sex.isDom(character)) {
			return character.getForeplayPreference(targetedCharacter);
		}
		return character.getMainSexPreference(targetedCharacter);
	}
	
	@Override
	public OrgasmBehaviour getCharacterOrgasmBehaviour(GameCharacter character) {
		if(GameState.sex.isDom(character)) {
			return OrgasmBehaviour.CREAMPIE;
		}
		return OrgasmBehaviour.DEFAULT;
	}
}
