package com.cometproject.server.network.messages.outgoing.handshake;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class HomeRoomMessageComposer extends MessageComposer {
	
	private final int roomId;
	private final int newRoom;
	
	public HomeRoomMessageComposer(final int roomId, final int newRoom) {
		this.roomId = roomId;
		this.newRoom = newRoom;
	}
	
	@Override
	public short getId() {
		return Composers.NavigatorSettingsMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(this.roomId);
		msg.writeInt(this.newRoom);
	}
	
}
