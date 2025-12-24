package com.cometproject.game.items.inventory.items;

import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.furniture.types.IFurnitureDefinition;
import com.cometproject.api.game.groups.types.IGroupData;
import com.cometproject.api.game.players.components.types.inventory.InventoryItemData;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.game.items.inventory.InventoryItem;
import org.apache.commons.lang.StringUtils;

public class GroupInventoryItem extends InventoryItem {
    public GroupInventoryItem(InventoryItemData inventoryItemData, IFurnitureDefinition furnitureDefinition) {
        super(inventoryItemData, furnitureDefinition);
    }

    @Override
    public boolean composeData(IComposerDataWrapper msg) {
        int groupId = 0;

        msg.writeInt(17);

        if (StringUtils.isNumeric(this.getExtraData())) {
            groupId = Integer.parseInt(this.getExtraData());
        }

        IGroupData groupData = groupId == 0 ? null : GameContext.getCurrent().getGroupService().getData(groupId);

        if (groupData == null) {
            msg.writeInt(2);
            msg.writeInt(0);
        } else {
            msg.writeInt(2);
            msg.writeInt(5);
            msg.writeString("0"); //state
            msg.writeString(groupId);
            msg.writeString(groupData.getBadge());

            String colourA = GameContext.getCurrent().getGroupService().getItemService().getSymbolColours().get(groupData.getColourA()) != null ? GameContext.getCurrent().getGroupService().getItemService().getSymbolColours().get(groupData.getColourA()).getFirstValue() : "ffffff";
            String colourB = GameContext.getCurrent().getGroupService().getItemService().getBackgroundColours().get(groupData.getColourB()) != null ?  GameContext.getCurrent().getGroupService().getItemService().getBackgroundColours().get(groupData.getColourB()).getFirstValue() : "ffffff";

            msg.writeString(colourA);
            msg.writeString(colourB);
        }

        return false;
    }
}
