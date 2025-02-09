package com.cometproject.server.network.messages.outgoing.user.details;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;


public class AvailabilityStatusMessageComposer extends MessageComposer {

    @Override
    public short getId() {
        return Composers.AvailabilityStatusMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeBoolean(false);
        msg.writeBoolean(false);
        msg.writeBoolean(true);
    }
}
