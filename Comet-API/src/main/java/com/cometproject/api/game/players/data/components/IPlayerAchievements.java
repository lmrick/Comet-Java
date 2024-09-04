package com.cometproject.api.game.players.data.components;

import com.cometproject.api.game.achievements.types.AchievementType;
import com.cometproject.api.game.players.components.IPlayerComponent;
import com.cometproject.api.game.players.data.components.achievements.IAchievementProgress;

public interface IPlayerAchievements extends IPlayerComponent {

    void progressAchievement(AchievementType type, int data);
    void loadAchievements();
    boolean hasStartedAchievement(AchievementType achievementType);
    IAchievementProgress getProgress(AchievementType achievementType);
    
}
