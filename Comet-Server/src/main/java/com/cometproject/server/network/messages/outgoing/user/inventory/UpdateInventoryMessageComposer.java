package com.cometproject.server.network.messages.outgoing.user.inventory;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;


public class UpdateInventoryMessageComposer extends MessageComposer {

    @Override
    public short getId() {
        return Composers.FurniListUpdateMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {

    }
}
