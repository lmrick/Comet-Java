package com.cometproject.server.game.rooms.objects.entities.pathfinding.validators.movement.items.rules;

import com.cometproject.server.game.rooms.objects.RoomObject;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.validators.movement.items.ItemMovementRule;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.validators.movement.MovementContext;
import com.cometproject.server.game.rooms.types.mapping.RoomEntityMovementNode;
import com.cometproject.server.game.rooms.types.mapping.RoomTile;

public class MovementNodeRule implements ItemMovementRule {
	
	@Override
	public boolean allows(RoomObject mover, RoomTile tile, MovementContext ctx) {
		if (tile.getMovementNode() == RoomEntityMovementNode.CLOSED) {
			return false;
		}
		
		return tile.getMovementNode() != RoomEntityMovementNode.END_OF_ROUTE || ctx.lastStep();
	}
	
}
