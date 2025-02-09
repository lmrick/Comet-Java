package com.cometproject.server.game.rooms.objects.entities.types;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.achievements.types.AchievementType;
import com.cometproject.api.game.bots.BotMode;
import com.cometproject.api.game.bots.BotType;
import com.cometproject.api.game.groups.types.IGroupData;
import com.cometproject.api.game.quests.QuestType;
import com.cometproject.api.game.rooms.entities.IPlayerRoomEntity;
import com.cometproject.api.game.rooms.entities.RoomEntityStatus;
import com.cometproject.api.game.rooms.settings.RoomAccessType;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.commands.CommandManager;
import com.cometproject.server.game.commands.vip.TransformCommand;
import com.cometproject.server.game.permissions.types.Rank;
import com.cometproject.server.game.players.PlayerManager;
import com.cometproject.server.game.players.data.PlayerData;
import com.cometproject.server.game.players.types.Player;
import com.cometproject.server.game.rooms.RoomManager;
import com.cometproject.server.game.rooms.RoomQueue;
import com.cometproject.server.game.rooms.objects.entities.IPlayerEntityAccess;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.types.ai.bots.WaiterAI;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.triggers.WiredTriggerPlayerSaysKeyword;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.triggers.custom.WiredTriggerLeavesRoom;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.components.games.GameTeam;
import com.cometproject.server.game.rooms.types.components.games.GameType;
import com.cometproject.server.game.rooms.types.components.types.trade.Trade;
import com.cometproject.server.logging.LogManager;
import com.cometproject.server.logging.entries.RoomVisitLogEntry;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.incoming.room.engine.InitializeRoomMessageEvent;
import com.cometproject.server.network.messages.outgoing.room.access.DoorbellRequestComposer;
import com.cometproject.server.network.messages.outgoing.room.access.RoomReadyMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.alerts.CantConnectMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.alerts.DoorbellNoAnswerComposer;
import com.cometproject.server.network.messages.outgoing.room.alerts.RoomErrorMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.avatar.*;
import com.cometproject.server.network.messages.outgoing.room.engine.*;
import com.cometproject.server.network.messages.outgoing.room.events.RoomPromotionMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.permissions.FloodFilterMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.permissions.YouAreControllerMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.permissions.YouAreOwnerMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.permissions.YouAreSpectatorMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.queue.RoomQueueStatusMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.settings.RoomRatingMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.inventory.PetInventoryMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.network.ws.messages.alerts.MutedMessage;
import com.cometproject.server.protocol.messages.MessageComposer;
import com.cometproject.server.storage.queries.pets.RoomPetDao;
import com.cometproject.server.utilities.attributes.Attributable;
import org.apache.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlayerEntity extends RoomEntity implements IPlayerEntityAccess, Attributable, IPlayerRoomEntity {
	
	private static final Logger log = Logger.getLogger(PlayerEntity.class.getName());
	private Player player;
	private PlayerData playerData;
	
	private int playerId;
	
	private final Map<String, Object> attributes = new HashMap<>();
	private RoomVisitLogEntry visitLogEntry;
	
	private boolean isFinalized = false;
	private boolean isKicked = false;
	
	private GameTeam gameTeam = GameTeam.NONE;
	private GameType gameType = null;
	private int kickWalkStage = 0;
	
	private boolean isQueueing = false;
	
	private int banzaiPlayerAchievement = 0;
	
	private boolean hasPlacedPet = false;
	
	private boolean builderFillFloor = false;
	private int lastMessageCounter = 0;
	private String lastMessage = "";
	
	private boolean isAway = false;
	private long lastAwayReminder = 0;
	private long awayTime = 0;
	
	public PlayerEntity(Player player, int identifier, Position startPosition, int startBodyRotation, int startHeadRotation, Room roomInstance) {
		super(identifier, startPosition, startBodyRotation, startHeadRotation, roomInstance);
		
		this.player = player;
		
		this.playerId = player.getId();
		this.playerData = player.getData();
		
		if (this.player.isInvisible()) {
			this.updateVisibility(false);
		}
		
		if (this.getPlayer().isTeleporting() && this.getPlayer().getTeleportRoomId() == roomInstance.getId()) {
			this.setOverriden(true);
		}
		
		if (LogManager.ENABLED) {
			this.visitLogEntry = LogManager.getInstance().getStore().getRoomVisitContainer().put(player.getId(), roomInstance.getId(), Comet.getTime());
		}
	}
	
	@Override
	public boolean joinRoom(Room room, String password) {
		if (this.isFinalized()) return this.getRoom().getId() == room.getId();
		
		boolean isAuthFailed = false;
		boolean isSpectating = this.getPlayer().isSpectating(room.getId());
		
		if (this.getRoom() == null) {
			this.getPlayer().getSession().send(new HotelViewMessageComposer());
			isAuthFailed = true;
		}
		
		if (!isSpectating && this.getPlayer().hasQueued(room.getId()) && !isAuthFailed && this.getPlayerId() != this.getRoom().getData().getOwnerId() && this.getRoom().getEntities().playerCount() >= this.getRoom().getData().getMaxUsers() && !this.getPlayer().getPermissions().getRank().roomEnterFull()) {
			
			if (RoomQueue.getInstance().hasQueue(room.getId())) {
				RoomQueue.getInstance().addPlayerToQueue(room.getId(), this.playerId);
				
				this.isQueueing = true;
				this.getPlayer().getSession().send(new RoomQueueStatusMessageComposer(RoomQueue.getInstance().getQueueCount(room.getId(), this.playerId)));
				return true;
			}
			
			this.getPlayer().getSession().send(new CantConnectMessageComposer(1));
			this.getPlayer().getSession().send(new HotelViewMessageComposer());
			isAuthFailed = true;
		}
		
		if (!isAuthFailed && this.getRoom().getRights().hasBan(this.getPlayerId()) && this.getPlayer().getPermissions().getRank().roomKickable()) {
			this.getPlayer().getSession().send(new CantConnectMessageComposer(4));
			isAuthFailed = true;
		}
		
		boolean isOwner = (this.getRoom().getData().getOwnerId() == this.getPlayerId());
		boolean isTeleporting = this.getPlayer().isTeleporting() && (this.getPlayer().getTeleportRoomId() == this.getRoom().getId());
		boolean isDoorbell = false;
		
		if (!isAuthFailed && !this.getPlayer().isBypassingRoomAuth() && (!isOwner && !this.getPlayer().getPermissions().getRank().roomEnterLocked() && !this.isDoorbellAnswered()) && !isTeleporting) {
			if (this.getRoom().getData().getAccess() == RoomAccessType.PASSWORD) {
				boolean matched;
				
				if (CometSettings.roomEncryptPasswords) {
					matched = BCrypt.checkpw(password, this.getRoom().getData().getPassword());
				} else {
					matched = this.getRoom().getData().getPassword().equals(password);
				}
				
				if (!matched) {
					this.getPlayer().getSession().send(new RoomErrorMessageComposer(-100002));
					this.getPlayer().getSession().send(new HotelViewMessageComposer());
					isAuthFailed = true;
				}
			} else if (this.getRoom().getData().getAccess() == RoomAccessType.DOORBELL) {
				if (!this.getRoom().getRights().hasRights(this.playerId)) {
					if (this.getRoom().getEntities().playerCount() < 1) {
						this.getPlayer().getSession().send(new DoorbellNoAnswerComposer());
						this.getPlayer().getSession().send(new HotelViewMessageComposer());
						
						isAuthFailed = true;
					} else {
						this.getRoom().getEntities().broadcastMessage(new DoorbellRequestComposer(this.getUsername()), true);
						this.getPlayer().getSession().send(new DoorbellRequestComposer(""));
						isAuthFailed = true;
						isDoorbell = true;
					}
				}
			}
		}
		
		this.getPlayer().bypassRoomAuth(false);
		this.getPlayer().setTeleportId(0);
		this.getPlayer().setTeleportRoomId(0);
		
		this.getPlayer().setRoomQueueId(0);
		
		if (isAuthFailed) {
			return isDoorbell;
		}
		
		this.getPlayer().getSession().send(new OpenConnectionMessageComposer());
		
		this.getRoom().getEntities().addEntity(this);
		this.finalizeJoinRoom();
		
		return true;
	}
	
	@Override
	protected void finalizeJoinRoom() {
		Session session = this.player.getSession();
		
		session.send(new RoomReadyMessageComposer(this.getRoom().getId(), this.getRoom().getModel().getId()));
		
		this.getRoom().getData().getDecorations().forEach((key, value) -> {
			if (key.equals("wallpaper") || key.equals("floor")) {
				if (value.equals("0.0")) {
					return;
				}
			}
			session.send(new RoomPropertyMessageComposer(key, value));
		});
		
		int accessLevel = 0;
		
		if (this.getRoom().getData().getOwnerId() == this.getPlayerId() || this.getPlayer().getPermissions().getRank().roomFullControl()) {
			this.addStatus(RoomEntityStatus.CONTROLLER, "useradmin");
			session.send(new YouAreOwnerMessageComposer());
			accessLevel = 4;
		} else if (this.getRoom().getRights().hasRights(this.getPlayerId())) {
			this.addStatus(RoomEntityStatus.CONTROLLER, "1");
			accessLevel = 1;
		}
		
		session.send(new YouAreControllerMessageComposer(accessLevel));
		
		boolean isSpectating = this.getPlayer().isSpectating(this.getRoom().getId());
		
		if (!isSpectating) {
			if (this.getRoom().getData().getRequiredBadge() != null) {
				if (!this.getPlayer().getInventory().hasBadge(this.getRoom().getData().getRequiredBadge())) {
					isSpectating = true;
				} else if (this.getPlayer().getInventory().getBadges().get(this.getRoom().getData().getRequiredBadge()) == 0) {
					isSpectating = true;
				}
			}
		}
		
		if (isSpectating) {
			session.send(new YouAreSpectatorMessageComposer());
			this.updateVisibility(false);
		}
		
		session.send(new RoomRatingMessageComposer(this.getRoom().getData().getScore(), this.canRateRoom()));
		
		InitializeRoomMessageEvent.heightmapMessageEvent.handle(session, null);
		
		session.send(RoomManager.getInstance().hasPromotion(this.getRoom().getId()) ? new RoomPromotionMessageComposer(this.getRoom().getData(), this.getRoom().getPromotion()) : new RoomPromotionMessageComposer(null, null));
		
		if (this.getPlayer().getEntity().isVisible())
			this.getRoom().getEntities().broadcastMessage(new AvatarsMessageComposer(this.getPlayer().getEntity()));
		
		this.isFinalized = true;
		this.getPlayer().setSpectatorRoomId(0);
		this.getPlayer().getAchievements().progressAchievement(AchievementType.ROOM_ENTRY, 1);
	}
	
	public boolean canRateRoom() {
		return !this.getRoom().getRatings().contains(this.getPlayerId());
	}
	
	@Override
	public void leaveRoom(boolean isOffline, boolean isKick, boolean toHotelView) {
		if (this.isQueueing) {
			RoomQueue.getInstance().removePlayerFromQueue(this.getRoom().getId(), this.playerId);
		}
		
		try {
			if (RoomQueue.getInstance().hasQueue(this.getRoom().getId()) && !this.isQueueing) {
				int nextPlayer = RoomQueue.getInstance().getNextPlayer(this.getRoom().getId());
				
				RoomQueue.getInstance().removePlayerFromQueue(this.getRoom().getId(), nextPlayer);
				Session nextPlayerSession = NetworkManager.getInstance().getSessions().getByPlayerId(nextPlayer);
				
				if (nextPlayerSession != null) {
					nextPlayerSession.getPlayer().setRoomQueueId(this.getRoom().getId());
					
					if (nextPlayerSession.getPlayer().getEntity() != null && nextPlayerSession.getPlayer().getEntity().getRoom().getId() == this.getRoom().getId()) {
						nextPlayerSession.send(new RoomForwardMessageComposer(this.getRoom().getId()));
					}
				}
			}
		} catch (Exception ignored) {
		
		}
		
		for (BotEntity entity : this.getRoom().getEntities().getBotEntities()) {
			if (entity.getAI().onPlayerLeave(this)) break;
		}
		
		this.getRoom().getItems().getFloorItems().entrySet().stream().filter(floorItem -> floorItem.getValue() != null).forEachOrdered(floorItem -> floorItem.getValue().onEntityLeaveRoom(this));
		
		Trade trade = this.getRoom().getTrade().get(this);
		
		if (trade != null) {
			trade.cancel(this.getPlayerId());
		}
		
		if (this.getMountedEntity() != null) {
			this.getMountedEntity().setOverriden(false);
			this.getMountedEntity().setHasMount(false);
		}
		
		this.getRoom().getItems().getItemsOnSquare(this.getPosition().getX(), this.getPosition().getY()).stream().filter(Objects::nonNull).forEachOrdered(item -> item.onEntityStepOff(this));
		
		if (isKick && !isOffline && this.getPlayer() != null && this.getPlayer().getSession() != null) {
			this.getPlayer().getSession().send(new RoomErrorMessageComposer(4008));
		}
		
		
		this.getRoom().getEntities().broadcastMessage(new LeaveRoomMessageComposer(this.getId()));
		
		if (!isOffline && toHotelView && this.getPlayer() != null && this.getPlayer().getSession() != null) {
			this.getPlayer().getSession().send(new HotelViewMessageComposer());
			
			if (this.getPlayer().getData() != null) {
				this.getPlayer().getMessenger().sendStatus(true, false);
			}
		}
		
		if (this.hasPlacedPet && this.getRoom().getData().getOwnerId() != this.playerId) {
			this.getRoom().getEntities().getPetEntities().stream().filter(petEntity -> petEntity.getData().getOwnerId() == this.getPlayerId()).forEachOrdered(petEntity -> {
				RoomPetDao.updatePet(0, 0, 0, petEntity.getData().getId());
				petEntity.leaveRoom(false);
				this.getPlayer().getPets().addPet(petEntity.getData());
				this.getPlayer().getSession().send(new PetInventoryMessageComposer(this.getPlayer().getPets().getPets()));
			});
		}
		
		this.getFollowingEntities().stream().filter(BotEntity.class::isInstance).map(BotEntity.class::cast).filter(botEntity -> botEntity.getData() != null).filter(botEntity -> botEntity.getData().getMode() == BotMode.RELAXED).forEachOrdered(botEntity -> botEntity.getData().setMode(BotMode.DEFAULT));
		
		this.getRoom().getEntities().removeEntity(this);
		
		if (this.player != null) {
			this.getPlayer().setEntity(null);
		}
		
		if (this.visitLogEntry != null) {
			this.visitLogEntry.setExitTime((int) Comet.getTime());
			
			LogManager.getInstance().getStore().getRoomVisitContainer().updateExit(this.visitLogEntry);
		}
		
		WiredTriggerLeavesRoom.executeTriggers(this);
		
		this.getStatuses().clear();
		this.attributes.clear();
		
		this.player = null;
		this.playerData = null;
	}
	
	@Override
	public void kick() {
		this.isKicked = true;
		this.setCanWalk(false);
		
		this.moveTo(this.getRoom().getModel().getDoorX(), this.getRoom().getModel().getDoorY());
	}
	
	@Override
	public boolean onChat(String message) {
		final long time = System.currentTimeMillis();
		
		final boolean isPlayerOnline = PlayerManager.getInstance().isOnline(this.getPlayerId());
		
		if (!isPlayerOnline) {
			this.leaveRoom(true, false, false);
			return false;
		}
		
		if (WiredTriggerPlayerSaysKeyword.executeTriggers(this, message)) {
			return false;
		}
		
		if (!this.getPlayer().getListeningPlayers().isEmpty()) {
			this.getPlayer().getListeningPlayers().stream().map(listeningPlayerId -> NetworkManager.getInstance().getSessions().getByPlayerId(listeningPlayerId)).filter(Objects::nonNull).forEachOrdered(session -> session.send(new WhisperMessageComposer(session.getPlayer().getId(), Locale.get("command.listen.message").replace("%username%", this.getUsername()).replace("%message%", message))));
		}
		
		if (!this.getPlayer().getPermissions().getRank().floodBypass()) {
			if (this.lastMessage.equals(message)) {
				this.lastMessageCounter++;
				
				if (this.lastMessageCounter >= 3) {
					this.getPlayer().setRoomFloodTime(this.getPlayer().getPermissions().getRank().floodTime());
				}
			} else {
				this.lastMessage = message;
				this.lastMessageCounter = 0;
			}
			
			if (time - this.getPlayer().getRoomLastMessageTime() < 750) {
				this.getPlayer().setRoomFloodFlag(this.getPlayer().getRoomFloodFlag() + 1);
				
				if (this.getPlayer().getRoomFloodFlag() >= 3) {
					this.getPlayer().setRoomFloodTime(this.getPlayer().getPermissions().getRank().floodTime());
					this.getPlayer().setRoomFloodFlag(0);
					
					this.getPlayer().getSession().send(new FloodFilterMessageComposer(player.getRoomFloodTime()));
				}
			} else {
				this.getPlayer().setRoomFloodFlag(0);
			}
			
			if (this.getPlayer().getRoomFloodTime() >= 1) {
				return false;
			}
			
			player.setRoomLastMessageTime(time);
			player.setLastMessage(message);
		}
		
		if (message.isEmpty() || message.length() > 100) return false;
		
		try {
			if (this.getPlayer() != null && this.getPlayer().getSession() != null) {
				if (CommandManager.getInstance().isCommand(message)) {
					if (CommandManager.getInstance().parse(message, this.getPlayer().getSession())) return false;
				} else if (CommandManager.getInstance().getNotifications().isNotificationExecutor(message, this.getPlayer().getData().getRank())) {
					CommandManager.getInstance().getNotifications().execute(this.player, message.substring(1));
				}
			}
		} catch (Exception e) {
			log.error("Error while executing command", e);
			return false;
		}
		
		
		if (!this.getPlayer().getData().getNameColour().equals("000000") || this.getPlayer().getPermissions().getRank().namePrefix() != null) {
			this.sendNameChange();
		}
		
		final boolean roomMute = this.isRoomMuted() && this.getRoom().getData().getOwnerId() != this.getPlayerId();
		final boolean playerMute = this.getRoom().getRights().hasMute(this.getPlayerId()) && this.getRoom().getData().getOwnerId() != this.getPlayerId();
		final boolean moderatorMute = this.getPlayer().getData().getTimeMuted() > Comet.getTime();
		
		if ((roomMute || playerMute || moderatorMute) && !this.getPlayer().getPermissions().getRank().roomMuteBypass()) {
			final Session session = this.getPlayer().getSession();
			
			if (session.getWsChannel() == null) {
				session.send(new MutedMessageComposer(this.getRoom().getRights().getMuteTime(this.getPlayerId())));
			} else {
				final MutedMessage.MuteType type = roomMute ? MutedMessage.MuteType.ROOM_MUTE : moderatorMute ? MutedMessage.MuteType.MODERATOR_MUTE : MutedMessage.MuteType.USER_MUTE;
				session.sendWs(new MutedMessage(type, true, null));
			}
			
			return false;
		}
		
		if (this.getRoom().getEntities().playerCount() > 1) {
			this.getPlayer().getQuests().progressQuest(QuestType.SOCIAL_CHAT);
		}
		
		this.unIdle();
		
		return true;
	}
	
	@Override
	public void unIdle() {
		if (this.isAway) {
			this.isAway = false;
			this.lastAwayReminder = 0;
			this.awayTime = 0;
		}
		
		super.unIdle();
	}
	
	private void sendNameChange() {
		final StringBuilder username = new StringBuilder();
		final String format = "%s<font color='#%s'>%s</font>";
		final String colour = this.getPlayer().getData().getNameColour();
		
		String prefix = "";
		final Rank rank = this.getPlayer().getPermissions().getRank();
		if (rank != null && rank.namePrefix() != null) {
			prefix = String.format("%s ", rank.namePrefix());
		}
		
		if (colour.contains(",")) {
			final String[] colours = colour.split(",");
			boolean alternate = true;
			
			for (char c : this.getUsername().toCharArray()) {
				username.append(String.format(format, prefix, colours[alternate ? 1 : 0], c));
				alternate = !alternate;
			}
		} else {
			username.append(String.format(format, prefix, colour, this.getUsername()));
		}
		
		final MessageComposer composer = new UserNameChangeMessageComposer(this.getRoom().getId(), this.getId(), username.toString());
		
		this.getRoom().getEntities().getPlayerEntities().stream().filter(playerEntity -> !playerEntity.getPlayer().getSettings().isUseOldChat()).forEachOrdered(playerEntity -> playerEntity.getPlayer().getSession().send(composer));
	}
	
	public void postChat(String message) {
		String triggerMessage = message.toLowerCase();
		
		boolean isDrinkRequest = false;
		
		for (WaiterAI.Drink drink : WaiterAI.drinks) {
			if (triggerMessage.contains(Locale.get("drink." + drink.getTrigger()))) {
				isDrinkRequest = true;
			}
		}
		
		if (isDrinkRequest) {
			final BotEntity nearestBot = this.nearestBotEntity(BotType.WAITER);
			
			if (nearestBot != null) {
				nearestBot.getAI().onTalk(this, message);
				return;
			}
		}
		
		this.getRoom().getEntities().getIntelligentEntities().stream().filter(entity -> entity.getAI() != null).forEachOrdered(entity -> entity.getAI().onTalk(this, message));
		
		if (!this.getPlayer().getData().getNameColour().equals("000000") || this.getPlayer().getData().nameColourChanged() || this.getPlayer().getPermissions().getRank().namePrefix() != null) {
			if (this.getPlayer().getData().nameColourChanged()) {
				this.getPlayer().getData().setNameColourChanged(false);
			}
			
			this.getRoom().getEntities().broadcastMessage(new UserNameChangeMessageComposer(this.getRoom().getId(), this.getId(), this.getUsername()));
		}
	}
	
	@Override
	public boolean onRoomDispose() {
		this.getStatuses().clear();
		
		this.getRoom().getEntities().broadcastMessage(new LeaveRoomMessageComposer(this.getId()));
		
		this.getPlayer().getSession().send(new HotelViewMessageComposer());
		this.getPlayer().getSession().getPlayer().getMessenger().sendStatus(true, false);
		
		Trade trade = this.getRoom().getTrade().get(this);
		
		if (trade != null) {
			trade.cancel(this.getPlayerId());
		}
		
		if (this.getPlayer() != null) {
			this.getPlayer().setEntity(null);
			this.player = null;
		}
		
		return false;
	}
	
	@Override
	public void setIdle() {
		super.setIdle();
		
		this.getRoom().getEntities().broadcastMessage(new IdleStatusMessageComposer(this, true));
	}
	
	public int getPlayerId() {
		return this.playerId;
	}
	
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}
	
	@Override
	public String getUsername() {
		return this.playerData == null ? "Unknown Player" + this.playerId : this.playerData.getUsername();
	}
	
	@Override
	public String getMotto() {
		return this.playerData == null ? "" : this.playerData.getMotto();
	}
	
	@Override
	public String getFigure() {
		return this.playerData == null ? "" : this.playerData.getFigure();
	}
	
	@Override
	public String getGender() {
		return this.playerData == null ? "M" : this.playerData.getGender();
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		if (this.hasAttribute("transformation")) {
			String[] transformationData = ((String) this.getAttribute("transformation")).split("#");
			TransformCommand.composeTransformation(msg, transformationData, this);
			return;
		}
		
		msg.writeInt(this.getPlayerId());
		msg.writeString(this.getUsername().replace("<", "").replace(">", "")); // Client sometimes parses the username as HTML...
		msg.writeString(this.getMotto());
		msg.writeString(this.getFigure());
		msg.writeInt(this.getId());
		
		msg.writeInt(this.getPosition().getX());
		msg.writeInt(this.getPosition().getY());
		msg.writeDouble(this.getPosition().getZ());
		
		msg.writeInt(this.getBodyRotation());
		msg.writeInt(1); // 1 = user 2 = pet 3 = bot
		
		msg.writeString(this.getGender().toLowerCase());
		
		if (this.playerData == null || this.playerData.getFavouriteGroup() == 0) {
			msg.writeInt(-1);
			msg.writeInt(-1);
			msg.writeInt(0);
		} else {
			IGroupData group = GameContext.getCurrent().getGroupService().getData(this.playerData.getFavouriteGroup());
			
			if (group == null) {
				msg.writeInt(-1);
				msg.writeInt(-1);
				msg.writeInt(0);
				
				this.playerData.setFavouriteGroup(0);
				this.playerData.save();
			} else {
				msg.writeInt(group.getId());
				msg.writeInt(2);
				msg.writeString(group.getTitle());
				msg.writeString("");
			}
		}
		
		msg.writeInt(this.playerData == null ? 0 : this.playerData.getAchievementPoints()); //achv points
		msg.writeBoolean(false);
	}
	
	@Override
	public Player getPlayer() {
		return this.player;
	}
	
	@Deprecated
	public void dispose() {
		this.leaveRoom(true, false, false);
		this.attributes.clear();
	}
	
	@Override
	public void setAttribute(String attributeKey, Object attributeValue) {
		if (this.attributes.containsKey(attributeKey)) {
			this.attributes.replace(attributeKey, attributeValue);
		} else {
			this.attributes.put(attributeKey, attributeValue);
		}
	}
	
	@Override
	public Object getAttribute(String attributeKey) {
		return this.attributes.get(attributeKey);
	}
	
	@Override
	public boolean hasAttribute(String attributeKey) {
		return this.attributes.containsKey(attributeKey);
	}
	
	@Override
	public void removeAttribute(String attributeKey) {
		this.attributes.remove(attributeKey);
	}
	
	public boolean isFinalized() {
		return isFinalized;
	}
	
	public GameTeam getGameTeam() {
		return gameTeam;
	}
	
	public void setGameTeam(GameTeam gameTeam, GameType gameType) {
		this.gameTeam = Objects.requireNonNullElse(gameTeam, GameTeam.NONE);
		this.gameType = gameType;
	}
	
	public GameType getGameType() {
		return this.gameType;
	}
	
	public boolean isKicked() {
		return isKicked;
	}
	
	public int getKickWalkStage() {
		return kickWalkStage;
	}
	
	public void increaseKickWalkStage() {
		this.kickWalkStage++;
	}
	
	public int getBanzaiPlayerAchievement() {
		return banzaiPlayerAchievement;
	}
	
	public void setBanzaiPlayerAchievement(int banzaiPlayerAchievement) {
		this.banzaiPlayerAchievement = banzaiPlayerAchievement;
	}
	
	public void incrementBanzaiPlayerAchievement() {
		this.banzaiPlayerAchievement++;
	}
	
	public void setPlacedPet(boolean hasPlacedPet) {
		this.hasPlacedPet = hasPlacedPet;
	}
	
	public boolean hasRights() {
		return this.getRoom().getRights().hasRights(this.playerId);
	}
	
	public boolean isBuilderFillFloor() {
		return builderFillFloor;
	}
	
	public void setBuilderFillFloor(boolean builderFillFloor) {
		this.builderFillFloor = builderFillFloor;
	}
	
	public boolean isAway() {
		return this.isAway;
	}
	
	public void setAway() {
		this.awayTime = System.currentTimeMillis();
		this.lastAwayReminder = this.awayTime / 1000;
		this.isAway = true;
		this.setIdle();
	}
	
	public long getLastAwayReminder() {
		return this.lastAwayReminder;
	}
	
	public void setLastAwayReminder(long lastAwayReminder) {
		this.lastAwayReminder = lastAwayReminder;
	}
	
	public long getAwayTime() {
		return awayTime;
	}
	
}
