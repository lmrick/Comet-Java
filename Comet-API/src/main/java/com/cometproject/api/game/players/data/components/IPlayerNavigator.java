package com.cometproject.api.game.players.data.components;

import com.cometproject.api.game.players.data.components.navigator.ISavedSearch;

import java.util.Map;
import java.util.Set;

public interface IPlayerNavigator {
    
    boolean isSearchSaved(ISavedSearch savedSearch);
    
    Set<Integer> getFavouriteRooms();
    Map<String, Integer> getViewModes();
    Map<Integer, ISavedSearch> getSavedSearches();
    
}
