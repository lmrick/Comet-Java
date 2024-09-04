package com.cometproject.server.game.rooms.objects.items.types.floor.wired.actions;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.DiceFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredActionItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.events.WiredItemEvent;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.triggers.WiredTriggerCollision;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.mapping.RoomTile;
import com.cometproject.server.network.messages.outgoing.room.items.SlideObjectBundleMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.items.UpdateFloorItemMessageComposer;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class WiredActionMoveRotate extends WiredActionItem {
	
	private static final int PARAM_MOVEMENT = 0;
	private static final int PARAM_ROTATION = 1;
	
	private final Random random = new Random();
	
	public WiredActionMoveRotate(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public boolean requiresPlayer() {
		return false;
	}
	
	@Override
	public int getInterface() {
		return 4;
	}
	
	@Override
	public void onEventComplete(WiredItemEvent event) {
		if (this.getWiredData().getParams().size() != 2) {
			return;
		}
		
		final int movement = this.getWiredData().getParams().get(PARAM_MOVEMENT);
		final int rotation = this.getWiredData().getParams().get(PARAM_ROTATION);
		
		synchronized (this.getWiredData().getSelectedIds()) {
			this.getWiredData().getSelectedIds().stream().mapToLong(itemId -> itemId).mapToObj(itemId -> this.getRoom().getItems().getFloorItem(itemId)).filter(floorItem -> floorItem != null && !(floorItem instanceof DiceFloorItem)).forEachOrdered(floorItem -> {
				final Position currentPosition = floorItem.getPosition().copy();
				final Position newPosition = this.handleMovement(currentPosition.copy(), movement);
				final int newRotation = this.handleRotation(floorItem.getRotation(), rotation);
				final boolean rotationChanged = newRotation != floorItem.getRotation();
				Arrays.stream(Position.COLLIDE_TILES).mapToObj(collisionDirection -> floorItem.getPosition().squareInFront(collisionDirection)).map(collisionPosition -> this.getRoom().getMapping().getTile(collisionPosition)).filter(Objects::nonNull).map(RoomTile::getEntity).filter(Objects::nonNull).forEachOrdered(entity -> WiredTriggerCollision.executeTriggers(entity, floorItem));
				if (this.getRoom().getItems().moveFloorItem(floorItem.getId(), newPosition, newRotation, true)) {
					this.getRoom().getEntities().broadcastMessage(!rotationChanged ? new SlideObjectBundleMessageComposer(currentPosition, newPosition, 0, 0, floorItem.getVirtualId()) : new UpdateFloorItemMessageComposer(floorItem));
				}
				floorItem.save();
			});
		}
	}
	
	private Position handleMovement(Position point, int movementType) {
		final boolean dir = Math.random() < 0.5;
		
		switch (movementType) {
			case 0 -> {
				return point;
			}
			
			case 1 -> {
				// Random
				int movement = random.nextInt((4 - 1) + 1 + 1);
				
				if (movement == 1) {
					point = handleMovement(point, 4);
				} else if (movement == 2) {
					point = handleMovement(point, 5);
				} else if (movement == 3) {
					point = handleMovement(point, 6);
				} else {
					point = handleMovement(point, 7);
				}
			}
			case 2 -> point = dir ? handleMovement(point, 7) : handleMovement(point, 5);
			case 3 -> point = dir ? handleMovement(point, 4) : handleMovement(point, 6);
			case 4 -> point.setY(point.getY() - 1);
			case 5 -> point.setX(point.getX() + 1);
			case 6 -> point.setY(point.getY() + 1);
			case 7 -> point.setX(point.getX() - 1);
		}
		
		return point;
	}
	
	private int handleRotation(int rotation, int rotationType) {
		switch (rotationType) {
			case 0 -> {
				return rotation;
			}
			case 1 -> {
				rotation = rotation + 2;
				if (rotation > 6) rotation = 0;
			}
			case 2 -> {
				rotation = rotation - 2;
				if (rotation > 6) rotation = 6;
			}
			case 3 -> {
				int i = random.nextInt((2 - 1) + 1 + 1);
				rotation = i == 1 ? handleRotation(rotation, 1) : handleRotation(rotation, 2);
			}
		}
		
		return rotation;
	}
	
}
