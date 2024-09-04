package com.cometproject.api.game.players.data.components.inventory;

import com.cometproject.api.game.furniture.types.LimitedEditionItem;

public record InventoryItemData(long id, int baseId, String extraData, LimitedEditionItem limitedEditionItem) {

}
