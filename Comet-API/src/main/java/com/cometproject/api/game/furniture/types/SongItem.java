package com.cometproject.api.game.furniture.types;

import com.cometproject.api.game.players.data.components.inventory.IPlayerItemSnapshot;

public interface SongItem {

    int getSongId();
	
	void setSongId(int songId);
	IPlayerItemSnapshot getItemSnapshot();
}
