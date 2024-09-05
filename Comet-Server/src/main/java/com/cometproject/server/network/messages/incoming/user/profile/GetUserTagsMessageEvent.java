package com.cometproject.server.network.messages.incoming.user.profile;

import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;

public class GetUserTagsMessageEvent implements Event {
	
	@Override
	public void handle(Session client, MessageEvent msg) throws Exception {
	
	}
	
}
