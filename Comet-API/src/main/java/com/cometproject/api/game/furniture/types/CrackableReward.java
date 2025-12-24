package com.cometproject.api.game.furniture.types;

public record CrackableReward(int hitRequirement, CrackableRewardType rewardType, CrackableType crackableType, String rewardData,
                              int rewardDataInt) {
    
}
