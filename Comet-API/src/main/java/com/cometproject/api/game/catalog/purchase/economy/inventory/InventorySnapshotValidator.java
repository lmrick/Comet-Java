package com.cometproject.api.game.catalog.purchase.economy.inventory;

import com.cometproject.api.game.players.components.types.inventory.IPlayerItemSnapshot;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class InventorySnapshotValidator {
	
	public InventoryDiff diff(
					InventorySnapshot current,
					InventorySnapshot replayed
	) {
		
		Set<Long> currentIds =
						current.items().stream()
										.map(IPlayerItemSnapshot::id)
										.collect(Collectors.toSet());
		
		Set<Long> replayedIds =
						replayed.items().stream()
										.map(IPlayerItemSnapshot::id)
										.collect(Collectors.toSet());
		
		Set<Long> extra = new HashSet<>(currentIds);
		extra.removeAll(replayedIds);
		
		Set<Long> missing = new HashSet<>(replayedIds);
		missing.removeAll(currentIds);
		
		return new InventoryDiff(extra, missing);
	}
	
}
