package com.cometproject.server.network.messages.outgoing.user.profile;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public class UserBadgesMessageComposer extends MessageComposer {
	
	private final int playerId;
	private final String[] badges;
	
	public UserBadgesMessageComposer(final int playerId, final String[] badges) {
		this.playerId = playerId;
		this.badges = badges;
	}
	
	@Override
	public short getId() {
		return Composers.HabboUserBadgesMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(playerId);
		
		int badgeCount = (int) Arrays.stream(this.badges).filter(Objects::nonNull).count();
		
		msg.writeInt(badgeCount);
		
		IntStream.range(0, badges.length).filter(i -> badges[i] != null).forEach(i -> {
			msg.writeInt(i + 1);
			msg.writeString(badges[i]);
		});
	}
	
}
