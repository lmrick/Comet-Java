package com.cometproject.api.game.players.components.types.achievements;

public interface IAchievementProgress {
    
    void increaseProgress(int amount);
    void decreaseProgress(int amount);
    void increaseLevel();
    int getLevel();
    int getProgress();
    void setProgress(int progress);
    
}
