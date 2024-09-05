package com.cometproject.server.network.messages.outgoing.room.engine;

import com.cometproject.api.game.rooms.models.IRoomModel;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class RelativeHeightmapMessageComposer extends MessageComposer {
	
	private final IRoomModel model;
	
	public RelativeHeightmapMessageComposer(final IRoomModel model) {
		this.model = model;
	}
	
	@Override
	public short getId() {
		return Composers.FloorHeightMapMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeBoolean(true); // ??
		msg.writeInt(model.getRoomModelData().getWallHeight()); // wall-height
		msg.writeString(model.getRelativeHeightmap());
	}
	
}
