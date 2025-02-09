package com.cometproject.server.network.messages.outgoing.room.access;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;


public class DoorbellRequestComposer extends MessageComposer {
    private final String username;

    public DoorbellRequestComposer(final String username) {
        this.username = username;
    }

    @Override
    public short getId() {
        return Composers.DoorbellMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeString(this.username);
    }
}
