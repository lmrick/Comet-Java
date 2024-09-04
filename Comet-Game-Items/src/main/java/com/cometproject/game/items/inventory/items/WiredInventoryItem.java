package com.cometproject.game.items.inventory.items;

import com.cometproject.api.game.furniture.types.IFurnitureDefinition;
import com.cometproject.api.game.players.data.components.inventory.InventoryItemData;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.game.items.inventory.InventoryItem;

public class WiredInventoryItem extends InventoryItem {
    public WiredInventoryItem(InventoryItemData inventoryItemData, IFurnitureDefinition furnitureDefinition) {
        super(inventoryItemData, furnitureDefinition);
    }

    @Override
    public boolean composeData(IComposerDataWrapper msg) {
        super.composeData(msg);

        msg.writeString("");
        return false;
    }

}
