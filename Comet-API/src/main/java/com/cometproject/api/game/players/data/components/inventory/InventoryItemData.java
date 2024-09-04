package com.cometproject.api.game.players.data.components.inventory;

import com.cometproject.api.game.furniture.types.ILimitedEditionItem;

public record InventoryItemData(long id, int baseId, String extraData, ILimitedEditionItem limitedEditionItem) {

}
