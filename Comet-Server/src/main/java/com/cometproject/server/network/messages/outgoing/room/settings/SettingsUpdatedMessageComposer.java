package com.cometproject.server.network.messages.outgoing.room.settings;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class SettingsUpdatedMessageComposer extends MessageComposer {

    private final int roomId;

    public SettingsUpdatedMessageComposer(int roomId) {
        this.roomId = roomId;
    }

    @Override
    public short getId() {
        return Composers.RoomSettingsSavedMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(this.roomId);
    }
}
