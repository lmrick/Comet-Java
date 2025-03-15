package com.cometproject.server.game.rooms;

import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.outgoing.room.queue.RoomQueueStatusMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomQueue {
	private static RoomQueue instance;
	private final Map<Integer, List<Integer>> roomQueues;
	
	public RoomQueue() {
		this.roomQueues = Maps.newConcurrentMap();
		this.roomQueues.put(1, new ArrayList<>());
	}
	
	public static RoomQueue getInstance() {
		if (instance == null) instance = new RoomQueue();
		return instance;
	}
	
	public boolean hasQueue(int roomId) {
		return this.roomQueues.containsKey(roomId);
	}
	
	public List<Integer> getQueue(int roomId) {
		return this.roomQueues.get(roomId);
	}
	
	public void addQueue(int roomId, int startingPlayer) {
		this.roomQueues.put(roomId, startingPlayer == 0 ? new ArrayList<>() : new ArrayList<>(startingPlayer));
	}
	
	public void removeQueue(int roomId) {
		this.roomQueues.remove(roomId);
	}
	
	public void removePlayerFromQueue(int roomId, Integer playerId) {
		if (this.hasQueue(roomId)) {
			this.roomQueues.get(roomId).remove(playerId);
			
			this.roomQueues.get(roomId).forEach(player -> {
				Session session = NetworkManager.getInstance().getSessions().getByPlayerId(player);
				if (session != null) session.send(new RoomQueueStatusMessageComposer(this.getQueueCount(roomId, player)));
			});
		}
	}
	
	public void addPlayerToQueue(int roomId, int playerId) {
		if (!this.hasQueue(roomId)) return;
		this.getQueue(roomId).add(playerId);
	}
	
	public int getNextPlayer(int roomId) {
		return !this.hasQueue(roomId) ? 0 : this.getQueue(roomId).getFirst();
	}
	
	public int getQueueCount(int roomId, int playerId) {
		return !this.hasQueue(roomId) ? 0 : (int) this.getQueue(roomId).stream().mapToInt(player -> player).takeWhile(player -> player != playerId).count();
	}
	
}
