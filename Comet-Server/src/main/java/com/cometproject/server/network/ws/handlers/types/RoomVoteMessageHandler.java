package com.cometproject.server.network.ws.handlers.types;

import com.cometproject.server.game.rooms.RoomManager;
import com.cometproject.server.game.rooms.types.components.types.vote.RoomVote;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.network.ws.handlers.AbstractWsHandler;
import com.cometproject.server.network.ws.request.types.room.RoomVoteRequest;
import com.cometproject.server.network.ws.messages.room.RoomVoteCountMessage;
import io.netty.channel.ChannelHandlerContext;

public class RoomVoteMessageHandler extends AbstractWsHandler<RoomVoteRequest> {
	
	public RoomVoteMessageHandler() {
		super(RoomVoteRequest.class);
	}
	
	@Override
	protected void onMessage(RoomVoteRequest message, ChannelHandlerContext ctx) {
		final RoomVote roomVote = RoomManager.getInstance().getRoomVote();
		final Session session = ctx.attr(SESSION).get();
		
		if (roomVote != null && session.getPlayer() != null) {
			if (roomVote.hasVoted(session.getPlayer().getId())) {
				return;
			}
			
			roomVote.vote(session.getPlayer().getId(), message.vote());
			
			final int[] votes = roomVote.getVotes();
			NetworkManager.getInstance().getSessions().broadcastWs(new RoomVoteCountMessage(votes[0], votes[1]));
		}
	}
	
}
