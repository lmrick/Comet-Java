package com.cometproject.api.utilities.events.catalog.args;

import com.cometproject.api.utilities.events.EventArgs;
import java.util.List;

public class PurchaseArgs extends EventArgs {
	private final long timestamp;
	private final int playerId;
	private final int catalogItemId;
	private final int quantity;
	private final List<Long> createdItemIds;
	
	public PurchaseArgs(long timestamp, int playerId, int catalogItemId, int quantity, List<Long> createdItemIds) {
		this.timestamp = timestamp;
		this.playerId = playerId;
		this.catalogItemId = catalogItemId;
		this.quantity = quantity;
		this.createdItemIds = createdItemIds;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public int getPlayerId() {
		return playerId;
	}
	
	public int getCatalogItemId() {
		return catalogItemId;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public List<Long> getCreatedItemIds() {
		return createdItemIds;
	}
	
}
