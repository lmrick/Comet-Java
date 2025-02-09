package com.cometproject.server.game.catalog.purchase.handlers;

import com.cometproject.api.game.catalog.types.ICatalogItem;
import com.cometproject.server.game.catalog.purchase.IPurchaseHandler;
import com.cometproject.server.game.catalog.purchase.PurchaseResult;
import com.cometproject.server.network.sessions.Session;

public class StickiesPurchaseHandler implements IPurchaseHandler {
	
	@Override
	public PurchaseResult handlePurchaseData(Session session, String purchaseData, ICatalogItem catalogItem, int amount) {
		return new PurchaseResult(amount * 20, "");
	}
	
}
