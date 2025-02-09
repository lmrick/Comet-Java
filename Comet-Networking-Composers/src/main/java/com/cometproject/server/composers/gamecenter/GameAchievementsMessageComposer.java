package com.cometproject.server.composers.gamecenter;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class GameAchievementsMessageComposer extends MessageComposer {
    @Override
    public short getId() {
        return Composers.GameAchievementsMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(0);
    }
}
