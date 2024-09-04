package com.cometproject.api.game.players.data.components;

import com.cometproject.api.game.players.components.IPlayerComponent;
import com.cometproject.api.game.players.data.components.permissions.IPlayerRank;

public interface IPlayerPermissions extends IPlayerComponent {
    
    IPlayerRank getRank();
    boolean hasCommand(String key);
    
}
