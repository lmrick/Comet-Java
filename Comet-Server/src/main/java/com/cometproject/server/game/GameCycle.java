package com.cometproject.server.game;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.game.achievements.types.AchievementType;
import com.cometproject.api.utilities.Initializable;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.moderation.BanManager;
import com.cometproject.server.game.rooms.RoomManager;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.outgoing.user.details.UserObjectMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.storage.queries.player.PlayerDao;
import com.cometproject.server.storage.queries.system.StatisticsDao;
import com.cometproject.server.tasks.ICometTask;
import com.cometproject.server.tasks.CometThreadManager;
import org.apache.log4j.Logger;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameCycle implements ICometTask, Initializable {
	
	private static final int interval = 1;
	private static GameCycle gameThreadInstance;
	private static final Logger log = Logger.getLogger(GameCycle.class.getName());
	private ScheduledFuture<?> gameFuture;
	private boolean active = false;
	private int currentOnlineRecord = 0;
	private int onlineRecord = 0;
	
	public GameCycle() {
	
	}
	
	public static GameCycle getInstance() {
		if (gameThreadInstance == null) gameThreadInstance = new GameCycle();
		return gameThreadInstance;
	}
	
	@Override
	public void initialize() {
		this.gameFuture = CometThreadManager.getInstance().executePeriodic(this, interval, interval, TimeUnit.MINUTES);
		this.active = true;
		
		this.onlineRecord = StatisticsDao.getPlayerRecord();
	}
	
	@Override
	public void run() {
		try {
			if (!this.active) {
				return;
			}
			
			BanManager.getInstance().processBans();
			
			final int usersOnline = NetworkManager.getInstance().getSessions().getUsersOnlineCount();
			boolean updateOnlineRecord = false;
			
			if (usersOnline > this.currentOnlineRecord) {
				this.currentOnlineRecord = usersOnline;
			}
			
			if (usersOnline > this.onlineRecord) {
				this.onlineRecord = usersOnline;
				updateOnlineRecord = true;
			}
			
			this.processSession();
			
			if (!updateOnlineRecord) {
				StatisticsDao.saveStatistics(usersOnline, RoomManager.getInstance().getRoomInstances().size(), Comet.getBuild());
			} else {
				StatisticsDao.saveStatistics(usersOnline, RoomManager.getInstance().getRoomInstances().size(), Comet.getBuild(), this.onlineRecord);
			}
			
			
		} catch (Exception e) {
			log.error("Error during game thread", e);
		}
	}
	
	private void processSession() {
		
		if (CometSettings.onlineRewardEnabled || CometSettings.updateDaily) {
			NetworkManager.getInstance().getSessions().getSessions().values().forEach(client -> {
				try {
					if (!(client instanceof Session) || client.getPlayer() == null || client.getPlayer().getData() == null) {
						return;
					}
					
					if ((Comet.getTime() - ((Session) client).getLastPing()) >= 300) {
						client.disconnect();
						return;
					}
					
					if (CometSettings.updateDaily) {
						client.getPlayer().getStats().setDailyRespects(CometSettings.dailyRespects);
						client.getPlayer().getStats().setScratches(CometSettings.dailyScratches);
						
						client.send(new UserObjectMessageComposer(((Session) client).getPlayer()));
					}
					
					((Session) client).getPlayer().getAchievements().progressAchievement(AchievementType.ONLINE_TIME, 1);
					
					final boolean needsReward = (Comet.getTime() - client.getPlayer().getLastReward()) >= (60L * CometSettings.onlineRewardInterval);
					final boolean needsDiamondsReward = (Comet.getTime() - client.getPlayer().getLastDiamondReward()) >= (60L * CometSettings.onlineRewardDiamondsInterval);
					
					if (needsReward || needsDiamondsReward) {
						if (needsReward) {
							if (CometSettings.onlineRewardCredits > 0) {
								client.getPlayer().getData().increaseCredits(CometSettings.onlineRewardCredits * (CometSettings.doubleRewards ? 2 : 1));
							}
							
							if (CometSettings.onlineRewardDuckets > 0) {
								client.getPlayer().getData().increaseActivityPoints(CometSettings.onlineRewardDuckets * (CometSettings.doubleRewards ? 2 : 1));
							}
							
							client.getPlayer().setLastReward(Comet.getTime());
						}
						
						if (needsDiamondsReward) {
							if (CometSettings.onlineRewardDiamonds > 0) {
								client.getPlayer().getData().increaseVipPoints(CometSettings.onlineRewardDiamonds * (CometSettings.doubleRewards ? 2 : 1));
							}
							
							client.getPlayer().setLastDiamondReward(Comet.getTime());
						}
						
						client.getPlayer().sendBalance();
						client.getPlayer().getData().save();
					}
				} catch (Exception e) {
					log.error("Error while cycling rewards", e);
				}
			});
			
			if (CometSettings.updateDaily) {
				PlayerDao.dailyPlayerUpdate(CometSettings.dailyRespects, CometSettings.dailyScratches);
			}
		}
	}
	
	public void stop() {
		this.active = false;
		this.gameFuture.cancel(false);
	}
	
	public int getCurrentOnlineRecord() {
		return this.currentOnlineRecord;
	}
	
	public int getOnlineRecord() {
		return this.onlineRecord;
	}
	
}
