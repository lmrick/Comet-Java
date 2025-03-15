package com.cometproject.server.game.players.components.types.messenger;

import com.cometproject.api.game.players.data.components.messenger.IMessengerSearchResult;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.game.players.PlayerManager;

public record MessengerSearchResult(int id, String username, String figure, String motto, String lastOnline) implements IMessengerSearchResult {
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(id);
		msg.writeString(username);
		msg.writeString(motto);
		msg.writeBoolean(PlayerManager.getInstance().isOnline(id)); // is online
		msg.writeBoolean(false);
		msg.writeString("");
		msg.writeInt(0);
		msg.writeString(figure);
		msg.writeString(lastOnline);
	}
	
	@Override
	public String figure() {
		return figure;
	}
	
	@Override
	public int id() {
		return id;
	}
	
	@Override
	public String lastOnline() {
		return lastOnline;
	}
	
	@Override
	public String motto() {
		return motto;
	}
	
	@Override
	public String username() {
		return username;
	}
	
}
