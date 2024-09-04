package com.cometproject.server.network.messages.outgoing.user.details;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class AvatarAspectUpdateMessageComposer extends MessageComposer {

    private final String figure;
    private final String gender;

    public AvatarAspectUpdateMessageComposer(String figure, String gender) {
        this.figure = figure;
        this.gender = gender;
    }

    @Override
    public short getId() {
        return Composers.AvatarAspectUpdateMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeString(this.figure);
        msg.writeString(this.gender);
    }
}
