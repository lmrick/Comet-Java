package com.cometproject.api.server;

import com.cometproject.api.events.IEventHandler;
import com.cometproject.api.networking.sessions.ISessionManager;
import java.util.concurrent.ScheduledExecutorService;

public interface IGameService {
    
    ISessionManager getSessionManager();
    IEventHandler eventHandler();
    ScheduledExecutorService getExecutorService();
    
}
