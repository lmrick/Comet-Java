package com.cometproject.api.game.groups.types;

import com.cometproject.api.game.groups.types.components.membership.IGroupMember;
import com.cometproject.api.game.players.data.IPlayerAvatar;

public record GroupMemberAvatar(IPlayerAvatar playerAvatar, boolean isRequest, IGroupMember groupMember) {

}
