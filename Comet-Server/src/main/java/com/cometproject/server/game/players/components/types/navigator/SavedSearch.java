package com.cometproject.server.game.players.components.types.navigator;

import com.cometproject.api.game.players.data.components.navigator.ISavedSearch;

public record SavedSearch(String view, String searchQuery) implements ISavedSearch {
	
	@Override
	public String searchQuery() {
		return searchQuery;
	}
	
	@Override
	public String view() {
		return view;
	}
	
}
