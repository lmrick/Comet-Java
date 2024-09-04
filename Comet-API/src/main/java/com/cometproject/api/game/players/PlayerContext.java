package com.cometproject.api.game.players;

public class PlayerContext {
	private static PlayerContext currentContext;
	private final IPlayer player;
	
	public PlayerContext(IPlayer player) {
		this.player = player;
	}
	
	public static PlayerContext getCurrentContext() {
		return currentContext;
	}
	
	public static void setCurrentContext(PlayerContext context) {
		PlayerContext.currentContext = context;
	}
	
	public IPlayer getPlayer() {
		return player;
	}
	
}
