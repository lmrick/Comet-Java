package com.cometproject.server.game.rooms.objects.items.types.floor.wired.highscore;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.data.score.ScoreboardItemData;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.data.score.ScoreboardScoreType;
import com.cometproject.server.game.rooms.types.Room;
import java.util.List;

public class HighScorePerTeamFloorItem extends HighScoreFloorItem {
    
    public HighScorePerTeamFloorItem(RoomItemData roomItemData, Room room) {
        super(roomItemData, room);
    }

    @Override
    public void onTeamWins(List<String> usernames, int score) {
        // do nothing
    }

    public void onScoreIncrease(List<String> usernames, int scoreIncrease, int currentScore, boolean hasTimers) {
        final ScoreboardItemData.HighScoreEntry currentEntry = this.getScoreData().getEntryByTeam(usernames);
        if (currentEntry != null && hasTimers) {
            if (currentEntry.getScore() < currentScore) {
                currentEntry.setScore(currentScore);

                this.getScoreData().reorder();
                this.update();
            }
        } else {
            this.addEntry(usernames, scoreIncrease, true, true);
        }
    }

    @Override
    public int getScoreType() {
        return ScoreboardScoreType.PERTEAM.getType();
    }
}
