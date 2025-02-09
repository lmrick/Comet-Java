package com.cometproject.server.game.rooms.objects.items.types.floor.snowboarding;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.network.messages.outgoing.room.avatar.ActionMessageComposer;
import com.cometproject.api.game.utilities.RandomUtil;

public class SnowboardJumpFloorItem extends RoomItemFloor {
	
	public SnowboardJumpFloorItem(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public void onEntityStepOn(RoomEntity entity) {
		Position tileGoal = this.getPartnerTile();
		
		boolean increaseY = false;
		boolean decreaseY = false;
		boolean increaseX = false;
		boolean decreaseX = false;
		
		if (tileGoal != null) {
			switch (this.getRotation()) {
				case 4 -> increaseY = true;
				case 0 -> decreaseY = true;
				case 6 -> decreaseX = true;
				case 2 -> increaseX = true;
			}
			
			entity.moveTo(tileGoal.getX() + (increaseX ? 1 : decreaseX ? -1 : 0), tileGoal.getY() + (increaseY ? 1 : decreaseY ? -1 : 0));
			this.getRoom().getEntities().broadcastMessage(new ActionMessageComposer(entity.getId(), 8));
		}
	}
	
	@Override
	public void onEntityStepOff(RoomEntity entity) {
		if (!(entity instanceof PlayerEntity playerEntity)) {
			return;
		}
		
		int actionId = RandomUtil.getRandomInt(9, 10);
		this.getRoom().getEntities().broadcastMessage(new ActionMessageComposer(playerEntity.getId(), actionId));
	}
	
}
