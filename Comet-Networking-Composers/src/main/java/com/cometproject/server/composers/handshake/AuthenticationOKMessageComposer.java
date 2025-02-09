package com.cometproject.server.composers.handshake;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;


public class AuthenticationOKMessageComposer extends MessageComposer {
    public AuthenticationOKMessageComposer() {

    }

    @Override
    public short getId() {
        return Composers.AuthenticationOKMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {

    }
}
