package com.cometproject.api.game.catalog.purchase.economy.inventory.rollback;

import com.cometproject.api.game.catalog.purchase.economy.inventory.InventorySnapshot;

public interface IInventoryRollbackService {
	
	void rollback(int playerId, InventorySnapshot snapshot);
	
}
