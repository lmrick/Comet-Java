package com.cometproject.server.network.messages.outgoing.room.avatar;

import com.cometproject.server.game.rooms.types.components.types.chat.emotions.ChatEmotion;
import com.cometproject.server.protocol.headers.Composers;


public class ShoutMessageComposer extends TalkMessageComposer {

    public ShoutMessageComposer(final int playerId, final String message, final ChatEmotion emotion, final int colour) {
        super(playerId, message, emotion, colour);
    }

    @Override
    public short getId() {
        return Composers.ShoutMessageComposer;
    }
}
