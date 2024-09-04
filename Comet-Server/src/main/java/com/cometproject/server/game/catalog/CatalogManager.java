package com.cometproject.server.game.catalog;

import com.cometproject.api.game.catalog.ICatalogService;
import com.cometproject.api.game.catalog.types.*;
import com.cometproject.api.game.catalog.types.purchase.ICatalogPurchaseHandler;
import com.cometproject.server.game.catalog.purchase.LegacyPurchaseHandler;
import com.cometproject.server.storage.queries.catalog.CatalogDao;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class CatalogManager implements ICatalogService {
	
	private static CatalogManager catalogManagerInstance;
	private final Map<Integer, ICatalogOffer> catalogOffers = new HashMap<>();
	private final List<Integer> giftBoxesNew = Lists.newArrayList();
	private final List<Integer> giftBoxesOld = Lists.newArrayList();
	private final List<ICatalogFrontPageEntry> frontPageEntries = new ArrayList<>();
	private final Map<String, IClothingItem> clothingItems = Maps.newConcurrentMap();
	private Map<Integer, ICatalogPage> pages;
	private Map<Integer, ICatalogItem> items;
	private Map<Integer, Integer> catalogItemIdToPageId;
	private ICatalogPurchaseHandler purchaseHandler;
	private static final Logger log = Logger.getLogger(CatalogManager.class.getName());
	private final List<ICatalogPage> parentPages = Lists.newCopyOnWriteArrayList();
	
	public CatalogManager() {
	
	}
	
	public static CatalogManager getInstance() {
		if (catalogManagerInstance == null) catalogManagerInstance = new CatalogManager();
		return catalogManagerInstance;
	}
	
	@Override
	public void initialize() {
		this.pages = new ListOrderedMap<>();
		this.items = new ListOrderedMap<>();
		
		this.catalogItemIdToPageId = new HashMap<>();
		
		this.purchaseHandler = new LegacyPurchaseHandler();
		
		this.loadItemsAndPages();
		this.loadGiftBoxes();
		this.loadClothingItems();
		
		log.info("CatalogManager initialized");
	}
	
	@Override
	public void loadItemsAndPages() {
		if (!this.items.isEmpty()) {
			this.items.clear();
		}
		
		if (!this.getPages().isEmpty()) {
			this.getPages().clear();
		}
		
		if (!this.frontPageEntries.isEmpty()) {
			this.frontPageEntries.clear();
		}
		
		if (!this.getCatalogOffers().isEmpty()) {
			getCatalogOffers().clear();
		}
		
		if (!this.catalogItemIdToPageId.isEmpty()) {
			this.catalogItemIdToPageId.clear();
		}
		
		try {
			CatalogDao.getItems(this.items);
			CatalogDao.getPages(this.pages);
			CatalogDao.getFeaturedPages(this.frontPageEntries);
		} catch (Exception e) {
			log.error("Error while loading catalog pages/items", e);
		}
		
		this.pages.values().forEach(page -> page.getItems().keySet().forEach(item -> this.catalogItemIdToPageId.put(item, page.getId())));
		
		this.sortCatalogChildren();
		
		log.info("Loaded " + this.getPages().size() + " catalog pages and " + this.items.size() + " catalog items");
	}
	
	@Override
	public void loadGiftBoxes() {
		if (!this.giftBoxesNew.isEmpty()) {
			this.giftBoxesNew.clear();
		}
		
		if (!this.giftBoxesOld.isEmpty()) {
			this.giftBoxesOld.clear();
		}
		
		CatalogDao.loadGiftBoxes(this.giftBoxesOld, this.giftBoxesNew);
		log.info("Loaded " + (this.giftBoxesNew.size() + this.giftBoxesOld.size()) + " gift wrappings");
	}
	
	@Override
	public void loadClothingItems() {
		if (!this.clothingItems.isEmpty()) {
			this.clothingItems.clear();
		}
		
		CatalogDao.getClothing(this.clothingItems);
		log.info("Loaded " + clothingItems.size() + " clothing items");
	}
	
	@Override
	public List<ICatalogPage> getPagesForRank(int rank) {
		return this.getPages().values().stream().filter(page -> rank >= page.getMinRank()).collect(Collectors.toList());
	}
	
	public void sortCatalogChildren() {
		this.parentPages.clear();
		
		this.pages.values().forEach(catalogPage -> {
			if (catalogPage.getParentId() != -1) {
				final ICatalogPage parentPage = this.getPage(catalogPage.getParentId());
				
				if (parentPage == null) {
					log.warn("Page " + catalogPage.getId() + " with invalid parent id: " + catalogPage.getParentId());
				} else {
					parentPage.getChildren().add(catalogPage);
				}
			} else {
				this.parentPages.add(catalogPage);
			}
		});
		
		this.parentPages.sort(Comparator.comparing(ICatalogPage::getOrder));
	}
	
	@Override
	public ICatalogItem getCatalogItemByOfferId(int offerId) {
		ICatalogOffer offer = getCatalogOffers().get(offerId);
		
		if (offer == null) return null;
		
		ICatalogPage page = this.getPage(offer.catalogPageId());
		if (page == null) return null;
		
		return page.getItems().get(offer.catalogItemId());
	}
	
	@Override
	public ICatalogPage getCatalogPageByCatalogItemId(int id) {
		if (!this.catalogItemIdToPageId.containsKey(id)) {
			return null;
		}
		
		return this.pages.get(this.catalogItemIdToPageId.get(id));
	}
	
	@Override
	public ICatalogItem getCatalogItemByItemId(int itemId) {
		if (!this.items.containsKey(itemId)) {
			return null;
		}
		
		return this.items.get(itemId);
	}
	
	@Override
	public Map<Integer, ICatalogItem> getItemsForPage(int pageId) {
		
		return this.items.entrySet().stream().filter(catalogItem -> catalogItem.getValue().getPageId() == pageId).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));
	}
	
	@Override
	public ICatalogPage getPage(int id) {
		return this.pageExists(id) ? this.getPages().get(id) : null;
		
	}
	
	@Override
	public ICatalogItem getCatalogItem(final int catalogItemId) {
		return this.items.get(catalogItemId);
	}
	
	@Override
	public boolean pageExists(int id) {
		return this.getPages().containsKey(id);
	}
	
	@Override
	public Map<Integer, ICatalogPage> getPages() {
		return this.pages;
	}
	
	@Override
	public ICatalogPurchaseHandler getPurchaseHandler() {
		return purchaseHandler;
	}
	
	@Override
	public List<Integer> getGiftBoxesNew() {
		return giftBoxesNew;
	}
	
	@Override
	public List<Integer> getGiftBoxesOld() {
		return giftBoxesOld;
	}
	
	@Override
	public List<ICatalogFrontPageEntry> getFrontPageEntries() {
		return this.frontPageEntries;
	}
	
	@Override
	public Map<String, IClothingItem> getClothingItems() {
		return this.clothingItems;
	}
	
	@Override
	public Map<Integer, ICatalogOffer> getCatalogOffers() {
		return catalogOffers;
	}
	
	@Override
	public List<ICatalogPage> getParentPages() {
		return parentPages;
	}
	
}
