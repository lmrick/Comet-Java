package com.cometproject.api.game.rooms;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;

public interface IRoomWriter {
    void write(IRoomData room, IComposerDataWrapper msg);

    void write(IRoomData room, IComposerDataWrapper msg, boolean skipAuth);

    void entryData(IRoomData room,
                   IComposerDataWrapper msg,
                   boolean isLoading,
                   boolean checkEntry,
                   boolean skipAuth,
                   boolean canMute);
}
