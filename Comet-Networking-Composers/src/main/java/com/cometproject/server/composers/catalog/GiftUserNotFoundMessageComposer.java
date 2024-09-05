package com.cometproject.server.composers.catalog;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class GiftUserNotFoundMessageComposer extends MessageComposer {
	
	public GiftUserNotFoundMessageComposer() {
	
	}
	
	@Override
	public short getId() {
		return Composers.GiftWrappingErrorMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
	
	}
	
}
