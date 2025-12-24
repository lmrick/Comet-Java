package com.cometproject.server.game.rooms.objects.entities.pathfinding.validators.movement;

import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class MovementController {
	
	private final Queue<Position> steps = new ArrayDeque<>();
	
	public void enqueue(List<Position> path) {
		steps.addAll(path);
	}
	
	public void tick(RoomEntity entity) {
		Position next = steps.poll();
		if (next != null) {
			entity.moveTo(next);
		}
	}
	
}
