package com.cometproject.server.game.players;

import com.cometproject.api.config.Configuration;
import com.cometproject.api.game.players.IPlayerService;
import com.cometproject.api.game.players.data.IPlayerAvatar;
import com.cometproject.api.game.players.data.IPlayerData;
import com.cometproject.api.networking.sessions.ISession;
import com.cometproject.api.utilities.caching.Cache;
import com.cometproject.common.caching.LastReferenceCache;
import com.cometproject.server.game.players.data.PlayerData;
import com.cometproject.server.game.players.login.PlayerLoginRequest;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.storage.queries.player.PlayerDao;
import com.cometproject.server.tasks.CometThreadManager;
import com.cometproject.server.tasks.CometConstants;

import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerManager implements IPlayerService {
	
	private static PlayerManager playerManagerInstance;
	private static final Logger log = Logger.getLogger(PlayerManager.class.getName());
	
	private Map<Integer, Integer> playerIdToSessionId;
	private Map<String, Integer> playerUsernameToPlayerId;
	
	private Map<String, List<Integer>> ipAddressToPlayerIds;
	
	private Map<String, Integer> ssoTicketToPlayerId;
	private Map<Integer, String> playerIdToUsername;
	private Map<String, Integer> authTokenToPlayerId;
	private Cache<Integer, IPlayerAvatar> playerAvatarCache;
	private Cache<Integer, IPlayerData> playerDataCache;
	private ExecutorService playerLoginService;
	
	public PlayerManager() {
	
	}
	
	public static PlayerManager getInstance() {
		if (playerManagerInstance == null) playerManagerInstance = new PlayerManager();
		return playerManagerInstance;
	}
	
	@Override
	public void initialize() {
		this.playerIdToSessionId = new ConcurrentHashMap<>();
		this.playerUsernameToPlayerId = new ConcurrentHashMap<>();
		this.ipAddressToPlayerIds = new ConcurrentHashMap<>();
		this.ssoTicketToPlayerId = new ConcurrentHashMap<>();
		
		this.playerLoginService = CometConstants.PLAYER_LOGIN_EXECUTOR;
		
		if ((boolean) Configuration.currentConfig().getOrDefault("comet.cache.players", true)) {
			log.info("Initializing Player cache");
			
			this.playerAvatarCache = new LastReferenceCache<>(43200 * 1000, 10000, (key, val) -> {}, CometThreadManager.getInstance().getCoreExecutor());
			this.playerDataCache = new LastReferenceCache<>(43200 * 1000, 10000, (key, val) -> {}, CometThreadManager.getInstance().getCoreExecutor());
			
		} else {
			log.info("Player data cache is disabled.");
		}
		
		log.info("Resetting player online status");
		PlayerDao.resetOnlineStatus();
		
		log.info("PlayerManager initialized");
	}
	
	@Override
	public void submitLoginRequest(ISession client, String ticket) {
		this.playerLoginService.submit(new PlayerLoginRequest((Session) client, ticket));
	}
	
	@Override
	public IPlayerAvatar getAvatarByPlayerId(int playerId, byte mode) {
		if (this.isOnline(playerId)) {
			Session session = NetworkManager.getInstance().getSessions().getByPlayerId(playerId);
			
			if (session != null && session.getPlayer() != null && session.getPlayer().getData() != null) {
				return session.getPlayer().getData();
			}
		}
		
		if (this.playerDataCache != null) {
			var cachedElement = this.playerDataCache.get(playerId);
			
			if (cachedElement != null) {
				return cachedElement;
			}
		}
		
		if (this.playerAvatarCache != null) {
			var cachedElement = this.playerAvatarCache.get(playerId);
			
			if (cachedElement != null) {
				
				if (cachedElement.getMotto() == null && mode == IPlayerAvatar.USERNAME_FIGURE_MOTTO) {
					cachedElement.setMotto(PlayerDao.getMottoByPlayerId(playerId));
				}
				
				return cachedElement;
			}
		}
		
		IPlayerAvatar playerAvatar = PlayerDao.getAvatarById(playerId, mode);
		if (playerAvatar != null && this.playerAvatarCache != null) {
			this.playerAvatarCache.add(playerId, playerAvatar);
		}
		
		return playerAvatar;
	}
	
	public void removeData(int playerId, boolean avatarData) {
		if (this.playerAvatarCache.get(playerId) == null || this.playerDataCache.get(playerId) == null) {
			return;
		}
		
		if (avatarData) {
			this.playerAvatarCache.remove(playerId);
		}
		
		this.playerDataCache.remove(playerId);
	}
	
	@Override
	public PlayerData getDataByPlayerId(int playerId) {
		if (this.isOnline(playerId)) {
			Session session = NetworkManager.getInstance().getSessions().getByPlayerId(playerId);
			
			if (session != null && session.getPlayer() != null && session.getPlayer().getData() != null) {
				return session.getPlayer().getData();
			}
		}
		
		if (this.playerDataCache != null) {
			var cachedElement = this.playerDataCache.get(playerId);
			
			if (cachedElement != null) {
				return (PlayerData) cachedElement;
			}
		}
		
		PlayerData playerData = PlayerDao.getDataById(playerId);
		
		if (playerData != null && this.playerDataCache != null) {
			this.playerDataCache.add(playerId, playerData);
		}
		
		return playerData;
	}
	
	@Override
	public int getPlayerCountByIpAddress(String ipAddress) {
		if (this.ipAddressToPlayerIds.containsKey(ipAddress)) {
			return this.ipAddressToPlayerIds.get(ipAddress).size();
		}
		
		return 0;
	}
	
	@Override
	public void put(int playerId, int sessionId, String username, String ipAddress) {
		this.playerIdToSessionId.remove(playerId);
		
		this.playerUsernameToPlayerId.remove(username.toLowerCase());
		
		if (!this.ipAddressToPlayerIds.containsKey(ipAddress)) {
			final List<Integer> list = new CopyOnWriteArrayList<Integer>() {{
				add(playerId);
			}};
			
			this.ipAddressToPlayerIds.put(ipAddress, list);
		} else {
			this.ipAddressToPlayerIds.get(ipAddress).add(playerId);
		}
		
		this.playerIdToSessionId.put(playerId, sessionId);
		this.playerUsernameToPlayerId.put(username.toLowerCase(), playerId);
	}
	
	@Override
	public void remove(int playerId, String username, int sessionId, String ipAddress) {
		if (this.getSessionIdByPlayerId(playerId) != sessionId) {
			return;
		}
		
		final List<Integer> playerIds = this.ipAddressToPlayerIds.get(ipAddress);
		
		if (playerIds != null && playerIds.contains(playerId)) {
			playerIds.remove((Integer) playerId);
			
			if (playerIds.isEmpty()) {
				this.ipAddressToPlayerIds.remove(ipAddress);
			}
		}
		
		this.playerIdToSessionId.remove(playerId);
		this.playerUsernameToPlayerId.remove(username.toLowerCase());
	}
	
	@Override
	public int getPlayerIdByUsername(String username) {
		if (this.playerUsernameToPlayerId.containsKey(username.toLowerCase())) {
			return this.playerUsernameToPlayerId.get(username.toLowerCase());
		}
		
		return -1;
	}
	
	@Override
	public int getSessionIdByPlayerId(int playerId) {
		if (this.playerIdToSessionId.containsKey(playerId)) {
			return this.playerIdToSessionId.get(playerId);
		}
		
		return -1;
	}
	
	@Override
	public void updateUsernameCache(final String oldName, final String newName) {
		final int playerId = this.getPlayerIdByUsername(oldName.toLowerCase());
		
		this.playerUsernameToPlayerId.remove(oldName.toLowerCase());
		this.playerUsernameToPlayerId.put(newName.toLowerCase(), playerId);
	}
	
	@Override
	public List<Integer> getPlayerIdsByIpAddress(String ipAddress) {
		return new ArrayList<>(this.ipAddressToPlayerIds.get(ipAddress));
	}
	
	@Override
	public boolean isOnline(int playerId) {
		return this.playerIdToSessionId.containsKey(playerId);
	}
	
	@Override
	public boolean isOnline(String username) {
		return this.playerUsernameToPlayerId.containsKey(username.toLowerCase());
	}
	
	@Override
	public int size() {
		return this.playerIdToSessionId.size();
	}
	
	@Override
	public Map<String, Integer> getSsoTicketToPlayerId() {
		return ssoTicketToPlayerId;
	}
	
	@Override
	public Integer getPlayerIdByAuthToken(String authToken) {
		return this.ssoTicketToPlayerId.get(authToken);
	}
	
	@Override
	public void createAuthToken(int playerId, String authToken) {
	
	}
	
	@Override
	public ExecutorService getPlayerLoadExecutionService() {
		return playerLoginService;
	}
	
}
