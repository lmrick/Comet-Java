package com.cometproject.server.game.achievements;

import com.cometproject.api.game.achievements.types.AchievementCategory;
import com.cometproject.api.game.achievements.types.IAchievement;
import com.cometproject.api.game.achievements.types.IAchievementGroup;
import java.util.Map;

public record AchievementGroup(int id, Map<Integer, IAchievement> achievements, String groupName, AchievementCategory category) implements IAchievementGroup {
    
    @Override
    public int getLevelCount() {
        return this.achievements.size();
    }
    
    @Override
    public IAchievement getAchievement(int level) {
        return this.achievements.get(level);
    }
    
    
}
