package com.cometproject.api.game.catalog.purchase.economy;

import com.cometproject.api.utilities.events.catalog.IEconomyEvent;
import java.util.List;

public interface IEconomyEventStore {
	
	void append(IEconomyEvent event);
	List<IEconomyEvent> loadByPlayer(int playerId);
	List<IEconomyEvent> loadByPlayerUntil(int playerId, long timestamp);
	
}
