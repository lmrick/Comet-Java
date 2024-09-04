package com.cometproject.server.game.commands.vip;

import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.DiceFloorItem;
import com.cometproject.server.game.rooms.types.mapping.RoomTile;
import com.cometproject.server.network.sessions.Session;

import java.util.Arrays;

public class DiceCommand extends ChatCommand {
	
	@Override
	public void execute(Session client, String[] params) {
		PlayerEntity entity = client.getPlayer().getEntity();
		
		Arrays.stream(entity.getTile().getAllAdjacentTiles()).forEachOrdered(tile -> tile.getItems().stream().filter(DiceFloorItem.class::isInstance).forEachOrdered(floorItem -> floorItem.onInteract(entity, floorItem.getItemData().getData().equals("0") ? 0 : -1, false)));
	}
	
	@Override
	public String getPermission() {
		return "dice_command";
	}
	
	@Override
	public String getParameter() {
		return null;
	}
	
	@Override
	public String getDescription() {
		return Locale.getOrDefault("command.dice.description", "Toggles all surrounding dice");
	}
	
}
