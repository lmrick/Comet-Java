package com.cometproject.server.network.messages.outgoing.room.alerts;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class CantConnectMessageComposer extends MessageComposer {
	
	private final int Error;
	
	public CantConnectMessageComposer(final int Error) {
		this.Error = Error;
	}
	
	@Override
	public short getId() {
		return Composers.CantConnectMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(Error);
	}
	
}
