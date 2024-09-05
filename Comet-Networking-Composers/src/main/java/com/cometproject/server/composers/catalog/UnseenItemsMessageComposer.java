package com.cometproject.server.composers.catalog;

import com.cometproject.api.game.furniture.IFurnitureService;
import com.cometproject.api.game.players.data.components.inventory.IPlayerItem;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UnseenItemsMessageComposer extends MessageComposer {
	private final Map<Integer, List<Integer>> newObjects;
	
	public UnseenItemsMessageComposer(Map<Integer, List<Integer>> newObjects) {
		this.newObjects = newObjects;
	}
	
	public UnseenItemsMessageComposer(final Set<IPlayerItem> PlayerItems, final IFurnitureService furnitureService) {
		this.newObjects = new HashMap<>();
		
		PlayerItems.forEach(playerItem -> {
			if (!this.newObjects.containsKey(1)) {
				this.newObjects.put(1, Lists.newArrayList(furnitureService.getItemVirtualId(playerItem.getId())));
			} else {
				this.newObjects.get(1).add(furnitureService.getItemVirtualId(playerItem.getId()));
			}
		});
	}
	
	@Override
	public short getId() {
		return Composers.FurniListNotificationMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(this.newObjects.size());
		
		this.newObjects.forEach((key, value) -> {
			msg.writeInt(key);
			msg.writeInt(value.size());
			value.forEach(msg::writeInt);
		});
	}
	
	@Override
	public void dispose() {
		this.newObjects.forEach((key, value) -> value.clear());
		this.newObjects.clear();
	}
	
}
