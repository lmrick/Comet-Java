package com.cometproject.server.network.messages.outgoing.landing;

import com.cometproject.api.game.players.data.IPlayerAvatar;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

import java.util.Map;

public class SendHotelViewLooksMessageComposer extends MessageComposer {

    private final String key;
    private final Map<IPlayerAvatar, Integer> players;

    public SendHotelViewLooksMessageComposer(String key, Map<IPlayerAvatar, Integer> players) {
        this.key = key;
        this.players = players;
    }

    @Override
    public short getId() {
        return Composers.SendHotelViewLooksMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeString(key);
        msg.writeInt(this.players.size());

        for (Map.Entry<IPlayerAvatar, Integer> player : players.entrySet()) {
            msg.writeInt(player.getKey().getId());
            msg.writeString(player.getKey().getUsername());
            msg.writeString(player.getKey().getFigure());
            msg.writeInt(1);//?
            msg.writeInt(player.getValue());
        }

    }
}
