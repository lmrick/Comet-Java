package com.cometproject.server.network.messages.outgoing.notification;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;


public class PurchaseErrorMessageComposer extends MessageComposer {

    private final int ErrorCode;

    public PurchaseErrorMessageComposer(final int ErrorCode) {
        this.ErrorCode = ErrorCode;
    }

    @Override
    public short getId() {
        return Composers.PurchaseErrorMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(ErrorCode);
    }
}
