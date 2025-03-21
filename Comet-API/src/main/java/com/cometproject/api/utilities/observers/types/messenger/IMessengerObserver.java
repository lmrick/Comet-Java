package com.cometproject.api.utilities.observers.types.messenger;

import com.cometproject.api.game.players.data.components.messenger.IMessengerFriend;
import com.cometproject.api.utilities.observers.IObserver;

public interface IMessengerObserver extends IObserver {
    
    void onFriendAdded(IMessengerFriend friend);
    void onFriendRemoved(int friendId);
    void onStatusUpdated(boolean online, boolean inRoom);

    default void flush(Object... arguments) { }

}

