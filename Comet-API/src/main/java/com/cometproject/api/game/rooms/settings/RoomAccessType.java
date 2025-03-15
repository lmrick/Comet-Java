package com.cometproject.api.game.rooms.settings;

public enum RoomAccessType {
	OPEN(0), DOORBELL(1), PASSWORD(2), INVISIBLE(3);

	private final int state;

	RoomAccessType(int state) {
		this.state = state;
	}

	public int getState() {
		return this.state;
	}

	public static RoomAccessType roomAccessToString(int access) {
		if (access == 1) {
			return RoomAccessType.DOORBELL;
		} else if (access == 2) {
			return RoomAccessType.PASSWORD;
		} else if (access == 3) {
			return RoomAccessType.INVISIBLE;
		}
		
		return RoomAccessType.OPEN;
	}

	public static int roomAccessToNumber(RoomAccessType access) {
		if (access == RoomAccessType.DOORBELL) {
			return 1;
		} else if (access == RoomAccessType.PASSWORD) {
			return 2;
		} else if (access == RoomAccessType.INVISIBLE) {
			return 3;
		}
		
		return 0;
	}

}
