package com.cometproject.server.network.messages.outgoing.room.settings;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class EnforceRoomCategoryMessageComposer extends MessageComposer {

    private final int defaultCategory = 16;

    public EnforceRoomCategoryMessageComposer() {

    }

    @Override
    public short getId() {
        return Composers.EnforceCategoryUpdateMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(defaultCategory);
    }
}
