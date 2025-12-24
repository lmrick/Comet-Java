package com.cometproject.game.items.inventory.items;

import com.cometproject.api.game.furniture.types.IFurnitureDefinition;
import com.cometproject.api.game.furniture.types.gift.GiftData;
import com.cometproject.api.game.furniture.types.gift.LegacyGiftData;
import com.cometproject.api.game.players.components.types.inventory.InventoryItemData;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.api.utilities.JsonUtil;
import com.cometproject.game.items.inventory.InventoryItem;

public class GiftInventoryItem extends InventoryItem {

    private final GiftData giftData;

    public GiftInventoryItem(InventoryItemData inventoryItemData, IFurnitureDefinition furnitureDefinition) {
        super(inventoryItemData, furnitureDefinition);

        this.giftData = this.getGiftData(inventoryItemData.extraData());
    }

    private GiftData getGiftData(String data) {

        try {
            if (data.startsWith(GiftData.EXTRA_DATA_HEADER)) {
                return JsonUtil.getInstance().fromJson(data.split(GiftData.EXTRA_DATA_HEADER)[1], GiftData.class);
            } else if (data.startsWith(LegacyGiftData.EXTRA_DATA_HEADER)) {
                return JsonUtil.getInstance().fromJson(data.split(LegacyGiftData.EXTRA_DATA_HEADER)[1], LegacyGiftData.class);
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean composeData(IComposerDataWrapper msg) {
        super.composeData(msg);

        msg.writeString("");
        return false;
    }

    @Override
    public int getExtraInt() {
        if(this.giftData == null) {
            return 0;
        }

        return this.giftData.getWrappingPaper() * 1000 + this.giftData.getDecorationType();
    }

}
