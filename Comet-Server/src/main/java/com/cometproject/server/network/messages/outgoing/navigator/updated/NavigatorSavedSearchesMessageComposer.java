package com.cometproject.server.network.messages.outgoing.navigator.updated;

import com.cometproject.api.game.players.data.components.navigator.ISavedSearch;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

import java.util.Map;

public class NavigatorSavedSearchesMessageComposer extends MessageComposer {
	
	private final Map<Integer, ISavedSearch> savedSearches;
	
	public NavigatorSavedSearchesMessageComposer(final Map<Integer, ISavedSearch> savedSearches) {
		this.savedSearches = savedSearches;
	}
	
	@Override
	public short getId() {
		return Composers.NavigatorSavedSearchesMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(this.savedSearches.size());//count
		
		for (Map.Entry<Integer, ISavedSearch> savedSearch : this.savedSearches.entrySet()) {
			msg.writeInt(savedSearch.getKey());
			msg.writeString(savedSearch.getValue().view());
			msg.writeString(savedSearch.getValue().searchQuery());
			msg.writeString("");
		}
	}
	
}
