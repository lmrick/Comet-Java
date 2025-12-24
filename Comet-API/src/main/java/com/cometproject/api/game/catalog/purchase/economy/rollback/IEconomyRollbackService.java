package com.cometproject.api.game.catalog.purchase.economy.rollback;

public interface IEconomyRollbackService {
	
	void rollbackPlayer(int playerId, long safeTimestamp);
	
}
