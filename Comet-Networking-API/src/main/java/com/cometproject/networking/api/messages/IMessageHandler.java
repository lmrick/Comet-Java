package com.cometproject.networking.api.messages;

import com.cometproject.api.networking.messages.wrappers.IEventDataWrapper;
import com.cometproject.networking.api.sessions.INetSession;

public interface IMessageHandler<T extends INetSession<?>> {
	
	void handleMessage(final IEventDataWrapper messageEvent, T session);
	
}
