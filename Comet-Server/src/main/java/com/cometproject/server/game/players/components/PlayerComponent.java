package com.cometproject.server.game.players.components;

import com.cometproject.api.game.players.IPlayer;
import com.cometproject.api.game.players.PlayerContext;
import com.cometproject.api.game.players.components.PlayerComponentContext;
import com.cometproject.api.game.players.components.IPlayerComponent;

public abstract class PlayerComponent implements IPlayerComponent {
	
	private final PlayerComponentContext playerComponentContext;
	
	public PlayerComponent(PlayerComponentContext playerComponentContext) {
		this.playerComponentContext = playerComponentContext;
	}
	
	@Override
	public PlayerComponentContext getComponentContext() {
		return playerComponentContext;
	}
	
	public void dispose() {
	}
	
	public IPlayer getPlayer() {
		return PlayerContext.getCurrentContext().getPlayer();
	}
	
}
