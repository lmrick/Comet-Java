package com.cometproject.server.game.rooms.objects.items.types.floor.games.football;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.components.games.GameTeam;

public class FootballScoreFloorItem extends RoomItemFloor {
	
	private GameTeam gameTeam;
	
	public FootballScoreFloorItem(RoomItemData roomItemData, Room room) {
		super(roomItemData, room);
		
		this.getItemData().setData("0");
		
		switch (this.getDefinition().getItemName()) {
			case "fball_score_b" -> this.gameTeam = GameTeam.BLUE;
			case "fball_score_r" -> this.gameTeam = GameTeam.RED;
			case "fball_score_y" -> this.gameTeam = GameTeam.YELLOW;
			case "fball_score_g" -> this.gameTeam = GameTeam.GREEN;
		}
	}
	
  @Override
	public void sendUpdate() {
		this.getItemData().setData(this.getRoom().getGame().getScore(this.gameTeam) + "");
		
		super.sendUpdate();
	}
	
	public void reset() {
		this.getItemData().setData(0 + "");
		this.sendUpdate();
	}
	
}
