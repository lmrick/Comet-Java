package com.cometproject.server.game.rooms.objects;

import com.cometproject.api.game.utilities.Position;
import com.cometproject.api.game.utilities.IPositionable;
import com.cometproject.server.game.rooms.types.Room;

public abstract class RoomFloorObject extends RoomObject implements IPositionable {
	
	private final int id;
	
	public RoomFloorObject(int id, Position position, Room room) {
		super(position, room);
		
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
}
