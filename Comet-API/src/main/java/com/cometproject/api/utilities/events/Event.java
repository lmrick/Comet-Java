package com.cometproject.api.utilities.events;

import java.util.function.Consumer;

public abstract class Event<T extends EventArgs> {
    private final Consumer<T> consumer;
    
    public Event(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    public void consume(T args) {
        this.consumer.accept(args);
    }

    public boolean isAsync() {
        return false;
    }
}
