package com.cometproject.server.network.messages.outgoing.room.permissions;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class YouAreSpectatorMessageComposer extends MessageComposer {
	
	@Override
	public short getId() {
		return Composers.YouAreSpectatorMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
	
	}
	
}
