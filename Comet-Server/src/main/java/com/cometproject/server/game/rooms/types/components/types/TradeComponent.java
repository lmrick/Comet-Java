package com.cometproject.server.game.rooms.types.components.types;

import com.cometproject.api.game.rooms.components.RoomComponentContext;
import com.cometproject.api.game.rooms.components.types.ITradeComponent;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.components.RoomComponent;
import com.cometproject.server.game.rooms.types.components.types.trade.Trade;

import java.util.ArrayList;
import java.util.List;


public class TradeComponent extends RoomComponent implements ITradeComponent {
    private final RoomComponentContext roomComponentContext;
    private final Room room;
    private final List<Trade> trades;

    public TradeComponent(RoomComponentContext roomComponentContext) {
        super(roomComponentContext);
        this.room = (Room) roomComponentContext.getRoom();
        this.roomComponentContext = roomComponentContext;
        this.trades = new ArrayList<>();
    }
    
    @Override
    public RoomComponentContext getRoomComponentContext() {
        return roomComponentContext;
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
