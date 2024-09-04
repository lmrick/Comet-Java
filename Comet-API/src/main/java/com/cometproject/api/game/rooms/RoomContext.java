package com.cometproject.api.game.rooms;

public class RoomContext {
	private static RoomContext currentContext;
	private final IRoom room;
	
	public RoomContext(IRoom room) {
		this.room = room;
	}
	
	public static RoomContext getCurrentContext() {
		return currentContext;
	}
	
	public static void setCurrentContext(RoomContext context) {
		RoomContext.currentContext = context;
	}
	
	public IRoom getRoom() {
		return room;
	}
	
}
