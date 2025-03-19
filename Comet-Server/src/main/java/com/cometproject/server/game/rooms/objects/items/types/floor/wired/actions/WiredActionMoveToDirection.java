package com.cometproject.server.game.rooms.objects.items.types.floor.wired.actions;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredActionItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.events.WiredItemEvent;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.triggers.WiredTriggerCollision;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.mapping.RoomTile;
import com.cometproject.server.network.messages.outgoing.room.items.SlideObjectBundleMessageComposer;
import com.cometproject.api.game.utilities.Direction;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class WiredActionMoveToDirection extends WiredActionItem {
	
	private static final int PARAM_START_DIR = 0;
	private static final int PARAM_ACTION_WHEN_BLOCKED = 1;
	private static final int ACTION_WAIT = 0;
	private static final int ACTION_TURN_RIGHT_45 = 1;
	private static final int ACTION_TURN_RIGHT_90 = 2;
	private static final int ACTION_TURN_LEFT_45 = 3;
	private static final int ACTION_TURN_LEFT_90 = 4;
	private static final int ACTION_TURN_BACK = 5;
	private static final int ACTION_TURN_RANDOM = 6;
	
	public WiredActionMoveToDirection(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public boolean requiresPlayer() {
		return false;
	}
	
	@Override
	public int getInterface() {
		return 13;
	}
	
	@Override
	public void onEventComplete(WiredItemEvent event) {
		if (this.getWiredData().getParams().size() != 2) {
			return;
		}
		
		final int startDir = this.getWiredData().getParams().get(PARAM_START_DIR);
		
		synchronized (this.getWiredData().getSelectedIds()) {
			this.getWiredData().getSelectedIds().stream()
			.mapToLong(itemId -> itemId)
			.mapToObj(itemId -> this.getRoom().getItems().getFloorItem(itemId))
			.filter(Objects::nonNull).forEachOrdered(floorItem -> {
				if (floorItem.getMoveDirection() == -1) {
					floorItem.setMoveDirection(startDir);
				}
				this.moveItem(floorItem, new AtomicInteger(0));
			});
		}
		
		return;
	}
	
	private void moveItem(RoomItemFloor floorItem, AtomicInteger tries) {
		final Position currentPosition = floorItem.getPosition().copy();
		final Position nextPosition = floorItem.getPosition().squareInFront(floorItem.getMoveDirection());
		
		if (this.getRoom().getItems().moveFloorItem(floorItem.getId(), floorItem.getPosition().squareInFront(floorItem.getMoveDirection()), floorItem.getRotation(), true)) {
			nextPosition.setZ(floorItem.getPosition().getZ());
			this.getRoom().getEntities().broadcastMessage(new SlideObjectBundleMessageComposer(currentPosition, nextPosition, this.getVirtualId(), 0, floorItem.getVirtualId()));
		} else {
			tries.incrementAndGet();
			
			if (tries.get() < 4) this.attemptBlockedAction(floorItem, tries);
		}
	}
	
	private void attemptBlockedAction(RoomItemFloor floorItem, AtomicInteger tries) {
		final int actionWhenBlocked = this.getWiredData().getParams().get(PARAM_ACTION_WHEN_BLOCKED);
		
		if (actionWhenBlocked == 0) {
			return;
		}
		
		int movementDirection = floorItem.getMoveDirection();
		final Position position = floorItem.getPosition().squareInFront(floorItem.getMoveDirection());
		final RoomTile roomTile = this.getRoom().getMapping().getTile(position);
		
		if (roomTile != null) {
			if (roomTile.getEntity() != null) {
				WiredTriggerCollision.executeTriggers(roomTile.getEntity(), floorItem);
				return;
			}
		}
		
		movementDirection = switch (actionWhenBlocked) {
			case ACTION_TURN_BACK -> Direction.get(movementDirection).invert().num;
			case ACTION_TURN_RANDOM -> Direction.random().num;
			case ACTION_TURN_RIGHT_45 -> this.getNextDirection(movementDirection);
			case ACTION_TURN_RIGHT_90 -> this.clockwise(movementDirection, 2);
			case ACTION_TURN_LEFT_45 -> this.getPreviousDirection(movementDirection);
			case ACTION_TURN_LEFT_90 -> this.antiClockwise(movementDirection, 2);
			default -> movementDirection;
		};
		
		floorItem.setMoveDirection(movementDirection);
		this.moveItem(floorItem, tries);
	}
	
	private int clockwise(int movementDirection, int times) {
		for (int i = 0; i < times; i++) {
			movementDirection = this.getNextDirection(movementDirection);
		}
		
		return movementDirection;
	}
	
	private int antiClockwise(int movementDirection, int times) {
		for (int i = 0; i < times; i++) {
			movementDirection = this.getPreviousDirection(movementDirection);
		}
		
		return movementDirection;
	}
	
	private int getNextDirection(int movementDirection) {
		if (movementDirection == 7) {
			return 0;
		}
		
		return movementDirection + 1;
	}
	
	private int getPreviousDirection(int movementDirection) {
		if (movementDirection == 0) {
			return 7;
		}
		
		return movementDirection - 1;
	}
	
}
