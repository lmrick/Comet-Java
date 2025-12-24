package com.cometproject.server.game.rooms.objects.entities.pathfinding.validators.movement;

import com.cometproject.api.game.utilities.Position;

public record MovementContext(
				Position from,
				Position to,
				boolean lastStep
) {}
