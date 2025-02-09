package com.cometproject.gamecenter.fastfood.net;

import com.cometproject.networking.api.messages.IMessageHandler;
import com.cometproject.networking.api.sessions.INetSession;
import com.cometproject.networking.api.sessions.INetSessionFactory;
import io.netty.channel.ChannelHandlerContext;

public class SessionFactory implements INetSessionFactory {
	
	private final IMessageHandler<FastFoodNetSession> messageHandler;
	
	public SessionFactory(final IMessageHandler<FastFoodNetSession> messageHandler) {
		this.messageHandler = messageHandler;
	}
	
	@Override
	public INetSession<?> createSession(ChannelHandlerContext channel) {
		return new FastFoodNetSession(channel, new FastFoodGameSession(), this.messageHandler);
	}
	
	@Override
	public void disposeSession(INetSession<?> session) {
		final FastFoodNetSession netSession = (FastFoodNetSession) session;
		netSession.getChannel().disconnect();
	}
	
}
