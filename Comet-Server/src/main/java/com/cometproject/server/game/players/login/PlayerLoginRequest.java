package com.cometproject.server.game.players.login;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.events.players.OnPlayerLoginEvent;
import com.cometproject.api.events.players.args.OnPlayerLoginEventArgs;
import com.cometproject.api.game.achievements.types.AchievementType;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.moderation.BanManager;
import com.cometproject.server.game.moderation.types.BanType;
import com.cometproject.server.game.players.PlayerManager;
import com.cometproject.server.game.players.types.Player;
import com.cometproject.server.game.rooms.RoomManager;
import com.cometproject.server.modules.ModuleManager;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.incoming.handshake.SSOTicketMessageEvent;
import com.cometproject.server.network.messages.outgoing.handshake.AuthenticationOKMessageComposer;
import com.cometproject.server.network.messages.outgoing.handshake.HomeRoomMessageComposer;
import com.cometproject.server.network.messages.outgoing.handshake.UniqueIDMessageComposer;
import com.cometproject.server.network.messages.outgoing.moderation.CfhTopicsInitMessageComposer;
import com.cometproject.server.network.messages.outgoing.moderation.ModToolMessageComposer;
import com.cometproject.server.network.messages.outgoing.navigator.FavouriteRoomsMessageComposer;
import com.cometproject.server.network.messages.outgoing.notification.AdvancedAlertMessageComposer;
import com.cometproject.server.network.messages.outgoing.notification.AlertMessageComposer;
import com.cometproject.server.network.messages.outgoing.notification.MotdNotificationMessageComposer;
import com.cometproject.server.network.messages.outgoing.notification.NotificationMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.details.AvailabilityStatusMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.details.PlayerSettingsMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.inventory.EffectsInventoryMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.permissions.FuserightsMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.network.sessions.SessionManager;
import com.cometproject.server.storage.cache.CacheManager;
import com.cometproject.server.storage.queries.player.PlayerAccessDao;
import com.cometproject.server.storage.queries.player.PlayerDao;
import com.cometproject.server.tasks.ICometTask;
import com.cometproject.server.tasks.CometThreadManager;
import org.apache.commons.lang.StringUtils;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

public class PlayerLoginRequest implements ICometTask {
	
	private final Session client;
	private final String ticket;
	
	public PlayerLoginRequest(Session client, String ticket) {
		this.client = client;
		this.ticket = ticket;
	}
	
	@Override
	public void run() {
		if (this.client == null) {
			return;
		}
		
		try {
			
			Player player = null;
			boolean normalPlayerLoad = false;
			
			if (ticket.contains(SSOTicketMessageEvent.TICKET_DELIMITER)) {
				String[] ticketData = ticket.split(SSOTicketMessageEvent.TICKET_DELIMITER);
				
				if (ticketData.length == 2) {
					String authTicket = ticketData[1];
					player = PlayerDao.getPlayer(authTicket);
				} else {
					normalPlayerLoad = true;
				}
			} else {
				normalPlayerLoad = true;
			}
			
			if (normalPlayerLoad) {
				player = PlayerDao.getPlayer(ticket);
			}
			
			if (player == null) {
				player = PlayerDao.getPlayerFallback(ticket);
				
				if (player == null) {
					client.disconnect();
					return;
				}
			}
			
			Session cloneSession = NetworkManager.getInstance().getSessions().getByPlayerId(player.getId());
			
			if (cloneSession != null && cloneSession.getPlayer() != null && cloneSession.getPlayer().getData() != null) {
				player.setData(cloneSession.getPlayer().getData());
				cloneSession.disconnect();
			}
			
			if (BanManager.getInstance().hasBan(Integer.toString(player.getId()), BanType.USER)) {
				client.getLogger().warn("Banned player: " + player.getId() + " tried logging in");
				client.disconnect("banned");
				return;
			}
			
			player.setSession(client);
			client.setPlayer(player);
			
			String ipAddress = client.getIpAddress();
			
			if (ipAddress != null && !ipAddress.isEmpty()) {
				if (BanManager.getInstance().hasBan(ipAddress, BanType.IP)) {
					client.getLogger().warn("Banned player: " + player.getId() + " tried logging in");
					client.disconnect("banned");
					return;
				}
				
				client.getPlayer().getData().setIpAddress(ipAddress);
				
				if (PlayerManager.getInstance().getPlayerCountByIpAddress(ipAddress) > CometSettings.maxConnectionsPerIpAddress) {
					client.disconnect();
					return;
				}
			}
			
			if (CometSettings.saveLogins) PlayerAccessDao.saveAccess(player.getId(), client.getUniqueId(), ipAddress);
			
			RoomManager.getInstance().loadRoomsForUser(player);
			
			client.getLogger().debug(client.getPlayer().getData().getUsername() + " logged in");
			
			player.setOnline(true);
			
			PlayerDao.updatePlayerStatus(player, player.isOnline(), true);
			sendLoginComposers(player);
			
			if (client.getPlayer().getPermissions().getRank().modTool()) {
				client.sendQueue(new ModToolMessageComposer());
			}
			
			if (CometSettings.motdEnabled) {
				client.sendQueue(new AdvancedAlertMessageComposer(CometSettings.hotelName, CometSettings.motdMessage.replace("%username%", player.getData().getUsername()).replace("%hotelName%", CometSettings.hotelName), "motd_image"));
			}
			
			if (!CometSettings.onlineRewardDoubleDays.isEmpty()) {
				LocalDate date = LocalDate.now();
				
				if (CometSettings.onlineRewardDoubleDays.contains(date.getDayOfWeek())) {
					client.sendQueue(new MotdNotificationMessageComposer(Locale.getOrDefault("reward.double.points", "Hey %username%, \n\nToday we're giving out double points!").replace("%username%", player.getData().getUsername())));
				}
			}
			
			client.flush();
			
			if (client.getPlayer().getPermissions().getRank().sendLoginNotif() || player.getSettings().sendLoginNotif()) {
				NetworkManager.getInstance().getSessions().broadcast(new NotificationMessageComposer("generic", Locale.getOrDefault("player.online", "%username% is online!").replace("%username%", player.getData().getUsername())));
			}
			
			client.getPlayer().getAchievements().progressAchievement(AchievementType.LOGIN, 1);
			double regDate = StringUtils.isNumeric(client.getPlayer().getData().getRegDate()) ? Integer.parseInt(client.getPlayer().getData().getRegDate()) : client.getPlayer().getData().getRegTimestamp();
			
			if (regDate != 0) {
				int daysSinceRegistration = (int) Math.floor((((int) Comet.getTime()) - regDate) / 86400);
				
				if (client.getPlayer().getAchievements().hasStartedAchievement(AchievementType.REGISTRATION_DURATION)) {
					client.getPlayer().getAchievements().progressAchievement(AchievementType.REGISTRATION_DURATION, daysSinceRegistration);
				} else {
					int progress = client.getPlayer().getAchievements().getProgress(AchievementType.REGISTRATION_DURATION).getProgress();
					if (daysSinceRegistration > client.getPlayer().getAchievements().getProgress(AchievementType.REGISTRATION_DURATION).getProgress()) {
						int amountToProgress = daysSinceRegistration - progress;
						client.getPlayer().getAchievements().progressAchievement(AchievementType.REGISTRATION_DURATION, amountToProgress);
					}
				}
			}
			
			if (player.getData().getAchievementPoints() < 0) {
				player.getData().setAchievementPoints(0);
				player.getData().save();
			}
			
			if (!Comet.isDebugging && !ticket.startsWith("COMET-")) {
				PlayerDao.nullifyAuthTicket(player.getData().getId());
			}
			
			if (ModuleManager.getInstance()
							.getEventHandler()
							.handleEvent(OnPlayerLoginEvent.class, new OnPlayerLoginEventArgs(client.getPlayer()))) {
				client.disconnect();
			}
			
			if (SessionManager.isLocked) {
				client.send(new AlertMessageComposer("Hotel's closed, come back later!"));
				CometThreadManager.getInstance().executeSchedule(client::disconnect, 5, TimeUnit.SECONDS);
			}
			
			if (client.getPlayer().getData().getTimeMuted() != 0) {
				if (client.getPlayer().getData().getTimeMuted() < (int) Comet.getTime()) {
					PlayerDao.addTimeMute(player.getData().getId(), 0);
				}
			}
			
			player.setSsoTicket(this.ticket);
			PlayerManager.getInstance().getSsoTicketToPlayerId().put(this.ticket, player.getId());
			
			CacheManager.getInstance().publishString("online.players", String.valueOf(Comet.getStats().getPlayers()), true, "online.players");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendLoginComposers(Player player) {
		client.sendQueue(new UniqueIDMessageComposer(client.getUniqueId()));
		client.sendQueue(new AuthenticationOKMessageComposer());
		client.sendQueue(new FuserightsMessageComposer(client.getPlayer().getSubscription().exists(), client.getPlayer().getData().getRank()));
		client.sendQueue(new FavouriteRoomsMessageComposer(client.getPlayer().getNavigator().getFavouriteRooms()));
		client.sendQueue(new AvailabilityStatusMessageComposer());
		client.sendQueue(new PlayerSettingsMessageComposer(player.getSettings()));
		client.sendQueue(new HomeRoomMessageComposer(player.getSettings().getHomeRoom(), player.getSettings().getHomeRoom()));
		client.sendQueue(new EffectsInventoryMessageComposer(player.getInventory().getEffects(), player.getInventory().getEquippedEffect()));
		client.sendQueue(new CfhTopicsInitMessageComposer());
	}
	
	
}
