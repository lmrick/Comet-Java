package com.cometproject.storage.api.repositories;

import java.util.Map;
import java.util.function.Consumer;
import com.cometproject.api.game.achievements.types.AchievementType;
import com.cometproject.api.game.achievements.types.IAchievementGroup;

public interface IAchievementRepository {
    
    void getAchievements(Map<AchievementType, IAchievementGroup> achievementGroups, Consumer<Integer> consumer);

}
