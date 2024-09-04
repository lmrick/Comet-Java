package com.cometproject.server.logging.containers;

import com.cometproject.server.logging.AbstractLogEntry;
import com.cometproject.server.logging.database.queries.LogQueries;
import com.cometproject.server.tasks.ICometTask;

public class LogEntryContainer {

    public void put(AbstractLogEntry logEntry) {
        LogQueries.putEntry(logEntry);
    }
    
}
