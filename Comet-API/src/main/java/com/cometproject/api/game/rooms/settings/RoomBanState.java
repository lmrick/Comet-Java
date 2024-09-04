package com.cometproject.api.game.rooms.settings;

public enum RoomBanState {
	NONE(0), RIGHTS(1);
	
	private final int state;
	
	RoomBanState(int state) {
		this.state = state;
	}
	
	public int getState() {
		return this.state;
	}
	
	public static RoomBanState valueOf(int state) {
		return state == 0 ? NONE : RIGHTS;
	}
}
