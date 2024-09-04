package com.cometproject.server.network.messages.outgoing.room.access;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class DoorbellAcceptedComposer extends MessageComposer {
    @Override
    public short getId() {
        return Composers.FlatAccessibleMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeString("");
    }
}
