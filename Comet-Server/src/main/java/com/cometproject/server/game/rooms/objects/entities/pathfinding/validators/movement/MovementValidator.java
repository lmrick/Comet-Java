package com.cometproject.server.game.rooms.objects.entities.pathfinding.validators.movement;

import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;

public class MovementValidator {
	private static final int MAX_STEP = 1;
	
	public boolean isValid(RoomEntity entity, Position from, Position to) {
		if (Math.abs(from.getX() - to.getX()) > 1 ||
				Math.abs(from.getY() - to.getY()) > 1) {
			entity.setTeleportGoal(from);
			entity.incrementPreviousSteps();
		}
		
		if (entity.getPreviousSteps() > 5) {
			return false;
		}
		
		return entity.getRoom()
						.getPathfinder()
						.canStep(entity, to.getX(), to.getY());
	}
	
}
