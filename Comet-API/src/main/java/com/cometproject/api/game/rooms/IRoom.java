package com.cometproject.api.game.rooms;

import com.cometproject.server.game.rooms.types.components.IRightsComponent;

public interface IRoom {
	
	IRoomData getData();
	
	IRightsComponent getRights();
	
}
