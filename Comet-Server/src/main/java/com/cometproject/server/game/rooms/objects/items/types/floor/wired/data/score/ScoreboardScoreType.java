package com.cometproject.server.game.rooms.objects.items.types.floor.wired.data.score;

public enum ScoreboardScoreType {
    PERTEAM(0),
    MOSTWIN(1),
    CLASSIC(2);

    private final int type;

    ScoreboardScoreType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static ScoreboardScoreType getByScoreType(int type) {
		return switch (type) {
			case 0 -> PERTEAM;
			case 1 -> MOSTWIN;
			default -> CLASSIC;
		};
	}
}
