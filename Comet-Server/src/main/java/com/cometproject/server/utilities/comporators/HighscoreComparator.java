package com.cometproject.server.utilities.comporators;

import com.cometproject.server.game.rooms.objects.items.types.floor.wired.data.ScoreboardItemData;

import java.util.Comparator;

public class HighscoreComparator implements Comparator<ScoreboardItemData.HighScoreEntry> {
	
	@Override
	public int compare(ScoreboardItemData.HighScoreEntry firstEntry, ScoreboardItemData.HighScoreEntry secondEntry) {
		return firstEntry.getScore() < secondEntry.getScore() ? 1 : -1;
	}
	
}
