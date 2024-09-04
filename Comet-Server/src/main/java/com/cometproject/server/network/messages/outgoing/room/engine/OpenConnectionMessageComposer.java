package com.cometproject.server.network.messages.outgoing.room.engine;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class OpenConnectionMessageComposer extends MessageComposer {
    @Override
    public short getId() {
        return Composers.OpenConnectionMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {

    }
}
