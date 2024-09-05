package com.cometproject.server.network.messages.incoming.moderation.tickets;

import com.cometproject.server.game.moderation.ModerationManager;
import com.cometproject.server.game.moderation.types.tickets.HelpTicket;
import com.cometproject.server.game.moderation.types.tickets.HelpTicketState;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;

import java.util.stream.IntStream;

public class ModToolReleaseIssueMessageEvent implements Event {
	
	@Override
	public void handle(Session client, MessageEvent msg) throws Exception {
		int ticketCount = msg.readInt();
		if (!client.getPlayer().getPermissions().getRank().modTool()) {
			client.disconnect();
			return;
		}
		
		IntStream.range(0, ticketCount).map(i -> msg.readInt()).mapToObj(ticketId -> ModerationManager.getInstance().getTicket(ticketId)).takeWhile(helpTicket -> helpTicket != null && helpTicket.getModeratorId() == client.getPlayer().getId()).forEachOrdered(helpTicket -> {
			helpTicket.setState(HelpTicketState.OPEN);
			helpTicket.setModeratorId(0);
			helpTicket.save();
			ModerationManager.getInstance().broadcastTicket(helpTicket);
		});
	}
	
}
