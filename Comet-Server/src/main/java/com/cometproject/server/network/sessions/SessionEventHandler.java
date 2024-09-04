package com.cometproject.server.network.sessions;

import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.protocol.headers.Events;
import com.cometproject.server.protocol.messages.MessageEvent;

import java.util.HashMap;
import java.util.Map;

public class SessionEventHandler {
	
	private final Session session;
	private final Map<Short, Boolean> loginEvents;
	
	public SessionEventHandler(Session session) {
		this.session = session;
		
		this.loginEvents = new HashMap<>() {{
			put(Events.GenerateSecretKeyMessageEvent, true);
			put(Events.InitCryptoMessageEvent, true);
			put(Events.SSOTicketMessageEvent, true);
		}};
	}
	
	public void handle(MessageEvent msg) {
		if (this.loginEvents.containsKey(msg.getId())) {
			if (!this.loginEvents.get(msg.getId())) {
				return;
			} else {
				this.loginEvents.replace(msg.getId(), false);
			}
		}
		
		NetworkManager.getInstance().getMessages().handle(msg, this.session);
	}
	
	public void dispose() {
		this.loginEvents.clear();
	}
	
}
