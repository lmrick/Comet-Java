package com.cometproject.api.game.rooms.settings;

public enum RoomTradeState {
	DISABLED(0), ENABLED(2), OWNER_ONLY(1);
	
	private final int state;
	
	RoomTradeState(int state) {
		this.state = state;
	}
	
	public int getState() {
		return this.state;
	}
	
	public static RoomTradeState valueOf(int state) {
		return switch (state) {
			case 0 -> DISABLED;
			case 2 -> ENABLED;
			default -> OWNER_ONLY;
		};
	}
}
