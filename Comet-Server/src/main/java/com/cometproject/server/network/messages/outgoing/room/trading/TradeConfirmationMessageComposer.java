package com.cometproject.server.network.messages.outgoing.room.trading;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;


public class TradeConfirmationMessageComposer extends MessageComposer {

    @Override
    public short getId() {
        return Composers.TradingCompleteMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {

    }
}
