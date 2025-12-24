package com.cometproject.api.game.catalog.purchase.economy.security;

public interface IDuplicateItemDetector {
	
	boolean hasDuplication(int playerId);
	
}
