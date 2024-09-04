package com.cometproject.server.game.rooms.types.components;

import com.cometproject.api.game.rooms.IRoom;
import com.cometproject.api.game.rooms.components.IRoomComponent;
import com.cometproject.api.game.rooms.components.RoomComponentContext;

public abstract class RoomComponent implements IRoomComponent {
	private final RoomComponentContext roomComponentContext;
	
	public RoomComponent(RoomComponentContext roomComponentContext) {
		this.roomComponentContext = roomComponentContext;
	}
	
	@Override
	public RoomComponentContext getRoomComponentContext() {
		return roomComponentContext;
	}
	
	@Override
	public void dispose() {
	
	}
	
	public IRoom getRoom() {
		return this.roomComponentContext.getRoom();
	}
	
}
