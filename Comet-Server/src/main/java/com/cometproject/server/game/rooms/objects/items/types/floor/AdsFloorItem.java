package com.cometproject.server.game.rooms.objects.items.types.floor;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.game.rooms.objects.items.types.DefaultFloorItem;
import com.cometproject.server.game.rooms.types.Room;

import java.util.stream.IntStream;

public class AdsFloorItem extends DefaultFloorItem {
	
	public AdsFloorItem(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public void composeItemData(IComposerDataWrapper msg) {
		msg.writeInt(0);
		msg.writeInt(1);
		
		if (!this.getItemData().getData().isEmpty() && !this.getItemData().getData().equals("0")) {
			String[] adsData = this.getItemData().getData().split(String.valueOf((char) 9));
			int count = adsData.length;
			
			msg.writeInt(count / 2);
			
			IntStream.rangeClosed(0, count - 1).mapToObj(i -> adsData[i]).forEachOrdered(msg::writeString);
		} else {
			msg.writeInt(0);
		}
	}
	
}
