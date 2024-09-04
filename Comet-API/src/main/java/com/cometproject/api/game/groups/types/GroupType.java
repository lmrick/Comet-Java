package com.cometproject.api.game.groups.types;

public enum GroupType {
	REGULAR(0), EXCLUSIVE(1), PRIVATE(2);
	
	private final int typeId;
	
	GroupType(int type) {
		this.typeId = type;
	}
	
	public int getTypeId() {
		return this.typeId;
	}
	
	public static GroupType valueOf(int typeId) {
		return switch (typeId) {
			case 0 -> REGULAR;
			case 1 -> EXCLUSIVE;
			default -> PRIVATE;
		};
	}
}
