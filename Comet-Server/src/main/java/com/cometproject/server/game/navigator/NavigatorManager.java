package com.cometproject.server.game.navigator;

import com.cometproject.api.utilities.process.Initializable;
import com.cometproject.server.game.navigator.types.categories.Category;
import com.cometproject.server.game.navigator.types.categories.NavigatorCategoryType;
import com.cometproject.server.game.navigator.types.publics.PublicRoom;
import com.cometproject.server.storage.queries.navigator.NavigatorDao;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NavigatorManager implements Initializable {
	private static NavigatorManager navigatorManagerInstance;
	private final Logger log = LogManager.getLogger(NavigatorManager.class.getName());
	private Map<Integer, Category> categories;
	private List<Category> userCategories;
	private Map<Integer, PublicRoom> publicRooms;
	private Set<Integer> staffPicks;
	
	public NavigatorManager() {
	}
	
	public static NavigatorManager getInstance() {
		if (navigatorManagerInstance == null) navigatorManagerInstance = new NavigatorManager();
		return navigatorManagerInstance;
	}
	
	@Override
	public void initialize() {
		this.loadCategories();
		this.loadPublicRooms();
		this.loadStaffPicks();
		
		log.info("NavigatorManager initialized");
	}
	
	public void loadPublicRooms() {
		try {
			if (this.publicRooms != null && !this.publicRooms.isEmpty()) {
				this.publicRooms.clear();
			}
			
			this.publicRooms = NavigatorDao.getPublicRooms();
			
		} catch (Exception e) {
			log.error("Error while loading public rooms", e);
		}
		
		log.info("Loaded " + this.publicRooms.size() + " public rooms");
	}
	
	public void loadStaffPicks() {
		try {
			if (this.staffPicks != null && !this.staffPicks.isEmpty()) {
				this.staffPicks.clear();
			}
			
			this.staffPicks = NavigatorDao.getStaffPicks();
			
		} catch (Exception e) {
			log.error("Error while loading staff picked rooms", e);
		}
		
		log.info("Loaded " + this.publicRooms.size() + " staff picks");
	}
	
	public void loadCategories() {
		try {
			if (this.categories != null && !this.categories.isEmpty()) {
				this.categories.clear();
			}
			
			if (this.userCategories == null) {
				this.userCategories = Lists.newArrayList();
			} else {
				this.userCategories.clear();
			}
			
			this.categories = NavigatorDao.getCategories();
			
			this.categories.values().stream().filter(category -> category.categoryType() == NavigatorCategoryType.CATEGORY).forEachOrdered(category -> this.userCategories.add(category));
		} catch (Exception e) {
			log.error("Error while loading navigator categories", e);
		}
		
		log.info("Loaded " + (this.getCategories() == null ? 0 : this.getCategories().size()) + " room categories");
	}
	
	public Category getCategory(int id) {
		return this.categories.get(id);
	}
	
	public boolean isStaffPicked(int roomId) {
		return this.staffPicks.contains(roomId);
	}
	
	public PublicRoom getPublicRoom(int roomId) {
		return this.publicRooms.get(roomId);
	}
	
	public Map<Integer, Category> getCategories() {
		return this.categories;
	}
	
	public Map<Integer, PublicRoom> getPublicRooms(String category) {
		return this.publicRooms.values().stream().filter(publicRoom -> publicRoom.category().equals(category)).collect(Collectors.toMap(PublicRoom::roomId, publicRoom -> publicRoom, (a, b) -> b, LinkedHashMap::new));
	}
	
	public Set<Integer> getStaffPicks() {
		return staffPicks;
	}
	
	public List<Category> getUserCategories() {
		return userCategories;
	}
	
}
