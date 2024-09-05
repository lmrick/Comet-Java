package com.cometproject.server.network.messages.incoming.room.trading;

import com.cometproject.server.game.rooms.types.components.types.trade.Trade;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;


public class UnacceptTradeMessageEvent implements Event {
    @Override
    public void handle(Session client, MessageEvent msg) throws Exception {
        if (client.getPlayer().getEntity() == null || client.getPlayer().getEntity().getRoom() == null) return;

        Trade trade = client.getPlayer().getEntity().getRoom().getTrade().get(client.getPlayer().getEntity());

        if (trade == null) return;

        trade.unAccept(trade.getPlayerIndex(client.getPlayer().getEntity()));
    }
}
