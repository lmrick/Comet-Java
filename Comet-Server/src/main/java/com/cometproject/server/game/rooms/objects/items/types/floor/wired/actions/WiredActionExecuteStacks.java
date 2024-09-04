package com.cometproject.server.game.rooms.objects.items.types.floor.wired.actions;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredActionItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.events.WiredItemEvent;
import com.cometproject.server.game.rooms.types.Room;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

public class WiredActionExecuteStacks extends WiredActionItem {
	
	public WiredActionExecuteStacks(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public void onEventComplete(WiredItemEvent event) {
		List<Position> tilesToExecute = this.getWiredData().getSelectedIds().stream().mapToLong(itemId -> itemId).mapToObj(itemId -> this.getRoom().getItems().getFloorItem(itemId)).filter(floorItem -> floorItem != null && (floorItem.getPosition().getX() != this.getPosition().getX() || floorItem.getPosition().getY() != this.getPosition().getY())).map(floorItem -> new Position(floorItem.getPosition().getX(), floorItem.getPosition().getY())).collect(Collectors.toList());
		
		List<WiredActionItem> actions = Lists.newArrayList();
		
		tilesToExecute.forEach(tileToUpdate -> this.getRoom().getMapping().getTile(tileToUpdate).getItems().stream().filter(roomItemFloor -> roomItemFloor instanceof WiredActionItem && !(roomItemFloor instanceof WiredActionExecuteStacks)).map(WiredActionItem.class::cast).forEachOrdered(actions::add));
		
		final int max = 30;
		int limiter = 0;
		
		for (WiredActionItem actionItem : actions) {
			if (limiter >= max) {
				break;
			}
			
			limiter++;
			actionItem.evaluate(event.entity, event.data);
		}
		
		tilesToExecute.clear();
	}
	
	@Override
	public boolean requiresPlayer() {
		return false;
	}
	
	@Override
	public int getInterface() {
		return 18;
	}
	
}
