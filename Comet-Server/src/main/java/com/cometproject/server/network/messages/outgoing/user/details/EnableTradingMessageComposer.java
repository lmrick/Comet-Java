package com.cometproject.server.network.messages.outgoing.user.details;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.messages.MessageComposer;


public class EnableTradingMessageComposer extends MessageComposer {

    private final boolean tradingEnabled;

    public EnableTradingMessageComposer(final boolean tradingEnabled) {
        this.tradingEnabled = tradingEnabled;
    }

    @Override
    public short getId() {
        return 0;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeBoolean(this.tradingEnabled);
    }
}
