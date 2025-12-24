package com.cometproject.api.game.catalog.purchase.economy.inventory.projection;

import com.cometproject.api.utilities.events.catalog.IEconomyEvent;
import java.util.Set;

public interface IInventoryProjection {
	
	void apply(IEconomyEvent event);
	Set<Long> snapshot();
	
}
