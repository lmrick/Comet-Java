package com.cometproject.server.network.messages.outgoing.navigator;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;


public class CreateRoomMessageComposer extends MessageComposer {
    private final int roomId;
    private final String roomName;

    public CreateRoomMessageComposer(final int roomId, final String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
    }

    @Override
    public short getId() {
        return Composers.FlatCreatedMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(this.roomId);
        msg.writeString(this.roomName);
    }
}
