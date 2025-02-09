package com.cometproject.server.composers.group.forums;

import com.cometproject.api.game.groups.types.components.forum.IForumThreadReply;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class GroupForumPostReplyMessageComposer extends MessageComposer {
	private final int groupId;
	private final int threadId;
	private final IForumThreadReply reply;
	
	public GroupForumPostReplyMessageComposer(int groupId, int threadId, IForumThreadReply reply) {
		this.groupId = groupId;
		this.threadId = threadId;
		this.reply = reply;
	}
	
	@Override
	public short getId() {
		return Composers.ThreadReplyMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(this.groupId);
		msg.writeInt(this.threadId);
		
		this.reply.compose(msg);
	}
	
}
