package com.cometproject.server.network.messages.outgoing.handshake;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class UniqueIDMessageComposer extends MessageComposer {
	
	private final String uniqueId;
	
	public UniqueIDMessageComposer(final String uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	@Override
	public short getId() {
		return Composers.UniqueMachineIDMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeString(this.uniqueId);
	}
	
}
