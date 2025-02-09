package com.cometproject.server.game.groups.items;

import com.cometproject.api.game.groups.IGroupItemService;
import com.cometproject.api.game.groups.items.IGroupBadgeItem;
import com.cometproject.server.storage.queries.groups.GroupItemDao;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupItemManager implements IGroupItemService {
	private static final Logger log = Logger.getLogger(GroupItemManager.class.getName());
	private List<IGroupBadgeItem> bases;
	private List<IGroupBadgeItem> symbols;
	private List<IGroupBadgeItem> baseColours;
	private Map<Integer, IGroupBadgeItem> symbolColours;
	private Map<Integer, IGroupBadgeItem> backgroundColours;
	
	public GroupItemManager() {
		this.load();
	}
	
	public void load() {
		if (bases == null) {
			bases = new ArrayList<>();
			symbols = new ArrayList<>();
			baseColours = new ArrayList<>();
			symbolColours = new HashMap<>();
			backgroundColours = new HashMap<>();
		} else if (symbols == null && baseColours == null && symbolColours == null && backgroundColours == null) {
			bases.clear();
			symbols.clear();
			baseColours.clear();
			symbolColours.clear();
			backgroundColours.clear();
		}
		
		int itemCount = GroupItemDao.loadGroupItems(bases, symbols, baseColours, symbolColours, backgroundColours);
		log.info(MessageFormat.format("Loaded {0} group items", itemCount));
	}
	
	public List<IGroupBadgeItem> getBases() {
		return this.bases;
	}
	
	public List<IGroupBadgeItem> getSymbols() {
		return this.symbols;
	}
	
	public List<IGroupBadgeItem> getBaseColours() {
		return this.baseColours;
	}
	
	public Map<Integer, IGroupBadgeItem> getSymbolColours() {
		return this.symbolColours;
	}
	
	public Map<Integer, IGroupBadgeItem> getBackgroundColours() {
		return this.backgroundColours;
	}
	
}
