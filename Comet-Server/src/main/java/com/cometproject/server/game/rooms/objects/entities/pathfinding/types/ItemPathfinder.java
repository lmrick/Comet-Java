package com.cometproject.server.game.rooms.objects.entities.pathfinding.types;

import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.rooms.objects.RoomObject;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.Pathfinder;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.validators.movement.items.ItemMovementRule;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.validators.movement.MovementContext;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.validators.movement.items.rules.*;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.validators.walk.TileContext;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.validators.walk.WalkRule;
import com.cometproject.server.game.rooms.types.mapping.RoomTile;
import com.cometproject.server.game.rooms.types.mapping.chunks.RoomChunk;

import java.util.List;

public class ItemPathfinder extends Pathfinder {
	private final List<WalkRule> walkRules;
	
	private final List<ItemMovementRule> rules = List.of(new ValidTileRule(), new DiagonalMovementRule(), new FootballGateRule(), new FootballHeightRule(), new WiredChaseRule(), new RollableRule(), new MovementNodeRule(), new StepHeightRule());
	
	private static ItemPathfinder pathfinderInstance;
	
	public ItemPathfinder(List<WalkRule> rules) {
		this.walkRules = rules;
	}
	
	public static ItemPathfinder getInstance() {
		if (pathfinderInstance == null) pathfinderInstance = new ItemPathfinder();
		return pathfinderInstance;
	}
	
	@Override
	public boolean isValidStep(RoomObject object, Position from, Position to, boolean lastStep, boolean isRetry) {
		var tile = object.getRoom().getMapping().getTile(to);
		var ctx = new MovementContext(from, to, lastStep);
		return rules.stream().allMatch(rule -> rule.allows(object, tile, ctx));
	}
	
	public boolean canStep(RoomEntity entity, int x, int y) {
		RoomChunk chunk = entity.getRoom()
						.getMapping()
						.getChunkAtTile(x, y);
		
		if (chunk == null) return false;
		RoomTile tile = chunk.getTile(x, y);
		TileContext ctx = new TileContext(
						tile,
						chunk,
						chunk.getItems(),
						chunk.getEntities()
		);
		
		return walkRules.stream().allMatch(rule -> rule.canWalk(entity, ctx));
	}
	
}
