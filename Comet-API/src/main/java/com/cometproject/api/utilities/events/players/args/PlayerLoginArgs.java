package com.cometproject.api.utilities.events.players.args;

import com.cometproject.api.game.players.IPlayer;
import com.cometproject.api.utilities.events.EventArgs;

public class PlayerLoginArgs extends EventArgs {
    private final IPlayer player;

    public PlayerLoginArgs(IPlayer player) {
        this.player = player;
    }

    public IPlayer getPlayer() {
        return this.player;
    }
}
