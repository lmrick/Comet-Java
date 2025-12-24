package com.cometproject.server.game.rooms.types.mapping;

public record RoomTileStatus(RoomTileStatusType statusType, int effectId, int positionX, int positionY, int rotation,
														 double interactionHeight) {
	
}
