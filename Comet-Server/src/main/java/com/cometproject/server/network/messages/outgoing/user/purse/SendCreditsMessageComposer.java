package com.cometproject.server.network.messages.outgoing.user.purse;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;


public class SendCreditsMessageComposer extends MessageComposer {
    private final String credits;

    public SendCreditsMessageComposer(final String credits) {
        this.credits = credits;
    }

    @Override
    public short getId() {
        return Composers.CreditBalanceMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeString((credits + ".0"));
    }
}
