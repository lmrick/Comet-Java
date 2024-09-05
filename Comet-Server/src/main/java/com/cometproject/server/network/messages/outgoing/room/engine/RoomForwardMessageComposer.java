package com.cometproject.server.network.messages.outgoing.room.engine;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class RoomForwardMessageComposer extends MessageComposer {
	
	private final int roomId;
	
	public RoomForwardMessageComposer(final int roomId) {
		this.roomId = roomId;
	}
	
	@Override
	public short getId() {
		return Composers.RoomForwardMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(roomId);
	}
	
}
