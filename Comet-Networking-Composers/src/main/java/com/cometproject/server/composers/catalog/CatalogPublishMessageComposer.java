package com.cometproject.server.composers.catalog;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class CatalogPublishMessageComposer extends MessageComposer {
	
	private final boolean showNotification;
	
	public CatalogPublishMessageComposer(final boolean showNotification) {
		this.showNotification = showNotification;
	}
	
	@Override
	public short getId() {
		return Composers.CatalogUpdatedMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeBoolean(this.showNotification);
	}
	
}
