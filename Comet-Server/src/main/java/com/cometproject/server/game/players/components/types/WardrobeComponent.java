package com.cometproject.server.game.players.components.types;

import com.cometproject.api.game.players.components.PlayerComponentContext;
import com.cometproject.api.game.players.data.components.IPlayerWardrobe;
import com.cometproject.server.game.players.components.PlayerComponent;
import com.cometproject.server.storage.queries.player.PlayerClothingDao;
import com.cometproject.server.utilities.collections.ConcurrentHashSet;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class WardrobeComponent extends PlayerComponent implements IPlayerWardrobe {
	private final Logger LOG = getLogger(WardrobeComponent.class);
	private final Set<String> purchasedClothing;
	
	public WardrobeComponent(PlayerComponentContext componentContext) {
		super(componentContext);
		
		this.purchasedClothing = new ConcurrentHashSet<>();
		PlayerClothingDao.getClothing(componentContext.getPlayer().getId(), this.purchasedClothing);
	}
	
	@Override
	public void dispose() {
		this.purchasedClothing.clear();
	}
	
	@Override
	public Set<String> getClothing() {
		return purchasedClothing;
	}
	
}
