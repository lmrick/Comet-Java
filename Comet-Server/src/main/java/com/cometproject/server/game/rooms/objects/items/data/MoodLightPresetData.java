package com.cometproject.server.game.rooms.objects.items.data;

public class MoodLightPresetData {
	
	public boolean backgroundOnly;
	public String colour;
	public int intensity;
	
	public MoodLightPresetData(boolean backgroundOnly, String colour, int intensity) {
		this.backgroundOnly = backgroundOnly;
		this.colour = colour;
		this.intensity = intensity;
	}
	
}
