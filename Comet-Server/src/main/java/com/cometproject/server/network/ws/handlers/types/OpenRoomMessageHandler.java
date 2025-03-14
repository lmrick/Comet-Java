package com.cometproject.server.network.ws.handlers.types;

import com.cometproject.server.network.messages.outgoing.room.engine.RoomForwardMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.network.ws.handlers.AbstractWsHandler;
import com.cometproject.server.network.ws.request.types.room.OpenRoomRequest;
import io.netty.channel.ChannelHandlerContext;

public class OpenRoomMessageHandler extends AbstractWsHandler<OpenRoomRequest> {
	
	public OpenRoomMessageHandler() {
		super(OpenRoomRequest.class);
	}
	
	@Override
	protected void onMessage(OpenRoomRequest message, ChannelHandlerContext ctx) {
		final Session session = ctx.channel().attr(SESSION).get();
		
		if (session != null) {
			session.send(new RoomForwardMessageComposer(message.roomId()));
		}
	}
	
}
