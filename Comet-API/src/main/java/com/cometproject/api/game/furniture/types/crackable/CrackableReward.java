package com.cometproject.api.game.furniture.types.crackable;

public record CrackableReward(int hitRequirement, CrackableRewardType rewardType, CrackableType crackableType, String rewardData,
                              int rewardDataInt) {
    
}
