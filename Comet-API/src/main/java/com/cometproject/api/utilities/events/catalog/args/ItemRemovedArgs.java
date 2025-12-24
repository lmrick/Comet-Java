package com.cometproject.api.utilities.events.catalog.args;

import com.cometproject.api.utilities.events.EventArgs;

public class ItemRemovedArgs extends EventArgs {
	private final long timestamp;
	private final int playerId;
	private final long itemId;
	private final int baseItemId;
	
	public ItemRemovedArgs(long timestamp, int playerId, long itemId, int baseItemId) {
		this.timestamp = timestamp;
		this.playerId = playerId;
		this.itemId = itemId;
		this.baseItemId = baseItemId;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public int getPlayerId() {
		return playerId;
	}
	
	public long getItemId() {
		return itemId;
	}
	
	public int getBaseItemId() {
		return baseItemId;
	}
	
}
