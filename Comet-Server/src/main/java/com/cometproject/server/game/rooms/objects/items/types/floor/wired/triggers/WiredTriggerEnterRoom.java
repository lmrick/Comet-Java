package com.cometproject.server.game.rooms.objects.items.types.floor.wired.triggers;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredTriggerItem;
import com.cometproject.server.game.rooms.types.Room;

import java.util.Objects;

public class WiredTriggerEnterRoom extends WiredTriggerItem {
	
	public WiredTriggerEnterRoom(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	public static void executeTriggers(PlayerEntity playerEntity) {
		if (playerEntity == null || playerEntity.getRoom() == null || playerEntity.getRoom().getItems() == null) {
			return;
		}
		
		getTriggers(playerEntity.getRoom(), WiredTriggerEnterRoom.class).stream()
						.filter(Objects::nonNull)
						.filter(floorItem -> floorItem.getWiredData().getText().isEmpty()
										|| floorItem.getWiredData().getText().equals(playerEntity.getUsername()))
						.forEachOrdered(floorItem -> floorItem.evaluate(playerEntity, null));
	}
	
	@Override
	public int getInterface() {
		return 7;
	}
	
	@Override
	public boolean suppliesPlayer() {
		return true;
	}
	
}
