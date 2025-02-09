package com.cometproject.server.network.messages.outgoing.user.buildersclub;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;


public class BuildersClubMembershipMessageComposer extends MessageComposer {
    @Override
    public short getId() {
        return Composers.BuildersClubMembershipMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(999999999);
        msg.writeInt(100);
        msg.writeInt(2);
    }
}
