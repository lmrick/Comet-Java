package com.cometproject.server.utilities.comporators;

import java.util.Comparator;

import com.cometproject.server.game.rooms.objects.items.types.floor.wired.data.score.ScoreboardItemData;

public class HighscoreComparator implements Comparator<ScoreboardItemData.HighScoreEntry> {
	
	@Override
	public int compare(ScoreboardItemData.HighScoreEntry firstEntry, ScoreboardItemData.HighScoreEntry secondEntry) {
		return firstEntry.getScore() < secondEntry.getScore() ? 1 : -1;
	}
	
}
