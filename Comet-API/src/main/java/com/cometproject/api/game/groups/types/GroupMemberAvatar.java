package com.cometproject.api.game.groups.types;

import com.cometproject.api.game.groups.types.components.membership.IGroupMember;
import com.cometproject.api.game.players.data.IPlayerAvatar;

public class GroupMemberAvatar {
    private final IPlayerAvatar playerAvatar;
    private final boolean isRequest;
    private final IGroupMember groupMember;

    public GroupMemberAvatar(IPlayerAvatar playerAvatar, boolean isRequest, IGroupMember groupMember) {
        this.playerAvatar = playerAvatar;
        this.isRequest = isRequest;
        this.groupMember = groupMember;
    }

    public IPlayerAvatar getPlayerAvatar() {
        return playerAvatar;
    }

    public IGroupMember getGroupMember() {
        return groupMember;
    }

    public boolean isRequest() {
        return isRequest;
    }
}
