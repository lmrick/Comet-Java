package com.cometproject.server.game.rooms;

import com.cometproject.api.caching.Cache;
import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.players.IPlayer;
import com.cometproject.api.game.rooms.IRoomData;
import com.cometproject.api.game.rooms.models.CustomFloorMapData;
import com.cometproject.api.game.rooms.settings.RoomAccessType;
import com.cometproject.api.game.rooms.settings.RoomTradeState;
import com.cometproject.api.networking.sessions.ISession;
import com.cometproject.api.utilities.Initializable;
import com.cometproject.common.caching.LastReferenceCache;
import com.cometproject.server.game.players.types.Player;
import com.cometproject.server.game.rooms.filter.WordFilter;
import com.cometproject.server.game.rooms.models.types.StaticRoomModel;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.WiredUtil;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.components.types.promotion.RoomPromotion;
import com.cometproject.server.game.rooms.types.components.types.listeners.RoomReloadListener;
import com.cometproject.server.game.rooms.types.components.types.chat.emotions.ChatEmotionsManager;
import com.cometproject.server.game.rooms.types.components.types.vote.RoomVote;
import com.cometproject.server.network.messages.outgoing.room.events.RoomPromotionMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.storage.cache.CacheManager;
import com.cometproject.server.storage.cache.objects.RoomDataObject;
import com.cometproject.server.storage.queries.rooms.RoomDao;
import com.cometproject.server.tasks.CometThreadManager;
import org.apache.log4j.Logger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoomManager implements Initializable {
	
	public static final Logger log = Logger.getLogger(RoomManager.class.getName());
	private static RoomManager roomManagerInstance;
	private Cache<Integer, IRoomData> roomDataInstances;
	
	private Map<Integer, Room> loadedRoomInstances;
	private Map<Integer, Room> unloadingRoomInstances;
	private final Set<Integer> deletedRooms = new HashSet<>();
	
	private Map<Integer, RoomPromotion> roomPromotions;
	
	private Map<String, StaticRoomModel> models;
	private WordFilter filterManager;
	
	private RoomCycle globalCycle;
	private ChatEmotionsManager emotions;
	
	private ExecutorService executorService;
	
	private Map<Integer, RoomReloadListener> reloadListeners;
	
	private RoomVote roomVote;
	
	public RoomManager() {
	
	}
	
	public static RoomManager getInstance() {
		if (roomManagerInstance == null) roomManagerInstance = new RoomManager();
		return roomManagerInstance;
	}
	
	@Override
	public void initialize() {
		this.loadedRoomInstances = new ConcurrentHashMap<>();
		this.unloadingRoomInstances = new ConcurrentHashMap<>();
		this.roomPromotions = new ConcurrentHashMap<>();
		this.reloadListeners = new ConcurrentHashMap<>();
		
		this.emotions = new ChatEmotionsManager();
		this.filterManager = new WordFilter();
		
		this.globalCycle = new RoomCycle();
		
		this.loadPromotedRooms();
		
		this.globalCycle.start();
		
		this.executorService = Executors.newFixedThreadPool(2, r -> {
			final Thread roomThread = new Thread(r, "Room-Load-Worker-" + UUID.randomUUID());
			
			roomThread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
			
			return roomThread;
		});
		
		this.roomDataInstances = new LastReferenceCache<>(43200 * 1000, 10000, (key, val) -> {
		}, CometThreadManager.getInstance().getCoreExecutor());
		
		log.info("RoomManager initialized");
	}
	
	public void loadPromotedRooms() {
		RoomDao.deleteExpiredRoomPromotions();
		RoomDao.getActivePromotions(this.roomPromotions);
		
		log.info("Loaded " + this.getRoomPromotions().size() + " room promotions");
	}
	
	public void initializeRoom(Session initializer, int roomId, String password) {
		this.executorService.submit(() -> {
			if (initializer != null && initializer.getPlayer() != null) {
				initializer.getPlayer().loadRoom(roomId, password);
			}
		});
	}
	
	public Room get(int id) {
		if (id < 1) return null;
		
		if (this.getRoomInstances().containsKey(id)) {
			return this.getRoomInstances().get(id);
		}
		
		Room room = null;
		
		if (CacheManager.getInstance().isEnabled() && CacheManager.getInstance().exists("rooms." + id)) {
			final RoomDataObject roomDataObject = CacheManager.getInstance().get(RoomDataObject.class, "rooms." + id);
			
			if (roomDataObject != null) {
				room = new Room(roomDataObject);
			}
		}
		
		if (room == null) {
			IRoomData data = GameContext.getCurrent().getRoomService().getRoomData(id);
			
			if (data == null) {
				return null;
			}
			
			room = new Room(data);
		}
		
		room.load();
		
		this.loadedRoomInstances.put(id, room);
		
		this.finalizeRoomLoad(room);
		
		return room;
	}
	
	private void finalizeRoomLoad(Room room) {
		if (room == null) {
			return;
		}
		room.getItems().onLoaded();
	}
	
	public void roomDeleted(int roomId) {
		this.removeData(roomId);
		this.forceUnload(roomId);
		
		this.deletedRooms.add(roomId);
	}
	
	public void unloadIdleRooms() {
		this.unloadingRoomInstances.values().forEach(room -> this.executorService.submit(() -> {
			room.dispose();
			
			if (room.isReloading()) {
				Room newRoom = this.get(room.getId());
				
				if (newRoom != null) {
					if (this.reloadListeners.containsKey(room.getId())) {
						final RoomReloadListener reloadListener = this.reloadListeners.get(newRoom.getId());
						
						reloadListener.onReloaded(newRoom);
						this.reloadListeners.remove(room.getId());
					}
				}
			}
		}));
		
		this.unloadingRoomInstances.clear();
		
		List<Room> idleRooms = this.loadedRoomInstances.values().stream().filter(Room::isIdle).toList();
		
		idleRooms.forEach(room -> {
			this.loadedRoomInstances.remove(room.getId());
			this.unloadingRoomInstances.put(room.getId(), room);
		});
	}
	
	public void forceUnload(int id) {
		if (this.loadedRoomInstances.containsKey(id)) {
			this.loadedRoomInstances.remove(id).dispose();
		}
	}
	
	public void removeData(int roomId) {
		if (this.getRoomDataInstances().get(roomId) == null) {
			return;
		}
		
		this.getRoomDataInstances().remove(roomId);
	}
	
	public void addReloadListener(int roomId, RoomReloadListener listener) {
		this.reloadListeners.put(roomId, listener);
	}
	
	public void loadRoomsForUser(IPlayer player) {
		player.getRooms().clear();
		player.getRoomsWithRights().clear();
		
		Map<Integer, IRoomData> rooms = RoomDao.getRoomsByPlayerId(player.getId());
		Map<Integer, IRoomData> roomsWithRights = RoomDao.getRoomsWithRightsByPlayerId(player.getId());
		
		rooms.forEach((key, value) -> {
			player.getRooms().add(key);
			if (!this.getRoomDataInstances().contains(key)) {
				this.getRoomDataInstances().add(key, value);
			}
		});
		
		roomsWithRights.forEach((key, value) -> {
			player.getRoomsWithRights().add(key);
			
			if (!this.getRoomDataInstances().contains(key)) {
				this.getRoomDataInstances().add(key, value);
			}
		});
	}
	
	public List<IRoomData> getRoomsByQuery(String query) {
		List<IRoomData> rooms = new ArrayList<>();
		
		switch (query) {
			case "owner:", "tag:", "group:" -> {
				return rooms;
			}
		}
		
		if (query.startsWith("roomname:")) {
			query = query.substring(9);
		}
		
		List<IRoomData> roomSearchResults = RoomDao.getRoomsByQuery(query);
		
		roomSearchResults.forEach(data -> {
			if (!this.getRoomDataInstances().contains(data.getId())) {
				this.getRoomDataInstances().add(data.getId(), data);
			}
			rooms.add(data);
		});
		
		if (rooms.isEmpty() && !query.toLowerCase().startsWith("owner:")) {
			return this.getRoomsByQuery("owner:" + query);
		}
		
		return rooms;
	}
	
	public boolean isActive(int id) {
		return this.getRoomInstances().containsKey(id);
	}
	
	public int createRoom(String name, String description, CustomFloorMapData model, int category, int maxVisitors, int tradeState, ISession client, int wallTickness, int floorThickness, String decorations, boolean hideWalls) {
		int roomId = RoomDao.createRoom(name, model, description, category, maxVisitors, RoomTradeState.valueOf(tradeState), client.getPlayer().getId(), client.getPlayer().getData().getUsername(), wallTickness, floorThickness, decorations, hideWalls);
		this.loadRoomsForUser(client.getPlayer());
		return roomId;
	}
	
	public int createRoom(String name, String description, String model, int category, int maxVisitors, int tradeState, Session client) {
		int roomId = RoomDao.createRoom(name, model, description, category, maxVisitors, RoomTradeState.valueOf(tradeState), client.getPlayer().getId(), client.getPlayer().getData().getUsername());
		this.loadRoomsForUser(client.getPlayer());
		return roomId;
	}
	
	public void rightsRoomsUpdate(Session client) {
		this.loadRoomsForUser(client.getPlayer());
	}
	
	private List<Integer> getActiveAvailableRooms() {
		final List<Integer> rooms = new ArrayList<>();
		
		this.loadedRoomInstances.values().stream().filter(activeRoom -> !this.unloadingRoomInstances.containsKey(activeRoom.getId())).forEachOrdered(activeRoom -> {
			final int playerCount = activeRoom.getEntities().playerCount();
			if (playerCount != 0 && playerCount < activeRoom.getData().getMaxUsers() && activeRoom.getData().getAccess() == RoomAccessType.OPEN) {
				rooms.add(activeRoom.getId());
			}
		});
		
		return rooms;
	}
	
	public int getRandomActiveRoom() {
		final List<Integer> rooms = this.getActiveAvailableRooms();
		final Integer roomId = WiredUtil.getRandomElement(rooms);
		
		rooms.clear();
		
		return roomId != null ? roomId : -1;
	}
	
	public List<IRoomData> getRoomsByCategory(int category, Player player) {
		return this.getRoomsByCategory(category, 0, player);
	}
	
	public List<IRoomData> getRoomsByCategory(int category, int minimumPlayers, Player player) {
		List<IRoomData> rooms = new ArrayList<>();
		
		this.getRoomInstances().values().stream().filter(room -> category == -1 || (room.getCategory() != null && room.getCategory().getId() == category)).filter(room -> room.getEntities() == null || room.getEntities().playerCount() >= minimumPlayers).forEachOrdered(room -> {
			if (room.getData().getAccess() == RoomAccessType.INVISIBLE && player.getData().getRank() < 3) {
				if (room.getGroup() != null) {
					if (!player.getGroups().contains(room.getGroup().getId())) {
						return;
					}
				} else {
					if (!room.getRights().hasRights(player.getId())) {
						return;
					}
				}
			}
			rooms.add(room.getData());
		});
		
		return rooms;
	}
	
	public void promoteRoom(int roomId, String name, String description) {
		if (this.roomPromotions.containsKey(roomId)) {
			RoomPromotion promo = this.roomPromotions.get(roomId);
			promo.setTimestampFinish(promo.getTimestampFinish() + (RoomPromotion.DEFAULT_PROMO_LENGTH * 60));
			
			RoomDao.updatePromotedRoom(promo);
		} else {
			RoomPromotion roomPromotion = new RoomPromotion(roomId, name, description);
			RoomDao.createPromotedRoom(roomPromotion);
			
			this.roomPromotions.put(roomId, roomPromotion);
		}
		
		final Room room = this.get(roomId);
		
		if (room != null) {
			if (room.getEntities() != null && room.getEntities().realPlayerCount() >= 1) {
				room.getEntities().broadcastMessage(new RoomPromotionMessageComposer(room.getData(), this.roomPromotions.get(roomId)));
			}
		}
	}
	
	public boolean hasPromotion(int roomId) {
		return this.roomPromotions.containsKey(roomId) && !this.roomPromotions.get(roomId).isExpired();
	}
	
	public final ChatEmotionsManager getEmotions() {
		return this.emotions;
	}
	
	public final Map<Integer, Room> getRoomInstances() {
		return this.loadedRoomInstances;
	}
	
	private Cache<Integer, IRoomData> getRoomDataInstances() {
		return this.roomDataInstances;
	}
	
	public final RoomCycle getGlobalCycle() {
		return this.globalCycle;
	}
	
	public final WordFilter getFilter() {
		return filterManager;
	}
	
	public Map<Integer, RoomPromotion> getRoomPromotions() {
		return roomPromotions;
	}
	
	public RoomVote getRoomVote() {
		return roomVote;
	}
	
	public void setRoomVote(RoomVote roomVote) {
		this.roomVote = roomVote;
	}
	
}
