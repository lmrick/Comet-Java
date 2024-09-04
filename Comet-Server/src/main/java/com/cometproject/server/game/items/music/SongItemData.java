package com.cometproject.server.game.items.music;

import com.cometproject.api.game.furniture.types.SongItem;
import com.cometproject.api.game.players.data.components.inventory.IPlayerItemSnapshot;
import com.cometproject.server.game.players.components.types.inventory.InventoryItemSnapshot;

public class SongItemData implements SongItem {
	
	private final InventoryItemSnapshot itemSnapshot;
	private int songId;
	
	public SongItemData(InventoryItemSnapshot itemSnapshot, int songId) {
		this.itemSnapshot = itemSnapshot;
		this.songId = songId;
	}
	
	@Override
	public int getSongId() {
		return songId;
	}
	
	@Override
	public void setSongId(int songId) {
		this.songId = songId;
	}
	
	@Override
	public IPlayerItemSnapshot getItemSnapshot() {
		return itemSnapshot;
	}
	
}
