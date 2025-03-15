package com.cometproject.server.game.players.components.types;

import com.cometproject.api.game.pets.IPetData;
import com.cometproject.api.game.players.components.PlayerComponentContext;
import com.cometproject.api.game.players.data.components.IPlayerPets;
import com.cometproject.server.game.players.components.PlayerComponent;
import com.cometproject.server.storage.queries.pets.PetDao;
import java.util.Map;

import org.checkerframework.checker.units.qual.s;

public class PetComponent extends PlayerComponent implements IPlayerPets {
	private Map<Integer, IPetData> pets;
	
	public PetComponent(PlayerComponentContext componentContext) {
		super(componentContext);
		
		this.pets = PetDao.getPetsByPlayerId(componentContext.getPlayer().getId());
	}
	
	@Override
	public IPetData getPet(int id) {
		if (this.getPets().containsKey(id)) {
			return this.getPets().get(id);
		}
		
		return null;
	}
	
	@Override
	public void clearPets() {
		this.pets.clear();
		
		this.getPlayer().flush(this);
		this.getPlayer().getPlayerObserver().notifyObservers(this);
	}
	
	@Override
	public void addPet(IPetData petData) {
		this.pets.put(petData.getId(), petData);
		
		this.getPlayer().flush(this);
		this.getPlayer().getPlayerObserver().notifyObservers(this);
	}
	
	@Override
	public void removePet(int id) {
		this.pets.remove(id);
		
		this.getPlayer().flush(this);
		this.getPlayer().getPlayerObserver().notifyObservers(this);
	}
	
	@Override
	public void dispose() {
		super.dispose();

		this.pets.clear();
		this.pets = null;
	}
	
	@Override
	public Map<Integer, IPetData> getPets() {
		return this.pets;
	}
	
}
