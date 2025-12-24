package com.cometproject.api.game.catalog.purchase.economy.inventory.services;

import com.cometproject.api.game.catalog.purchase.economy.inventory.InventorySnapshot;

public interface IInventorySnapshotReplayService {
	
	InventorySnapshot rebuildSnapshot(int playerId, long untilTimestamp);
	
}
