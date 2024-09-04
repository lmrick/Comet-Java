package com.cometproject.api.game.players.data.components;

public interface IPlayerSubscription {
	
	void load();
	
	void add(int days);
	void delete();
	boolean isValid();
	boolean exists();
	int getExpire();
	
}
