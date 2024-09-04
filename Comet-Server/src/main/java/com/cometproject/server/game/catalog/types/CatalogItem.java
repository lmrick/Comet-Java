package com.cometproject.server.game.catalog.types;

import com.cometproject.api.game.catalog.types.ICatalogBundledItem;
import com.cometproject.api.game.catalog.types.ICatalogItem;
import com.cometproject.api.game.furniture.types.FurnitureDefinition;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.catalog.CatalogManager;
import com.cometproject.server.game.items.ItemManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CatalogItem implements ICatalogItem {
    private final int id;
    private final String itemId;
    private final String displayName;
    private final int costCredits;
    private final int costActivityPoints;
    private final int costDiamonds;
    private final int costSeasonal;
    private final int amount;
    private final boolean vip;
    private List<ICatalogBundledItem> items;
    private final int limitedTotal;
    private int limitedSells;
    private final boolean allowOffer;
    private final String badgeId;
    private final String presetData;
    private final int pageId;

    public CatalogItem(int id, String itemId, String displayName, int costCredits, int costActivityPoints, int costDiamonds, int costSeasonal, int amount, boolean vip, int limitedTotal, int limitedSells, boolean allowOffer, String badgeId, String presetData, int pageId) {
        this(id, itemId, null, displayName, costCredits, costActivityPoints, costDiamonds, costSeasonal, amount, vip, limitedTotal, limitedSells, allowOffer, badgeId, presetData, pageId);
    }

    public CatalogItem(int id, String itemId, List<ICatalogBundledItem> bundledItems, String displayName, int costCredits, int costActivityPoints, int costDiamonds, int costSeasonal, int amount, boolean vip, int limitedTotal, int limitedSells, boolean allowOffer, String badgeId, String presetData, int pageId) {
        this.id = id;
        this.itemId = itemId;
        this.displayName = displayName;
        this.costCredits = costCredits;
        this.costActivityPoints = costActivityPoints;
        this.costDiamonds = costDiamonds;
        this.costSeasonal = costSeasonal;
        this.amount = amount;
        this.vip = vip;
        this.limitedTotal = limitedTotal;
        this.limitedSells = limitedSells;
        this.allowOffer = allowOffer;
        this.badgeId = badgeId;
        this.presetData = presetData;
        this.pageId = pageId;

        this.items = bundledItems != null ? bundledItems : new ArrayList<>();

        if (items.isEmpty()) {
            if (!this.itemId.equals("-1")) {
                if (bundledItems != null) {
                    items = bundledItems;
                } else {

                    if (itemId.contains(",")) {
                        String[] split = itemId.replace("\n", "").split(",");
											
											Arrays.stream(split).filter(str -> !str.isEmpty()).map(str -> str.split(":")).filter(parts -> parts.length == 3).forEachOrdered(parts -> {
												try {
													final int aItemId = Integer.parseInt(parts[0]);
													final int aAmount = Integer.parseInt(parts[1]);
													final String aPresetData = parts[2];
													
													this.items.add(new CatalogBundledItem(aPresetData, aAmount, aItemId));
												} catch (Exception ignored) {
													Comet.getServer().getLogger().warn("Invalid item data for catalog item: " + this.id);
												}
											});
                    } else {
                        this.items.add(new CatalogBundledItem(this.presetData, this.amount, Integer.parseInt(this.itemId)));
                    }
                }
            }

            if (this.getItems().isEmpty()) return;

            List<ICatalogBundledItem> itemsToRemove = new ArrayList<>();
					
					this.items.forEach(catalogBundledItem -> {
						final FurnitureDefinition itemDefinition = ItemManager.getInstance().getDefinition(catalogBundledItem.itemId());
						if (itemDefinition == null) {
							itemsToRemove.add(catalogBundledItem);
						}
					});

            this.items.removeAll(itemsToRemove);
            itemsToRemove.clear();

            if (this.items.isEmpty()) {
                return;
            }

            if (ItemManager.getInstance().getDefinition(this.getItems().getFirst().itemId()) == null) return;
            int offerId = ItemManager.getInstance().getDefinition(this.getItems().getFirst().itemId()).getOfferId();

            if (!CatalogManager.getInstance().getCatalogOffers().containsKey(offerId)) {
                CatalogManager.getInstance().getCatalogOffers().put(offerId, new CatalogOffer(offerId, this.getPageId(), this.getId()));
            }
        }
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        final FurnitureDefinition firstItem = this.itemId.equals("-1") ? null : ItemManager.getInstance().getDefinition(this.getItems().get(0).itemId());

        msg.writeInt(this.getId());
        msg.writeString(this.getDisplayName());
        msg.writeBoolean(false);

        msg.writeInt(this.getCostCredits());

        if (this.getCostDiamonds() > 0) {
            msg.writeInt(this.getCostDiamonds());
            msg.writeInt(105);
        } else if (this.getCostActivityPoints() > 0) {
            msg.writeInt(this.getCostActivityPoints());
            msg.writeInt(0);
        } else if(this.getCostSeasonal() > 0) {
            msg.writeInt(this.getCostSeasonal());
            msg.writeInt(106);
        } else {
            msg.writeInt(0);
            msg.writeInt(0);
        }

        msg.writeBoolean(firstItem != null && firstItem.canGift());

        if (!this.hasBadge()) {
            msg.writeInt(this.getItems().size());
        } else {
            msg.writeInt(this.isBadgeOnly() ? 1 : this.getItems().size() + 1);
            msg.writeString("b");
            msg.writeString(this.getBadgeId().split(",")[0]);
        }

        if (!this.isBadgeOnly()) {
					this.getItems().forEach(bundledItem -> {
						FurnitureDefinition def = ItemManager.getInstance().getDefinition(bundledItem.itemId());
						msg.writeString(def.getType());
						msg.writeInt(def.getSpriteId());
						msg.writeString(this.getDisplayName().contains("wallpaper_single") || this.getDisplayName().contains("floor_single") || this.getDisplayName().contains("landscape_single") ? this.getDisplayName().split("_")[2] : bundledItem.presetData());
						msg.writeInt(bundledItem.amount());
						msg.writeBoolean(this.getLimitedTotal() != 0);
						if (this.getLimitedTotal() > 0) {
							msg.writeInt(this.getLimitedTotal());
							msg.writeInt(this.getLimitedTotal() - this.getLimitedSells());
						}
					});
        }

        msg.writeInt(0); // club level
        msg.writeBoolean(!(this.getLimitedTotal() > 0) && this.allowOffer());
        msg.writeBoolean(false);
        msg.writeString("");
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    @Override
    public List<ICatalogBundledItem> getItems() {
        return this.items;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int getCostCredits() {
        return costCredits;
    }

    @Override
    public int getCostActivityPoints() {
        return costActivityPoints;
    }

    @Override
    public int getCostDiamonds() {
        return costDiamonds;
    }

    @Override
    public int getCostSeasonal() {
        return costSeasonal;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public boolean isVip() {
        return vip;
    }

    @Override
    public int getLimitedTotal() {
        return this.limitedTotal;
    }

    @Override
    public int getLimitedSells() {
        return this.limitedSells;
    }

    @Override
    public boolean allowOffer() {
        return this.allowOffer;
    }

    @Override
    public void increaseLimitedSells(int amount) {
        this.limitedSells += amount;
    }

    @Override
    public boolean hasBadge() {
        return !(this.badgeId.isEmpty());
    }

    @Override
    public boolean isBadgeOnly() {
        return this.items.isEmpty() && this.hasBadge();
    }

    @Override
    public String getBadgeId() {
        return this.badgeId;
    }

    @Override
    public String getPresetData() {
        return presetData;
    }

    @Override
    public int getPageId() {
        return pageId;
    }
}
