package com.lilithsthrone.game.dialogue.places.dominion.lilayashome;

import java.util.ArrayList;
import java.util.List;

import com.lilithsthrone.game.character.npc.NPC;
import com.lilithsthrone.game.dialogue.DialogueNode;
import com.lilithsthrone.game.dialogue.companions.CompanionManagement;
import com.lilithsthrone.game.dialogue.companions.OccupantManagementDialogue;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.utils.UtilText;
import com.lilithsthrone.main.GameState;
import com.lilithsthrone.world.places.AbstractPlaceUpgrade;
import com.lilithsthrone.world.places.PlaceUpgrade;

/**
 * @since 0.3.9
 * @version 0.3.9
 * @author Innoxia
 */
public class LilayaOfficeDialogue {

	public static final DialogueNode ROOM_OFFICE = new DialogueNode("Room", "", false) {
		@Override
		public int getSecondsPassed() {
			return 10;
		}
		@Override
		public String getLabel() {
			return GameState.game.getPlayer().getLocationPlace().getName();
		}
		@Override
		public String getContent() {
			StringBuilder sb = new StringBuilder();
			
			sb.append(LilayaHomeGeneric.getBaseRoomDescription());
			
			sb.append("<p>"
						+ "<b style='color:"+PlaceUpgrade.LILAYA_OFFICE.getColour().toWebHexString()+";'>"+PlaceUpgrade.LILAYA_OFFICE.getName()+"</b><br/>"
						+ PlaceUpgrade.LILAYA_OFFICE.getRoomDescription(GameState.game.getPlayerCell())
					+ "</p>");
			
			for(AbstractPlaceUpgrade up : GameState.game.getPlayerCell().getPlace().getPlaceUpgrades()) {
				if(!up.isCoreRoomUpgrade()) {
					sb.append("<p>"
								+ "<b style='color:"+up.getColour().toWebHexString()+";'>"+up.getName()+"</b><br/>"
								+ up.getRoomDescription(GameState.game.getPlayerCell())
							+ "</p>");
				}
			}
			
			return sb.toString();
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
			
			List<NPC> charactersPresent = LilayaHomeGeneric.getSlavesAndOccupantsPresent();
			List<NPC> slavesAssignedToRoom = new ArrayList<>();
			slavesAssignedToRoom.addAll(charactersPresent);
			
			if(index==0) {
				return null;
				
			} else if (index == 1) {
				if(GameState.game.getPlayer().isAbleToAccessRoomManagement()) {
					return new Response("Manage room", "Enter the management screen for this particular room.", OccupantManagementDialogue.ROOM_UPGRADES) {
						@Override
						public void effects() {
							OccupantManagementDialogue.cellToInspect = GameState.game.getPlayerCell();
						}
					};
				} else {
					return new Response("Manage room", "You need a slaver license or permission from Lilaya to house your friends or dolls in order to access this menu!",  null);
				}
				
			}  else if (index == 2) {
				if(GameState.game.getPlayer().isAbleToAccessRoomManagement()) {
					return new Response("Manage people", "Enter the management screen for your slaves and friendly occupants.", OccupantManagementDialogue.getSlaveryRoomListDialogue(null, null)) {
						@Override
						public void effects() {
							CompanionManagement.initManagement(GameState.game.getDefaultDialogue(), 0, null);
						}
					};
				} else {
					return new Response("Manage people", "You need a slaver license or permission from Lilaya to house your friends or dolls in order to access this menu!",  null);
				}
				
			} else if(index==3) {
				return new Response("Occupancy ledger", "Open the occupancy ledger screen, from which you can manage all rooms, slaves, and friendly occupants.", OccupantManagementDialogue.getSlaveryOverviewDialogue(null)) {
					@Override
					public void effects() {
						CompanionManagement.initManagement(GameState.game.getDefaultDialogue(), 0, null);
					}
				};
			}
			
			int indexPresentStart = 4;
			if(index-indexPresentStart<slavesAssignedToRoom.size()) {
				NPC character = slavesAssignedToRoom.get(index-indexPresentStart);
				if(charactersPresent.contains(character) || (character.getHomeCell().equals(GameState.game.getPlayerCell()) && GameState.game.getPlayer().getCompanions().contains(character))) {
					return LilayaHomeGeneric.interactWithNPC(character);
				} else {
					return new Response(UtilText.parse(character, "[npc.Name]"), 
							UtilText.parse(character, "Although this is [npc.namePos] room, [npc.sheIs] "
									+(character.getLocationPlace().getPlaceUpgrades().contains(PlaceUpgrade.LILAYA_SLAVE_LOUNGE)
											?"relaxing in a slave lounge at the moment."
											:"out at work at the moment.")), null);
				}
			}
			
			return null;
		}
	};
}
