package com.cometproject.server.network.messages.outgoing.help;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.messages.MessageComposer;


public class TicketSentMessageComposer extends MessageComposer {
    public TicketSentMessageComposer() {

    }

    @Override
    public short getId() {
        return 0;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(0);
        msg.writeInt(0);
    }
}
