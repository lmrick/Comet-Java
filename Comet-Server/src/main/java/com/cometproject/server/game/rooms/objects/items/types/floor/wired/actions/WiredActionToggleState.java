package com.cometproject.server.game.rooms.objects.items.types.floor.wired.actions;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.DiceFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.games.football.FootballTimerFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.games.freeze.FreezeTimerFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.WiredFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredActionItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.events.WiredItemEvent;
import com.cometproject.server.game.rooms.types.Room;
import com.google.common.collect.Lists;

import java.util.List;

public class WiredActionToggleState extends WiredActionItem {
	
	public WiredActionToggleState(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public boolean requiresPlayer() {
		return false;
	}
	
	@Override
	public int getInterface() {
		return 0;
	}
	
	@Override
	public void onEventComplete(WiredItemEvent event) {
		List<Position> tilesToUpdate = Lists.newArrayList();
		
		this.getWiredData().getSelectedIds().stream()
						.mapToLong(itemId -> itemId)
						.mapToObj(itemId -> this.getRoom().getItems().getFloorItem(itemId))
						.filter(floorItem -> floorItem != null && !(floorItem instanceof WiredFloorItem) && !(floorItem instanceof DiceFloorItem))
						.forEachOrdered(floorItem -> {
			floorItem.onInteract(null, (floorItem instanceof FootballTimerFloorItem || floorItem instanceof FreezeTimerFloorItem ? 1 : 0), true);
			tilesToUpdate.add(new Position(floorItem.getPosition().getX(), floorItem.getPosition().getY()));
		});
		
		tilesToUpdate.forEach(tileToUpdate -> this.getRoom().getMapping().updateTile(tileToUpdate.getX(), tileToUpdate.getY()));
		
		tilesToUpdate.clear();
	}
	
}
