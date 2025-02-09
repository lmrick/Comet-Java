package com.cometproject.server.network.messages.outgoing.room.items.lovelock;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class LoveLockConfirmedMessageComposer extends MessageComposer {
	
	private final int itemId;
	
	public LoveLockConfirmedMessageComposer(final int itemId) {
		this.itemId = itemId;
	}
	
	@Override
	public short getId() {
		return Composers.LoveLockDialogueSetLockedMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(itemId);
	}
	
}
