package com.cometproject.server.network.messages.incoming.performance;

import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;

public class EventLogMessageEvent implements Event {
	
	@Override
	public void handle(Session client, MessageEvent msg) throws Exception {
		final String category = msg.readString();
		final String type = msg.readString();
		final String action = msg.readString();
		final String extraString = msg.readString();
		final int extraInt = msg.readInt();
		
		if (client.getPlayer() == null || client.getPlayer().isDisposed) {
			return;
		}
		
		client.getPlayer().getEventLogCategories().add(category);
	}
	
}
