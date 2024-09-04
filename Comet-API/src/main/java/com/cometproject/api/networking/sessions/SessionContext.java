package com.cometproject.api.networking.sessions;

public class SessionContext {
    private static SessionContext instance;
    private ISessionService sessionService;

    public ISessionService getSessionManager() {
        return sessionService;
    }

    public void setSessionService(ISessionService sessionService) {
        this.sessionService = sessionService;
    }

    public static SessionContext getInstance() {
        if (instance == null) instance = new SessionContext();
        return instance;
    }
    
}
