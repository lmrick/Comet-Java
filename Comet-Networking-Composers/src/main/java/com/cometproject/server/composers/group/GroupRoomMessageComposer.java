package com.cometproject.server.composers.group;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;


public class GroupRoomMessageComposer extends MessageComposer {
    private final int roomId;
    private final int groupId;

    public GroupRoomMessageComposer(final int roomId, final int groupId) {
        this.roomId = roomId;
        this.groupId = groupId;
    }

    @Override
    public short getId() {
        return Composers.NewGroupInfoMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(roomId);
        msg.writeInt(groupId);
    }
}
