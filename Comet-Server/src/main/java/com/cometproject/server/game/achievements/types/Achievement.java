package com.cometproject.server.game.achievements.types;

import com.cometproject.api.game.achievements.types.IAchievement;

public record Achievement(int level, int rewardActivityPoints, int rewardAchievement, int progressNeeded) implements IAchievement {

}
