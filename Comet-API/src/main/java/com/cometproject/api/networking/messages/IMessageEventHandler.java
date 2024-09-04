package com.cometproject.api.networking.messages;

import com.cometproject.api.networking.messages.wrappers.IEventDataWrapper;

public interface IMessageEventHandler {
    
    void handle(IEventDataWrapper eventData) throws Exception;
    
}
