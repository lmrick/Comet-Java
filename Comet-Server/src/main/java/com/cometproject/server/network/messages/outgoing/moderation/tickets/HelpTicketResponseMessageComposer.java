package com.cometproject.server.network.messages.outgoing.moderation.tickets;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class HelpTicketResponseMessageComposer extends MessageComposer {

    private int response;

    public HelpTicketResponseMessageComposer(final int response) {
        this.response = response;
    }

    @Override
    public short getId() {
        return Composers.ModeratorSupportTicketResponseMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(this.response);
        msg.writeString("");
    }
}
