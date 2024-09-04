package com.cometproject.server.game.players.components.types.settings;

import com.cometproject.api.game.players.data.types.IVolumeData;
import com.google.gson.JsonObject;

public class VolumeData implements IVolumeData {
	
	private int systemVolume;
	private int furniVolume;
	private int traxVolume;
	
	public VolumeData(int systemVolume, int furniVolume, int traxVolume) {
		this.systemVolume = systemVolume;
		this.furniVolume = furniVolume;
		this.traxVolume = traxVolume;
	}
	
	@Override
	public int getSystemVolume() {
		return systemVolume;
	}
	
	@Override
	public void setSystemVolume(int systemVolume) {
		this.systemVolume = systemVolume;
	}
	
	@Override
	public int getFurniVolume() {
		return furniVolume;
	}
	
	@Override
	public void setFurniVolume(int furniVolume) {
		this.furniVolume = furniVolume;
	}
	
	@Override
	public int getTraxVolume() {
		return traxVolume;
	}
	
	@Override
	public void setTraxVolume(int traxVolume) {
		this.traxVolume = traxVolume;
	}
	
	public JsonObject toJson() {
		final JsonObject coreObject = new JsonObject();
		
		coreObject.addProperty("systemVolume", systemVolume);
		coreObject.addProperty("furniVolume", furniVolume);
		coreObject.addProperty("traxVolume", traxVolume);
		
		return coreObject;
	}
	
}
