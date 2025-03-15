package com.cometproject.server.game.rooms.objects.entities;

import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredTriggerItem;
import com.cometproject.server.tasks.ICometTask;

public class WiredTriggerExecutor<T extends WiredTriggerItem> implements ICometTask {
	private final RoomEntity roomEntity;
	private final Class<? extends WiredTriggerItem> triggerClass;
	private final Object data;
	
	public WiredTriggerExecutor(Class<T> triggerClass, RoomEntity entity, Object data) {
		this.roomEntity = entity;
		this.data = data;
		this.triggerClass = triggerClass;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		WiredTriggerItem.getTriggers(this.roomEntity.getRoom(), this.triggerClass).stream()
		.map(wiredItem -> ((T) wiredItem))
		.filter(trigger -> trigger.getWiredData().getSelectedIds().contains(((RoomItemFloor) data).getId()))
		.forEachOrdered(trigger -> trigger.evaluate(this.roomEntity, data));
	}
	
}
