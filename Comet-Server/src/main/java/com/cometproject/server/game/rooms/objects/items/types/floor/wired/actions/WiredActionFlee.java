package com.cometproject.server.game.rooms.objects.items.types.floor.wired.actions;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.Square;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.types.ItemPathfinder;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredActionItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.events.WiredItemEvent;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.triggers.WiredTriggerCollision;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.mapping.RoomTile;
import com.cometproject.server.network.messages.outgoing.room.items.SlideObjectBundleMessageComposer;
import com.cometproject.api.game.utilities.RandomUtil;
import com.google.common.collect.Maps;

import java.util.*;

public class WiredActionFlee extends WiredActionItem {
	
	public WiredActionFlee(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public boolean requiresPlayer() {
		return false;
	}
	
	@Override
	public int getInterface() {
		return 12;
	}
	
	@Override
	public void onEventComplete(WiredItemEvent event) {
		if (getWiredData().getSelectedIds().isEmpty()) {
			return;
		}
		
		getWiredData().getSelectedIds().forEach(itemId -> {
			RoomItemFloor floorItem = getRoom().getItems().getFloorItem(itemId);
			if (floorItem == null) {
				getWiredData().getSelectedIds().remove(itemId);
			} else {
				PlayerEntity nearestEntity = floorItem.nearestPlayerEntity();
				Position positionFrom = floorItem.getPosition().copy();
				
				if (nearestEntity != null && nearestEntity.getPosition() != null) {
					Position newCoordinate = floorItem.getPosition().squareBehind(Position.calculateRotation(floorItem.getPosition().getX(), floorItem.getPosition().getY(), nearestEntity.getPosition().getX(), nearestEntity.getPosition().getY(), false));
					
					List<Square> tilesToEntity = ItemPathfinder.getInstance().makePath(floorItem, newCoordinate, (byte) 0, false);
					if ((tilesToEntity != null) && (!tilesToEntity.isEmpty())) {
						Position positionTo = new Position(tilesToEntity.getFirst().x, tilesToEntity.getFirst().y);
						moveToTile(floorItem, positionFrom, positionTo);
						tilesToEntity.clear();
					} else {
						moveToTile(floorItem, positionFrom, null);
					}
				} else {
					moveToTile(floorItem, positionFrom, null);
				}
			}
		});
	}
	
	public boolean isCollided(PlayerEntity entity, RoomItemFloor floorItem) {
		boolean tilesTouching = entity.getPosition().touching(floorItem.getPosition());
		
		if (tilesTouching) {
			boolean xMatches = entity.getPosition().getX() == floorItem.getPosition().getX();
			boolean yMatches = entity.getPosition().getY() == floorItem.getPosition().getY();
			
			return !((!xMatches) && (!yMatches));
			
		}
		
		return false;
	}
	
	private void moveToTile(RoomItemFloor floorItem, Position from, Position to) {
		if (from == null) {
			return;
		}
		if (to == null) {
			for (int i = 0; i < 16; i++) {
				if (to != null) break;
				to = random(floorItem, from);
			}
			
			if (to == null) {
				return;
			}
		}
		
		if (getRoom().getItems().moveFloorItem(floorItem.getId(), to, floorItem.getRotation(), true)) {
			final Map<Integer, Double> items = Maps.newHashMap();
			
			items.put(floorItem.getVirtualId(), floorItem.getPosition().getZ());
			
			getRoom().getEntities().broadcastMessage(new SlideObjectBundleMessageComposer(from, to, 0, 0, items));
		}
		
		
		Arrays.stream(Position.COLLIDE_TILES).mapToObj(collisionDirection -> floorItem.getPosition().squareInFront(collisionDirection)).map(collisionPosition -> this.getRoom().getMapping().getTile(collisionPosition)).filter(Objects::nonNull).map(RoomTile::getEntity).filter(Objects::nonNull).forEachOrdered(entity -> {
			floorItem.setCollision(entity);
			WiredTriggerCollision.executeTriggers(entity, floorItem);
		});
		
		
		floorItem.nullifyCollision();
	}
	
	private Position random(RoomItemFloor floorItem, Position from) {
		int randomDirection = RandomUtil.getRandomInt(0, 3) * 2;
		Position newPosition = from.squareBehind(randomDirection);
		RoomTile tile = floorItem.getRoom().getMapping().getTile(newPosition.getX(), newPosition.getY());
		
		if ((tile != null) && (tile.isReachable(floorItem))) {
			return newPosition;
		}
		
		return null;
	}
	
}
