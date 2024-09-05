package com.cometproject.server.composers.group.forums;

import com.cometproject.api.game.groups.types.components.forum.IForumThread;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class GroupForumPostThreadMessageComposer extends MessageComposer {
	
	private final int groupId;
	private final IForumThread forumThread;
	
	public GroupForumPostThreadMessageComposer(int groupId, IForumThread forumThread) {
		this.groupId = groupId;
		this.forumThread = forumThread;
	}
	
	@Override
	public short getId() {
		return Composers.ThreadCreatedMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(groupId);
		
		forumThread.compose(msg);
	}
	
}
