package com.cometproject.server.game.players.components.types;

import com.cometproject.api.game.players.components.PlayerComponentContext;
import com.cometproject.api.game.players.data.components.IPlayerRelationships;
import com.cometproject.api.game.players.data.components.messenger.RelationshipLevel;
import com.cometproject.server.game.players.components.PlayerComponent;
import com.cometproject.server.storage.queries.player.relationships.RelationshipDao;

import java.util.Map;

public class RelationshipComponent extends PlayerComponent implements IPlayerRelationships {
	
	private Map<Integer, RelationshipLevel> relationships;
	
	public RelationshipComponent(PlayerComponentContext componentContext) {
		super(componentContext);
		
		this.relationships = RelationshipDao.getRelationshipsByPlayerId(componentContext.getPlayer().getId());
	}
	
	@Override
	public void dispose() {
		this.relationships.clear();
		this.relationships = null;
	}
	
	@Override
	public RelationshipLevel get(int playerId) {
		return this.relationships.get(playerId);
	}
	
	@Override
	public void remove(int playerId) {
		this.getRelationships().remove(playerId);
		
		this.getPlayer().flush();
	}
	
	@Override
	public int count() {
		return this.relationships.size();
	}
	
	@Override
	public Map<Integer, RelationshipLevel> getRelationships() {
		return this.relationships;
	}
	
}
