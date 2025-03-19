package com.cometproject.server.game.rooms.objects.items.types.wall;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemWall;
import com.cometproject.server.game.rooms.objects.items.data.MoodLightData;
import com.cometproject.server.game.rooms.objects.items.data.MoodLightPresetData;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.storage.queries.items.MoodlightDao;

public class MoodLightWallItem extends RoomItemWall {
	private MoodLightData moodlightData;
	
	public MoodLightWallItem(RoomItemData roomItemData, Room room) {
		super(roomItemData, room);
	}
	
	public static boolean isValidColour(String colour) {
		return switch (colour) {
			case "#000000", "#0053F7", "#EA4532", "#82F349", "#74F5F5", "#E759DE", "#F2F851" -> true;
			default -> false;
		};
	}
	
	@Override
	public boolean onInteract(RoomEntity entity, int requestData, boolean isWiredTrigger) {
		return super.onInteract(entity, requestData, isWiredTrigger);
	}
	
	@Override
	public void onLoad() {
		this.onPlaced();
	}
	
	@Override
	public void onPlaced() {
		if (this.getRoom().getItems().setMoodLight(this.getId())) {
			this.moodlightData = MoodlightDao.getMoodlightData(this.getId());
		}
	}
	
	@Override
	public void onUnload() {
		this.onPickup();
	}
	
	@Override
	public void onPickup() {
		if (this.getRoom().getItems().isMoodLightMatches(this)) {
			this.getRoom().getItems().removeMoodLight();
		}
	}
	
	public String generateExtraData() {
		MoodLightPresetData preset = (this.getMoodlightData().getPresets().size() >= this.getMoodlightData().getActivePreset()) ? this.getMoodlightData().getPresets().get(this.getMoodlightData().getActivePreset() - 1) : new MoodLightPresetData(true, "#000000", 255);
		return (this.getMoodlightData().isEnabled() ? 2 : 1) + "," + this.getMoodlightData().getActivePreset() + "," + (preset.backgroundOnly ? 2 : 1) + "," + preset.colour + "," + preset.intensity;
	}
	
	public MoodLightData getMoodlightData() {
		return this.moodlightData;
	}
	
}
