package com.cometproject.gamecenter.fastfood.net;

import com.cometproject.networking.api.messages.IMessageHandler;
import com.cometproject.networking.api.sessions.INetSession;
import io.netty.channel.ChannelHandlerContext;

public class FastFoodNetSession implements INetSession<FastFoodGameSession> {
	private final ChannelHandlerContext channelHandlerContext;
	private final FastFoodGameSession gameSession;
	private final IMessageHandler<FastFoodNetSession> messageHandler;
	
	public FastFoodNetSession(ChannelHandlerContext ctx, FastFoodGameSession gameSession, IMessageHandler<FastFoodNetSession> messageHandler) {
		this.channelHandlerContext = ctx;
		this.gameSession = gameSession;
		this.messageHandler = messageHandler;
	}
	
	@Override
	public ChannelHandlerContext getChannel() {
		return this.channelHandlerContext;
	}
	
	@Override
	public IMessageHandler<FastFoodNetSession> getMessageHandler() {
		return this.messageHandler;
	}
	
	@Override
	public FastFoodGameSession getGameSession() {
		return this.gameSession;
	}
	
}
