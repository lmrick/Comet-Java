package com.cometproject.server.game.players.components.types;

import com.cometproject.api.game.furniture.types.GiftData;
import com.cometproject.api.game.furniture.types.ILimitedEditionItem;
import com.cometproject.api.game.furniture.types.ISongItem;
import com.cometproject.api.game.players.components.PlayerComponentContext;
import com.cometproject.api.game.players.data.components.IPlayerInventory;
import com.cometproject.api.game.players.data.components.inventory.IPlayerItem;
import com.cometproject.server.composers.catalog.UnseenItemsMessageComposer;
import com.cometproject.server.game.players.components.PlayerComponent;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.game.items.music.SongItemData;
import com.cometproject.server.game.players.components.types.inventory.InventoryItem;
import com.cometproject.server.game.players.components.types.inventory.InventoryItemSnapshot;
import com.cometproject.server.network.messages.outgoing.notification.AlertMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.items.wired.WiredRewardMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.inventory.BadgeInventoryMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.inventory.InventoryMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.inventory.RemoveObjectFromInventoryMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.network.ws.messages.alerts.NewBadgeMessage;
import com.cometproject.server.storage.queries.achievements.PlayerAchievementDao;
import com.cometproject.server.storage.queries.player.PlayerDao;
import com.cometproject.server.storage.queries.player.inventory.InventoryDao;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InventoryComponent extends PlayerComponent implements IPlayerInventory {
	private final Logger LOG = super.getLogger(InventoryComponent.class);
	private Map<Long, IPlayerItem> inventoryItems;

	private Map<String, Integer> badges;

	private boolean itemsLoaded = false;

	private boolean isViewingInventory = false;
	private int viewingInventoryUser = 0;

	private int equippedEffect = -1;
	private Set<Integer> effects;

	public InventoryComponent(PlayerComponentContext componentContext) {
		super(componentContext);

		this.inventoryItems = new ConcurrentHashMap<>();

		this.badges = new ConcurrentHashMap<>();

		this.loadEffects();
		this.loadBadges();
	}

	@Override
	public void loadEffects() {
		if (this.effects != null) {
			this.effects.clear();
			this.effects = null;
		}

		this.effects = PlayerDao.getEffects(this.getPlayer().getId());
	}

	@Override
	public void loadItems(int userId) {
		this.itemsLoaded = true;
		this.inventoryItems.clear();

		this.isViewingInventory = userId != 0;
		this.viewingInventoryUser = userId;

		try {
			Map<Long, IPlayerItem> inventoryItems = InventoryDao.getInventoryByPlayerId(userId == 0 ? this.getPlayer().getId() : userId);
			
			if (inventoryItems != null) {
				this.inventoryItems.putAll(inventoryItems);
			}
	
			this.getPlayer().flush(this);
		} catch (Exception e) {
			LOG.error("Error while loading user inventory", e);
		}
	}

	@Override
	public void loadBadges() {
		try {
			this.badges = InventoryDao.getBadgesByPlayerId(this.getPlayer().getId());
		} catch (Exception e) {
			LOG.error("Error while loading user badges");
		}
	}

	@Override
	public void addBadge(String code, boolean insert) {
		this.addBadge(code, insert, true);
	}

	@Override
	public void addBadge(String code, boolean insert, boolean sendAlert) {
		this.addBadge(code, insert, sendAlert, false);
	}

	@Override
	public void addBadge(String code, boolean insert, boolean sendAlert, boolean isAchievement) {
		if (!badges.containsKey(code)) {
			if (insert) {
				InventoryDao.addBadge(code, this.getPlayer().getId());
			}

			this.badges.put(code, 0);

			this.getPlayer().getSession().send(new BadgeInventoryMessageComposer(this.getBadges()))
					.send(new UnseenItemsMessageComposer(new HashMap<Integer, List<Integer>>() {
						{
							put(4, Lists.newArrayList(1));
						}
					}));

			if (sendAlert || isAchievement) {
				final Session session = (Session) this.getPlayer().getSession();
				if (session.getWsChannel() != null) {
					session.sendWs(new NewBadgeMessage(code));
				} else if (!isAchievement) {
					this.getPlayer().getSession().send(new WiredRewardMessageComposer(7));
				}
			}

			this.getPlayer().flush(this);
		}
	}

	@Override
	public void send() {
		if (this.inventoryItems.isEmpty()) {
			this.getPlayer().getSession().send(new InventoryMessageComposer(1, 0, Maps.newHashMap()));
			return;
		}

		List<Map.Entry<Long, IPlayerItem>> entries = List.copyOf(this.inventoryItems.entrySet());
		int totalPages = (int) Math.ceil((double) entries.size() / InventoryMessageComposer.ITEMS_PER_PAGE);
		int currentPage = 0;

		for (int i = 0; i < entries.size(); i += InventoryMessageComposer.ITEMS_PER_PAGE) {
			Map<Long, IPlayerItem> inventoryPage = entries.subList(i, (int) Math.min(i + InventoryMessageComposer.ITEMS_PER_PAGE, entries.size()))
					.stream()
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			this.getPlayer().getSession().send(new InventoryMessageComposer(totalPages, currentPage++, inventoryPage));
		}
	}


	@Override
	public boolean hasBadge(String code) {
		return this.badges.containsKey(code);
	}

	@Override
	public void removeBadge(String code, boolean delete) {
		this.removeBadge(code, delete, true, true);
	}

	@Override
	public void removeBadge(String code, boolean delete, boolean sendAlert, boolean sendUpdate) {
		if (badges.containsKey(code)) {
			if (delete) {
				InventoryDao.removeBadge(code, this.getPlayer().getId());
			}

			this.badges.remove(code);

			if (sendAlert) {
				this.getPlayer().getSession().send(new AlertMessageComposer(Locale.get("badge.deleted")));
			}

			this.getPlayer().getSession().send(new BadgeInventoryMessageComposer(this.badges));

			this.getPlayer().flush(this);
		}
	}

	@Override
	public void achievementBadge(String achievement, int level) {
		final String oldBadge = achievement + (level - 1);
		final String newBadge = achievement + level;

		boolean isUpdated = false;

		if (this.badges.containsKey(oldBadge)) {
			this.removeBadge(oldBadge, false, false, false);

			PlayerAchievementDao.updateBadge(oldBadge, newBadge, this.getPlayer().getId());
			isUpdated = true;
		}

		this.addBadge(newBadge, !isUpdated, false, true);
	}

	@Override
	public void resetBadgeSlots() {
		this.badges.forEach((key, value) -> {
			if (value != 0)
				this.badges.replace(key, 0);
		});

		this.getPlayer().flush(this);
	}

	@Override
	public String[] equippedBadges() {	
		final String[] badges = new String[6];

		this.getBadges().forEach((key, value) -> {
			if (value > 0 && value < badges.length) { 
				badges[value] = key;
			}
		});

		return badges;
	}


	@Override
	public IPlayerItem add(long id, int itemId, String extraData, GiftData giftData, ILimitedEditionItem limitedEditionItem) {
		IPlayerItem item = new InventoryItem(id, itemId, extraData, giftData, limitedEditionItem);

		this.inventoryItems.put(id, item);

		this.getPlayer().flush(this);

		return item;
	}

	@Override
	public List<ISongItem> getSongs() {
		return this.inventoryItems.values().stream().filter(inventoryItem -> inventoryItem.getDefinition().isSong())
				.map(inventoryItem -> new SongItemData((InventoryItemSnapshot) inventoryItem.createSnapshot(), inventoryItem.getDefinition().getSongId()))
				.collect(Collectors.toList());
	}

	@Override
	public void add(long id, int itemId, String extraData, ILimitedEditionItem limitedEditionItem) {
		add(id, itemId, extraData, null, limitedEditionItem);
	}

	@Override
	public void addItem(IPlayerItem item) {
		this.inventoryItems.put(item.getId(), item);

		this.getPlayer().flush(this);
	}

	@Override
	public void removeItem(IPlayerItem item) {
		this.removeItem(item.getId());
	}

	@Override
	public void removeItem(long itemId) {
		this.inventoryItems.remove(itemId);
		this.getPlayer().getSession().send(new RemoveObjectFromInventoryMessageComposer(ItemManager.getInstance().getItemVirtualId(itemId)));

		this.getPlayer().flush(this);
	}

	@Override
	public boolean hasItem(long id) {
		return this.getInventoryItems().containsKey(id);
	}

	@Override
	public IPlayerItem getItem(long id) {
		return this.inventoryItems.get(id);
	}

	@Override
	public void dispose() {
		if (this.inventoryItems != null) {
			this.inventoryItems.values().forEach(inventoryItem -> ItemManager.getInstance().disposeItemVirtualId(inventoryItem.getId()));
			this.inventoryItems.clear();
			this.inventoryItems = null;
		}

		if (this.effects != null) {
			this.effects.clear();
			this.effects = null;
		}

		if (this.badges != null) {
			this.badges.clear();
			this.badges = null;
		}
	}

	@Override
	public int getTotalSize() {
		return this.inventoryItems.size();
	}

	@Override
	public Map<Long, IPlayerItem> getInventoryItems() {
		return this.inventoryItems;
	}

	@Override
	public Map<String, Integer> getBadges() {
		return this.badges;
	}

	@Override
	public boolean hasEffect(int effectId) {
		return this.effects.contains(effectId);
	}

	@Override
	public Set<Integer> getEffects() {
		return this.effects;
	}

	@Override
	public boolean itemsLoaded() {
		return itemsLoaded;
	}

	@Override
	public int getEquippedEffect() {
		return this.equippedEffect;
	}

	@Override
	public void setEquippedEffect(int equippedEffect) {
		this.equippedEffect = equippedEffect;
	}

	@Override
	public boolean isViewingInventory() {
		return this.isViewingInventory;
	}

	@Override
	public int viewingInventoryUserId() {
		return this.viewingInventoryUser;
	}

}
