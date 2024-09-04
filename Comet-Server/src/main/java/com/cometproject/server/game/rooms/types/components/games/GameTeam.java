package com.cometproject.server.game.rooms.types.components.games;

import com.cometproject.server.game.rooms.objects.entities.effects.PlayerEffectType;

public enum GameTeam {
	NONE(0), RED(1), GREEN(2), BLUE(3), YELLOW(4);
	
	private final int teamId;
	
	GameTeam(int team) {
		this.teamId = team;
	}
	
	public int getTeamId() {
		return this.teamId;
	}
	
	public int getEffect(GameType gameType) {
		return switch (gameType) {
			case FREEZE -> this.getFreezeEffect();
			case BANZAI -> this.getBanzaiEffect();
			default -> 0;
		};
	}
	
	public int getBanzaiEffect() {
		return switch (teamId) {
			case 1 -> PlayerEffectType.BB_RED.getEffectId();
			case 2 -> PlayerEffectType.BB_GREEN.getEffectId();
			case 3 -> PlayerEffectType.BB_BLUE.getEffectId();
			case 4 -> PlayerEffectType.BB_YELLOW.getEffectId();
			default -> 0;
		};
		
	}
	
	public char getTeamLetter() {
		return switch (teamId) {
			case 1 -> 'r';
			case 2 -> 'g';
			case 3 -> 'b';
			case 4 -> 'y';
			default -> 0;
		};
		
	}
	
	public int getFreezeEffect() {
		return switch (teamId) {
			case 1 -> PlayerEffectType.ES_RED.getEffectId();
			case 2 -> PlayerEffectType.ES_GREEN.getEffectId();
			case 3 -> PlayerEffectType.ES_BLUE.getEffectId();
			case 4 -> PlayerEffectType.ES_YELLOW.getEffectId();
			default -> 0;
		};
		
	}
}
