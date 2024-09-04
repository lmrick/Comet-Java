package com.cometproject.server.game.rooms.objects.items.types.floor.wired.conditions.positive;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredConditionItem;
import com.cometproject.server.game.rooms.types.Room;

import java.util.Objects;

public class WiredConditionFurniHasPlayers extends WiredConditionItem {
	
	public WiredConditionFurniHasPlayers(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public int getInterface() {
		return 8;
	}
	
	@Override
	public boolean evaluate(RoomEntity entity, Object data) {
		int itemsWithPlayers = (int) this.getWiredData().getSelectedIds().stream().mapToLong(itemId -> itemId).mapToObj(itemId -> this.getRoom().getItems().getFloorItem(itemId)).filter(Objects::nonNull).filter(floorItem -> !floorItem.getEntitiesOnItem().isEmpty()).count();
		
		
		// System.out.format("%s, %s, %s\n", this.getId(), floorItem.getId(), floorItem.getTile().getEntity().getUsername());
		
		return isNegative ? itemsWithPlayers == 0 : itemsWithPlayers == this.getWiredData().getSelectedIds().size();
	}
	
}
