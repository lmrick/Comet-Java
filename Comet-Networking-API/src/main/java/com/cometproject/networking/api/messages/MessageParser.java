package com.cometproject.networking.api.messages;

import com.cometproject.api.networking.messages.wrappers.IEventDataWrapper;

public abstract class MessageParser {
    private boolean hasError = false;

    public boolean hasError() {
        return this.hasError;
    }

    public void setHasError(boolean error) {
        this.hasError = error;
    }

    public abstract void parse(IEventDataWrapper event);
    public void flush() { }
    
}
