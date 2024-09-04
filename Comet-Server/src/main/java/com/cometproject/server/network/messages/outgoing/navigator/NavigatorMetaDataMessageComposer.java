package com.cometproject.server.network.messages.outgoing.navigator;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class NavigatorMetaDataMessageComposer extends MessageComposer {
    @Override
    public short getId() {
        return Composers.NavigatorMetaDataParserMessageComposer;
    }

    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(4);

        msg.writeString("official_view");
        msg.writeInt(0);

        msg.writeString("hotel_view");
        msg.writeInt(0);

        msg.writeString("roomads_view");
        msg.writeInt(0);

        msg.writeString("myworld_view");
        msg.writeInt(0);
    }
}
