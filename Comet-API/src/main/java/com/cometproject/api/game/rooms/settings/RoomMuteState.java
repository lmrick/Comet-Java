package com.cometproject.api.game.rooms.settings;

public enum RoomMuteState {
	NONE(0), RIGHTS(1);
	
	private final int state;
	
	RoomMuteState(int state) {
		this.state = state;
	}
	
	public int getState() {
		return this.state;
	}
	
	public static RoomMuteState valueOf(int state) {
		return state == 0 ? NONE : RIGHTS;
	}
}
