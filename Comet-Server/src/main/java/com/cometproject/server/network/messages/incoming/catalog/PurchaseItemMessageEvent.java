package com.cometproject.server.network.messages.incoming.catalog;

import com.cometproject.server.game.catalog.CatalogManager;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;

public class PurchaseItemMessageEvent implements Event {
	
	@Override
	public void handle(Session client, MessageEvent msg) {
		int pageId = msg.readInt();
		int itemId = msg.readInt();
		String data = msg.readString();
		int amount = msg.readInt();
		
		CatalogManager.getInstance().getPurchaseHandler().purchaseItem(client, pageId, itemId, data, amount, null);
	}
	
}
