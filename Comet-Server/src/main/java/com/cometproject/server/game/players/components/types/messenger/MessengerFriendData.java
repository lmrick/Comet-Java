package com.cometproject.server.game.players.components.types.messenger;

import com.cometproject.api.game.players.data.components.messenger.IMessengerFriendData;

public class MessengerFriendData implements IMessengerFriendData {
	private String username;
	private String figure;
	private String motto;
	
	public MessengerFriendData(String username, String figure, String motto) {
		this.username = username;
		this.figure = figure;
		this.motto = motto;
	}
	
	@Override
	public String getUsername() {
		return username;
	}
	
	@Override
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Override
	public String getFigure() {
		return figure;
	}
	
	@Override
	public void setFigure(String figure) {
		this.figure = figure;
	}
	
	@Override
	public String getMotto() {
		return motto;
	}
	
	@Override
	public void setMotto(String motto) {
		this.motto = motto;
	}
	
}
