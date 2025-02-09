package com.cometproject.server.game.rooms.objects.items.types.floor.wired.actions;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.entities.effects.PlayerEffect;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredActionItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.events.WiredItemEvent;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.components.games.GameTeam;
import com.cometproject.server.game.rooms.types.components.games.GameType;

public class WiredActionJoinTeam extends WiredActionItem {
	
	private static final int PARAM_TEAM_ID = 0;
	
	public WiredActionJoinTeam(RoomItemData itemData, Room room) {
		super(itemData, room);
		
		if (this.getWiredData().getParams().size() != 1) {
			this.getWiredData().getParams().put(PARAM_TEAM_ID, 1); // team red
		}
	}
	
	@Override
	public boolean requiresPlayer() {
		return true;
	}
	
	@Override
	public int getInterface() {
		return 9;
	}
	
	@Override
	public void onEventComplete(WiredItemEvent event) {
		if (!(event.entity instanceof PlayerEntity playerEntity)) {
			return;
		}
		
		if (playerEntity.getGameTeam() != GameTeam.NONE) {
			return; // entity already in a team!
		}
		
		final GameTeam gameTeam = this.getTeam();
		
		if (this.getTeam() == GameTeam.NONE) return;
		
		playerEntity.setGameTeam(gameTeam, GameType.OTHER);
		this.getRoom().getGame().joinTeam(gameTeam, playerEntity);
		
		playerEntity.applyEffect(new PlayerEffect(gameTeam.getFreezeEffect(), false));
	}
	
	private GameTeam getTeam() {
		return switch (this.getWiredData().getParams().get(PARAM_TEAM_ID)) {
			case 1 -> GameTeam.RED;
			case 2 -> GameTeam.GREEN;
			case 3 -> GameTeam.BLUE;
			case 4 -> GameTeam.YELLOW;
			default -> GameTeam.NONE;
		};
	}
	
}
