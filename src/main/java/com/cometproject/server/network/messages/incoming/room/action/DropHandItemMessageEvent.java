package com.cometproject.server.network.messages.incoming.room.action;

import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.types.MessageEvent;
import com.cometproject.server.network.sessions.Session;


public class DropHandItemMessageEvent implements Event {
    @Override
    public void handle(Session client, MessageEvent msg) throws Exception {
        if(client.getPlayer() == null || client.getPlayer().getEntity() == null) return;

        client.getPlayer().getEntity().carryItem(0);
    }
}
