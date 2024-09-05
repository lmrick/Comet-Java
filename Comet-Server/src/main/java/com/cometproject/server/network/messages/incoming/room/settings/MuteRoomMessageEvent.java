package com.cometproject.server.network.messages.incoming.room.settings;

import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.RoomEntityType;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.outgoing.room.avatar.WhisperMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.settings.RoomMuteMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;

public class MuteRoomMessageEvent implements Event {
	
	@Override
	public void handle(Session client, MessageEvent msg) throws Exception {
		if (client.getPlayer().getEntity() == null) return;
		
		Room room = client.getPlayer().getEntity().getRoom();
		
		if (room.getData().getOwnerId() != client.getPlayer().getId() && !client.getPlayer().getPermissions().getRank().roomFullControl()) {
			return;
		}
		
		if (room.hasRoomMute()) {
			room.getEntities().getAllEntities().values().forEach(entity -> entity.setRoomMuted(false));
			
			client.getPlayer().getEntity().getRoom().getEntities().getPlayerEntities().stream().filter(entity -> entity.getEntityType() == RoomEntityType.PLAYER).forEachOrdered(entity -> entity.getPlayer().getSession().send(new WhisperMessageComposer(entity.getId(), Locale.getOrDefault("event.room.unmute", "You are now able to chat again :-)"))));
			
			room.setRoomMute(false);
		} else {
			room.getEntities().getAllEntities().values().forEach(entity -> entity.setRoomMuted(true));
			
			room.setRoomMute(true);
			
			client.getPlayer().getEntity().getRoom().getEntities().getPlayerEntities().stream().filter(entity -> entity.getEntityType() == RoomEntityType.PLAYER).forEachOrdered(entity -> entity.getPlayer().getSession().send(new WhisperMessageComposer(entity.getId(), Locale.getOrDefault("event.room.muted", "The room owner has muted the room."))));
		}
		
		room.getEntities().broadcastMessage(new RoomMuteMessageComposer(room.hasRoomMute()));
	}
	
}
