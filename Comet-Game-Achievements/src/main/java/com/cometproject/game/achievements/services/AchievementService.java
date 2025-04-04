package com.cometproject.game.achievements.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.cometproject.api.game.achievements.IAchievementsService;
import com.cometproject.api.game.achievements.types.AchievementType;
import com.cometproject.api.game.achievements.types.IAchievementGroup;
import com.cometproject.storage.api.data.DataWrapper;
import com.cometproject.storage.api.repositories.IAchievementRepository;

public class AchievementService implements IAchievementsService {
	private final IAchievementRepository achievementRepository;
    private final Map<AchievementType, IAchievementGroup> achievementGroups;

    public AchievementService(IAchievementRepository achievementRepository) {
		this.achievementRepository = achievementRepository;
		this.achievementGroups = new ConcurrentHashMap<>();
	}

    @Override
	public void initialize() {
		this.loadAchievements();
	}

    @Override
	public void loadAchievements() {
		if (!this.achievementGroups.isEmpty()) {
			this.achievementGroups.values().stream()
			.filter(achievementGroup -> !achievementGroup.achievements().isEmpty())
			.forEachOrdered(achievementGroup -> achievementGroup.achievements().clear());
			this.achievementGroups.clear();
		}

		final DataWrapper<Integer> achievementCount = new DataWrapper<>();
		this.achievementRepository.getAchievements(this.achievementGroups, achievementCount::set);
		System.out.println("Loaded " + achievementCount.get() + " achievements (" + this.achievementGroups.size() + " groups)");
    }

    @Override
	public IAchievementGroup getAchievementGroup(AchievementType groupName) {
		return this.achievementGroups.get(groupName);
	}
	
	@Override
	public Map<AchievementType, IAchievementGroup> getAchievementGroups() {
		return this.achievementGroups;
	}

}
