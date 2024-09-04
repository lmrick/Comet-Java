package com.cometproject.api.server;

import com.cometproject.api.events.IEventHandler;
import com.cometproject.api.networking.sessions.ISessionService;
import java.util.concurrent.ScheduledExecutorService;

public interface IGameService {
    
    ISessionService getSessionService();
    IEventHandler eventHandler();
    ScheduledExecutorService getExecutorService();
    
}
