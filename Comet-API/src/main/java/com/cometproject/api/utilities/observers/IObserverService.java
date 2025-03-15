package com.cometproject.api.utilities.observers;

public interface IObserverService {

    void notifyObservers(Object... arguments);
    void addObserver(IObserver observer);
    void removeObserver(IObserver observer);
    void dispose();

}
