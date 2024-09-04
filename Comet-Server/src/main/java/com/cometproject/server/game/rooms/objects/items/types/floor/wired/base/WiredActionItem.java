package com.cometproject.server.game.rooms.objects.items.types.floor.wired.base;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFactory;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.WiredFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.data.WiredActionItemData;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.events.WiredItemEvent;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.network.messages.outgoing.room.items.wired.dialog.WiredActionMessageComposer;
import com.cometproject.server.protocol.messages.MessageComposer;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

public abstract class WiredActionItem extends WiredFloorItem {
	
	public WiredActionItem(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public MessageComposer getDialog() {
		return new WiredActionMessageComposer(this);
	}
	
	@Override
	public final boolean evaluate(RoomEntity entity, Object data) {
		if (this.hasTicks()) return false;
		
		final WiredItemEvent itemEvent = new WiredItemEvent(entity, data);
		
		if (this.getWiredData().getDelay() >= 1 && this.usesDelay()) {
			itemEvent.setTotalTicks(RoomItemFactory.getProcessTime(this.getWiredData().getDelay() / 2F));
			
			this.queueEvent(itemEvent);
		} else {
			itemEvent.onCompletion(this);
			this.onEventComplete(itemEvent);
		}
		
		return true;
	}
	
	@Override
	public WiredActionItemData getWiredData() {
		return (WiredActionItemData) super.getWiredData();
	}
	
	public List<WiredTriggerItem> getIncompatibleTriggers() {
		List<WiredTriggerItem> incompatibleTriggers = Lists.newArrayList();
		
		if (this.requiresPlayer()) {
			incompatibleTriggers = this.getItemsOnStack().stream()
              .filter(WiredTriggerItem.class::isInstance)
              .filter(floorItem -> !((WiredTriggerItem) floorItem).suppliesPlayer())
              .map(WiredTriggerItem.class::cast)
              .collect(Collectors.toList());
		}
		
		return incompatibleTriggers;
	}
	
	public abstract boolean requiresPlayer();
	
}
