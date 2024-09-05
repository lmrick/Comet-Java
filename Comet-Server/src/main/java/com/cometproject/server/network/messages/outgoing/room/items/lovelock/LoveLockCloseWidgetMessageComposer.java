package com.cometproject.server.network.messages.outgoing.room.items.lovelock;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class LoveLockCloseWidgetMessageComposer extends MessageComposer {
	
	private final int itemId;
	
	public LoveLockCloseWidgetMessageComposer(final int itemId) {
		this.itemId = itemId;
	}
	
	@Override
	public short getId() {
		return Composers.LoveLockDialogueCloseMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(this.itemId);
	}
	
}
