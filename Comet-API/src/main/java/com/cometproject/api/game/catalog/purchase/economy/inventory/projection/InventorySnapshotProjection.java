package com.cometproject.api.game.catalog.purchase.economy.inventory.projection;

import com.cometproject.api.game.catalog.purchase.economy.inventory.InventorySnapshot;
import com.cometproject.api.game.players.components.types.inventory.IPlayerItemSnapshot;
import com.cometproject.api.game.players.components.types.inventory.InventoryItemData;
import com.cometproject.api.utilities.events.catalog.IEconomyEvent;
import com.cometproject.api.utilities.events.catalog.args.ItemCreatedArgs;
import com.cometproject.api.utilities.events.catalog.args.ItemRemovedArgs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InventorySnapshotProjection implements IInventorySnapshotProjection {
	private final Map<Long, IPlayerItemSnapshot> items = new HashMap<>();
	
	@Override
	public void apply(IEconomyEvent event) {
		
		if (event instanceof ItemCreatedArgs e) {
			
			items.put(e.getItemId(), new InventoryItemData(e.getItemId(), e.getBaseItemId(), null, null));
		}
		
		if (event instanceof ItemRemovedArgs e) {
			items.remove(e.getItemId());
		}
	}
	
	@Override
	public InventorySnapshot snapshot(int playerId, long timestamp) {
		return new InventorySnapshot(playerId, new ArrayList<>(items.values()), timestamp);
	}
	
}
