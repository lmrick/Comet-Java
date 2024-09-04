package com.cometproject.api.game.achievements.types;

public interface IAchievement {
    int level();

    int rewardActivityPoints();

    int rewardAchievement();

    int progressNeeded();
}
