package com.cometproject.api.utilities.observers.types.players;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.cometproject.api.utilities.observers.IObserver;
import com.cometproject.api.utilities.observers.IObserverService;

public class PlayerObserver implements IObserverService {
    private List<IObserver> observers = new CopyOnWriteArrayList<>();

    @Override
    public void notifyObservers(Object... arguments) {
        observers.forEach(observer -> observer.flush(arguments));
    }

    @Override
    public void addObserver(IObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(IObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void dispose() {
        observers.clear();
    }
    
}
