package com.cometproject.server.game.rooms.objects.items.types.floor.wired.actions.custom;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.boutique.MannequinFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredActionItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.events.WiredItemEvent;
import com.cometproject.server.game.rooms.types.Room;

import java.util.Arrays;
import java.util.stream.Collectors;

public class WiredCustomChangeClothes extends WiredActionItem {
	
	public WiredCustomChangeClothes(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public boolean requiresPlayer() {
		return true;
	}
	
	@Override
	public int getInterface() {
		return 0;
	}
	
	@Override
	public int getFurniSelection() {
		return 1;
	}
	
	@Override
	public void onEventComplete(WiredItemEvent event) {
		if (!(event.entity instanceof PlayerEntity playerEntity)) {
			return;
		}
		
		if (playerEntity.getPlayer() == null || playerEntity.getPlayer().getSession() == null) {
			return;
		}
		
		for (long itemId : this.getWiredData().getSelectedIds()) {
			final RoomItemFloor floorItem = this.getRoom().getItems().getFloorItem(itemId);
			
			if (!(floorItem instanceof MannequinFloorItem mannequinFloorItem)) return;
			
			String newFigure = Arrays.stream(playerEntity.getFigure().split("\\.")).filter(playerFigurePart -> !playerFigurePart.startsWith("ch") && !playerFigurePart.startsWith("lg")).map(playerFigurePart -> playerFigurePart + ".").collect(Collectors.joining());
			
			String newFigureParts = "";
			
			switch (playerEntity.getGender().toUpperCase()) {
				case "M" -> newFigureParts = mannequinFloorItem.getFigure();
				case "F" -> newFigureParts = mannequinFloorItem.getFigure();
			}
			
			for (String newFigurePart : newFigureParts.split("\\.")) {
				if (newFigurePart.startsWith("hd")) newFigureParts = newFigureParts.replace(newFigurePart, "");
			}
			
			if (newFigureParts.isEmpty()) return;
			
			final String figure = newFigure + newFigureParts;
			
			if (figure.length() > 512) return;
			
			playerEntity.getPlayer().getData().setFigure(figure);
			playerEntity.getPlayer().getData().setGender(mannequinFloorItem.getGender());
			
			playerEntity.getPlayer().getData().save();
			playerEntity.getPlayer().poof();
		}
	}
	
}
