package com.cometproject.api.game.players.data.components.inventory;

import com.cometproject.api.game.furniture.types.IFurnitureDefinition;
import com.cometproject.api.game.furniture.types.ILimitedEditionItem;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;

public interface IPlayerItem {
    
    long getId();
    IFurnitureDefinition getDefinition();
    int getBaseId();
    String getExtraData();
    ILimitedEditionItem getLimitedEditionItem();
    int getVirtualId();
    void compose(IComposerDataWrapper message);
    IPlayerItemSnapshot createSnapshot();
    
}

