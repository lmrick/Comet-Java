package com.cometproject.api.game.furniture.types.sounds;

public interface IMusicData {
	
	int songId();
	String name();
	String title();
	String artist();
	String data();
	int lengthSeconds();
	int getLengthMilliseconds();
	
}
