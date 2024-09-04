package com.cometproject.api.game.players.data.components;

import com.cometproject.api.game.furniture.types.GiftData;
import com.cometproject.api.game.furniture.types.ILimitedEditionItem;
import com.cometproject.api.game.furniture.types.ISongItem;
import com.cometproject.api.game.players.components.IPlayerComponent;
import com.cometproject.api.game.players.data.components.inventory.IPlayerItem;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IPlayerInventory extends IPlayerComponent {
    void loadItems(int id);

    void loadBadges();

    void loadEffects();

    void send();

    void addBadge(String code, boolean insert);

    void addBadge(String code, boolean insert, boolean sendAlert);

    void addBadge(String code, boolean insert, boolean sendAlert, boolean isAchievement);

    boolean hasBadge(String code);

    void removeBadge(String code, boolean delete);

    void removeBadge(String code, boolean delete, boolean sendAlert, boolean sendUpdate);

    void achievementBadge(String achievement, int level);

    void resetBadgeSlots();

    String[] equippedBadges();

    IPlayerItem add(long id, int itemId, String extraData, GiftData giftData, ILimitedEditionItem limitedEditionItem);

    List<ISongItem> getSongs();

    void add(long id, int itemId, String extraData, ILimitedEditionItem limitedEditionItem);

    void addItem(IPlayerItem item);

    void removeItem(IPlayerItem item);

    void removeItem(long itemId);

    boolean hasItem(long id);

    IPlayerItem getItem(long id);

    int getTotalSize();

    Map<Long, IPlayerItem> getInventoryItems();

    Map<String, Integer> getBadges();

    boolean hasEffect(int effectId);

    Set<Integer> getEffects();

    int getEquippedEffect();

    void setEquippedEffect(int effectId);

    boolean itemsLoaded();

    boolean isViewingInventory();

    int viewingInventoryUserId();
}
