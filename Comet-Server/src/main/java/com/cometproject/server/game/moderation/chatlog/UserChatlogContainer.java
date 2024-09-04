package com.cometproject.server.game.moderation.chatlog;

import com.cometproject.server.logging.entries.RoomChatLogEntry;

import java.util.ArrayList;
import java.util.List;

public class UserChatlogContainer {
	private final List<LogSet> logs;
	
	public UserChatlogContainer() {
		this.logs = new ArrayList<>();
	}
	
	public void addAll(int roomId, List<RoomChatLogEntry> chatLogs) {
		if (chatLogs.isEmpty()) return;
		this.logs.add(new LogSet(roomId, chatLogs));
	}
	
	public void dispose() {
		logs.forEach(logSet -> logSet.logs().clear());
		this.logs.clear();
	}
	
	public int size() {
		return logs.size();
	}
	
	public List<LogSet> getLogs() {
		return this.logs;
	}
	
	public record LogSet(int roomId, List<RoomChatLogEntry> logs) { }
	
}
