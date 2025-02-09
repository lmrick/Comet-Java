package com.cometproject.server.network.messages.outgoing.user.details;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class UpdateUsernameMessageComposer extends MessageComposer {
    private final String user;

    public UpdateUsernameMessageComposer(String user) {
        this.user = user;
    }

    @Override
    public short getId() {
        return Composers.UpdateUsernameMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(0);
        msg.writeString(user);
        msg.writeInt(0);
    }
}
