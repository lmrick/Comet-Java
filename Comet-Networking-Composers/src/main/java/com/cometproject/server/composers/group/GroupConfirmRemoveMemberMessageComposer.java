package com.cometproject.server.composers.group;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class GroupConfirmRemoveMemberMessageComposer extends MessageComposer {
	
	private final int playerId;
	private final int furniCount;
	
	public GroupConfirmRemoveMemberMessageComposer(int playerId, int furniCount) {
		this.playerId = playerId;
		this.furniCount = furniCount;
	}
	
	@Override
	public short getId() {
		return Composers.GroupConfirmRemoveMemberMessageComposer;
	}
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(this.playerId);
		msg.writeInt(this.furniCount);
	}
	
}
