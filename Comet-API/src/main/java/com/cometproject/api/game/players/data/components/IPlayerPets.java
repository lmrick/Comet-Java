package com.cometproject.api.game.players.data.components;

import com.cometproject.api.game.pets.IPetData;
import com.cometproject.api.game.players.components.IPlayerComponent;

import java.util.Map;

public interface IPlayerPets extends IPlayerComponent {
    
    IPetData getPet(int id);
    void clearPets();
    void addPet(IPetData petData);
    void removePet(int id);
    Map<Integer, IPetData> getPets();

}
