package com.cometproject.api.game.catalog.purchase.economy.inventory.rollback;

import com.cometproject.api.game.catalog.purchase.economy.inventory.InventorySnapshot;

public class InventoryRollbackService implements IInventoryRollbackService {
	
	private final IInventoryRepository inventoryRepository;
	
	public InventoryRollbackService(IInventoryRepository inventoryRepository) {
	
	}
	
	@Override
	public void rollback(int playerId, InventorySnapshot snapshot) {
		inventoryRepository.replaceInventory(playerId, snapshot.items());
	}
	
}
