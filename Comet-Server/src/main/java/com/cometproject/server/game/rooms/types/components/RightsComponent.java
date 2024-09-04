package com.cometproject.server.game.rooms.types.components;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.game.groups.types.IGroup;
import com.cometproject.api.game.rooms.IRoom;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.components.types.RoomBan;
import com.cometproject.server.game.rooms.types.components.types.RoomMute;
import com.cometproject.server.network.ws.messages.alerts.MutedMessage;
import com.cometproject.server.storage.queries.rooms.RightsDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class RightsComponent implements IRightsComponent {
	
	private final Room room;
	private List<Integer> rights;
	private final Map<Integer, RoomBan> bannedPlayers;
	private final Map<Integer, RoomMute> mutedPlayers;
	
	public RightsComponent(Room room) {
		this.room = room;
		
		try {
			if (room.getCachedData() != null) {
				this.rights = room.getCachedData().getRights();
			} else {
				this.rights = RightsDao.getRightsByRoomId(room.getId());
			}
		} catch (Exception e) {
			this.rights = new CopyOnWriteArrayList<>();
			this.room.log.error("Error while loading room rights", e);
		}
		
		this.bannedPlayers = RightsDao.getRoomBansByRoomId(this.room.getId());
		this.mutedPlayers = new ConcurrentHashMap<>();
	}
	
	@Override
	public void dispose() {
		this.rights.clear();
		this.bannedPlayers.clear();
	}
	
	@Override
	public boolean hasRights(int playerId) {
		return this.hasRights(playerId, true);
	}
	
	@Override
	public boolean hasRights(int playerId, boolean checkGroup) {
		if (checkGroup && checkGroupRights(playerId)) return true;
		
		return this.room.getData().getOwnerId() == playerId || this.rights.contains(playerId);
	}
	
	@Override
	public boolean canPlaceFurniture(final int playerId) {
		if (checkGroupRights(playerId)) return true;
		
		if (this.hasRights(playerId, false) && CometSettings.playerRightsItemPlacement) {
			return true;
		}
		
		return this.room.getData().getOwnerId() == playerId;
	}
	
	@Override
	public void removeRights(int playerId) {
		if (this.rights.contains(playerId)) {
			this.rights.remove(rights.indexOf(playerId));
			RightsDao.delete(playerId, room.getId());
		}
	}
	
	@Override
	public void addRights(int playerId) {
		this.rights.add(playerId);
		RightsDao.add(playerId, this.room.getId());
	}
	
	@Override
	public void addBan(int playerId, String playerName, int expireTimestamp) {
		this.bannedPlayers.put(playerId, new RoomBan(playerId, playerName, expireTimestamp));
		RightsDao.addRoomBan(playerId, this.room.getId(), expireTimestamp);
	}
	
	@Override
	public void addMute(int playerId, int minutes) {
		final PlayerEntity playerEntity = ((Room) this.getRoom()).getEntities().getEntityByPlayerId(playerId);
		if (playerEntity != null) {
			if (playerEntity.getPlayer().getSession().getWsChannel() != null) {
				playerEntity.getPlayer().getSession().sendWs(new MutedMessage(MutedMessage.MuteType.USER_MUTE, true, null));
			}
		}
		
		this.mutedPlayers.put(playerId, new RoomMute(playerId, (minutes * 60) * 2));
	}
	
	@Override
	public boolean hasBan(int userId) {
		return this.bannedPlayers.containsKey(userId);
	}
	
	@Override
	public void removeBan(int playerId) {
		this.bannedPlayers.remove(playerId);
		
		// delete it from the db.
		RightsDao.deleteRoomBan(playerId, this.room.getId());
	}
	
	@Override
	public boolean hasMute(int playerId) {
		return this.mutedPlayers.containsKey(playerId);
	}
	
	@Override
	public int getMuteTime(int playerId) {
		if (this.hasMute(playerId)) {
			return this.mutedPlayers.get(playerId).getTicksLeft() / 2;
		}
		
		return 0;
	}
	
	@Override
	public void tick() {
		List<RoomBan> bansToRemove;
		List<RoomMute> mutesToRemove = new ArrayList<>();
		
		bansToRemove = this.bannedPlayers.values().stream().filter(ban -> ban.getExpireTimestamp() <= Comet.getTime() && !ban.isPermanent()).collect(Collectors.toList());
		
		this.mutedPlayers.values().forEach(mute -> {
			if (mute.getTicksLeft() <= 0) {
				final PlayerEntity entity = ((Room) this.getRoom()).getEntities().getEntityByPlayerId(mute.getPlayerId());
				
				if (entity != null) {
					if (entity.getPlayer().getSession().getWsChannel() != null) {
						entity.getPlayer().getSession().sendWs(new MutedMessage(MutedMessage.MuteType.USER_MUTE, false, null));
					}
				}
				
				mutesToRemove.add(mute);
			}
			mute.decreaseTicks();
		});
		
		
		bansToRemove.forEach(ban -> this.bannedPlayers.remove(ban.getPlayerId()));
		
		mutesToRemove.forEach(mute -> this.mutedPlayers.remove(mute.getPlayerId()));
		
		bansToRemove.clear();
		mutesToRemove.clear();
	}
	
	public Map<Integer, RoomBan> getBannedPlayers() {
		return this.bannedPlayers;
	}
	
	@Override
	public List<Integer> getAll() {
		return this.rights;
	}
	
	@Override
	public IRoom getRoom() {
		return this.room;
	}
	
}
