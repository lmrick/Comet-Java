package com.cometproject.server.network.messages.outgoing.room.avatar;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;


public class TypingStatusMessageComposer extends MessageComposer {
    private final int playerId;
    private final int status;

    public TypingStatusMessageComposer(final int playerId, final int status) {
        this.playerId = playerId;
        this.status = status;
    }

    @Override
    public short getId() {
        return Composers.UserTypingMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(playerId);
        msg.writeInt(status);
    }
}
