package com.cometproject.api.game.catalog.purchase.economy.inventory;

import com.cometproject.api.game.players.components.types.inventory.IPlayerItemSnapshot;
import java.util.List;

public record InventorySnapshot(
				int playerId,
				List<IPlayerItemSnapshot> items,
				long timestamp
) {}
