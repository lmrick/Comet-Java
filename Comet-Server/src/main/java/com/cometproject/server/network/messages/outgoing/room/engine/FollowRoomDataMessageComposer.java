package com.cometproject.server.network.messages.outgoing.room.engine;

import com.cometproject.api.networking.messages.IComposer;
import com.cometproject.server.game.rooms.types.RoomData;
import com.cometproject.server.game.rooms.types.RoomWriter;
import com.cometproject.server.network.messages.composers.MessageComposer;
import com.cometproject.server.network.messages.headers.Composers;

public class FollowRoomDataMessageComposer extends MessageComposer {
    private final RoomData roomData;
    private final boolean checkEntry;

    public FollowRoomDataMessageComposer(final RoomData room, boolean checkEntry) {
        this.roomData = room;
        this.checkEntry = checkEntry;
    }

    @Override
    public short getId() {
        return Composers.RoomDataMessageComposer;
    }

    @Override
    public void compose(IComposer composer) {
        RoomWriter.entryData(this.roomData, composer, false, this.checkEntry);
    }
}
