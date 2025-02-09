package com.cometproject.server.game.rooms.objects.items.types.floor.wired.conditions.positive;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredConditionItem;
import com.cometproject.server.game.rooms.types.Room;

import java.util.Objects;

public class WiredConditionHasFurniOn extends WiredConditionItem {
	
	private final static int PARAM_MODE = 0;
	
	public WiredConditionHasFurniOn(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public int getInterface() {
		return 7;
	}
	
	public int getMode() {
		return this.getWiredData().getParams().get(PARAM_MODE);
	}
	
	@Override
	public boolean evaluate(RoomEntity entity, Object data) {
		int mode;
		
		try {
			mode = this.getWiredData().getParams().get(PARAM_MODE);
		} catch (Exception e) {
			mode = 0;
		}
		
		int selectedItemsWithFurni = this.getWiredData().getSelectedIds().stream()
            .mapToLong(itemId -> itemId)
            .mapToObj(itemId -> this.getRoom().getItems().getFloorItem(itemId))
            .filter(Objects::nonNull)
            .mapToInt(floorItem -> (int) floorItem.getItemsOnStack().stream()
            .filter(itemOnSq -> itemOnSq.getPosition().getZ() != 0.0
            && itemOnSq.getPosition().getZ() >= floorItem.getPosition().getZ()
            && itemOnSq.getId() != floorItem.getId()
            ).count()).sum();
		
		boolean result = false;
		
		if (mode == 0) {
			if (selectedItemsWithFurni >= 1) result = true;
		} else {
			if (selectedItemsWithFurni == this.getWiredData().getSelectedIds().size()) result = true;
		}
		
		return this.isNegative != result;
	}
	
}