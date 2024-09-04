package com.cometproject.api.game.rooms;

import com.cometproject.api.game.rooms.components.IRightsComponent;

public interface IRoom {
	
	IRoomData getData();
	
	IRightsComponent getRights();
	
	
}
