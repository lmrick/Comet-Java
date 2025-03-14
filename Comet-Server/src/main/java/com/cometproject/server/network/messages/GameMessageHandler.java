package com.cometproject.server.network.messages;

import org.checkerframework.checker.units.qual.g;

import com.cometproject.api.networking.messages.wrappers.IEventDataWrapper;
import com.cometproject.networking.api.messages.IMessageHandler;
import com.cometproject.networking.api.sessions.INetSession;
import com.cometproject.server.network.sessions.net.NetSession;
import com.cometproject.server.protocol.messages.MessageEvent;

@SuppressWarnings("rawtypes")
public class GameMessageHandler implements IMessageHandler {
	
	@Override
	public void handleMessage(IEventDataWrapper messageEvent, INetSession session) {
		if (!(session instanceof NetSession netSession)) {
			return;
		}
		
		netSession.getGameSession().handleMessageEvent((MessageEvent) messageEvent);
	}
	
}
