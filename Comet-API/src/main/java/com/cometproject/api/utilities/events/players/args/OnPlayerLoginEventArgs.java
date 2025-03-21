package com.cometproject.api.utilities.events.players.args;

import com.cometproject.api.game.players.IPlayer;
import com.cometproject.api.utilities.events.EventArgs;

public class OnPlayerLoginEventArgs extends EventArgs {
    private final IPlayer player;

    public OnPlayerLoginEventArgs(IPlayer player) {
        this.player = player;
    }

    public IPlayer getPlayer() {
        return this.player;
    }
}
