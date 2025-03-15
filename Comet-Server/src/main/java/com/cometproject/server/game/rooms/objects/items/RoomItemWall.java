package com.cometproject.server.game.rooms.objects.items;

import com.cometproject.api.game.furniture.types.IFurnitureDefinition;
import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.network.messages.outgoing.room.items.UpdateWallItemMessageComposer;
import com.cometproject.storage.api.StorageContext;

public abstract class RoomItemWall extends RoomItem {
	
	private IFurnitureDefinition itemDefinition;
	private String wallPosition;
	
	public RoomItemWall(RoomItemData roomItemData, Room room) {
		super(roomItemData, room);
		
		this.wallPosition = roomItemData.getWallPosition();
	}
	
	@Override
	public void serialize(IComposerDataWrapper msg) {
		msg.writeString(this.getVirtualId());
		msg.writeInt(this.getDefinition().getSpriteId());
		msg.writeString(this.getWallPosition());
		
		msg.writeString(this.getState());
		msg.writeInt(!this.getDefinition().getInteraction().equals("default") ? 1 : 0);
		msg.writeInt(-1);
		msg.writeInt(-1);
		
		msg.writeInt(1);
	}
	
	public String getState() {
		return this.getItemData().getData();
	}
	
	@Override
	public void sendUpdate() {
		Room room = this.getRoom();
		
		if (room != null && room.getEntities() != null) {
			room.getEntities().broadcastMessage(new UpdateWallItemMessageComposer(this, this.getItemData().getOwnerId(), this.getRoom().getData().getOwner()));
		}
	}
	
	public void save() {
		this.saveData();
	}
	
	@Override
	public void saveData() {
		StorageContext.getCurrentContext().getRoomItemRepository().saveData(this.getId(), this.getItemData().getData());
	}
	
	@Override
	public IFurnitureDefinition getDefinition() {
		if (this.itemDefinition == null) {
			this.itemDefinition = ItemManager.getInstance().getDefinition(this.getItemData().getItemId());
		}
		
		return this.itemDefinition;
	}
	
	public String getWallPosition() {
		return this.wallPosition;
	}
	
	public void setWallPosition(String wallPosition) {
		this.wallPosition = wallPosition;
	}
	
	public int getRotation() {
		return 0;
	}
	
	public void setRotation(int rotation) {
	}
	
}
