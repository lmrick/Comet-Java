package com.cometproject.server.network.messages.outgoing.room.settings;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.game.rooms.types.components.types.ban.RoomBan;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

import java.util.Map;


public class RoomBannedListMessageComposer extends MessageComposer {
    private final int roomId;
    private final Map<Integer, RoomBan> bans;

    public RoomBannedListMessageComposer(int roomId, Map<Integer, RoomBan> bans) {
        this.roomId = roomId;
        this.bans = bans;
    }

    @Override
    public short getId() {
        return Composers.GetRoomBannedUsersMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(roomId);
        msg.writeInt(bans.size());

        for (RoomBan ban : bans.values()) {
            msg.writeInt(ban.getPlayerId());
            msg.writeString(ban.getPlayerName());
        }
    }
}
