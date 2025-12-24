package com.cometproject.server.game.rooms.objects.items.types.floor.wired;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.api.utilities.JsonUtil;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.AdvancedFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredActionItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.data.WiredActionItemData;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.data.WiredItemData;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.events.WiredItemEvent;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.events.WiredItemFlashEvent;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.protocol.messages.MessageComposer;
import com.cometproject.server.utilities.attributes.Stateable;
import com.google.common.collect.Lists;
import java.util.List;

public abstract class WiredFloorItem extends AdvancedFloorItem<WiredItemEvent> implements WiredItemSnapshot.Refreshable, Stateable {

    private WiredItemData wiredItemData = null;
    private boolean state;
    private final boolean hasTicked = false;
    private boolean isFlashing = false;

    public WiredFloorItem(RoomItemData itemData, Room room) {
        super(itemData, room);

        if (!this.getItemData().getData().startsWith("{")) {
            this.getItemData().setData("{}");
        }

        this.load();
    }

    @Override
    public void save() {
        this.getItemData().setData(JsonUtil.getInstance().toJson(wiredItemData));

        super.save();
    }
    public void load() {
        try {
            if (this.getItemData().getData().equals("{}")) {
                this.wiredItemData = (this instanceof WiredActionItem) ? new WiredActionItemData() : new WiredItemData();
                return;
            }

            this.wiredItemData = JsonUtil.getInstance().fromJson(this.getItemData().getData(), (this instanceof WiredActionItem) ? WiredActionItemData.class : WiredItemData.class);
            this.onDataRefresh();
        } catch (Exception e) {
            Comet.getServer().getLogger().warn("Invalid wired item data " + this.getItemData().getData() + " ID: " + this.getId());
        }
    }

    @Override
    public void onPickup() {
        this.getItemData().setData("{}");
    }

    @Override
    public boolean onInteract(RoomEntity entity, int requestData, boolean isWiredTrigger) {
        if (!(entity instanceof PlayerEntity playerEntity)) {
            return true;
        }
			
			if (!this.getRoom().getRights().hasRights(playerEntity.getPlayerId()) && !playerEntity.getPlayer().getPermissions().getRank().roomFullControl()) {
            return true;
        }

        this.flash();
        ((PlayerEntity) entity).getPlayer().getSession().send(this.getDialog());
        return true;
    }

    @Override
    public void refreshSnapshots() {
        List<Long> toRemove = Lists.newArrayList();
        this.getWiredData().getSnapshots().clear();
			
			this.getWiredData().getSelectedIds().forEach(itemId -> {
				RoomItemFloor floorItem = this.getRoom().getItems().getFloorItem(itemId);
				if (floorItem == null) {
					toRemove.add(itemId);
					return;
				}
				this.getWiredData().getSnapshots().put(itemId, new WiredItemSnapshot(floorItem));
			});
			
			toRemove.forEach(itemToRemove -> this.getWiredData().getSelectedIds().remove(itemToRemove));

        this.save();
    }

    public void flash() {
        if (this.isFlashing) {
            return;
        }

        this.switchState();

        this.isFlashing = true;

        final WiredItemFlashEvent flashEvent = new WiredItemFlashEvent();
        this.queueEvent(flashEvent);
    }

    public void switchState() {
        if (this.isFlashing) {
            this.isFlashing = false;
        }

        this.state = !this.state;
        this.sendUpdate();
    }

    @Override
    public void onTick() {
        super.onTick();

        // any wired-only ontick stuff here?
    }

    @Override
    public void onEventComplete(WiredItemEvent event) {

    }

    public WiredItemData getWiredData() {
        return wiredItemData;
    }

    public abstract int getInterface();
    public abstract MessageComposer getDialog();
    public abstract boolean evaluate(RoomEntity entity, Object data);

    public void onDataRefresh() {

    }

    public void onDataChange() {

    }

    public boolean usesDelay() {
        return true;
    }

    public int getFurniSelection() {
        return WiredUtil.MAX_FURNI_SELECTION;
    }

    @Override
    public boolean getState() {
        return this.state;
    }
    
}
