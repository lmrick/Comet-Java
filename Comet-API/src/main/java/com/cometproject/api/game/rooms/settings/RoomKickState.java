package com.cometproject.api.game.rooms.settings;

public enum RoomKickState {
	EVERYONE(2), RIGHTS(1), NONE(0);
	
	private final int state;
	
	RoomKickState(int state) {
		this.state = state;
	}
	
	public int getState() {
		return this.state;
	}
	
	public static RoomKickState valueOf(int state) {
		return switch (state) {
			case 0 -> NONE;
			case 1 -> RIGHTS;
			default -> EVERYONE;
		};
	}
}
