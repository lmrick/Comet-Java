package com.cometproject.server.game.rooms.objects.items.types;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.state.FloorItemEvent;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.utilities.collections.ConcurrentHashSet;
import java.util.Set;

public abstract class AdvancedFloorItem<T extends FloorItemEvent> extends RoomItemFloor {
	private static final int MAX_ITEM_EVENTS = 5000;
	private final Set<T> itemEvents = new ConcurrentHashSet<T>();
	
	public AdvancedFloorItem(RoomItemData itemData, Room room) {
		super(itemData, room);
	}

	public abstract void onEventComplete(T event);
	
	@Override
	public void onTick() {
		final Set<T> finishedEvents = new ConcurrentHashSet<>();
		
		itemEvents.forEach(itemEvent -> {
			Comet.getServer().getLogger().debug("{} incrementing tick", this.getId());
			itemEvent.incrementTicks();
			if (itemEvent.isFinished()) {
				Comet.getServer().getLogger().debug("{} event finished", this.getId());
				
				finishedEvents.add(itemEvent);
			}
		});
		
		finishedEvents.forEach(finishedEvent -> {
			this.itemEvents.remove(finishedEvent);
			finishedEvent.onCompletion(this);
			if (finishedEvent.isInteractiveEvent()) {
				Comet.getServer().getLogger().debug("{} calling onComplete", this.getId());
				
				this.onEventComplete(finishedEvent);
			}
		});
		
		finishedEvents.clear();
	}
	
	public T getNextEvent() {
		if (this.itemEvents.isEmpty()) {
			return null;
		}
		
		return this.itemEvents.iterator().next();
	}
	
	public void queueEvent(final T floorItemEvent) {
		if (this.getMaxEvents() <= this.itemEvents.size()) {
			return;
		}
		
		this.itemEvents.add(floorItemEvent);
	}
	
	public int getMaxEvents() {
		return MAX_ITEM_EVENTS;
	}
	
}
