package com.cometproject.server.composers.group.forums;

import com.cometproject.api.game.groups.types.IGroup;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

import java.util.List;

public class GroupForumListMessageComposer extends MessageComposer {
	
	private final int code;
	private final List<IGroup> groups;
	private final int playerId;
	
	public GroupForumListMessageComposer(final int code, final List<IGroup> groups, final int playerId) {
		this.code = code;
		this.groups = groups;
		this.playerId = playerId;
	}
	
	@Override
	public short getId() {
		return Composers.ForumsListDataMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(this.code);
		msg.writeInt(this.groups.size());
		msg.writeInt(0);
		msg.writeInt(this.groups.size()); //???
		
		this.groups.forEach(group -> group.getForum().composeData(msg, group.getData()));
	}
	
}
