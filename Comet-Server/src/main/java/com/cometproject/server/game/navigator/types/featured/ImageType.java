package com.cometproject.server.game.navigator.types.featured;

public enum ImageType {
	INTERNAL, EXTERNAL;
	
	public static ImageType get(String type) {
		return type.equals("internal") ? INTERNAL : EXTERNAL;
	}
}
