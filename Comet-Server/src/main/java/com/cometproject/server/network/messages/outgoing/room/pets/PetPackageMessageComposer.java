package com.cometproject.server.network.messages.outgoing.room.pets;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class PetPackageMessageComposer extends MessageComposer {

    private final int itemId;

    public PetPackageMessageComposer(final int itemId) {
        this.itemId = itemId;
    }

    @Override
    public short getId() {
        return Composers.PetPackageMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(this.itemId);
    }
}
