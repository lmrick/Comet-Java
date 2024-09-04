package com.cometproject.server.network.messages.outgoing.messenger;

import com.cometproject.api.game.players.data.IPlayerAvatar;
import com.cometproject.api.networking.messages.IComposer;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;


public class FriendRequestMessageComposer extends MessageComposer {
    private final IPlayerAvatar playerAvatar;

    public FriendRequestMessageComposer(final IPlayerAvatar playerAvatar) {
        this.playerAvatar = playerAvatar;
    }

    @Override
    public short getId() {
        return Composers.NewBuddyRequestMessageComposer;
    }

    @Override
    public void compose(IComposer msg) {
        msg.writeInt(this.playerAvatar.getId());
        msg.writeString(this.playerAvatar.getUsername());
        msg.writeString(this.playerAvatar.getFigure());
    }
}