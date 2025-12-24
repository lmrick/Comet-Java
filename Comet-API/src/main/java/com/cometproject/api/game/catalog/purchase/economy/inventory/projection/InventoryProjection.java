package com.cometproject.api.game.catalog.purchase.economy.inventory.projection;

import com.cometproject.api.utilities.events.catalog.IEconomyEvent;
import com.cometproject.api.utilities.events.catalog.args.ItemCreatedArgs;
import com.cometproject.api.utilities.events.catalog.args.ItemRemovedArgs;
import java.util.HashSet;
import java.util.Set;

public class InventoryProjection implements IInventoryProjection {
	private final Set<Long> itemIds = new HashSet<>();
	
	@Override
	public void apply(IEconomyEvent event) {
		
		if (event instanceof ItemCreatedArgs e) {
			itemIds.add(e.getItemId());
		}
		
		if (event instanceof ItemRemovedArgs e) {
			itemIds.remove(e.getItemId());
		}
	}
	
	@Override
	public Set<Long> snapshot() {
		return Set.copyOf(itemIds);
	}
	
}
