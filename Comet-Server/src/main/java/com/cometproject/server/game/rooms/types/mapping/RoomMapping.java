package com.cometproject.server.game.rooms.types.mapping;

import com.cometproject.api.game.rooms.models.IRoomModel;
import com.cometproject.api.game.rooms.models.RoomTileState;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.rooms.objects.RoomFloorObject;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.types.PetEntity;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.types.floor.OneWayGateFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.pet.breeding.BreedingBoxFloorItem;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.api.game.utilities.RandomUtil;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RoomMapping {
	private final Room room;
	private RoomTile[][] tiles;
	
	public RoomMapping(Room roomInstance) {
		this.room = roomInstance;
	}
	
	public void init() {
		if (this.getModel() == null) {
			return;
		}
		
		int sizeX = this.getModel().getSizeX();
		int sizeY = this.getModel().getSizeY();
		
		this.tiles = new RoomTile[sizeX][sizeY];
		
		//System.out.print("\n");
		for (int x = 0; x < sizeX; x++) {
			RoomTile[] xArray = new RoomTile[sizeY];
			
			for (int y = 0; y < sizeY; y++) {
				RoomTile instance = new RoomTile(this, new Position(x, y, 0.0D));
				instance.reload();
				
				xArray[y] = instance;
				
				//System.out.printf("[%s, %s] ", x, y);
			}
			
			this.tiles[x] = xArray;
			//System.out.print("\n");
		}
	}
	
	public void dispose() {
		Arrays.stream(tiles).forEachOrdered(roomTiles -> Arrays.stream(roomTiles).filter(Objects::nonNull).forEachOrdered(RoomTile::dispose));
	}
	
	public void tick() {
		// clear out the entity grid
		for (RoomTile[] roomTiles : tiles) {
			for (RoomTile roomTile : roomTiles) {
				List<RoomEntity> entitiesToRemove = new ArrayList<>();
				
				try {
					
					entitiesToRemove = roomTile.getEntities().stream().filter(PlayerEntity.class::isInstance).filter(entity -> ((PlayerEntity) entity).getPlayer() == null).collect(Collectors.toList());
					
					entitiesToRemove.forEach(entityToRemove -> roomTile.getEntities().remove(entityToRemove));
				} catch (Exception e) {
					// TODO: Look into why this would cause an exception...
				}
				
				entitiesToRemove.clear();
			}
		}
	}
	
	public void updateTile(int x, int y) {
		if (x < 0 || y < 0) {
			return;
		}
		
		if (this.tiles.length > x) {
			if (tiles[x].length > y) this.tiles[x][y].reload();
		}
	}
	
	public RoomTile getTile(Position position) {
		if (position == null) return null;
		
		return this.getTile(position.getX(), position.getY());
	}
	
	public RoomTile getTile(int x, int y) {
		if (x < 0 || y < 0) return null;
		if (x >= this.tiles.length || (this.tiles[x] == null || y >= this.tiles[x].length)) return null;
		
		return this.tiles[x][y];
	}
	
	public RoomTile getRandomReachableTile(RoomFloorObject roomFloorObject) {
		for (int tries = 0; tries < this.getModel().getSizeX() * this.getModel().getSizeY(); tries++) {
			int randomX = RandomUtil.getRandomInt(0, this.getModel().getSizeX() - 1);
			int randomY = RandomUtil.getRandomInt(0, this.getModel().getSizeY() - 1);
			
			final RoomTile tile = this.getTile(randomX, randomY);
			if (tile.isReachable(roomFloorObject)) {
				return tile;
			}
		}
		
		return null;
	}
	
	public boolean positionHasUser(Integer entityId, Position position) {
		boolean hasMountedPet = false;
		int entitySize = 0;
		boolean hasMe = false;
		
		if (entityId == null || entityId == -1) return false;
		
		for (RoomEntity entity : this.room.getEntities().getEntitiesAt(position)) {
			entitySize++;
			
			if (entity.getMountedEntity() != null) {
				if (entity.getMountedEntity().getId() == entityId) {
					return false;
				}
			}
			
			if (entity instanceof PetEntity && entity.getTile().getTopItemInstance() instanceof BreedingBoxFloorItem) {
				return false;
			}
			//
			//            if (entity.hasMount()) {
			//                if(entity.getMountedEntity() != null && entity.getMountedEntity().getId() == entityId) {
			//                    return false;
			//                }
			//            } else if(entity instanceof PlayerEntity) {
			//                IRoomEntity myEntity = this.getRoom().getEntities().getEntity(entityId);
			//
			//                if(myEntity != null) {
			//                    if (myEntity.getMountedEntity() != null && myEntity.getMountedEntity() == entity)) {
			//                        return false;
			//                    }
			//                }
			//
			//            }
			
			// Do we need a null check here? Not sure yet..
			if (entityId != 0 && entity.getId() == entityId) {
				hasMe = true;
			}
		}
		
		return !(hasMe && entitySize == 1) && entitySize > 0;
	}
	
	public boolean canStepUpwards(double height0, double height1) {
		return (height0 - height1) <= 1.5;
	}
	
	public boolean isValidEntityStep(RoomEntity entity, Position currentPosition, Position toPosition, boolean isFinalMove) {
		
		if (entity != null) return isValidStep(entity.getId(), currentPosition, toPosition, isFinalMove, false, true);
		else return isValidStep(0, currentPosition, toPosition, isFinalMove, true, true);
	}
	
	public boolean isValidStep(Position from, Position to, boolean lastStep) {
		return isValidStep(null, from, to, lastStep, false, false);
	}
	
	public boolean isValidStep(Position from, Position to, boolean lastStep, boolean isFloorItem) {
		return isValidStep(null, from, to, lastStep, isFloorItem, false);
	}
	
	public boolean isValidStep(Integer entity, Position from, Position to, boolean lastStep, boolean isFloorItem, boolean isRetry) {
		return isValidStep(entity, from, to, lastStep, isFloorItem, isRetry, false, false);
	}
	
	public boolean isValidStep(Integer entity, Position from, Position to, boolean lastStep, boolean isFloorItem, boolean isRetry, boolean ignoreHeight, boolean isItemOnRoller) {
		if (from.getX() == to.getX() && from.getY() == to.getY()) {
			return true;
		}
		
		if (!(to.getX() < this.getModel().getSquareState().length)) {
			return false;
		}
		
		if (isValidPosition(to) || (this.getModel().getSquareState()[to.getX()][to.getY()] == RoomTileState.INVALID)) {
			return false;
		}
		
		final boolean isAtDoor = this.getModel().getDoorX() == from.getX() && this.getModel().getDoorY() == from.getY();
		
		if (to.getX() == this.getModel().getDoorX() && to.getY() == this.getModel().getDoorY() && !lastStep) {
			return false;
		}
		
		int entityId;
		
		if (entity == null) {
			entityId = -1;
		} else if (isFloorItem) {
			entityId = 0;
		} else {
			entityId = entity;
		}
		
		if (isFloorItem) {
			if (this.getTile(to).hasGate()) {
				return false;
			}
		}
		
		final int rotation = Position.calculateRotation(from, to);
		
		if (rotation == 1 || rotation == 3 || rotation == 5 || rotation == 7) {
			// Get all tiles at passing corners
			RoomTile left = null;
			RoomTile right = switch (rotation) {
				case 1 -> {
					left = this.getTile(from.squareInFront(rotation + 1));
					yield this.getTile(to.squareBehind(rotation + 1));
				}
				case 3 -> {
					left = this.getTile(to.squareBehind(rotation + 1));
					yield this.getTile(to.squareBehind(rotation - 1));
				}
				case 5 -> {
					left = this.getTile(from.squareInFront(rotation - 1));
					yield this.getTile(to.squareBehind(rotation - 1));
				}
				case 7 -> {
					left = this.getTile(to.squareBehind(rotation - 1));
					yield this.getTile(from.squareInFront(rotation - 1));
				}
				default -> null;
			};
			
			if (left != null && right != null) {
				if (left.getMovementNode() != RoomEntityMovementNode.OPEN && right.getState() == RoomTileState.INVALID) {
					return false;
				}
				
				if (right.getMovementNode() != RoomEntityMovementNode.OPEN && left.getState() == RoomTileState.INVALID) {
					return false;
				}
				
				if (left.getMovementNode() != RoomEntityMovementNode.OPEN && right.getMovementNode() != RoomEntityMovementNode.OPEN) {
					return false;
				}
			}
		}
		
		final boolean positionHasUser = positionHasUser(entityId, to);
		
		if (positionHasUser) {
			if (!isRetry) {
				return false;
			}
			
			if ((!room.getData().getAllowWalkthrough() || isFloorItem) && !isAtDoor) {
				return false;
				
			} else if ((room.getData().getAllowWalkthrough()) && lastStep && !isAtDoor) {
				return false;
			}
		}
		
		RoomTile tile = tiles[to.getX()][to.getY()];
		
		if (tile == null) {
			return false;
		}
		
		// todo: we need a per-item canStepOn(Entity entity) boolean or something.
		if (tile.getTopItemInstance() instanceof OneWayGateFloorItem item) {
			
			if (entity != null && item.getInteractingEntity() != null && item.getInteractingEntity().getId() == entity) {
				return true;
			}
		}
		
		if ((tile.getMovementNode() == RoomEntityMovementNode.CLOSED || (tile.getMovementNode() == RoomEntityMovementNode.END_OF_ROUTE && !lastStep)) && !isItemOnRoller) {
			return false;
		}
		
		if (ignoreHeight) {
			return true;
		}
		
		final double fromHeight = this.getStepHeight(from);
		final double toHeight = this.getStepHeight(to);
		
		if (isAtDoor) return true;
		
		return !(fromHeight < toHeight && (toHeight - fromHeight) > 1.5D);
	}
	
	public double getStepHeight(Position position) {
		if (this.tiles.length <= position.getX() || this.tiles[position.getX()].length <= position.getY()) return 0.0D;
		
		RoomTile instance = this.tiles[position.getX()][position.getY()];
		
		if (isValidPosition(instance.getPosition())) {
			return 0.0;
		}
		
		RoomTileStatusType tileStatus = instance.getStatus();
		double height = instance.getWalkHeight();
		
		if (tileStatus == null) {
			return 0.0D;
		}
		
		return height;
	}
	
	public List<Position> tilesWithFurniture() {
		List<Position> tilesWithFurniture = new ArrayList<>();
		
		for (int x = 0; x < this.tiles.length; x++) {
			for (int y = 0; y < this.tiles[x].length; y++) {
				if (this.tiles[x][y].hasItems()) tilesWithFurniture.add(new Position(x, y));
			}
		}
		
		return tilesWithFurniture;
	}
	
	public boolean isValidPosition(Position position) {
		return ((position.getX() < 0) || (position.getY() < 0) || (position.getX() >= this.getModel().getSizeX()) || (position.getY() >= this.getModel().getSizeY()));
	}
	
	public final Room getRoom() {
		return this.room;
	}
	
	public IRoomModel getModel() {
		return this.room.getModel();
	}
	
	@Override
	public String toString() {
		String mapString = "";
		
		for (RoomTile[] tile : this.tiles) {
			mapString = Arrays.stream(tile).map(roomTile -> roomTile.getMovementNode() == RoomEntityMovementNode.CLOSED ? " " : "X").collect(Collectors.joining("", "", "\n"));
		}
		
		return mapString;
	}
	
	public String visualiseEntityGrid() {
		final var builder = new StringBuilder();
		
		Arrays.stream(this.tiles).forEachOrdered(tile -> {
			Arrays.stream(tile).map(roomTile -> !roomTile.getEntities().isEmpty() ? "E" : "[]").forEachOrdered(builder::append);
			builder.append("\n");
		});
		
		return builder.toString();
	}
	
}
