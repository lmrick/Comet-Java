package com.cometproject.server.game.rooms.objects.items.types.floor.wired.data;

public enum ScoreboardClearType {
	ALL_TIME(0), DAILY(1), WEEKLY(2), MONTHLY(3);
	
	private final int clearType;
	
	ScoreboardClearType(int clearType) {
		this.clearType = clearType;
	}
	
	public int getClearTypeId() {
		return clearType;
	}
	
	public static ScoreboardClearType getByFurniType(int type) {
		return switch (type) {
			case 2 -> DAILY;
			case 3 -> WEEKLY;
			case 4 -> MONTHLY;
			default -> ALL_TIME;
		};
	}
}
