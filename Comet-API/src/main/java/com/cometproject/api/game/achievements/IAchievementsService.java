package com.cometproject.api.game.achievements;

import com.cometproject.api.game.achievements.types.AchievementType;
import com.cometproject.api.game.achievements.types.IAchievementGroup;
import com.cometproject.api.utilities.Initializable;

import java.util.Map;

public interface IAchievementsService extends Initializable {
    void loadAchievements();

    IAchievementGroup getAchievementGroup(AchievementType groupName);

    Map<AchievementType, IAchievementGroup> getAchievementGroups();
}
