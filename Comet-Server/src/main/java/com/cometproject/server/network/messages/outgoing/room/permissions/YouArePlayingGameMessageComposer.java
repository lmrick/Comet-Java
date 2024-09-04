package com.cometproject.server.network.messages.outgoing.room.permissions;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class YouArePlayingGameMessageComposer extends MessageComposer {

    private final boolean isPlaying;

    public YouArePlayingGameMessageComposer(final boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    @Override
    public short getId() {
        return Composers.YouArePlayingGameMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeBoolean(isPlaying);
    }
}
