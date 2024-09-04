package com.cometproject.server.network.messages.outgoing.room.settings;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class ThumbnailTakenMessageComposer extends MessageComposer {
    @Override
    public short getId() {
        return Composers.ThumbnailSavedMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeBoolean(true);//limit reached
        msg.writeBoolean(true);
    }
}
