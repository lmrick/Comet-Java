package com.cometproject.server.composers.catalog.ads;

import com.cometproject.api.game.rooms.IRoomData;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

import java.util.List;

public class CatalogPromotionGetRoomsMessageComposer extends MessageComposer {
	
	private final List<IRoomData> promotableRooms;
	
	public CatalogPromotionGetRoomsMessageComposer(final List<IRoomData> rooms) {
		this.promotableRooms = rooms;
	}
	
	@Override
	public short getId() {
		return Composers.PromotableRoomsMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeBoolean(false);
		msg.writeInt(this.promotableRooms.size());
		
		this.promotableRooms.forEach(data -> {
			msg.writeInt(data.getId());
			msg.writeString(data.getName());
			msg.writeBoolean(false);
		});
	}
	
}
