package com.cometproject.server.game.players.components.types;

import com.cometproject.api.game.bots.IBotData;
import com.cometproject.api.game.players.components.PlayerComponentContext;
import com.cometproject.api.game.players.data.components.IPlayerBots;
import com.cometproject.server.game.players.components.PlayerComponent;
import com.cometproject.server.storage.queries.bots.PlayerBotDao;
import java.util.Map;

public class InventoryBotComponent extends PlayerComponent implements IPlayerBots {
	private Map<Integer, IBotData> bots;
	
	public InventoryBotComponent(PlayerComponentContext componentContext) {
		super(componentContext);
		
		this.bots = PlayerBotDao.getBotsByPlayerId(componentContext.getPlayer().getId());

		this.getPlayer().flush(this);
	}
	
	@Override
	public void addBot(IBotData bot) {
		this.bots.put(bot.getId(), bot);
		
		this.getPlayer().flush(this);
	}
	
	@Override
	public IBotData getBot(int id) {
		return this.bots.get(id);
	}
	
	@Override
	public void remove(int id) {
		this.bots.remove(id);
		
		this.getPlayer().flush(this);
	}
	
	@Override
	public boolean isBot(int id) {
		return this.bots.containsKey(id);
	}
	
	@Override
	public Map<Integer, IBotData> getBots() {
		return this.bots;
	}
	
	@Override
	public void clearBots() {
		this.bots.clear();
		
		this.getPlayer().flush(this);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		if (this.bots != null) {
			this.bots.clear();
			this.bots = null;
		}
	}
	
}
