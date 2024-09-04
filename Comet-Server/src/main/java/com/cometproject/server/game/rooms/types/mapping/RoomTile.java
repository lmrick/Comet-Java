package com.cometproject.server.game.rooms.types.mapping;

import com.cometproject.api.game.rooms.models.RoomTileState;
import com.cometproject.api.game.utilities.Direction;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.rooms.objects.RoomObject;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.AffectedTile;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.Square;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.types.EntityPathfinder;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.types.ItemPathfinder;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.*;
import com.cometproject.server.game.rooms.objects.items.types.floor.games.freeze.FreezeBlockFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.groups.GroupGateFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.pet.breeding.BreedingBoxFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.snowboarding.SnowboardJumpFloorItem;
import com.cometproject.server.utilities.collections.ConcurrentHashSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RoomTile {
    public Set<RoomEntity> entities;
    private RoomMapping mappingInstance;
    private Position position;
    private RoomEntityMovementNode movementNode;
    private RoomTileStatusType status;
    private RoomTileState state;
    private boolean canStack;
    private long topItem = 0;
    private double stackHeight = 0d;
    private long originalTopItem = 0;
    private double originalHeight = 0d;
    private Position redirect = null;
    private boolean canPlaceItemHere = false;
    private boolean hasItems = false;
    private boolean hasMagicTile = false;
    private boolean hasAdjustableHeight = false;
    private boolean hasGate = false;
    private List<RoomItemFloor> items;
    private Map<Integer, Consumer<RoomEntity>> pendingEvents = new ConcurrentHashMap<>();

    public RoomTile(RoomMapping mappingInstance, Position position) {
        this.mappingInstance = mappingInstance;
        this.position = position;
        this.entities = new ConcurrentHashSet<>();
        this.items = new ArrayList<>();

        this.reload();
    }

    public RoomTile[] getAllAdjacentTiles() {
        RoomTile[] tiles = new RoomTile[Direction.values().length];
			
			IntStream.range(0, tiles.length).forEachOrdered(i -> {
				RoomTile tile = this.mappingInstance.getTile(this.position.squareInFront(Direction.get(i)));
				tiles[i] = tile;
			});

        return tiles;
    }

    public List<RoomTile> getAdjacentTiles(Position from) {
			
			return Arrays.stream(Position.COLLIDE_TILES).mapToObj(rotation -> this.mappingInstance.getTile(this.getPosition().squareInFront(rotation))).sorted((left, right) -> {
				if (left == null || right == null || left.getPosition() == null || right.getPosition() == null) return -1;
				
				final double distanceFromLeft = left.getPosition().distanceTo(from);
				final double distanceFromRight = right.getPosition().distanceTo(from);
				
				return Double.compare(distanceFromLeft, distanceFromRight);
			}).collect(Collectors.toList());
    }

    public void reload() {
        this.hasItems = false;
        this.redirect = null;
        this.movementNode = RoomEntityMovementNode.OPEN;
        this.status = RoomTileStatusType.NONE;
        this.canStack = true;
        this.hasMagicTile = false;
        this.topItem = 0;
        this.originalHeight = 0.0D;
        this.originalTopItem = 0;
        this.stackHeight = 0.0D;
        this.hasAdjustableHeight = false;
        this.hasGate = false;

        if (this.mappingInstance.getModel().getSquareState()[this.getPosition().getX()][this.getPosition().getY()] == null) {
            this.canPlaceItemHere = false;
            this.state = RoomTileState.INVALID;
        } else {
            this.canPlaceItemHere = this.mappingInstance.getModel().getSquareState()[this.getPosition().getX()][this.getPosition().getY()].equals(RoomTileState.VALID);
            this.state = this.mappingInstance.getModel().getSquareState()[this.getPosition().getX()][this.getPosition().getY()];
        }

        // component item is an item that can be used along with an item that overrides the height.
        boolean hasComponentItem = false;

        double highestHeight = 0d;
        long highestItem = 0;

        Double staticOverrideHeight = null;
        Double overrideHeight = null;

        this.items.clear();
			
			// it's null!
			this.mappingInstance.getRoom().getItems().getFloorItems().values().stream().filter(item -> item != null && item.getDefinition() != null).forEachOrdered(item -> {
				if (item.getPosition().getX() == this.position.getX() && item.getPosition().getY() == this.position.getY()) {
					items.add(item);
				} else {
					List<AffectedTile> affectedTiles = AffectedTile.getAffectedTilesAt(item.getDefinition().getLength(), item.getDefinition().getWidth(), item.getPosition().getX(), item.getPosition().getY(), item.getRotation());
					
					affectedTiles.stream().filter(tile -> this.position.getX() == tile.x && this.position.getY() == tile.y).filter(tile -> !items.contains(item)).forEachOrdered(tile -> items.add(item));
				}
			});

        for (RoomItemFloor item : new ArrayList<>(items)) {
            if (item.getDefinition() == null)
                continue;

            if (item instanceof GateFloorItem || item instanceof GroupGateFloorItem) {
                this.hasGate = true;
            }

            this.hasItems = true;

            final double totalHeight = item.getPosition().getZ() + (item.getOverrideHeight() != -1d ? item.getOverrideHeight() : item.getDefinition().getHeight());

            if (totalHeight > highestHeight) {
                highestHeight = totalHeight;
                highestItem = item.getId();
            }

            final boolean isGate = item instanceof GateFloorItem;

            if (item instanceof MagicStackFloorItem) {
                this.hasMagicTile = true;
            }

            if (!item.getDefinition().canWalk() && !isGate) {
                if (highestItem == item.getId())
                    movementNode = RoomEntityMovementNode.CLOSED;
            }
					
					switch (item.getDefinition().getInteraction().toLowerCase()) {
						case "bed" -> {
							status = RoomTileStatusType.LAY;
							movementNode = RoomEntityMovementNode.END_OF_ROUTE;
							
							if (item.getRotation() == 2 || item.getRotation() == 6) {
								this.redirect = new Position(item.getPosition().getX(), this.getPosition().getY());
							} else if (item.getRotation() == 0 || item.getRotation() == 4) {
								this.redirect = new Position(this.getPosition().getX(), item.getPosition().getY());
							}
						}
						case "gate" -> {
							if (item instanceof GateFloorItem) {
								movementNode = ((GateFloorItem) item).isOpen() ? RoomEntityMovementNode.OPEN : RoomEntityMovementNode.CLOSED;
							}
						}
						case "onewaygate" -> movementNode = RoomEntityMovementNode.CLOSED;
						case "wf_pyramid" -> movementNode = item.getItemData().getData().equals("1") ? RoomEntityMovementNode.OPEN : RoomEntityMovementNode.CLOSED;
						case "freeze_block" -> {
							if (item instanceof FreezeBlockFloorItem) {
                  movementNode = ((FreezeBlockFloorItem) item).isDestroyed() ? RoomEntityMovementNode.OPEN : RoomEntityMovementNode.CLOSED;
							}
						}
					}

            if (item instanceof BreedingBoxFloorItem) {
                movementNode = RoomEntityMovementNode.END_OF_ROUTE;
            }

            if (item instanceof SnowboardJumpFloorItem) {
                hasComponentItem = true;
            }

            if (item.getDefinition().canSit()) {
                status = RoomTileStatusType.SIT;
                movementNode = RoomEntityMovementNode.END_OF_ROUTE;
            }

            if (item.getDefinition().getInteraction().equals("bed")) {
                status = RoomTileStatusType.LAY;
                movementNode = RoomEntityMovementNode.END_OF_ROUTE;
            }

            if (item.getDefinition().canStack()) {
                this.canStack = false;
            }

            if (item.getOverrideHeight() != -1d) {
                if (item instanceof MagicStackFloorItem) {
                    staticOverrideHeight = item.getOverrideHeight();
                }

                if (overrideHeight != null) {
                    overrideHeight += item.getOverrideHeight() + (hasComponentItem ? 1.0D : 0.0D);
                } else {
                    overrideHeight = item.getOverrideHeight() + (hasComponentItem ? 1.0D : 0.0D);
                }
            }
        }

        if (overrideHeight != null) {
            this.hasAdjustableHeight = true;
            this.canStack = true;
            this.stackHeight = staticOverrideHeight != null ? staticOverrideHeight : overrideHeight;

            this.originalHeight = highestHeight;
        } else {
            this.stackHeight = highestHeight;
        }

        this.topItem = highestItem;

        if (this.stackHeight == 0.0D) {
            this.stackHeight = this.mappingInstance.getModel().getSquareHeight()[this.position.getX()][this.position.getY()];
        }

        if (this.originalHeight == 0.0D)
            this.originalHeight = this.stackHeight;
    }

    public void dispose() {
        this.pendingEvents.clear();
        this.items.clear();
        this.entities.clear();
    }

    public void onEntityEntersTile(RoomEntity entity) {
        if (this.pendingEvents.containsKey(entity.getId())) {
            this.pendingEvents.get(entity.getId()).accept(entity);
            this.pendingEvents.remove(entity.getId());
        }
    }

    public void scheduleEvent(int entityId, Consumer<RoomEntity> event) {
        this.pendingEvents.put(entityId, event);
    }

    public RoomEntityMovementNode getMovementNode() {
        return this.movementNode;
    }

    public double getStackHeight() {
        return this.getStackHeight(null);
    }

    public double getStackHeight(RoomItemFloor itemToStack) {
        RoomItemFloor topItem = this.getTopItemInstance();
			
			return this.hasMagicTile() || (topItem instanceof AdjustableHeightFloorItem) ? this.stackHeight : itemToStack != null && itemToStack.getId() == this.getTopItem() ? itemToStack.getPosition().getZ() : this.originalHeight;
    }

    public double getWalkHeight() {
        double height = this.stackHeight;

        RoomItemFloor roomItemFloor = this.mappingInstance.getRoom().getItems().getFloorItem(this.topItem);

        if (roomItemFloor != null && !(roomItemFloor instanceof RollerFloorItem) && (roomItemFloor.getDefinition().canSit() || roomItemFloor instanceof BedFloorItem || roomItemFloor instanceof SnowboardJumpFloorItem)) {
            if (roomItemFloor instanceof SnowboardJumpFloorItem) {
                height += 1.0;
            } else {
                if (roomItemFloor instanceof SeatFloorItem || roomItemFloor instanceof BedFloorItem)
                    height -= roomItemFloor.getDefinition().getHeight();
            }
        }

        if (this.hasAdjustableHeight && roomItemFloor instanceof SeatFloorItem) {
            height += ((SeatFloorItem) roomItemFloor).getSitHeight();
        }

        return height;
    }

    public boolean isReachable(RoomEntity entity) {
        List<Square> path = EntityPathfinder.getInstance().makePath(entity, this.position);
        return path != null && !path.isEmpty();
    }

    public boolean isReachable(RoomObject object) {
        List<Square> path = ItemPathfinder.getInstance().makePath(object, this.position);
        return path != null && !path.isEmpty();
    }

    public RoomEntity getEntity() {
			return this.getEntities().stream().findFirst().orElse(null);
			
		}

    public Set<RoomEntity> getEntities() {
        return this.entities;
    }

    public RoomTileStatusType getStatus() {
        return this.status;
    }

    public Position getPosition() {
        return this.position;
    }

    public boolean canStack() {
        return this.canStack;
    }

    public long getTopItem() {
        return this.topItem;
    }

    public void setTopItem(int topItem) {
        this.topItem = topItem;
    }

    public RoomItemFloor getTopItemInstance() {
        return this.mappingInstance.getRoom().getItems().getFloorItem(this.getTopItem());
    }

    public Position getRedirect() {
        return redirect;
    }

    public void setRedirect(Position redirect) {
        this.redirect = redirect;
    }

    public long getOriginalTopItem() {
        return originalTopItem;
    }

    public double getOriginalHeight() {
        return originalHeight;
    }

    public boolean canPlaceItemHere() {
        return canPlaceItemHere;
    }

    public boolean hasItems() {
        return hasItems;
    }

    public double getTileHeight() {
        return this.mappingInstance.getModel().getSquareHeight()[this.position.getX()][this.position.getY()];
    }

    public boolean hasMagicTile() {
        return this.hasMagicTile;
    }

    public List<RoomItemFloor> getItems() {
        return items;
    }

    public boolean hasAdjustableHeight() {
        return hasAdjustableHeight;
    }

    public RoomTileState getState() {
        return state;
    }

    public boolean hasGate() {
        return this.hasGate;
    }
}
