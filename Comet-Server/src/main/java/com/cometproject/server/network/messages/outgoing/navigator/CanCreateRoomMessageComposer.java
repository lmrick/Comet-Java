package com.cometproject.server.network.messages.outgoing.navigator;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;


public class CanCreateRoomMessageComposer extends MessageComposer {

    @Override
    public short getId() {
        return Composers.CanCreateRoomMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(0);
        msg.writeInt(300);
    }
    
}
