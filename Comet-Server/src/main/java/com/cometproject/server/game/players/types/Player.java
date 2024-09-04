package com.cometproject.server.game.players.types;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.game.players.IPlayer;
import com.cometproject.api.game.players.PlayerContext;
import com.cometproject.api.game.players.components.IPlayerComponent;
import com.cometproject.api.game.players.components.PlayerComponentContext;
import com.cometproject.api.game.players.data.components.IPlayerInventory;
import com.cometproject.api.game.quests.IQuest;
import com.cometproject.api.game.quests.QuestType;
import com.cometproject.api.networking.sessions.ISession;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.guides.GuideManager;
import com.cometproject.server.game.guides.types.HelpRequest;
import com.cometproject.server.game.guides.types.HelperSession;
import com.cometproject.server.game.players.PlayerManager;
import com.cometproject.server.game.players.components.*;
import com.cometproject.server.game.players.components.types.*;
import com.cometproject.server.game.players.data.PlayerData;
import com.cometproject.server.game.quests.QuestManager;
import com.cometproject.server.game.rooms.RoomManager;
import com.cometproject.server.game.rooms.objects.entities.effects.PlayerEffect;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.components.types.ChatMessageColour;
import com.cometproject.server.network.messages.outgoing.notification.AdvancedAlertMessageComposer;
import com.cometproject.server.network.messages.outgoing.notification.MotdNotificationMessageComposer;
import com.cometproject.server.network.messages.outgoing.quests.QuestStartedMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.avatar.UpdateInfoMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.engine.HotelViewMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.purse.CurrenciesMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.purse.SendCreditsMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageComposer;
import com.cometproject.server.storage.queries.catalog.CatalogDao;
import com.cometproject.server.storage.queries.permissions.PermissionsDao;
import com.cometproject.server.storage.queries.player.PlayerDao;
import com.cometproject.server.utilities.collections.ConcurrentHashSet;
import com.cometproject.storage.api.StorageContext;
import com.google.common.collect.Sets;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Player implements IPlayer {
	
	private final PlayerContext playerContext;
	private PermissionComponent permissions;
	private InventoryComponent inventory;
	private SubscriptionComponent subscription; //TODO MOVE INTERFACE AND INJECT FROM CONTEXT
	private MessengerComponent messenger;
	private RelationshipComponent relationships;
	private InventoryBotComponent bots;
	private PetComponent pets;
	private QuestComponent quests;
	private AchievementComponent achievements;
	private NavigatorComponent navigator;
	private WardrobeComponent wardrobe; //TODO MOVE INTERFACE AND INJECT FROM CONTEXT
	
	private boolean online;
	
	public boolean cancelPageOpen = false;
	public boolean isDisposed = false;
	public int lastBannedListRequest = 0;
	private final int id;
	private PlayerSettings settings;
	private PlayerData data;
	private final PlayerStatistics stats;
	private PlayerEntity entity;
	private Session session;
	private HelperSession helperSession;
	private List<Integer> rooms = new ArrayList<>();
	private List<Integer> roomsWithRights = new ArrayList<>();
	private List<Integer> enteredRooms = new ArrayList<>();
	private Set<Integer> groups = Sets.newConcurrentHashSet();
	private List<Integer> ignoredPlayers = new ArrayList<>();
	private long roomLastMessageTime = 0;
	private double roomFloodTime = 0;
	private int lastForumPost = 0;
	private long lastRoomRequest = 0;
	private long lastBadgeUpdate = 0;
	private int lastFigureUpdate = 0;
	private int roomFloodFlag = 0;
	private long messengerLastMessageTime = 0;
	private double messengerFloodTime = 0;
	private int messengerFloodFlag = 0;
	private boolean usernameConfirmed = false;
	private long teleportId = 0;
	private int teleportRoomId = 0;
	private String lastMessage = "";
	private int lastVoucherRedeemAttempt = 0;
	private int voucherRedeemAttempts = 0;
	private int notifCooldown = 0;
	private int lastRoomId;
	private int lastGift = 0;
	private int lastRoomCreated = 0;
	private boolean isDeletingGroup = false;
	private long deletingGroupAttempt = 0;
	private boolean bypassRoomAuth;
	private long lastDiamondReward = 0;
	private long lastReward = 0;
	private boolean invisible = false;
	private int lastTradePlayer = 0;
	private long lastTradeTime = 0;
	private int lastTradeFlag = 0;
	private long lastTradeFlood = 0;
	private long lastPhotoTaken = 0;
	private double itemPlacementHeight = -1;
	private int itemPlacementState = -1;
	private String ssoTicket;
	private Set<Integer> recentPurchases;
	
	private Set<Integer> listeningPlayers = Sets.newConcurrentHashSet();
	private Set<String> eventLogCategories = Sets.newConcurrentHashSet();
	private ChatMessageColour chatMessageColour = null;
	private HelpRequest helpRequest = null;
	private boolean petsMuted;
	private boolean botsMuted;
	private String lastPhoto = null;
	private int roomQueueId = 0;
	private int spectatorRoomId = 0;
	
	public Player(ResultSet data, boolean isFallback) throws SQLException {
		this.id = data.getInt("playerId");
		
		this.data = new PlayerData(data, this);
		
		if (isFallback) {
			this.settings = PlayerDao.getSettingsById(this.id);
			this.stats = PlayerDao.getStatisticsById(this.id);
		} else {
			this.settings = new PlayerSettings(data, true, this);
			this.stats = new PlayerStatistics(data, true, this);
		}
		
		injectComponentDependencies();
		
		StorageContext.getCurrentContext().getGroupRepository().getGroupIdsByPlayerId(this.id, groups -> this.groups.addAll(groups));
		
		this.entity = null;
		this.lastReward = Comet.getTime();
		this.lastDiamondReward = Comet.getTime();
		
		this.playerContext = new PlayerContext(this);
		PlayerContext.setCurrentContext(playerContext);
		
	}
	
	public void injectComponentDependencies() {
		var componentCtx = createComponentContext();
		var componentFactory = new PlayerComponentFactory(componentCtx);
		
		injectComponents(componentCtx, componentFactory);
		assignComponents(componentCtx);
	}
	
	private PlayerComponentContext createComponentContext() {
		return new PlayerComponentContext(this);
	}
	
	private void injectComponents(PlayerComponentContext ctx, PlayerComponentFactory factory) {
		ctx.setPlayerPermissions(factory.createPlayerPermissionsComponent());
		ctx.setPlayerInventory(factory.createPlayerInventoryComponent());
		ctx.setPlayerMessenger(factory.createPlayerMessengerComponent());
		ctx.setPlayerRelationships(factory.createPlayerRelationshipsComponent());
		ctx.setPlayerBots(factory.createPlayerBotsComponent());
		ctx.setPlayerPets(factory.createPlayerPetsComponent());
		ctx.setPlayerQuests(factory.createPlayerQuestsComponent());
		ctx.setPlayerAchievements(factory.createPlayerAchievementsComponent());
		ctx.setPlayerNavigator(factory.createPlayerNavigatorComponent());
		ctx.setPlayerSubscription(factory.createPlayerSubscriptionComponent());
		ctx.setPlayerWardrobe(factory.createPlayerWardrobeComponent());
	}
	
	private void assignComponents(PlayerComponentContext ctx) {
		this.permissions = (PermissionComponent) ctx.getPlayerPermissions();
		this.inventory = (InventoryComponent) ctx.getPlayerInventory();
		this.messenger = (MessengerComponent) ctx.getPlayerMessenger();
		this.relationships = (RelationshipComponent) ctx.getPlayerRelationships();
		this.bots = (InventoryBotComponent) ctx.getPlayerBots();
		this.pets = (PetComponent) ctx.getPlayerPets();
		this.quests = (QuestComponent) ctx.getPlayerQuests();
		this.achievements = (AchievementComponent) ctx.getPlayerAchievements();
		this.navigator = (NavigatorComponent) ctx.getPlayerNavigator();
		this.wardrobe = (WardrobeComponent) ctx.getPlayerWardrobe();
		this.subscription = (SubscriptionComponent) ctx.getPlayerSubscription();
	}
	
	private void disposeAllComponents() {
		PlayerComponentFactory.getComponents().forEach(IPlayerComponent::dispose);
	}
	
	@Override
	public void dispose() {
		this.setOnline(false);
		flush();
		
		if (this.getEntity() != null) {
			try {
				this.getEntity().leaveRoom(true, false, false);
			} catch (Exception e) {
				this.getSession().getLogger().error("Error while disposing entity when player disconnects", e);
			}
		}
		
		if (this.helperSession != null) {
			GuideManager.getInstance().finishPlayerDuty(this.helperSession);
			this.helperSession = null;
		}
		
		disposeAllComponents();
		
		try {
			PlayerManager.getInstance().getSsoTicketToPlayerId().remove(this.ssoTicket);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.session.getLogger().debug(this.getData().getUsername() + " logged out");
		
		PlayerDao.updatePlayerStatus(this, this.isOnline(), false);
		
		this.rooms.clear();
		this.rooms = null;
		
		this.roomsWithRights.clear();
		this.roomsWithRights = null;
		
		this.groups.clear();
		this.groups = null;
		
		this.ignoredPlayers.clear();
		this.ignoredPlayers = null;
		
		this.enteredRooms.clear();
		this.enteredRooms = null;
		
		this.eventLogCategories.clear();
		this.eventLogCategories = null;
		
		if (this.recentPurchases != null) {
			this.recentPurchases.clear();
			this.recentPurchases = null;
		}
		
		this.listeningPlayers.clear();
		
		this.settings = null;
		this.data = null;
		
		this.isDisposed = true;
	}
	
	@Override
	public void sendBalance() {
		session.send(composeCurrenciesBalance());
		session.send(composeCreditBalance());
	}
	
	@Override
	public void sendNotif(String title, String message) {
		session.send(new AdvancedAlertMessageComposer(title, message));
	}
	
	@Override
	public void sendMotd(String message) {
		session.send(new MotdNotificationMessageComposer(message));
	}
	
	@Override
	public MessageComposer composeCreditBalance() {
		return new SendCreditsMessageComposer(CometSettings.playerInfiniteBalance ? INFINITE_BALANCE : Integer.toString(session.getPlayer().getData().getCredits()));
	}
	
	@Override
	public MessageComposer composeCurrenciesBalance() {
		if (getData() == null) {
			return null;
		}
		
		Map<Integer, Integer> currencies = new HashMap<>();
		currencies.put(0, getData().getActivityPoints());
		currencies.put(105, getData().getVipPoints());
		currencies.put(5, getData().getVipPoints());
		currencies.put(106, getData().getSeasonalPoints());
		
		return new CurrenciesMessageComposer(currencies);
	}
	
	@Override
	public void loadRoom(int id, String password) {
		if (entity != null && entity.getRoom() != null) {
			entity.leaveRoom(true, false, false);
			setEntity(null);
		}
		
		Room room = RoomManager.getInstance().get(id);
		
		if (room == null) {
			session.send(new HotelViewMessageComposer());
			return;
		}
		
		if (room.getEntities() == null) {
			return;
		}
		
		if (room.getEntities().getEntityByPlayerId(this.id) != null) {
			room.getEntities().getEntityByPlayerId(this.id).leaveRoom(true, false, false);
		}
		
		PlayerEntity playerEntity = room.getEntities().createEntity(this);
		setEntity(playerEntity);
		
		if (!playerEntity.joinRoom(room, password)) {
			setEntity(null);
		}
		
		if (this.getData().getQuestId() != 0) {
			IQuest quest = QuestManager.getInstance().getById(this.getData().getQuestId());
			
			if (quest != null && this.getQuests().hasStartedQuest(quest.getId()) && !this.getQuests().hasCompletedQuest(quest.getId())) {
				this.getSession().send(new QuestStartedMessageComposer(quest, this));
				
				if (quest.getType() == QuestType.SOCIAL_VISIT) {
					this.getQuests().progressQuest(QuestType.SOCIAL_VISIT);
				}
			}
		}
		
		if (!this.enteredRooms.contains(id) && !this.rooms.contains(id)) {
			this.enteredRooms.add(id);
		}
		
		if (getSettings().hasPersonalStaff()) {
			List<Map.Entry<Integer, Integer>> rankPermList = new ArrayList<>(PermissionsDao.getEffects().entrySet());
			rankPermList.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));
			
			for (Map.Entry<Integer, Integer> entry : rankPermList) {
				
				if (this.getPermissions().getRank().getId() < entry.getValue()) continue;
				
				if (this.getSettings().hasPersonalStaff()) {
					this.getEntity().applyEffect(new PlayerEffect(entry.getKey()));
				} else this.getEntity().applyEffect(new PlayerEffect(0));
				
				break;
			}
		}
		
	}
	
	@Override
	public void poof() {
		this.getSession().send(new UpdateInfoMessageComposer(-1, this.getData().getFigure(), this.getData().getGender(), this.getData().getMotto(), this.getData().getAchievementPoints()));
		
		if (this.getEntity() != null && this.getEntity().getRoom() != null && this.getEntity().getRoom().getEntities() != null) {
			this.getEntity().unIdle();
			this.getEntity().getRoom().getEntities().broadcastMessage(new UpdateInfoMessageComposer(this.getEntity()));
		}
	}
	
	@Override
	public void ignorePlayer(int playerId) {
		if (this.ignoredPlayers == null) {
			this.ignoredPlayers = new ArrayList<>();
		}
		
		this.ignoredPlayers.add(playerId);
	}
	
	@Override
	public void unignorePlayer(int playerId) {
		this.ignoredPlayers.remove((Integer) playerId);
	}
	
	@Override
	public boolean ignores(int playerId) {
		return this.ignoredPlayers == null || !this.ignoredPlayers.contains(playerId);
	}
	
	@Override
	public List<Integer> getRooms() {
		return rooms;
	}
	
	@Override
	public void setRooms(List<Integer> rooms) {
		this.rooms = rooms;
		
		flush();
	}
	
	@Override
	public List<Integer> getRoomsWithRights() {
		return roomsWithRights;
	}
	
	public PlayerEntity getEntity() {
		return this.entity;
	}
	
	public void setEntity(PlayerEntity avatar) {
		this.entity = avatar;
		
		flush();
	}
	
	@Override
	public Session getSession() {
		return this.session;
	}
	
	@Override
	public void setSession(ISession client) {
		this.session = ((Session) client);
	}
	
	@Override
	public PlayerData getData() {
		return this.data;
	}
	
	public void setData(PlayerData playerData) {
		this.data = playerData;
	}
	
	@Override
	public PlayerStatistics getStats() {
		return this.stats;
	}
	
	@Override
	public PermissionComponent getPermissions() {
		return this.permissions;
	}
	
	public MessengerComponent getMessenger() {
		return this.messenger;
	}
	
	public IPlayerInventory getInventory() {
		return this.inventory;
	}
	
	public SubscriptionComponent getSubscription() {
		return this.subscription;
	}
	
	@Override
	public RelationshipComponent getRelationships() {
		return this.relationships;
	}
	
	public InventoryBotComponent getBots() {
		return this.bots;
	}
	
	public PetComponent getPets() {
		return this.pets;
	}
	
	public QuestComponent getQuests() {
		return quests;
	}
	
	public AchievementComponent getAchievements() {
		return achievements;
	}
	
	@Override
	public PlayerSettings getSettings() {
		return this.settings;
	}
	
	@Override
	public int getId() {
		return this.id;
	}
	
	@Override
	public boolean isTeleporting() {
		return this.teleportId != 0;
	}
	
	@Override
	public long getTeleportId() {
		return this.teleportId;
	}
	
	@Override
	public void setTeleportId(long teleportId) {
		this.teleportId = teleportId;
	}
	
	@Override
	public long getRoomLastMessageTime() {
		return roomLastMessageTime;
	}
	
	@Override
	public void setRoomLastMessageTime(long roomLastMessageTime) {
		this.roomLastMessageTime = roomLastMessageTime;
	}
	
	@Override
	public double getRoomFloodTime() {
		return roomFloodTime;
	}
	
	@Override
	public void setRoomFloodTime(double roomFloodTime) {
		this.roomFloodTime = roomFloodTime;
	}
	
	@Override
	public int getRoomFloodFlag() {
		return roomFloodFlag;
	}
	
	@Override
	public void setRoomFloodFlag(int roomFloodFlag) {
		this.roomFloodFlag = roomFloodFlag;
	}
	
	@Override
	public String getLastMessage() {
		return lastMessage;
	}
	
	@Override
	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}
	
	@Override
	public Set<Integer> getGroups() {
		return groups == null ? Sets.newHashSet() : groups;
	}
	
	@Override
	public int getNotifCooldown() {
		return this.notifCooldown;
	}
	
	@Override
	public void setNotifCooldown(int notifCooldown) {
		this.notifCooldown = notifCooldown;
	}
	
	@Override
	public int getLastRoomId() {
		return lastRoomId;
	}
	
	@Override
	public void setLastRoomId(int lastRoomId) {
		this.lastRoomId = lastRoomId;
		
		flush();
	}
	
	@Override
	public int getLastGift() {
		return lastGift;
	}
	
	@Override
	public void setLastGift(int lastGift) {
		this.lastGift = lastGift;
	}
	
	@Override
	public long getMessengerLastMessageTime() {
		return messengerLastMessageTime;
	}
	
	@Override
	public void setMessengerLastMessageTime(long messengerLastMessageTime) {
		this.messengerLastMessageTime = messengerLastMessageTime;
	}
	
	@Override
	public double getMessengerFloodTime() {
		return messengerFloodTime;
	}
	
	@Override
	public void setMessengerFloodTime(double messengerFloodTime) {
		this.messengerFloodTime = messengerFloodTime;
	}
	
	@Override
	public int getMessengerFloodFlag() {
		return messengerFloodFlag;
	}
	
	@Override
	public void setMessengerFloodFlag(int messengerFloodFlag) {
		this.messengerFloodFlag = messengerFloodFlag;
	}
	
	@Override
	public boolean isDeletingGroup() {
		return isDeletingGroup;
	}
	
	@Override
	public void setDeletingGroup(boolean isDeletingGroup) {
		this.isDeletingGroup = isDeletingGroup;
	}
	
	@Override
	public long getDeletingGroupAttempt() {
		return deletingGroupAttempt;
	}
	
	@Override
	public void setDeletingGroupAttempt(long deletingGroupAttempt) {
		this.deletingGroupAttempt = deletingGroupAttempt;
	}
	
	@Override
	public void bypassRoomAuth(final boolean bypassRoomAuth) {
		this.bypassRoomAuth = bypassRoomAuth;
	}
	
	@Override
	public boolean isBypassingRoomAuth() {
		return bypassRoomAuth;
	}
	
	@Override
	public int getLastFigureUpdate() {
		return lastFigureUpdate;
	}
	
	@Override
	public void setLastFigureUpdate(int lastFigureUpdate) {
		this.lastFigureUpdate = lastFigureUpdate;
	}
	
	public int getTeleportRoomId() {
		return teleportRoomId;
	}
	
	public void setTeleportRoomId(int teleportRoomId) {
		this.teleportRoomId = teleportRoomId;
	}
	
	@Override
	public long getLastReward() {
		return lastReward;
	}
	
	@Override
	public void setLastReward(long lastReward) {
		this.lastReward = lastReward;
	}
	
	@Override
	public long getLastDiamondReward() {
		return lastDiamondReward;
	}
	
	@Override
	public void setLastDiamondReward(long lastDiamondReward) {
		this.lastDiamondReward = lastDiamondReward;
	}
	
	public int getLastForumPost() {
		return lastForumPost;
	}
	
	public void setLastForumPost(int lastForumPost) {
		this.lastForumPost = lastForumPost;
	}
	
	public boolean hasQueued(int id) {
		return roomQueueId != id;
	}
	
	public void setRoomQueueId(int id) {
		this.roomQueueId = id;
	}
	
	public boolean isSpectating(int id) {
		return this.spectatorRoomId == id;
		
	}
	
	public void setSpectatorRoomId(int id) {
		this.spectatorRoomId = id;
	}
	
	public int getLastRoomCreated() {
		return lastRoomCreated;
	}
	
	public void setLastRoomCreated(int lastRoomCreated) {
		this.lastRoomCreated = lastRoomCreated;
	}
	
	public long getLastRoomRequest() {
		return lastRoomRequest;
	}
	
	public void setLastRoomRequest(long lastRoomRequest) {
		this.lastRoomRequest = lastRoomRequest;
	}
	
	public long getLastBadgeUpdate() {
		return lastBadgeUpdate;
	}
	
	public void setLastBadgeUpdate(long lastBadgeUpdate) {
		this.lastBadgeUpdate = lastBadgeUpdate;
	}
	
	public boolean isInvisible() {
		return invisible;
	}
	
	public void setInvisible(boolean invisible) {
		this.invisible = invisible;
		
		flush();
	}
	
	public int getLastTradePlayer() {
		return lastTradePlayer;
	}
	
	public void setLastTradePlayer(int lastTradePlayer) {
		this.lastTradePlayer = lastTradePlayer;
	}
	
	public long getLastTradeTime() {
		return lastTradeTime;
	}
	
	public void setLastTradeTime(long lastTradeTime) {
		this.lastTradeTime = lastTradeTime;
	}
	
	public int getLastTradeFlag() {
		return lastTradeFlag;
	}
	
	public void setLastTradeFlag(int lastTradeFlag) {
		this.lastTradeFlag = lastTradeFlag;
	}
	
	public long getLastTradeFlood() {
		return lastTradeFlood;
	}
	
	public void setLastTradeFlood(long lastTradeFlood) {
		this.lastTradeFlood = lastTradeFlood;
	}
	
	public String getSsoTicket() {
		return this.ssoTicket;
	}
	
	public void setSsoTicket(final String ssoTicket) {
		this.ssoTicket = ssoTicket;
	}
	
	public long getLastPhotoTaken() {
		return lastPhotoTaken;
	}
	
	public void setLastPhotoTaken(long lastPhotoTaken) {
		this.lastPhotoTaken = lastPhotoTaken;
	}
	
	public int getLastVoucherRedeemAttempt() {
		return lastVoucherRedeemAttempt;
	}
	
	public void setLastVoucherRedeemAttempt(int lastVoucherRedeem) {
		this.lastVoucherRedeemAttempt = lastVoucherRedeem;
	}
	
	public int getVoucherRedeemAttempts() {
		return voucherRedeemAttempts;
	}
	
	public void setVoucherRedeemAttempts(int voucherRedeemAttempts) {
		this.voucherRedeemAttempts = voucherRedeemAttempts;
	}
	
	public boolean isUsernameConfirmed() {
		return usernameConfirmed;
	}
	
	public void setUsernameConfirmed(boolean usernameConfirmed) {
		this.usernameConfirmed = usernameConfirmed;
	}
	
	public Set<String> getEventLogCategories() {
		return eventLogCategories;
	}
	
	public ChatMessageColour getChatMessageColour() {
		return chatMessageColour;
	}
	
	public void setChatMessageColour(ChatMessageColour chatMessageColour) {
		this.chatMessageColour = chatMessageColour;
	}
	
	public HelperSession getHelperSession() {
		return helperSession;
	}
	
	public void setHelperSession(HelperSession helperSession) {
		this.helperSession = helperSession;
	}
	
	public HelpRequest getHelpRequest() {
		return helpRequest;
	}
	
	public void setHelpRequest(HelpRequest helpRequest) {
		this.helpRequest = helpRequest;
	}
	
	public Set<Integer> getRecentPurchases() {
		if (this.recentPurchases == null) {
			this.recentPurchases = new ConcurrentHashSet<>();
			this.recentPurchases.addAll(CatalogDao.findRecentPurchases(30, this.id));
		}
		
		return this.recentPurchases;
	}
	
	public NavigatorComponent getNavigator() {
		return navigator;
	}
	
	public boolean petsMuted() {
		return petsMuted;
	}
	
	public void setPetsMuted(boolean petsMuted) {
		this.petsMuted = petsMuted;
	}
	
	public boolean botsMuted() {
		return botsMuted;
	}
	
	public void setBotsMuted(boolean botsMuted) {
		this.botsMuted = botsMuted;
	}
	
	public WardrobeComponent getWardrobe() {
		return wardrobe;
	}
	
	public String getLastPhoto() {
		return lastPhoto;
	}
	
	public void setLastPhoto(String lastPhoto) {
		this.lastPhoto = lastPhoto;
	}
	
	public double getItemPlacementHeight() {
		return itemPlacementHeight;
	}
	
	public int getItemPlacementState() {
		return itemPlacementState;
	}
	
	public void setItemPlacementHeight(double itemPlacementHeight) {
		this.itemPlacementHeight = itemPlacementHeight;
	}
	
	public void setItemPlacementState(int state) {
		this.itemPlacementState = state;
	}
	
	public Set<Integer> getListeningPlayers() {
		return listeningPlayers;
	}
	
	public void setListeningPlayers(Set<Integer> listeningPlayers) {
		this.listeningPlayers = listeningPlayers;
	}
	
	public void flush() {
	}
	
	public String toString() {
		return String.format("id=%d, username=%s", this.id, this.data.getUsername());
	}
	
	public boolean isOnline() {
		return this.online;
	}
	
	public void setOnline(boolean online) {
		this.online = online;
	}
	
}
