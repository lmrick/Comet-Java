package com.cometproject.server.network.messages.incoming.moderation;

import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.rooms.IRoomData;
import com.cometproject.server.logging.LogManager;
import com.cometproject.server.logging.database.queries.LogQueries;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.outgoing.moderation.ModToolRoomChatlogMessageComposer;
import com.cometproject.server.network.messages.outgoing.notification.AdvancedAlertMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;

public class ModToolRoomChatlogMessageEvent implements Event {
	
    @Override
	public void handle(Session client, MessageEvent msg) {
		int context = msg.readInt();
		int roomId = msg.readInt();
		
		if (!client.getPlayer().getPermissions().getRank().modTool()) {
			client.disconnect();
			return;
		}
		
		if (!LogManager.ENABLED) {
			client.send(new AdvancedAlertMessageComposer("Notice", "Logging is not currently enabled, please contact your system administrator to enable it."));
			return;
		}
		
		IRoomData roomData = GameContext.getCurrent().getRoomService().getRoomData(roomId);
		
		if (roomData != null) {
			client.send(new ModToolRoomChatlogMessageComposer(roomData.getId(), roomData.getName(), LogQueries.getChatLogsForRoom(roomData.getId())));
		} else {
			client.send(new AdvancedAlertMessageComposer("Notice", "There seems to be an issue with fetching the logs for this room (ID: " + roomId + ", Context: " + context + ")"));
		}
	}
	
}
