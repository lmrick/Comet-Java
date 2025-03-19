package com.cometproject.server.game.rooms.objects.items.types.floor.wired.actions;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.WiredUtil;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredActionItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.events.WiredItemEvent;
import com.cometproject.server.game.rooms.types.Room;
import com.google.common.collect.Lists;
import java.util.List;

public class WiredActionRandomEffect extends WiredActionItem {
	
	public WiredActionRandomEffect(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public void onEventComplete(WiredItemEvent event) {
		if (!(event.entity instanceof PlayerEntity playerEntity)) {
			return;
		}
		
		List<WiredActionItem> actionItems = Lists.newArrayList();
		
		this.getWiredData().getSelectedIds().stream()
						.mapToLong(itemId -> itemId)
						.mapToObj(itemId -> this.getRoom().getItems().getFloorItem(itemId))
						.forEachOrdered(floorItem -> this.getRoom().getItems().getItemsOnSquare(floorItem.getPosition().getX(), floorItem.getPosition().getY()).stream()
										.filter(WiredActionItem.class::isInstance)
										.map(WiredActionItem.class::cast)
										.forEachOrdered(actionItems::add));
		
		WiredActionItem actionItem = WiredUtil.getRandomElement(actionItems);
		
		if (actionItem == null) return;
		
		actionItem.evaluate(playerEntity, null);
		
	}
	
	@Override
	public boolean requiresPlayer() {
		return true;
	}
	
	@Override
	public int getInterface() {
		return 0;
	}
	
}
