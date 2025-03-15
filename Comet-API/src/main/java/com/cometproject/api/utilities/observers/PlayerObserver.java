package com.cometproject.api.utilities.observers;

import java.util.ArrayList;
import java.util.List;

public class PlayerObserver implements IObserverService {
    private List<IObserver> observers = new ArrayList<>();

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
