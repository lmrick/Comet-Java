package com.cometproject.server.game.rooms.objects.items.types.floor.wired.triggers.custom;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredTriggerItem;
import com.cometproject.server.game.rooms.types.Room;

public class WiredTriggerCustomIdle extends WiredTriggerItem {
	
	public WiredTriggerCustomIdle(RoomItemData roomItemData, Room room) {
		super(roomItemData, room);
	}
	
	public static boolean executeTriggers(PlayerEntity playerEntity) {
		boolean wasExecuted = false;
		
		for (WiredTriggerCustomIdle wiredItem : getTriggers(playerEntity.getRoom(), WiredTriggerCustomIdle.class)) {
			
			if (playerEntity.isIdle()) wasExecuted = wiredItem.evaluate(playerEntity, wiredItem);
		}
		
		return wasExecuted;
	}
	
	@Override
	public boolean suppliesPlayer() {
		return true;
	}
	
	@Override
	public int getInterface() {
		return 9;
	}
	
}
