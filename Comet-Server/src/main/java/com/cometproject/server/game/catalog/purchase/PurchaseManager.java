package com.cometproject.server.game.catalog.purchase;

import com.cometproject.api.game.achievements.types.AchievementType;
import com.cometproject.api.game.furniture.types.GiftData;
import com.cometproject.server.composers.catalog.GiftUserNotFoundMessageComposer;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.catalog.purchase.handlers.BotPurchaseHandler;
import com.cometproject.server.game.catalog.purchase.handlers.PetPurchaseHandler;
import com.cometproject.server.game.catalog.purchase.handlers.StickiesPurchaseHandler;
import com.cometproject.server.game.catalog.purchase.handlers.TrophyPurchaseHandler;
import com.cometproject.server.network.messages.outgoing.notification.AlertMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.storage.queries.player.PlayerDao;

import java.util.HashMap;
import java.util.Map;

public class PurchaseManager {
	private static PurchaseManager instance;
	private final Map<String, IPurchaseHandler> handlers;
	
	public PurchaseManager() {
		this.handlers = new HashMap<>();
		
		this.handlers.put("trophy", new TrophyPurchaseHandler());
		this.handlers.put("postit", new StickiesPurchaseHandler());
		this.handlers.put("pet", new PetPurchaseHandler());
		this.handlers.put("bot", new BotPurchaseHandler());
	}
	
	public void handlePurchase(Session session, int pageId, int itemId, String data, int amount, GiftData giftData) {
		if (amount > 100) {
			session.send(new AlertMessageComposer(Locale.get("catalog.error.tooMany")));
			return;
		}
		
		final int playerIdToDeliver = giftData == null ? -1 : PlayerDao.getIdByUsername(giftData.getReceiver());
		
		if (giftData != null) {
			if (playerIdToDeliver == 0) {
				session.send(new GiftUserNotFoundMessageComposer());
			} else {
				session.getPlayer().getAchievements().progressAchievement(AchievementType.GIFT_GIVER, 1);
			}
		}
		
	}
	
	public PurchaseManager getInstance() {
		if(instance == null) instance = new PurchaseManager();
		return instance;
	}
	
}
