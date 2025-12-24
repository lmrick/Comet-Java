package com.cometproject.api.game.players.components.types.messenger;

import com.cometproject.api.game.players.data.IPlayerAvatar;
import com.cometproject.api.networking.sessions.ISession;
import com.google.gson.JsonObject;

public interface IMessengerFriend {
    
    boolean isInRoom();
    IPlayerAvatar getAvatar();
    int getUserId();
    boolean isOnline();
    ISession getSession();
    JsonObject toJson();
    
}
