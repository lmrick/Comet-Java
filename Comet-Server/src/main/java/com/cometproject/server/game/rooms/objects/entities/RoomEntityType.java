package com.cometproject.server.game.rooms.objects.entities;

public enum RoomEntityType {
    PLAYER(1),
    BOT(4),
    PET(2),
    UNKNOWN(3);

    private final int typeId;

    RoomEntityType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return this.typeId;
    }
}
