package com.cometproject.server.network.messages.outgoing.room.items.wired;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class WiredRewardMessageComposer extends MessageComposer {
	private final int reason;
	
	public WiredRewardMessageComposer(final int reason) {
		this.reason = reason;
	}
	
	@Override
	public short getId() {
		return Composers.WiredRewardMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(reason);
	}
	
}
