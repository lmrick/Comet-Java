package com.cometproject.server.game.catalog.purchase;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.achievements.types.AchievementType;
import com.cometproject.api.game.bots.BotMode;
import com.cometproject.api.game.bots.BotType;
import com.cometproject.api.game.bots.IBotData;
import com.cometproject.api.game.catalog.types.CatalogPageType;
import com.cometproject.api.game.catalog.types.ICatalogBundledItem;
import com.cometproject.api.game.catalog.types.ICatalogItem;
import com.cometproject.api.game.catalog.types.ICatalogPage;
import com.cometproject.api.game.catalog.types.bundles.IRoomBundle;
import com.cometproject.api.game.catalog.types.purchase.CatalogPurchase;
import com.cometproject.api.game.catalog.types.purchase.ICatalogPurchaseHandler;
import com.cometproject.api.game.furniture.types.IFurnitureDefinition;
import com.cometproject.api.game.furniture.types.GiftData;
import com.cometproject.api.game.furniture.types.IMusicData;
import com.cometproject.api.game.furniture.types.ItemType;
import com.cometproject.api.game.groups.types.IGroup;
import com.cometproject.api.game.players.data.components.inventory.IPlayerItem;
import com.cometproject.api.game.rooms.objects.IRoomItemData;
import com.cometproject.api.game.rooms.objects.data.LimitedEditionItemData;
import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.api.networking.sessions.ISession;
import com.cometproject.api.utilities.JsonUtil;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.composers.catalog.BoughtItemMessageComposer;
import com.cometproject.server.composers.catalog.GiftUserNotFoundMessageComposer;
import com.cometproject.server.composers.catalog.UnseenItemsMessageComposer;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.catalog.CatalogManager;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.game.pets.PetManager;
import com.cometproject.server.game.pets.data.PetData;
import com.cometproject.server.game.pets.data.StaticPetProperties;
import com.cometproject.server.game.rooms.RoomManager;
import com.cometproject.server.game.rooms.bundles.RoomBundleManager;
import com.cometproject.server.game.rooms.bundles.types.RoomBundle;
import com.cometproject.server.game.rooms.objects.entities.types.data.PlayerBotData;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.outgoing.notification.*;
import com.cometproject.server.network.messages.outgoing.room.engine.RoomForwardMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.settings.EnforceRoomCategoryMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.inventory.BotInventoryMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.inventory.EffectsInventoryMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.inventory.PetInventoryMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.inventory.UpdateInventoryMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.storage.queries.bots.PlayerBotDao;
import com.cometproject.server.storage.queries.catalog.CatalogDao;
import com.cometproject.server.storage.queries.items.LimitedEditionDao;
import com.cometproject.server.storage.queries.items.TeleporterDao;
import com.cometproject.server.storage.queries.pets.PetDao;
import com.cometproject.server.storage.queries.player.PlayerDao;
import com.cometproject.storage.api.StorageContext;
import com.cometproject.storage.api.data.DataWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class LegacyPurchaseHandler implements ICatalogPurchaseHandler {
	private final Logger log = Logger.getLogger(LegacyPurchaseHandler.class.getName());
	private ExecutorService executorService;

	public LegacyPurchaseHandler() {

	}

	@Override
	public void purchaseItem(ISession client, int pageId, int itemId, String data, int amount, GiftData giftData) {
		if (CometSettings.asyncCatalogPurchase) {
			if (this.executorService == null) {
				this.executorService = Executors.newFixedThreadPool(2);
			}

			this.executorService.submit(() -> this.handle(client, pageId, itemId, data, amount, giftData));
		} else {
			this.handle(client, pageId, itemId, data, amount, giftData);
		}
	}

	@Override
	public void handle(ISession client, int pageId, int itemId, String data, int amount, GiftData giftData) {
		if (client == null || client.getPlayer() == null)
			return;

		if (amount > 100) {
			client.send(new AlertMessageComposer(Locale.get("catalog.error.tooMany")));
			return;
		}

		final int playerIdToDeliver = giftData == null ? -1 : PlayerDao.getIdByUsername(giftData.getReceiver());
		if (giftData != null && playerIdToDeliver == 0) {
			client.send(new GiftUserNotFoundMessageComposer());
			return;
		}

		Set<IPlayerItem> unseenItems = Sets.newHashSet();
		ICatalogPage page = CatalogManager.getInstance().getPage(pageId);

		try {
			ICatalogItem item;

			try {
				if (page == null || page.getType() == CatalogPageType.RECENT_PURCHASES) {
					page = CatalogManager.getInstance().getCatalogPageByCatalogItemId(itemId);

					if (page.getMinRank() > client.getPlayer().getData().getRank() || !page.getItems().containsKey(itemId)) {
						client.disconnect();
						return;
					}
				}

				item = page.getItems().get(itemId);
			} catch (Exception e) {
				return;
			}

			if (item == null || item.getPageId() != page.getId()) {
				return;
			}

			if (giftData != null) {
				try {
					final IFurnitureDefinition itemDefinition = ItemManager.getInstance().getDefinition(item.getItems().get(0).itemId());

					if (itemDefinition == null) {
						return;
					}

					if (!itemDefinition.canGift()) {
						return;
					}
				} catch (Exception e) {
					return;
				}

				if (client.getPlayer().getLastGift() != 0
						&& !client.getPlayer().getPermissions().getRank().floodBypass()) {
					if (((int) Comet.getTime() - client.getPlayer().getLastGift()) < CometSettings.playerGiftCooldown) {
						client.send(new AdvancedAlertMessageComposer(Locale.get("catalog.error.giftTooFast")));
						client.send(new BoughtItemMessageComposer(BoughtItemMessageComposer.PurchaseType.BADGE));
						return;
					}
				}

				client.getPlayer().setLastGift((int) Comet.getTime());
				client.getPlayer().getAchievements().progressAchievement(AchievementType.GIFT_GIVER, 1);
			}

			if (item.isBadgeOnly()) {

				if (item.hasBadge() && client.getPlayer().getInventory().hasBadge(item.getBadgeId())) {
					client.send(new PurchaseErrorMessageComposer(1));
					client.send(new BoughtItemMessageComposer(BoughtItemMessageComposer.PurchaseType.BADGE));
					return;
				}
			}

			if (amount > 1 && !item.allowOffer()) {
				client.send(new AlertMessageComposer(Locale.get("catalog.error.noOffer")));

				return;
			}

			int totalCostCredits;
			int totalCostPoints;
			int totalCostActivityPoints;
			int totalCostSeasonal;

			if (item.getLimitedSells() >= item.getLimitedTotal() && item.getLimitedTotal() != 0) {
				// client.send(new LimitedEditionSoldOutMessageComposer());
				// TODO: Fix this.
				return;
			}

			try {
				if (CatalogManager.getInstance().getPage(item.getPageId()).getMinRank() > client.getPlayer().getData()
						.getRank()) {
					client.disconnect();
					return;
				}
			} catch (Exception ignored) {
				// Invalid page id..
				return;
			}

			if (item.allowOffer()) {
				totalCostCredits = applyDiscount(item.getCostCredits(), amount);
				totalCostPoints = applyDiscount(item.getCostDiamonds(), amount);
				totalCostActivityPoints = applyDiscount(item.getCostActivityPoints(), amount);
				totalCostSeasonal = applyDiscount(item.getCostSeasonal(), amount);
			} else {
				totalCostCredits = item.getCostCredits();
				totalCostPoints = item.getCostDiamonds();
				totalCostActivityPoints = item.getCostActivityPoints();
				totalCostSeasonal = item.getCostSeasonal();
			}

			if ((!CometSettings.playerInfiniteBalance && (client.getPlayer().getData().getCredits() < totalCostCredits
					|| client.getPlayer().getData().getActivityPoints() < totalCostActivityPoints))
					|| client.getPlayer().getData().getVipPoints() < totalCostPoints) {
				client.getLogger()
						.warn("Player with ID: " + client.getPlayer().getId() + " tried to purchase item with ID: "
								+ item.getId() + " with the incorrect amount of credits or points.");
				client.send(new AlertMessageComposer(Locale.get("catalog.error.notEnough")));
				return;
			}

			if (item.getLimitedTotal() > 0) {
				item.increaseLimitedSells(amount);
				CatalogDao.updateLimitSellsForItem(item.getId(), amount);
			}

			if (!CometSettings.playerInfiniteBalance) {
				client.getPlayer().getData().decreaseCredits(totalCostCredits);
				client.getPlayer().getData().decreaseActivityPoints(totalCostActivityPoints);
			}

			client.getPlayer().getData().decreaseVipPoints(totalCostPoints);
			client.getPlayer().getData().decreaseSeasonalPoints(totalCostSeasonal);

			client.getPlayer().sendBalance();
			client.getPlayer().getData().save();

			if (page != null) {
				if (page.getType() == CatalogPageType.BUNDLE) {
					purchaseBundle(page, client);
					return;
				}
			}

			if (item.isBadgeOnly()) {
				if (item.getPresetData().equals("name_colour")) {
					client.getPlayer().getData().setNameColour(item.getBadgeId());
				} else {
					if (item.hasBadge() && !client.getPlayer().getInventory().hasBadge(item.getBadgeId())) {
						client.getPlayer().getInventory().addBadge(item.getBadgeId(), true);
					}
				}

				client.send(new BoughtItemMessageComposer(BoughtItemMessageComposer.PurchaseType.BADGE));
				return;
			}

			for (ICatalogBundledItem bundledItem : item.getItems()) {
				IFurnitureDefinition def = ItemManager.getInstance().getDefinition(bundledItem.itemId());

				if (def == null) {
					continue;
				}

				client.send(new BoughtItemMessageComposer(item, def));

				if (def.getItemName().equals("DEAL_HC_1")) {
					// TODO: HC buying
					throw new Exception("HC purchasing is not implemented");
				}

				String extraData = "";

				boolean isTeleport = false;

				if (def.getItemType() == ItemType.EFFECT) {
					int effectId = def.getSpriteId();

					// deliver effect

					if (!client.getPlayer().getInventory().hasEffect(effectId)) {
						client.getPlayer().getInventory().getEffects().add(effectId);
						PlayerDao.saveEffect(client.getPlayer().getId(), effectId);

						client.send(new EffectsInventoryMessageComposer(client.getPlayer().getInventory().getEffects(),
								client.getPlayer().getInventory().getEquippedEffect()));
					}

					return;
				} else if (def.getInteraction().equals("trophy")) {
					extraData += client.getPlayer().getData().getUsername() + Character.toChars(9)[0]
							+ DateTime.now().getDayOfMonth() + "-" + DateTime.now().getMonthOfYear() + "-"
							+ DateTime.now().getYear() + Character.toChars(9)[0] + data;
				} else if (def.isTeleporter()) {
					amount = amount * 2;
					isTeleport = true;
				} else if (item.getDisplayName().startsWith("a0 pet")) {
					final String petRace = item.getDisplayName().replace("a0 pet", "");
					final String[] petData = data.split("\n");

					if (petData.length != 3) {
						throw new Exception("Invalid pet data length: " + petData.length);
					}

					final String petName = petData[0];
					if (PetManager.getInstance().validatePetName(petName) > 0) {
						// client wouldn't let them do this since there is pre-validation
						// if they send an invalid name at this point, it's obviously via packet
						// injection,
						// in which case they can, politely, fuck right off. :)
						client.disconnect();
						throw new Exception("Invalid pet name");
					}

					int petId = PetDao.createPet(client.getPlayer().getId(), petName, Integer.parseInt(petRace),
							Integer.parseInt(petData[1]), petData[2]);

					client.getPlayer().getAchievements().progressAchievement(AchievementType.PET_LOVER, 1);
					client.getPlayer().getPets()
							.addPet(new PetData(petId, petName, 0, StaticPetProperties.DEFAULT_LEVEL,
									StaticPetProperties.DEFAULT_HAPPINESS, StaticPetProperties.DEFAULT_EXPERIENCE,
									StaticPetProperties.DEFAULT_ENERGY, StaticPetProperties.DEFAULT_HUNGER,
									client.getPlayer().getId(), client.getPlayer().getData().getUsername(), petData[2],
									Integer.parseInt(petData[1]), Integer.parseInt(petRace)));
					client.send(new PetInventoryMessageComposer(client.getPlayer().getPets().getPets()));

					client.send(new UnseenItemsMessageComposer(new HashMap<Integer, List<Integer>>() {
						{
							put(3, Lists.newArrayList(petId));
						}
					}));
					return;
				} else if (def.getInteraction().equals("postit")) {
					amount = 20; 
					extraData = "";
				} else if (def.isRoomDecor()) {
					if (data.isEmpty()) {
						extraData += "0";
					} else {
						extraData += data.replace(",", ".");
					}
				} else if (def.getInteraction().equals("group_item") || def.getInteraction().equals("group_gate")) {
					if (data.isEmpty() || !StringUtils.isNumeric(data))
						return;

					if (!client.getPlayer().getGroups().contains(Integer.parseInt(data))) {
						return;
					}

					extraData = data;
				} else if (def.getType().equals("r")) {
					String botName = "New Bot";
					String botFigure = item.getPresetData();
					String botGender = "m";
					String botMotto = "Beeb beeb boop beep!";
					BotType type = BotType.GENERIC;
					BotMode mode = BotMode.DEFAULT;

					type = switch (item.getDisplayName()) {
						case "bot_bartender" -> BotType.WAITER;
						case "bot_spy" -> BotType.SPY;
						default -> type;
					};

					final int botId = PlayerBotDao.createBot(client.getPlayer().getId(), botName, botFigure, botGender, botMotto, type);
					final IBotData botData = new PlayerBotData(botId, botName, botMotto, botFigure, botGender,
							client.getPlayer().getData().getUsername(), client.getPlayer().getId(), "", true, 7, type,
							mode, "");

					client.getPlayer().getBots().addBot(botData);
					client.send(new BotInventoryMessageComposer(client.getPlayer().getBots().getBots()));

					client.send(new UnseenItemsMessageComposer(new HashMap<Integer, List<Integer>>() {
						{
							put(5, Lists.newArrayList(botId));
						}
					}));

					return;
				} else if (def.getInteraction().equals("badge_display")) {
					if (client.getPlayer().getInventory().getBadges().get(data) == null) {
						return;
					}

					extraData = data + "~" + client.getPlayer().getData().getUsername() + "~" + DateTime.now().getDayOfMonth() + "-" + DateTime.now().getMonthOfYear() + "-" + DateTime.now().getYear();
				} else if (def.getInteraction().equals("group_forum")) {
					if (data.isEmpty() || !StringUtils.isNumeric(data))
						return;

					if (!client.getPlayer().getGroups().contains(Integer.parseInt(data))) {
						return;
					}

					int groupId = Integer.parseInt(data);
					IGroup group = GameContext.getCurrent().getGroupService().getGroup(groupId);

					if (!group.getData().hasForum() && group.getData().getOwnerId() == client.getPlayer().getId()) {
						GameContext.getCurrent().getGroupService().addForum(group);

						Map<String, String> notificationParams = Maps.newHashMap();

						notificationParams.put("groupId", groupId + "");
						notificationParams.put("groupName", group.getData().getTitle());

						client.send(new NotificationMessageComposer("forums.delivered", notificationParams));

					}

					extraData = "" + groupId;
				} else if (def.isSong()) {
					final String songName = item.getPresetData();

					if (songName != null && !songName.isEmpty()) {
						IMusicData musicData = ItemManager.getInstance().getMusicDataByName(songName);

						if (musicData != null) {
							extraData = String.format("%s\n%s\n%s\n%s\n%s\n%s",
									client.getPlayer().getData().getUsername(),
									Calendar.getInstance().get(Calendar.YEAR),
									Calendar.getInstance().get(Calendar.MONTH),
									Calendar.getInstance().get(Calendar.DAY_OF_MONTH), musicData.lengthSeconds(),
									musicData.title());
						}
					}
				}

				long[] teleportIds = null;
				if (isTeleport) {
					teleportIds = new long[amount];
				}

				List<CatalogPurchase> purchases = new ArrayList<>();
				if (giftData != null) {
					giftData.setExtraData(extraData);
					IFurnitureDefinition itemDefinition = ItemManager.getInstance().getBySpriteId(giftData.getSpriteId());

					purchases.add(new CatalogPurchase(playerIdToDeliver,
							itemDefinition == null ? CatalogManager.getInstance().getGiftBoxesOld().get(0) : itemDefinition.getId(),
							GiftData.EXTRA_DATA_HEADER + JsonUtil.getInstance().toJson(giftData)));
				} else {
					for (int purchaseCount = 0; purchaseCount < amount; purchaseCount++) {
						for (int itemCount = 0; itemCount < bundledItem.amount(); itemCount++) {
							purchases.add(new CatalogPurchase(client.getPlayer().getId(), bundledItem.itemId(), extraData));
						}
					}
				}

				final List<Long> newItems;
				final DataWrapper<List<Long>> idsData = DataWrapper.createEmpty();
				StorageContext.getCurrentContext().getRoomItemRepository().createItems(purchases, idsData::set,
						client.getPlayer().getInventory().viewingInventoryUserId());

				if (!idsData.has()) {
					throw new Exception("Failed to insert items");
				}

				newItems = idsData.get();

				for (long newItem : newItems) {
					if (item.getLimitedTotal() > 0) {
						LimitedEditionDao.save(new LimitedEditionItemData(newItem, item.getLimitedSells(), item.getLimitedTotal()));
					}

					if (giftData == null) {
						unseenItems.add(client.getPlayer().getInventory().add(newItem, bundledItem.itemId(), extraData,
								giftData, item.getLimitedTotal() > 0
										? new LimitedEditionItemData(bundledItem.itemId(), item.getLimitedSells(), item.getLimitedTotal()) : null));

						if (isTeleport && teleportIds != null) {
							teleportIds[newItems.indexOf(newItem)] = newItem;
						}
					}
				}

				if (isTeleport) {
					long lastId = 0;
					if (teleportIds != null) {
						for (int a = 0; a < teleportIds.length; a++) {
							for (int i = 0; i < teleportIds.length; i++) {
								if (lastId == 0) {
									lastId = teleportIds[i];
								}

								if (i % 2 == 0 && lastId != 0) {
									lastId = teleportIds[i];
									continue;
								}

								TeleporterDao.savePair(teleportIds[i], lastId);
							}
						}
					}
				}

				if (giftData != null) {
					this.deliverGift(playerIdToDeliver, giftData, newItems, client.getPlayer().getData().getUsername());
				} else {
					if (item.hasBadge()) {
						client.getPlayer().getInventory().addBadge(item.getBadgeId(), true);
					}

					client.send(new UnseenItemsMessageComposer(unseenItems, ItemManager.getInstance()));
					client.send(new UpdateInventoryMessageComposer());

					if (CometSettings.logCatalogPurchases) {
						CatalogDao.saveRecentPurchase(client.getPlayer().getId(), item.getId(), amount, extraData);
					}

					client.getPlayer().getRecentPurchases().add(item.getId());
				}
			}

		} catch (Exception e) {
			log.error("Error while buying catalog item", e);
		} finally {
			// Clean up the purchase - even if there was an exception!!
			unseenItems.clear();
		}
	}

	@Override
	public void deliverGift(int playerId, GiftData giftData, List<Long> newItems, String senderUsername) {
		Session client = NetworkManager.getInstance().getSessions().getByPlayerId(playerId);

		if (client != null) {
			Set<IPlayerItem> unseenItems = Sets.newHashSet();

			if (client.getPlayer() != null) {
				if (client.getPlayer().getInventory() != null) {
					unseenItems = newItems.stream().mapToLong(newItem -> newItem)
							.mapToObj(newItem -> client.getPlayer().getInventory().add(newItem,
									ItemManager.getInstance().getBySpriteId(giftData.getSpriteId()).getId(),
									GiftData.EXTRA_DATA_HEADER + JsonUtil.getInstance().toJson(giftData), giftData,
									null))
							.collect(Collectors.toSet());
				}

				if (client.getPlayer().getAchievements() != null) {
					client.getPlayer().getAchievements().progressAchievement(AchievementType.GIFT_RECEIVER, 1);
				}
			}

			client.send(new UnseenItemsMessageComposer(unseenItems, ItemManager.getInstance()));
			client.send(new UpdateInventoryMessageComposer());
			client.send(new NotificationMessageComposer("gift_received",
					Locale.getOrDefault("notification.giftReceived", "You have just received a gift from %username%!").replace("%username%", senderUsername)));

		}
	}

	@Override
	public void purchaseBundle(IRoomBundle roomBundle, ISession client) {
		try {
			int roomId = RoomManager.getInstance().createRoom(
					roomBundle.getConfig().getRoomName().replace("%username%",
							client.getPlayer().getData().getUsername()),
					"", roomBundle.getRoomModelData(), 0, 20, 0, client, roomBundle.getConfig().getThicknessWall(),
					roomBundle.getConfig().getThicknessFloor(), roomBundle.getConfig().getDecorations(),
					roomBundle.getConfig().isHideWalls());
			final Set<IRoomItemData> roomItemData = Sets.newHashSet();

			roomBundle.getRoomBundleData().forEach(roomBundleItem -> {
				final Position position = roomBundleItem.getWallPosition() == null
						? new Position(roomBundleItem.getX(), roomBundleItem.getY(), roomBundleItem.getZ())
						: null;
				roomItemData.add(new RoomItemData(-1, roomBundleItem.getItemId(), client.getPlayer().getId(), "",
						position, roomBundleItem.getRotation(), roomBundleItem.getExtraData(),
						roomBundleItem.getWallPosition(), null));
			});

			StorageContext.getCurrentContext().getRoomItemRepository().placeBundle(roomId, roomItemData);

			client.send(new RoomForwardMessageComposer(roomId));
			client.send(new EnforceRoomCategoryMessageComposer());
			client.send(new BoughtItemMessageComposer(BoughtItemMessageComposer.PurchaseType.BADGE));

			client.getPlayer().setLastRoomCreated((int) Comet.getTime());
		} catch (Exception e) {
			client.send(new MotdNotificationMessageComposer("Invalid room bundle data, please contact an administrator."));
			client.send(new BoughtItemMessageComposer(BoughtItemMessageComposer.PurchaseType.BADGE));
		}
	}

	@Override
	public void purchaseBundle(ICatalogPage page, ISession client) {
		RoomBundle roomBundle = RoomBundleManager.getInstance().getBundle(page.getExtraData());
		purchaseBundle(roomBundle, client);
	}

	@Override
	public int applyDiscount(int cost, int quantity) {
		int uCost = cost;
		cost = cost * quantity;

		int discountMultiplier = 0;
		int[] thresholds = { 5, 10, 16, 22, 28, 34, 40, 46, 52, 59, 64, 70, 76, 82, 88, 94, 100 };
		int[] multipliers = { 0, 1, 2, 4, 6, 8, 10, 13, 15, 17, 20, 22, 24, 26, 28, 30, 32 };

		for (int i = 0; i < thresholds.length; i++) {
			if (quantity <= thresholds[i]) {
				discountMultiplier = multipliers[i] + (quantity - (i > 0 ? thresholds[i - 1] : 0)) / 2;
				break;
			}
		}

		return cost - uCost * discountMultiplier;
	}

}
