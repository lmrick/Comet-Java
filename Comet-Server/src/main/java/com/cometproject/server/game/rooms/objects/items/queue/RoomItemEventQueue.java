package com.cometproject.server.game.rooms.objects.items.queue;

import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.RoomItemWall;

import java.util.LinkedList;
import java.util.List;

public class RoomItemEventQueue {
	
	private final List<RoomItemEventQueueEntry> eventQueue = new LinkedList<>();
	private final Object lock = new Object();
	
	public void cycle() {
		if (this.eventQueue.isEmpty()) {
			return;
		}
		
		final List<RoomItemEventQueueEntry> eventQueueCopy;
		
		synchronized (this.lock) {
			eventQueueCopy = new LinkedList<>(this.eventQueue);
			
			this.eventQueue.clear();
		}
		
		for (RoomItemEventQueueEntry e : eventQueueCopy) {
			if (e.getItem() instanceof RoomItemWall wall) {
				
				switch (e.getType()) {
					case Pickup -> wall.onPickup();
					case PreStepOn -> {
					}
					case StepOn -> {
					}
					case StepOff -> {
					}
					case Placed -> {
						wall.onPlaced();
						return;
					}
					case Interact -> wall.onInteract(e.getEntity(), e.getRequestData(), e.isWiredTrigger());
				}
			} else if (e.getItem() instanceof RoomItemFloor floor) {
				
				switch (e.getType()) {
					case Pickup -> floor.onPickup();
					case Placed -> {
						floor.onPlaced();
						return;
					}
					case Interact -> floor.onInteract(e.getEntity(), e.getRequestData(), e.isWiredTrigger());
					case PreStepOn -> floor.onEntityPreStepOn(e.getEntity());
					case StepOn -> floor.onEntityStepOn(e.getEntity());
					case StepOff -> floor.onEntityStepOff(e.getEntity());
				}
			}
		}
		
		eventQueueCopy.clear();
	}
	
	public void queue(RoomItemEventQueueEntry entry) {
		synchronized (this.lock) {
			this.eventQueue.add(entry);
		}
	}
	
}
