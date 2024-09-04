package com.cometproject.api.game.rooms.components;

import com.cometproject.api.game.groups.types.IGroup;
import com.cometproject.api.game.rooms.IRoom;

import java.util.List;

public interface IRightsComponent {
	
	void dispose();
	boolean hasRights(int playerId);
	boolean hasRights(int playerId, boolean checkGroup);
	default boolean checkGroupRights(int playerId) {
		final IGroup group = this.getRoom().getGroup();
		
		if (group != null && group.getData() != null && group.getMembers() != null && group.getMembers().getAll() != null) {
			if (group.getData().canMembersDecorate() && group.getMembers().getAll().containsKey(playerId)) {
				return true;
			}
			
			return group.getMembers().getAdministrators().contains(playerId);
		}
		return false;
	}
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
