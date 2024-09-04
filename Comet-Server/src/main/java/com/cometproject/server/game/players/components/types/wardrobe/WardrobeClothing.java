package com.cometproject.server.game.players.components.types.wardrobe;

import com.cometproject.api.game.players.data.components.wardrobe.IWardrobeClothing;

public record WardrobeClothing(int id, int partId, int part) implements IWardrobeClothing {
	
	@Override
	public int id() {
		return id;
	}
	
	@Override
	public int partId() {
		return partId;
	}
	
	@Override
	public int part() {
		return part;
	}
	
}
