package com.cometproject.server.modules;

import com.cometproject.api.events.IEventHandler;
import com.cometproject.api.networking.sessions.ISessionManager;
import com.cometproject.api.server.IGameService;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.tasks.CometThreadManager;

import java.util.concurrent.ScheduledExecutorService;

public record CometGameService(IEventHandler eventHandler) implements IGameService {
    
    @Override
    public ScheduledExecutorService getExecutorService() {
        return CometThreadManager.getInstance().getCoreExecutor();
    }
    
    @Override
    public ISessionManager getSessionManager() {
        return NetworkManager.getInstance().getSessions();
    }
    
}
