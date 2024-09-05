package com.cometproject.server.network.ws.handlers;

import io.netty.channel.ChannelHandlerContext;

public interface IWsHandler {
	
	void handle(String data, ChannelHandlerContext ctx);
	
}
