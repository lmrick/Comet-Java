package com.cometproject.api.game.players.data.components.inventory;

public interface IPlayerItemFactory {

    IPlayerItem createItem(InventoryItemData itemData);

}
