package com.cometproject.server.composers.group;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

import java.util.Map;

public class GroupBadgesMessageComposer extends MessageComposer {
	
	private final Map<Integer, String> badges;
	private final int groupId;
	private final String badge;
	
	public GroupBadgesMessageComposer(final Map<Integer, String> badges) {
		this.badges = badges;
		this.groupId = 0;
		this.badge = null;
	}
	
	public GroupBadgesMessageComposer(final int groupId, final String badge) {
		this.badges = null;
		this.groupId = groupId;
		this.badge = badge;
	}
	
	@Override
	public short getId() {
		return Composers.HabboGroupBadgesMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		if (this.badges != null) {
			msg.writeInt(badges.size());
			
			badges.forEach((key, value) -> this.composeGroupBadge(msg, key, value));
		} else {
			msg.writeInt(1);
			
			this.composeGroupBadge(msg, this.groupId, this.badge);
		}
	}
	
	private void composeGroupBadge(final IComposerDataWrapper msg, final int groupId, final String badge) {
		msg.writeInt(groupId);
		msg.writeString(badge);
	}
	
}
