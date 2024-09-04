package com.cometproject.api.game.rooms.components.types;

import com.cometproject.api.game.rooms.IRoom;
import com.cometproject.api.game.rooms.components.IRoomComponent;
import java.util.List;

public interface IRightsComponent extends IRoomComponent {
	
	boolean hasRights(int playerId);
	boolean hasRights(int playerId, boolean checkGroup);
	boolean checkGroupRights(int playerId);
	boolean canPlaceFurniture(int playerId);
	void removeRights(int playerId);
	void addRights(int playerId);
	void addBan(int playerId, String playerName, int expireTimestamp);
	void addMute(int playerId, int minutes);
	boolean hasBan(int userId);
	void removeBan(int playerId);
	boolean hasMute(int playerId);
	int getMuteTime(int playerId);
	void tick();
	List<Integer> getAll();
	IRoom getRoom();
	
}
