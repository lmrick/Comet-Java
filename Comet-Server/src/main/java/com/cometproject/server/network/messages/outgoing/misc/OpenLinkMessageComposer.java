package com.cometproject.server.network.messages.outgoing.misc;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class OpenLinkMessageComposer extends MessageComposer {
    private final String link;

    public OpenLinkMessageComposer(String link) {
        this.link = link;
    }

    @Override
    public short getId() {
        return Composers.OpenLinkMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeString(link);
    }
}
