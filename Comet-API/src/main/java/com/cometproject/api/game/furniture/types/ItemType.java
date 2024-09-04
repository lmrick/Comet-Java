package com.cometproject.api.game.furniture.types;

public enum ItemType {
	WALL("i"), FLOOR("s"), EFFECT("e");
	
	private final String type;
	
	ItemType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public static ItemType forString(final String str) {
		return switch (str.toLowerCase()) {
			case "i" -> WALL;
			case "e" -> EFFECT;
			default -> FLOOR;
		};
	}
	
}
