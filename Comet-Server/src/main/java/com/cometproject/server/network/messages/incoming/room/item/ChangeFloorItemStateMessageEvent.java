package com.cometproject.server.network.messages.incoming.room.item;

import com.cometproject.api.game.quests.QuestType;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.AffectedTile;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.triggers.WiredTriggerStateChanged;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.mapping.RoomTile;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.outgoing.room.engine.UpdateStackMapMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;

import java.util.ArrayList;
import java.util.List;

public class ChangeFloorItemStateMessageEvent implements Event {
	
	public void handle(Session client, MessageEvent msg) {
		
		int virtualId = msg.readInt();
		
		Long itemId = ItemManager.getInstance().getItemIdByVirtualId(virtualId);
		
		if (itemId == null) {
			return;
		}
		
		if (client.getPlayer().getEntity() == null || client.getPlayer().getEntity().getRoom() == null) {
			return;
		}
		
		if (!client.getPlayer().getEntity().isVisible()) {
			return;
		}
		
		Room room = client.getPlayer().getEntity().getRoom();
		
		RoomItemFloor item = room.getItems().getFloorItem(itemId);
		
		if (item == null) {
			return;
		}
		
		client.getPlayer().getQuests().progressQuest(QuestType.EXPLORE_FIND_ITEM, item.getDefinition().getSpriteId());
		
		WiredTriggerStateChanged.executeTriggers(client.getPlayer().getEntity(), item);
		
		if (item.onInteract(client.getPlayer().getEntity(), msg.readInt(), false)) {
			List<Position> tilesToUpdate = new ArrayList<>();
			tilesToUpdate.add(new Position(item.getPosition().getX(), item.getPosition().getY(), 0d));
			
			for (AffectedTile tile : AffectedTile.getAffectedTilesAt(item.getDefinition().getLength(), item.getDefinition().getWidth(), item.getPosition().getX(), item.getPosition().getY(), item.getRotation())) {
				if (room.getEntities().getEntitiesAt(new Position(tile.x, tile.y)).size() >= 0)
					tilesToUpdate.add(new Position(tile.x, tile.y, 0d));
			}
			
			for (Position tileToUpdate : tilesToUpdate) {
				RoomTile tile = room.getMapping().getTile(tileToUpdate);
				
				if (tile != null) {
					tile.reload();
					
					room.getEntities().broadcastMessage(new UpdateStackMapMessageComposer(tile));
				}
			}
		}
	}
	
}
