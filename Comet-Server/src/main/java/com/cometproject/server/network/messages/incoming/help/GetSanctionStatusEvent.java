package com.cometproject.server.network.messages.incoming.help;

import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.outgoing.help.SanctionStatusComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;

public class GetSanctionStatusEvent implements Event {
	
    @Override
	public void handle(Session client, MessageEvent msg) {
		client.send(new SanctionStatusComposer());
	}
	
}
