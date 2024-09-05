package com.cometproject.server.network.ws.messages.auth;

import com.cometproject.server.network.ws.messages.WsMessage;
import com.cometproject.server.network.ws.messages.WsMessageType;

public class AuthOKMessage extends WsMessage {
	
	public AuthOKMessage() {
		super(WsMessageType.AUTH_OK);
	}
	
}
