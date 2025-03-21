package com.cometproject.api.server;

import com.cometproject.api.networking.sessions.ISessionService;
import com.cometproject.api.utilities.events.IEventHandler;

import java.util.concurrent.ScheduledExecutorService;

public interface IGameService {
    
    ISessionService getSessionService();
    IEventHandler eventHandler();
    ScheduledExecutorService getExecutorService();
    
}
