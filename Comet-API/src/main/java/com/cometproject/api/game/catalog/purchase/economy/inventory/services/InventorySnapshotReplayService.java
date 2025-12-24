package com.cometproject.api.game.catalog.purchase.economy.inventory.services;

import com.cometproject.api.game.catalog.purchase.economy.IEconomyEventStore;
import com.cometproject.api.game.catalog.purchase.economy.inventory.InventorySnapshot;
import com.cometproject.api.game.catalog.purchase.economy.inventory.projection.InventorySnapshotProjection;

public class InventorySnapshotReplayService implements IInventorySnapshotReplayService {
	
	private final IEconomyEventStore store;
	
	public InventorySnapshotReplayService(IEconomyEventStore store) {
		this.store = store;
	}
	
	@Override
	public InventorySnapshot rebuildSnapshot(int playerId, long untilTimestamp) {
		var projection = new InventorySnapshotProjection();
		store.loadByPlayerUntil(playerId, untilTimestamp).forEach(projection::apply);
		
		return projection.snapshot(playerId, untilTimestamp);
	}
	
}
