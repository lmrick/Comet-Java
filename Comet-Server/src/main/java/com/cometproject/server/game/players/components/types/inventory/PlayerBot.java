package com.cometproject.server.game.players.components.types.inventory;

import com.cometproject.api.game.bots.IBotData;
import com.cometproject.api.game.players.data.components.bots.IPlayerBot;

public record PlayerBot(IBotData botData) implements IPlayerBot {
	
	@Override
	public int getId() {
		return this.botData.getId();
	}
	
}
