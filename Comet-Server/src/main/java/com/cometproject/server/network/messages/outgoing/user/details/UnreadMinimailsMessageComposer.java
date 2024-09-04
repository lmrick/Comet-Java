package com.cometproject.server.network.messages.outgoing.user.details;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.messages.MessageComposer;


public class UnreadMinimailsMessageComposer extends MessageComposer {

    @Override
    public short getId() {
        return 0;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        // TODO: Minimail
        msg.writeInt(0);
    }
}
