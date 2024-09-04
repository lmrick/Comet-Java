package com.cometproject.server.game.navigator.types.featured;

public enum BannerType {
	BIG, SMALL;
	
	public static BannerType get(String type) {
		return type.equals("big") ? BIG : SMALL;
	}
}
