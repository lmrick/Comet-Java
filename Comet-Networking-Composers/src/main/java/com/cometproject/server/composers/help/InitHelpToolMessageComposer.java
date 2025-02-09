package com.cometproject.server.composers.help;

import com.cometproject.api.game.moderation.IHelpTicket;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;


public class InitHelpToolMessageComposer extends MessageComposer {
    private final IHelpTicket helpTicket;

    public InitHelpToolMessageComposer(IHelpTicket helpTicket) {
        this.helpTicket = helpTicket;
    }

    @Override
    public short getId() {
        return Composers.CallForHelpPendingCallsMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(this.helpTicket == null ? 0 : 1);

        if (this.helpTicket != null) {
            msg.writeString(this.helpTicket.getId());
            msg.writeString(this.helpTicket.getDateSubmitted());
            msg.writeString(this.helpTicket.getMessage());
        }
    }
}
