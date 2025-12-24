package com.cometproject.api.game.catalog.purchase.economy.inventory.services;

import com.cometproject.api.game.catalog.purchase.economy.IEconomyEventStore;
import com.cometproject.api.game.catalog.purchase.economy.inventory.projection.InventoryProjection;
import java.util.Set;

public class InventoryReplayService implements IInventoryReplayService {
	private final IEconomyEventStore store;
	
	public InventoryReplayService(IEconomyEventStore store) {
		this.store = store;
	}
	
	@Override
	public Set<Long> rebuildInventory(int playerId, long untilTimestamp) {
		var projection = new InventoryProjection();
		store.loadByPlayerUntil(playerId, untilTimestamp).forEach(projection::apply);
		
		return projection.snapshot();
	}
	
}
