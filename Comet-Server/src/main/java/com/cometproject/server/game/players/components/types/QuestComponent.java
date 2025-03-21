package com.cometproject.server.game.players.components.types;

import com.cometproject.api.game.players.components.PlayerComponentContext;
import com.cometproject.api.game.players.data.components.IPlayerQuests;
import com.cometproject.api.game.quests.IQuest;
import com.cometproject.api.game.quests.QuestType;
import com.cometproject.server.game.players.components.PlayerComponent;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.quests.QuestManager;
import com.cometproject.server.network.messages.outgoing.quests.QuestCompletedMessageComposer;
import com.cometproject.server.network.messages.outgoing.quests.QuestListMessageComposer;
import com.cometproject.server.network.messages.outgoing.quests.QuestStartedMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.purse.UpdateActivityPointsMessageComposer;
import com.cometproject.server.storage.queries.quests.PlayerQuestsDao;
import org.apache.log4j.Logger;
import java.util.Map;

public class QuestComponent extends PlayerComponent implements IPlayerQuests {
	private final Logger LOG = super.getLogger(QuestComponent.class);
	private Map<Integer, Integer> questProgression;
	
	public QuestComponent(PlayerComponentContext componentContext) {
		super(componentContext);
		
		this.loadQuestProgression();
	}
	
	@Override
	public void loadQuestProgression() {
		this.questProgression = PlayerQuestsDao.getQuestProgression(this.getPlayer().getId());
	}
	
	@Override
	public boolean hasStartedQuest(int questId) {
		return this.questProgression.containsKey(questId);
	}
	
	@Override
	public boolean hasCompletedQuest(int questId) {
		final IQuest quest = QuestManager.getInstance().getById(questId);
		
		if (quest == null) return false;
		
		if (this.questProgression.containsKey(questId)) {
			return this.questProgression.get(questId) >= quest.getGoalData();
		}
		
		return false;
	}
	
	@Override
	public void startQuest(IQuest quest) {
		if (this.questProgression.containsKey(quest.getId())) {
			// We've already started this quest
			return;
		}
		
		this.questProgression.put(quest.getId(), 0);
		PlayerQuestsDao.saveProgression(true, this.getPlayer().getId(), quest.getId(), 0);
		
		this.getPlayer().getSession().send(new QuestStartedMessageComposer(quest, this.getPlayer()));
		this.getPlayer().getSession().send(new QuestListMessageComposer(QuestManager.getInstance().getQuests(), this.getPlayer(), false));
		
		this.getPlayer().getData().setQuestId(quest.getId());
		this.getPlayer().getData().save();
		
		this.getPlayer().flush(this);
	}
	
	@Override
	public void cancelQuest(int questId) {
		PlayerQuestsDao.cancelQuest(questId, this.getPlayer().getId());
		this.questProgression.remove(questId);
	}
	
	@Override
	public void progressQuest(QuestType type) {
		this.progressQuest(type, 0);
	}
	
	@Override
	public void progressQuest(QuestType type, int data) {
		int questId = this.getPlayer().getData().getQuestId();
		
		if (questId == 0 || !this.hasStartedQuest(questId)) {
			return;
		}
		
		IQuest quest = QuestManager.getInstance().getById(questId);
		
		if (quest == null) {
			return;
		}
		
		if (quest.getType() != type) {
			return;
		}
		
		if (this.hasCompletedQuest(questId)) {
			return;
		}
		
		int newProgressValue = this.getProgress(questId);
		
		switch (quest.getType()) {
			case FURNI_MOVE -> {
			}
			case FURNI_ROTATE -> {
			}
			case FURNI_PLACE -> {
			}
			case FURNI_PICK -> {
			}
			case FURNI_SWITCH -> {
			}
			case FURNI_STACK -> {
			}
			case FURNI_DECORATION_FLOOR -> {
			}
			case FURNI_DECORATION_WALL -> {
			}
			case SOCIAL_VISIT -> {
			}
			case SOCIAL_CHAT -> {
			}
			case SOCIAL_FRIEND -> {
			}
			case SOCIAL_RESPECT -> {
			}
			case SOCIAL_DANCE -> {
			}
			case SOCIAL_WAVE -> {
			}
			case PROFILE_CHANGE_LOOK -> {
			}
			case PROFILE_CHANGE_MOTTO -> {
			}
			case PROFILE_BADGE -> {
			}
			default -> newProgressValue++;
			case EXPLORE_FIND_ITEM -> {
				if (quest.getGoalData() != data) {
					return;
				}
				
				newProgressValue = quest.getGoalData();
			}
		}
		
		if (newProgressValue >= quest.getGoalData()) {
			boolean refreshCreditBalance = false;
			boolean refreshCurrenciesBalance = false;
			
			try {
				switch (quest.getRewardType()) {
					case ACTIVITY_POINTS -> {
						this.getPlayer().getData().increaseActivityPoints(quest.getReward());
						refreshCurrenciesBalance = true;
					}
					case ACHIEVEMENT_POINTS -> {
						this.getPlayer().getData().increaseAchievementPoints(quest.getReward());
						this.getPlayer().sendNotification("Alert", Locale.get("game.received.achievementPoints").replace("%points%", quest.getReward() + ""));
						this.getPlayer().poof();
					}
					case VIP_POINTS -> {
						this.getPlayer().getData().increaseVipPoints(quest.getReward());
						refreshCurrenciesBalance = true;
					}
					case CREDITS -> {
						this.getPlayer().getData().increaseCredits(quest.getReward());
						refreshCreditBalance = true;
					}
				}
				
				if (!quest.getBadgeId().isEmpty()) {
					// Deliver badge
					this.getPlayer().getInventory().addBadge(quest.getBadgeId(), true);
				}
			} catch (Exception e) {
				LOG.error("Failed to deliver reward to player: " + this.getPlayer().getData().getUsername());
			}
			
			if (refreshCreditBalance) {
				this.getPlayer().getSession().send(this.getPlayer().composeCreditBalance());
			} else if (refreshCurrenciesBalance) {
				this.getPlayer().getSession().send(this.getPlayer().composeCurrenciesBalance());
				this.getPlayer().getSession().send(new UpdateActivityPointsMessageComposer(this.getPlayer().getData().getActivityPoints(), quest.getReward()));
			}
			
			this.getPlayer().getData().save();
		}
		
		if (this.questProgression.containsKey(questId)) {
			this.questProgression.replace(questId, newProgressValue);
		}
		
		PlayerQuestsDao.saveProgression(false, this.getPlayer().getId(), questId, newProgressValue);
		
		this.getPlayer().getSession().send(new QuestCompletedMessageComposer(quest, this.getPlayer()));
		this.getPlayer().getSession().send(new QuestListMessageComposer(QuestManager.getInstance().getQuests(), this.getPlayer(), false));
		
		this.getPlayer().flush(this);
	}
	
	@Override
	public int getProgress(int quest) {
		if (this.questProgression.containsKey(quest)) {
			return this.questProgression.get(quest);
		}
		
		return 0;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		this.questProgression.clear();
		this.questProgression = null;
	}
	
}
