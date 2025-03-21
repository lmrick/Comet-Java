package com.cometproject.api.game.furniture;

import com.cometproject.api.game.furniture.types.CrackableReward;
import com.cometproject.api.game.furniture.types.IFurnitureDefinition;
import com.cometproject.api.game.furniture.types.IMusicData;
import com.cometproject.api.utilities.process.Initializable;

import org.apache.log4j.Logger;

import java.util.Map;

public interface IFurnitureService extends Initializable {
    void loadItemDefinitions();

    void loadMusicData();

    int getItemVirtualId(long itemId);

    void disposeItemVirtualId(long itemId);

    Long getItemIdByVirtualId(int virtualId);

    long getTeleportPartner(long itemId);

    int roomIdByItemId(long itemId);

    IFurnitureDefinition getDefinition(int itemId);

    IMusicData getMusicData(int songId);

    IMusicData getMusicDataByName(String name);

    Map<Long, Integer> getItemIdToVirtualIds();

    IFurnitureDefinition getBySpriteId(int spriteId);

    Logger getLogger();

    Map<Integer, IFurnitureDefinition> getItemDefinitions();

    Map<Integer, CrackableReward> getCrackableRewards();

    Integer getSaddleId();
}
