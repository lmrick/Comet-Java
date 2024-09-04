package com.cometproject.server.game.rooms.objects.items.types.state.data;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;

public interface ItemStuffData {
    int getType();

    void compose(IComposerDataWrapper msg);
}
