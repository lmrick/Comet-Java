package com.cometproject.server.game.players.components.types.achievements;

import com.cometproject.api.game.players.data.components.achievements.IAchievementProgress;

public class AchievementProgress implements IAchievementProgress {
	
	private int level;
	private int progress;
	
	public AchievementProgress(int level, int progress) {
		this.level = level;
		this.progress = progress;
	}
	@Override
	public void increaseProgress(int amount) {
		this.progress += amount;
	}
	@Override
	public void decreaseProgress(int difference) {
		this.progress -= difference;
	}
	@Override
	public void increaseLevel() {
		this.level += 1;
	}
	@Override
	public int getLevel() {
		return this.level;
	}
	@Override
	public int getProgress() {
		return this.progress;
	}
	@Override
	public void setProgress(int progress) {
		this.progress = progress;
	}
	
}
