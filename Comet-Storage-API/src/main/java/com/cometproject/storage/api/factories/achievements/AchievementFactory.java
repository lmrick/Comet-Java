package com.cometproject.storage.api.factories.achievements;

import java.util.Map;
import com.cometproject.api.game.achievements.types.AchievementCategory;
import com.cometproject.api.game.achievements.types.IAchievement;
import com.cometproject.api.game.achievements.types.IAchievementGroup;
import com.cometproject.storage.api.data.achievements.Achievement;
import com.cometproject.storage.api.data.achievements.AchievementGroup;

public class AchievementFactory {

    public IAchievement createAchievement(int level, int rewardActivityPoints, int rewardAchievement, int progressNeeded) {
        return new Achievement(level, rewardActivityPoints, rewardAchievement, progressNeeded);
    }

    public IAchievementGroup createAchievementGroup(int id, Map<Integer, IAchievement> achievements, String groupName, AchievementCategory category) {
        return new AchievementGroup(id, achievements, groupName, category);
    }
    
}
