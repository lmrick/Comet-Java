package com.cometproject.server.modules;

import com.cometproject.api.events.EventHandler;
import com.cometproject.api.networking.sessions.ISessionManager;
import com.cometproject.api.server.IGameService;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.tasks.CometThreadManager;

import java.util.concurrent.ScheduledExecutorService;

public class CometGameService implements IGameService {
    private EventHandler eventHandler;

    public CometGameService(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public EventHandler getEventHandler() {
        return this.eventHandler;
    }

    public ScheduledExecutorService getExecutorService() {
        return CometThreadManager.getInstance().getCoreExecutor();
    }

    @Override
    public ISessionManager getSessionManager() {
        return NetworkManager.getInstance().getSessions();
    }
}
