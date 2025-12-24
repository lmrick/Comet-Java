package com.cometproject.server.logging;

import com.cometproject.api.config.Configuration;
import com.cometproject.api.utilities.process.Initializable;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class LogService implements Initializable {
    public static final boolean ENABLED = Configuration.currentConfig().get("comet.game.logging.enabled").equals("true");
    private static LogService logManagerInstance;
    private final Logger log = LogManager.getLogger(LogService.class.getName());
    private LogStore store;

    public LogService() {
    }

    public static LogService getInstance() {
        if (logManagerInstance == null) logManagerInstance = new LogService();
        return logManagerInstance;
    }

    @Override
    public void initialize() {
        this.store = new LogStore();
    }

    public LogStore getStore() {
        return store;
    }
}