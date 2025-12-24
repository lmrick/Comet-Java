package com.cometproject.server.network.messages.incoming.moderation;

import com.cometproject.server.game.moderation.chatlog.UserChatlogContainer;
import com.cometproject.server.logging.LogService;
import com.cometproject.server.logging.database.queries.LogQueries;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.outgoing.moderation.ModToolUserChatlogMessageComposer;
import com.cometproject.server.network.messages.outgoing.notification.AdvancedAlertMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;

public class ModToolUserChatlogMessageEvent implements Event {
	
	@Override
	public void handle(Session client, MessageEvent msg) {
		int userId = msg.readInt();
		
		if (!client.getPlayer().getPermissions().getRank().modTool()) {
			return;
		}
		
		if (!LogService.ENABLED) {
			client.send(new AdvancedAlertMessageComposer("Notice", "Logging is not currently enabled, please contact your system administrator to enable it."));
		}
		
		UserChatlogContainer chatlogContainer = new UserChatlogContainer();
		LogQueries.getLastRoomVisits(userId, 500).forEach(visit -> chatlogContainer.addAll(visit.getRoomId(), LogQueries.getChatLogsByCriteria(visit.getPlayerId(), visit.getRoomId(), visit.getEntryTime(), visit.getExitTime())));
		
		client.send(new ModToolUserChatlogMessageComposer(userId, chatlogContainer));
	}
	
}
