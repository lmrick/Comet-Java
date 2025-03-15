package com.cometproject.api.game.rooms.entities;

import com.cometproject.api.game.players.IPlayer;
import com.cometproject.api.game.rooms.IRoom;

public interface IPlayerRoomEntity extends IRoomEntity {
    
    IPlayer getPlayer();
    IRoom getRoom();
    
}
