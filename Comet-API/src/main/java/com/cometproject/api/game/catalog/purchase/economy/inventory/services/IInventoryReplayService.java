package com.cometproject.api.game.catalog.purchase.economy.inventory.services;

import java.util.Set;

public interface IInventoryReplayService {
	
	Set<Long> rebuildInventory(int playerId, long untilTimestamp);
	
}
