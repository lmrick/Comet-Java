package com.cometproject.api.game.catalog.purchase.economy.security;

import com.cometproject.api.game.catalog.purchase.economy.IEconomyEventStore;
import com.cometproject.api.utilities.events.catalog.args.ItemCreatedArgs;
import java.util.HashSet;

public class DuplicateItemDetector implements IDuplicateItemDetector {
	
	private final IEconomyEventStore store;
	
	public DuplicateItemDetector(IEconomyEventStore store) {
		this.store = store;
	}
	
	@Override
	public boolean hasDuplication(int playerId) {
		var seen = new HashSet<Long>();
		
		return store.loadByPlayer(playerId).stream()
						.filter(ItemCreatedArgs.class::isInstance)
						.map(ItemCreatedArgs.class::cast)
						.anyMatch(created -> !seen.add(created.getItemId()));
	}
	
}
