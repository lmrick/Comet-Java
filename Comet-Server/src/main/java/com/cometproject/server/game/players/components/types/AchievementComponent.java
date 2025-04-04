package com.cometproject.server.game.players.components.types;

import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.achievements.IAchievementsService;
import com.cometproject.api.game.achievements.types.AchievementType;
import com.cometproject.api.game.achievements.types.IAchievement;
import com.cometproject.api.game.achievements.types.IAchievementGroup;
import com.cometproject.api.game.players.components.PlayerComponentContext;
import com.cometproject.api.game.players.data.components.IPlayerAchievements;
import com.cometproject.api.game.players.data.components.achievements.IAchievementProgress;
import com.cometproject.server.game.players.components.PlayerComponent;
import com.cometproject.server.game.players.components.types.achievements.AchievementProgress;
import com.cometproject.server.network.messages.outgoing.user.achievements.AchievementPointsMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.achievements.AchievementProgressMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.achievements.AchievementUnlockedMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.purse.UpdateActivityPointsMessageComposer;
import com.cometproject.server.storage.queries.achievements.PlayerAchievementDao;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Map;
import org.apache.log4j.Logger;

public class AchievementComponent extends PlayerComponent implements IPlayerAchievements {
	private final Logger LOG = super.getLogger(AchievementComponent.class);
	private Map<AchievementType, IAchievementProgress> progression;
	
	public AchievementComponent(PlayerComponentContext componentContext) {
		super(componentContext);
		
		this.loadAchievements();
	}
	
	@Override
	public void loadAchievements() {
		if (this.progression != null) {
			this.progression.clear();
		}
		
		this.progression = PlayerAchievementDao.getAchievementProgress(this.getPlayer().getId());

		// Notify player that achievements are updated
		this.getPlayer().flush(this);
	}
	
	@Override
	public void progressAchievement(AchievementType type, int data) {
		IAchievementGroup achievementGroup = GameContext.getCurrent().getService(IAchievementsService.class).getAchievementGroup(type);
		
		if (achievementGroup == null) {
			return;
		}
		
		IAchievementProgress progress;
		
		if (this.progression.containsKey(type)) {
			progress = this.progression.get(type);
		} else {
			progress = new AchievementProgress(1, 0);
			this.progression.put(type, progress);
		}
		
		if (achievementGroup.getAchievement(progress.getLevel()) == null) return;
		
		if (achievementGroup.achievements() == null) return;
		
		if (achievementGroup.achievements().size() <= progress.getLevel() && achievementGroup.getAchievement(progress.getLevel()).progressNeeded() <= progress.getProgress()) {
			return;
		}
		
		final int targetLevel = progress.getLevel() + 1;
		final IAchievement currentAchievement = achievementGroup.getAchievement(progress.getLevel());
		final IAchievement targetAchievement = achievementGroup.getAchievement(targetLevel);
		
		if (targetAchievement == null && achievementGroup.getLevelCount() != 1) {
			progress.setProgress(currentAchievement.progressNeeded());
			PlayerAchievementDao.saveProgress(this.getPlayer().getId(), type, progress);
			
			this.getPlayer().getData().save();
			this.getPlayer().getInventory().achievementBadge(type.getGroupName(), currentAchievement.level());
			return;
		}
		
		int progressToGive = Math.min(currentAchievement.progressNeeded(), data);
		int remainingProgress = progressToGive >= data ? 0 : data - progressToGive;
		
		progress.increaseProgress(progressToGive);
		
		if (progress.getProgress() > currentAchievement.progressNeeded()) {
			// subtract the difference and add it onto remainingProgress.
			int difference = progress.getProgress() - currentAchievement.progressNeeded();
			
			progress.decreaseProgress(difference);
			remainingProgress += difference;
		}
		
		if (currentAchievement.progressNeeded() <= progress.getProgress()) {
			this.processUnlock(currentAchievement, targetAchievement, achievementGroup, progress, targetLevel, type);
		} else {
			this.getPlayer().getSession().send(new AchievementProgressMessageComposer(progress, achievementGroup));
		}
		
		boolean hasFinishedGroup = progress.getLevel() >= achievementGroup.getLevelCount() && progress.getProgress() >= achievementGroup.getAchievement(achievementGroup.getLevelCount()).progressNeeded();
		
		if (remainingProgress != 0 && !hasFinishedGroup) {
			this.progressAchievement(type, remainingProgress);
			return;
		}
		
		this.getPlayer().getData().save();
		PlayerAchievementDao.saveProgress(this.getPlayer().getId(), type, progress);
		
		this.getPlayer().flush(this);
	}
	
	private void processUnlock(IAchievement currentAchievement, IAchievement targetAchievement, IAchievementGroup achievementGroup, IAchievementProgress progress, int targetLevel, AchievementType type) {
		this.getPlayer().getData().increaseAchievementPoints(currentAchievement.rewardAchievement());
		this.getPlayer().getData().increaseActivityPoints(currentAchievement.rewardActivityPoints());
		
		this.getPlayer().poof();
		
		this.getPlayer().getSession().send(this.getPlayer().composeCurrenciesBalance());
		this.getPlayer().getSession().send(new UpdateActivityPointsMessageComposer(this.getPlayer().getData().getActivityPoints(), currentAchievement.rewardAchievement()));
		
		if (achievementGroup.getAchievement(targetLevel) != null) {
			progress.increaseLevel();
		}
		
		// Achievement unlocked!
		this.getPlayer().getSession().send(new AchievementPointsMessageComposer(this.getPlayer().getData().getAchievementPoints()));
		this.getPlayer().getSession().send(new AchievementProgressMessageComposer(progress, achievementGroup));
		this.getPlayer().getSession().send(new AchievementUnlockedMessageComposer(achievementGroup.category().toString(), achievementGroup.groupName(), achievementGroup.id(), targetAchievement));
		
		this.getPlayer().getInventory().achievementBadge(type.getGroupName(), currentAchievement.level());
		
		this.getPlayer().flush(this);
	}
	
	@Override
	public boolean hasStartedAchievement(AchievementType achievementType) {
		return this.progression.containsKey(achievementType);
	}
	
	@Override
	public IAchievementProgress getProgress(AchievementType achievementType) {
		return this.progression.get(achievementType);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		if (this.progression != null) {
			this.progression.clear();
			this.progression = null;
		}
	}
	
	
	public JsonArray toJson() {
		final JsonArray coreArray = new JsonArray();
		
		progression.forEach((key, value) -> {
			final JsonObject achievementObject = new JsonObject();
			
			achievementObject.addProperty("type", key.getGroupName());
			achievementObject.addProperty("level", value.getLevel());
			achievementObject.addProperty("progress", value.getProgress());
			
			coreArray.add(achievementObject);
		});
		
		return coreArray;
	}
	
}
