package com.cometproject.server.logging.entries;

import com.cometproject.server.boot.Comet;
import com.cometproject.server.logging.AbstractLogEntry;
import com.cometproject.server.logging.LogEntryType;

import java.text.MessageFormat;

public class MessengerChatLogEntry extends AbstractLogEntry {
    private final int senderId;
    private final int receiverId;
    private final String message;
    private final long timestamp;

    public MessengerChatLogEntry(int senderId, int receiverId, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = Comet.getTime();
    }

    @Override
    public LogEntryType getType() {
        return LogEntryType.MESSENGER_CHATLOG;
    }

    @Override
    public String getString() {
        return MessageFormat.format("To: {0}, Message: {1}", this.receiverId, this.message);
    }

    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public int getPlayerId() {
        return this.senderId;
    }
}
