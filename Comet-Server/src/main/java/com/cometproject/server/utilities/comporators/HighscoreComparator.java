package com.cometproject.server.utilities.comporators;

import com.cometproject.server.game.rooms.objects.items.types.floor.wired.data.ScoreboardItemData;

import java.util.Comparator;

public class HighscoreComparator implements Comparator<ScoreboardItemData.HighScoreEntry> {
	
	@Override
	public int compare(ScoreboardItemData.HighScoreEntry o1, ScoreboardItemData.HighScoreEntry o2) {
		return o1.getScore() < o2.getScore() ? 1 : -1;
	}
	
}
