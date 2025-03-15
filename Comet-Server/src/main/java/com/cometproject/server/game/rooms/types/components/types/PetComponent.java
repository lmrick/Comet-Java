package com.cometproject.server.game.rooms.types.components.types;

import com.cometproject.api.game.pets.IPetData;
import com.cometproject.api.game.rooms.components.RoomComponentContext;
import com.cometproject.api.game.rooms.components.types.IPetComponent;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.pets.PetManager;
import com.cometproject.server.game.rooms.objects.entities.types.PetEntity;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.components.RoomComponent;
import com.cometproject.server.storage.queries.pets.RoomPetDao;

public class PetComponent extends RoomComponent implements IPetComponent {
	private final Room room;
	
	public PetComponent(RoomComponentContext roomComponentContext) {
		super(roomComponentContext);
		
		this.room = (Room) roomComponentContext.getRoom();
		
		this.load();
	}
	
	@Override
	public RoomComponentContext getRoomComponentContext() {
		return super.getRoomComponentContext();
	}
	
	public void load() {
		(this.room.getCachedData() != null ? this.room.getCachedData().getPets() : RoomPetDao.getPetsByRoomId(this.room.getId())).forEach(this::loadPet);
	}
	
	private void loadPet(IPetData petData) {
		if (PetManager.getInstance().validatePetName(petData.getName()) > 0) {
			return;
		}
		
		PetEntity petEntity = new PetEntity(petData, room.getEntities().getFreeId(), petData.getRoomPosition(), 3, 3, room);
		this.getRoom().getEntities().addEntity(petEntity);
	}
	
	public PetEntity addPet(IPetData pet, Position position) {
		RoomPetDao.updatePet(this.room.getId(), position.getX(), position.getY(), pet.getId());
		
		int virtualId = room.getEntities().getFreeId();
		PetEntity petEntity = new PetEntity(pet, virtualId, position, 3, 3, room);
		this.getRoom().getEntities().addEntity(petEntity);
		
		return petEntity;
	}
	
	public Room getRoom() {
		return this.room;
	}
	
}
