package com.cometproject.server.modules;

import com.cometproject.api.networking.sessions.ISessionService;
import com.cometproject.api.server.IGameService;
import com.cometproject.api.utilities.events.IEventHandler;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.tasks.CometThreadManager;
import java.util.concurrent.ScheduledExecutorService;

public record CometGameService(IEventHandler eventHandler) implements IGameService {
    
    @Override
    public ScheduledExecutorService getExecutorService() {
        return CometThreadManager.getInstance().getCoreExecutor();
    }
    
    @Override
    public ISessionService getSessionService() {
        return NetworkManager.getInstance().getSessions();
    }
    
}
