package com.cometproject.server.network.ws.handlers.types;

import com.cometproject.server.network.messages.outgoing.misc.OpenLinkMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.network.ws.handlers.AbstractWsHandler;
import com.cometproject.server.network.ws.request.types.link.OpenLinkRequest;
import io.netty.channel.ChannelHandlerContext;

public class OpenLinkMessageHandler extends AbstractWsHandler<OpenLinkRequest> {
	
	public OpenLinkMessageHandler() {
		super(OpenLinkRequest.class);
	}
	
	@Override
	protected void onMessage(OpenLinkRequest message, ChannelHandlerContext ctx) {
		final Session session = ctx.attr(SESSION).get();
		
		if (session.getPlayer() != null) {
			session.send(new OpenLinkMessageComposer(message.link()));
		}
	}
	
}
