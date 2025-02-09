package com.cometproject.server.game.rooms.types.components.types.chat.emotions;

public enum ChatEmotion {
    NONE(0),
    SMILE(1),
    ANGRY(2),
    SHOCKED(3),
    SAD(4),
    LAUGH(6);

    private final int emotionId;

    ChatEmotion(int emotionId) {
        this.emotionId = emotionId;
    }

    public int getEmotionId() {
        return emotionId;
    }
}
