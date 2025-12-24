package com.cometproject.api.utilities.observers.types.messenger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.cometproject.api.game.players.data.components.messenger.IMessengerFriend;
import com.cometproject.api.utilities.observers.IObserver;
import com.cometproject.api.utilities.observers.IObserverService;

public class MessengerObserverService implements IObserverService, IMessengerObserver {
    private final List<IMessengerObserver> observers = new CopyOnWriteArrayList<>();

    public MessengerObserverService() {}

    @Override
    public void notifyObservers(Object... arguments) {
        observers.forEach(observer -> observer.flush(arguments));
    }

    public void flush(Object... arguments) {

    }

    @Override
    public void onFriendAdded(IMessengerFriend friend) {
        observers.forEach(observer -> observer.onFriendAdded(friend));
    }

    @Override
    public void onFriendRemoved(int friendId) {
        observers.forEach(observer -> observer.onFriendRemoved(friendId));
    }

    @Override
    public void onStatusUpdated(boolean online, boolean inRoom) {
        observers.forEach(observer -> observer.onStatusUpdated(online, inRoom));
    }

    @Override
    public void addObserver(IObserver observer) {
        observers.add((IMessengerObserver) observer);
    }

    @Override
    public void removeObserver(IObserver observer) {
        observers.remove((IMessengerObserver) observer);
    }

    @Override
    public void dispose() {
        observers.clear();
    }
    
}
