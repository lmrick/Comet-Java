package com.cometproject.server.game.players.components.types.settings;

import com.cometproject.api.game.players.data.types.IPlaylistItem;

public class PlaylistItem implements IPlaylistItem {
	
	private String videoId;
	private String title;
	private String description;
	private int duration;
	
	public PlaylistItem(String videoId, String title, String description, int duration) {
		this.videoId = videoId;
		this.title = title;
		this.description = description;
		this.duration = duration;
	}
	
	@Override
	public String getVideoId() {
		return videoId;
	}
	
	@Override
	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public int getDuration() {
		return duration;
	}
	
	@Override
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
}
