package com.cometsrv.game.rooms.types.components;

import com.cometsrv.game.rooms.types.Room;
import com.cometsrv.game.rooms.types.components.types.Trade;
import com.cometsrv.network.sessions.Session;
import javolution.util.FastList;

import java.util.List;

public class TradeComponent {
    private Room room;

    private List<Trade> trades;

    public TradeComponent(Room room) {
        this.room = room;

        this.trades = new FastList<>();
    }

    public void dispose() {
        for(Trade trade : trades) {
            if(trade != null)
                trade.dispose();
        }

        this.trades.clear();
        this.trades = null;
        this.room = null;
    }

    public void add(Trade trade) {
        this.trades.add(trade);
    }

    public Trade get(Session client) {
        for(Trade trade : this.getTrades()) {
            if(trade.getUser1() == client || trade.getUser2() == client)
                return trade;
        }

        return null;
    }

    public void remove(Trade trade) {
        this.trades.remove(trade);
    }

    public synchronized List<Trade> getTrades() {
        return this.trades;
    }
}
