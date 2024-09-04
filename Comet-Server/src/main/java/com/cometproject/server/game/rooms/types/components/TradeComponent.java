package com.cometproject.server.game.rooms.types.components;

import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.components.types.Trade;

import java.util.ArrayList;
import java.util.List;


public class TradeComponent {
    private final Room room;
    private final List<Trade> trades;

    public TradeComponent(Room room) {
        this.room = room;
        this.trades = new ArrayList<>();
    }

    public void add(Trade trade) {
        trade.setTradeComponent(this);

        this.trades.add(trade);
    }

    public Trade get(PlayerEntity client) {
			return this.getTrades().stream().filter(trade -> trade.getUser1() == client || trade.getUser2() == client).findFirst().orElse(null);
			
		}

    public void remove(Trade trade) {
        this.trades.remove(trade);
    }

    public synchronized List<Trade> getTrades() {
        return this.trades;
    }

    public Room getRoom() {
        return room;
    }
}
