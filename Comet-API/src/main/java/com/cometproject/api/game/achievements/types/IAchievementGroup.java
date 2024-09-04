package com.cometproject.api.game.achievements.types;

import java.util.Map;

public interface IAchievementGroup {
    int id();

    int getLevelCount();

    IAchievement getAchievement(int level);

    Map<Integer, IAchievement> achievements();

    String groupName();

    AchievementCategory category();
}
