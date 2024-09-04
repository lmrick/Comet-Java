package com.cometproject.api.game.players.data.components;

import com.cometproject.api.game.players.data.components.messenger.RelationshipLevel;
import java.util.Map;

public interface IPlayerRelationships {
    
    static int countByLevel(RelationshipLevel level, Map<Integer, RelationshipLevel> relationships) {
			return (int) relationships.values().stream().filter(relationship -> relationship == level).count();
    }

    RelationshipLevel get(int playerId);
    void remove(int playerId);
    int count();
    Map<Integer, RelationshipLevel> getRelationships();
    
}
