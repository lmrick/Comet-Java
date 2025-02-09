package com.cometproject.server.game.rooms.objects.items.types.floor.wired.addons;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFactory;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.api.game.utilities.RandomUtil;


public class WiredAddonPyramid extends RoomItemFloor {
    private boolean hasEntity = false;

    public WiredAddonPyramid(RoomItemData roomItemData, Room room) {
        super(roomItemData, room);

        this.setTicks(RandomUtil.getRandomInt(5, 8) * 2);
    }

    @Override
    public void onEntityStepOn(RoomEntity entity) {
        this.hasEntity = true;
    }

    @Override
    public void onEntityStepOff(RoomEntity entity) {
        this.hasEntity = false;
    }

    @Override
    public void onTickComplete() {
        if (this.hasEntity) {
            this.setTicks(RoomItemFactory.getProcessTime(1.0));
            return;
        }
			
			this.getItemData().setData(this.getItemData().getData().equals("1") ? "0" : "1");

        this.sendUpdate();
        this.setTicks(RoomItemFactory.getProcessTime(RandomUtil.getRandomInt(5, 8)));

        this.getRoom().getMapping().updateTile(this.getPosition().getX(), this.getPosition().getY());
    }
}
