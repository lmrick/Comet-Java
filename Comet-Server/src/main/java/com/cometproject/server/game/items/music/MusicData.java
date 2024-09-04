package com.cometproject.server.game.items.music;

import com.cometproject.api.game.furniture.types.IMusicData;

public record MusicData(int songId, String name, String title, String artist, String data, int lengthSeconds) implements IMusicData {
	
	@Override
	public int getLengthMilliseconds() {
		return lengthSeconds * 1000;
	}
	
}
