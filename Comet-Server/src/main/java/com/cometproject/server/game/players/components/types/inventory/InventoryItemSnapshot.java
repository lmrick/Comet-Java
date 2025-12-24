package com.cometproject.server.game.players.components.types.inventory;

import com.cometproject.api.game.players.components.types.inventory.IPlayerItemSnapshot;

public record InventoryItemSnapshot(long id, int baseItemId, String extraData) implements IPlayerItemSnapshot {

}
