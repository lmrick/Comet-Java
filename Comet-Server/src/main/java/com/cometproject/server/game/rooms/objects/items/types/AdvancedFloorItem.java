package com.cometproject.server.game.rooms.objects.items.types;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.state.FloorItemEvent;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.utilities.collections.ConcurrentHashSet;

import java.util.HashSet;
import java.util.Set;

public abstract class AdvancedFloorItem<T extends FloorItemEvent> extends RoomItemFloor {
	
	private final Set<T> itemEvents = new ConcurrentHashSet<T>();
	
	public AdvancedFloorItem(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public void onTick() {
		final Set<T> finishedEvents = new HashSet<T>();
		
		itemEvents.forEach(itemEvent -> {
			Comet.getServer().getLogger().debug(this.getId() + " incrementing tick");
			itemEvent.incrementTicks();
			if (itemEvent.isFinished()) {
				Comet.getServer().getLogger().debug(this.getId() + " event finished");
				
				finishedEvents.add(itemEvent);
			}
		});
		
		finishedEvents.forEach(finishedEvent -> {
			this.itemEvents.remove(finishedEvent);
			finishedEvent.onCompletion(this);
			if (finishedEvent.isInteractiveEvent()) {
				Comet.getServer().getLogger().debug(this.getId() + " calling onComplete");
				
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
	
	public abstract void onEventComplete(T event);
	
	public int getMaxEvents() {
		return 5000;
	}
	
}
