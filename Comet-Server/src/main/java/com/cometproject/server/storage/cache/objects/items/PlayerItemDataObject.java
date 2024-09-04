package com.cometproject.server.storage.cache.objects.items;

import com.cometproject.api.game.players.data.components.inventory.IPlayerItem;
import com.cometproject.server.storage.cache.CachableObject;
import com.google.gson.JsonObject;

public class PlayerItemDataObject extends CachableObject {
	
	private final IPlayerItem playerItem;
	
	public PlayerItemDataObject(IPlayerItem playerItem) {
		this.playerItem = playerItem;
	}
	
	@Override
	public JsonObject toJson() {
		JsonObject coreObject = new JsonObject();
		
		coreObject.addProperty("id", playerItem.getId());
		coreObject.addProperty("baseId", playerItem.getBaseId());
		coreObject.addProperty("extraData", playerItem.getExtraData());
		
		return coreObject;
	}
	
}
