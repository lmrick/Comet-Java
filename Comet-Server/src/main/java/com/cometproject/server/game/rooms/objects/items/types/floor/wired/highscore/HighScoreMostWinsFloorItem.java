package com.cometproject.server.game.rooms.objects.items.types.floor.wired.highscore;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.types.Room;

import java.util.List;

public class HighScoreMostWinsFloorItem extends HighScoreFloorItem {
    public HighScoreMostWinsFloorItem(RoomItemData roomItemData, Room room) {
        super(roomItemData, room);
    }

    @Override
    public void onTeamWins(List<String> usernames, int score) {
        this.addEntry(usernames, 1, true, true);
    }

    @Override
    public int getScoreType() {
        return 1;
    }
}
