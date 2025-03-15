package com.cometproject.server.game.rooms.objects.items;

import com.cometproject.api.game.furniture.types.IFurnitureDefinition;
import com.cometproject.api.game.rooms.objects.IFloorItem;
import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.AffectedTile;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.types.DefaultFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.AdjustableHeightFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.MagicStackFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.SoundMachineFloorItem;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.network.messages.outgoing.room.items.UpdateFloorItemMessageComposer;
import com.cometproject.server.utilities.attributes.Collidable;
import com.google.common.collect.Lists;
import java.util.List;

public abstract class RoomItemFloor extends RoomItem implements Collidable, IFloorItem {
	
	private IFurnitureDefinition itemDefinition;
	private RoomEntity collidedEntity;
	private boolean hasQueuedSave;
	private String coreState;
	private boolean stateSwitched = false;
	
	public RoomItemFloor(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	public void serialize(IComposerDataWrapper msg, boolean isNew) {
		msg.writeInt(this.getVirtualId());
		msg.writeInt(this.getDefinition().getSpriteId());
		msg.writeInt(this.getPosition().getX());
		msg.writeInt(this.getPosition().getY());
		msg.writeInt(this.getRotation());
		
		msg.writeString(this instanceof MagicStackFloorItem ? this.getItemData().getData() : this.getPosition().getZ());
		
		final double walkHeight = this instanceof AdjustableHeightFloorItem ? this.getOverrideHeight() : this.getDefinition().getHeight();
		msg.writeString(walkHeight);
		
		if (this.getLimitedEditionItemData() != null) {
			msg.writeInt(0);
			msg.writeString("");
			msg.writeBoolean(true);
			msg.writeBoolean(false);
			msg.writeString(this.getItemData().getData());
			
			msg.writeInt(this.getLimitedEditionItemData().getLimitedRare());
			msg.writeInt(this.getLimitedEditionItemData().getLimitedRareTotal());
		} else {
			this.composeItemData(msg);
		}
		
		msg.writeInt(-1);
		msg.writeInt(!(this instanceof DefaultFloorItem) && !(this instanceof SoundMachineFloorItem) ? 1 : 0);
		msg.writeInt(this.getItemData().getOwnerId());
		
		if (isNew) {
			msg.writeString(this.getItemData().getOwnerName());
		}
	}
	
	@Override
	public void serialize(IComposerDataWrapper msg) {
		this.serialize(msg, false);
	}
	
	public IFurnitureDefinition getDefinition() {
		if (this.itemDefinition == null) {
			this.itemDefinition = ItemManager.getInstance().getDefinition(this.getItemData().getItemId());
		}
		
		return this.itemDefinition;
	}
	
	public boolean interactionBlocked(RoomEntity entity, boolean isWiredTrigger) {
		if (!isWiredTrigger) {
			if (!(entity instanceof PlayerEntity playerEntity)) {
				return true;
			}
			
			return !this.getRoom().getRights().hasRights(playerEntity.getPlayerId()) && !playerEntity.getPlayer().getPermissions().getRank().roomFullControl();
		}
		
		return false;
	}
	
	public void onItemAddedToStack(RoomItemFloor floorItem) {
		// override me
	}
	
	public void onItemAddedToStack(RoomItemFloor floorItem, RoomEntity trigger) {
	
	}
	
	public void onEntityPreStepOn(RoomEntity entity) {
		// override me
	}
	
	public void onEntityStepOn(RoomEntity entity) {
		// override me
	}
	
	public void onEntityPostStepOn(RoomEntity entity) {
		// override me
	}
	
	public void onEntityStepOff(RoomEntity entity) {
		// override me
	}
	
	public void onPositionChanged(Position newPosition) {
		// override me
	}
	
	public boolean isMovementCancelled(RoomEntity entity) {
		return false;
	}
	
	public boolean isMovementCancelled(RoomEntity entity, Position position) {
		return this.isMovementCancelled(entity);
	}
	
	@Override
	public void save() {
		this.getItemData().setData(this.getDataObject());
		this.getRoom().getItemProcess().saveItem(this);
	}
	
	@Override
	public void saveData() {
		
		this.save();
	}
	
	@Override
	public void sendUpdate() {
		Room room = this.getRoom();
		if (room != null) {
			room.getEntities().broadcastMessage(new UpdateFloorItemMessageComposer(this));
		}
	}
	
	public void tempState(int state) {
		this.stateSwitched = true;
		this.coreState = this.getItemData().getData();
		
		this.getItemData().setData(state);
		this.sendUpdate();
	}
	
	public void restoreState() {
		this.stateSwitched = false;
		
		this.getItemData().setData(coreState);
		this.sendUpdate();
	}
	
	public String getDataObject() {
		return this.getItemData().getData();
	}
	
	public List<RoomItemFloor> getItemsOnStack() {
		List<RoomItemFloor> floorItems = Lists.newArrayList();
		List<AffectedTile> affectedTiles = AffectedTile.getAffectedTilesAt(this.getDefinition().getLength(), this.getDefinition().getWidth(), this.getPosition().getX(), this.getPosition().getY(), this.getRotation());
		floorItems.addAll(this.getRoom().getItems().getItemsOnSquare(this.getPosition().getX(), this.getPosition().getY()));
		affectedTiles.forEach(tile -> this.getRoom().getItems().getItemsOnSquare(tile.x, tile.y).stream().filter(floorItem -> !floorItems.contains(floorItem)).forEachOrdered(floorItems::add));
		return floorItems;
	}
	
	public List<RoomEntity> getEntitiesOnItem() {
		List<RoomEntity> entities = Lists.newArrayList();
		entities.addAll(this.getRoom().getEntities().getEntitiesAt(this.getPosition()));
		AffectedTile.getAffectedTilesAt(this.getDefinition().getLength(), this.getDefinition().getWidth(), this.getPosition().getX(), this.getPosition().getY(), this.getRotation()).stream().map(affectedTile -> this.getRoom().getEntities().getEntitiesAt(new Position(affectedTile.x, affectedTile.y))).forEachOrdered(entities::addAll);
		return entities;
	}
	
	public Position getPartnerTile() {
		return this.getDefinition().getLength() != 2 ? null : AffectedTile.getAffectedBothTilesAt(this.getDefinition().getLength(), this.getDefinition().getWidth(), this.getPosition().getX(), this.getPosition().getY(), this.getRotation()).stream().filter(affTile -> affTile.x != this.getPosition().getX() || affTile.y != this.getPosition().getY()).findFirst().map(affTile -> new Position(affTile.x, affTile.y)).orElse(null);
	}
	
	public RoomEntity getCollision() {
		return this.collidedEntity;
	}
	
	public void setCollision(RoomEntity entity) {
		this.collidedEntity = entity;
	}
	
	public void nullifyCollision() {
		this.collidedEntity = null;
	}
	
	public double getOverrideHeight() {
		return -1.0D;
	}
	
	public boolean hasQueuedSave() {
		return hasQueuedSave;
	}
	
	public void setHasQueuedSave(boolean hasQueuedSave) {
		this.hasQueuedSave = hasQueuedSave;
	}
	
	public boolean isStateSwitched() {
		return stateSwitched;
	}
	
	public void setStateSwitched(boolean stateSwitched) {
		this.stateSwitched = stateSwitched;
	}
	
	public int getRotation() {
		return this.getItemData().getRotation();
	}
	
	public void setRotation(int rotation) {
		this.getItemData().setRotation(rotation);
	}
	
}
