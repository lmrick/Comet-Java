package com.cometproject.server.game.players.components.types;

import com.cometproject.api.game.players.components.PlayerComponentContext;
import com.cometproject.api.game.players.data.components.IPlayerNavigator;
import com.cometproject.api.game.players.data.components.navigator.ISavedSearch;
import com.cometproject.server.game.players.components.PlayerComponent;
import com.cometproject.server.storage.queries.player.PlayerDao;

import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class NavigatorComponent extends PlayerComponent implements IPlayerNavigator {
	private final Logger LOG = getLogger(NavigatorComponent.class);
	private final Map<Integer, ISavedSearch> savedSearches;
	private final Map<String, Integer> viewModes;
	private final Set<Integer> favouriteRooms;
	
	public NavigatorComponent(final PlayerComponentContext componentContext) {
		super(componentContext);
		
		this.savedSearches = PlayerDao.getSavedSearches(this.getPlayer().getId());
		this.favouriteRooms = PlayerDao.getFavouriteRooms(this.getPlayer().getId());
		this.viewModes = PlayerDao.getViewModes(this.getPlayer().getId());
	}
	
	@Override
	public void dispose() {
		this.savedSearches.clear();
		this.favouriteRooms.clear();
		this.viewModes.clear();
	}
	
	@Override
	public boolean isSearchSaved(ISavedSearch newSearch) {
		for (ISavedSearch savedSearch : this.getSavedSearches().values()) {
			if (savedSearch.view().equals(newSearch.view()) && savedSearch.searchQuery().equals(newSearch.searchQuery()))
				return true;
		}
		
		return false;
	}
	
	@Override
	public Set<Integer> getFavouriteRooms() {
		return this.favouriteRooms;
	}
	
	@Override
	public Map<String, Integer> getViewModes() {
		return this.viewModes;
	}
	
	@Override
	public Map<Integer, ISavedSearch> getSavedSearches() {
		return this.savedSearches;
	}
	
}
