package com.cometproject.api.game.catalog.purchase.economy.inventory.projection;

import com.cometproject.api.game.catalog.purchase.economy.inventory.InventorySnapshot;
import com.cometproject.api.utilities.events.catalog.IEconomyEvent;

public interface IInventorySnapshotProjection {
	
	void apply(IEconomyEvent event);
	InventorySnapshot snapshot(int playerId, long timestamp);
	
}
