package com.cometproject.api.game.catalog;

import com.cometproject.api.game.catalog.purchase.ICatalogPurchaseHandler;
import com.cometproject.api.game.catalog.types.clothing.IClothingItem;
import com.cometproject.api.game.catalog.types.items.ICatalogItem;
import com.cometproject.api.game.catalog.types.offers.ICatalogOffer;
import com.cometproject.api.game.catalog.types.pages.ICatalogFrontPageEntry;
import com.cometproject.api.game.catalog.types.pages.ICatalogPage;

import java.util.List;
import java.util.Map;

public interface ICatalogService {
    Map<Integer, ICatalogOffer> getCatalogOffers();

    void initialize();

    void loadItemsAndPages();

    void loadGiftBoxes();

    void loadClothingItems();

    List<ICatalogPage> getPagesForRank(int rank);

    ICatalogItem getCatalogItemByOfferId(int offerId);

    ICatalogPage getCatalogPageByCatalogItemId(int id);

    ICatalogItem getCatalogItemByItemId(int itemId);

    Map<Integer, ICatalogItem> getItemsForPage(int pageId);

    ICatalogPage getPage(int id);

    ICatalogItem getCatalogItem(int catalogItemId);

    boolean pageExists(int id);

    Map<Integer, ICatalogPage> getPages();

    ICatalogPurchaseHandler getPurchaseHandler();

    List<Integer> getGiftBoxesNew();

    List<Integer> getGiftBoxesOld();

    List<ICatalogFrontPageEntry> getFrontPageEntries();

    Map<String, IClothingItem> getClothingItems();

    List<ICatalogPage> getParentPages();
}
