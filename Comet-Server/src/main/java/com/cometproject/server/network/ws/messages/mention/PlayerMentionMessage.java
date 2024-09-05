package com.cometproject.server.network.ws.messages.mention;

import com.cometproject.server.network.ws.messages.WsMessage;
import com.cometproject.server.network.ws.messages.WsMessageType;

public class PlayerMentionMessage extends WsMessage {
	
	private final String player;
	private final int roomId;
	
	public PlayerMentionMessage(String player, int roomId) {
		super(WsMessageType.PLAYER_MENTION);
		this.player = player;
		this.roomId = roomId;
	}
	
	public String getPlayer() {
		return player;
	}
	
	public int getRoomId() {
		return roomId;
	}
	
}
