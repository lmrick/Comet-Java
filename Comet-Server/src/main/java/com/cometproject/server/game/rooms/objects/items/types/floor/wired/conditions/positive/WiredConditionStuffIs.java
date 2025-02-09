package com.cometproject.server.game.rooms.objects.items.types.floor.wired.conditions.positive;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredConditionItem;
import com.cometproject.server.game.rooms.types.Room;

public class WiredConditionStuffIs extends WiredConditionItem {
	
	public WiredConditionStuffIs(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public int getInterface() {
		return 8;
	}
	
	@Override
	public boolean evaluate(RoomEntity entity, Object data) {
		if (!(data instanceof RoomItemFloor floor)) {
			return false;
		}
		
		boolean result = this.getWiredData().getSelectedIds().contains(floor.getId());
		
		return !this.isNegative == result;
	}
	
}
