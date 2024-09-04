package com.cometproject.server.game.rooms.objects.items.types.floor.wired.conditions.positive;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredConditionItem;
import com.cometproject.server.game.rooms.types.Room;

public class WiredConditionPlayerInTeam extends WiredConditionItem {
	
	private static int PARAM_TEAM = 0;
	
	public WiredConditionPlayerInTeam(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public int getInterface() {
		return 6;
	}
	
	@Override
	public boolean evaluate(RoomEntity entity, Object data) {
		if (this.getWiredData().getParams().size() != 1) {
			return false;
		}
		
		if (!(entity instanceof PlayerEntity playerEntity)) {
			return false;
		}
		
		final int team = this.getWiredData().getParams().get(PARAM_TEAM);
		
		return !this.isNegative ? playerEntity.getGameTeam() != null && playerEntity.getGameTeam().getTeamId() == team : playerEntity.getGameTeam() == null || playerEntity.getGameTeam().getTeamId() != team;
	}
	
}
