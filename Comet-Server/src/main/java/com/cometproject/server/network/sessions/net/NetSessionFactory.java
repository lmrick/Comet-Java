package com.cometproject.server.network.sessions.net;

import com.cometproject.networking.api.messages.IMessageHandler;
import com.cometproject.networking.api.sessions.INetSession;
import com.cometproject.networking.api.sessions.INetSessionFactory;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.network.sessions.SessionManager;
import io.netty.channel.ChannelHandlerContext;

@SuppressWarnings("rawtypes")
public class NetSessionFactory implements INetSessionFactory {
	
	private final SessionManager sessionManager;
	private final IMessageHandler messageHandler;
	
	public NetSessionFactory(SessionManager sessionManager, IMessageHandler messageHandler) {
		this.sessionManager = sessionManager;
		this.messageHandler = messageHandler;
		
	}
	
	@Override
	public INetSession createSession(ChannelHandlerContext channel) {
		if (!sessionManager.add(channel)) {
			return null;
		}
		
		final Session session = channel.attr(SessionManager.SESSION_ATTR).get();
		
		return new NetSession(session, this.messageHandler);
	}
	
	@Override
	public void disposeSession(INetSession session) {
		((Session) session.getGameSession()).onDisconnect();
		
		this.sessionManager.remove(session.getChannel());
	}
	
}
