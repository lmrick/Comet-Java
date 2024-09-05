package com.cometproject.server.logging.entries;

import com.cometproject.server.boot.Comet;

public class RoomVisitLogEntry {
	
	private int id;
	private int playerId;
	private int roomId;
	private long entryTime;
	private long exitTime;
	
	public RoomVisitLogEntry(int id, int playerId, int roomId, int time) {
		this.id = id;
		this.playerId = playerId;
		this.roomId = roomId;
		this.entryTime = time;
		this.exitTime = 0;
	}
	
	public RoomVisitLogEntry(int id, int playerId, int roomId, long timeEnter, long timeExit) {
		this.id = id;
		this.playerId = playerId;
		this.roomId = roomId;
		this.entryTime = timeEnter;
		this.exitTime = timeExit == 0 ? Comet.getTime() : timeExit;
	}
	
	public int getPlayerId() {
		return playerId;
	}
	
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}
	
	public int getRoomId() {
		return roomId;
	}
	
	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}
	
	public long getEntryTime() {
		return entryTime;
	}
	
	public void setEntryTime(long entryTime) {
		this.entryTime = entryTime;
	}
	
	public long getExitTime() {
		return exitTime;
	}
	
	public void setExitTime(long exitTime) {
		this.exitTime = exitTime;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
}
