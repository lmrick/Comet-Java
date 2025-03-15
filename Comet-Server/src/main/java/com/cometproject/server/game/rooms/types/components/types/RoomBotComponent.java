package com.cometproject.server.game.rooms.types.components.types;

import com.cometproject.api.game.bots.IBotData;
import com.cometproject.api.game.rooms.components.RoomComponentContext;
import com.cometproject.api.game.rooms.components.types.IRoomBotComponent;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.bots.BotData;
import com.cometproject.server.game.rooms.objects.entities.types.BotEntity;
import com.cometproject.server.game.rooms.objects.entities.types.data.PlayerBotData;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.components.RoomComponent;
import com.cometproject.server.storage.queries.bots.RoomBotDao;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;

public class RoomBotComponent extends RoomComponent implements IRoomBotComponent {
    private final Room room;
    private final Map<String, Integer> botNameToId;

    public RoomBotComponent(RoomComponentContext roomComponentContext) {
        super(roomComponentContext);
        this.room = (Room) roomComponentContext.getRoom();
        this.botNameToId = Maps.newHashMap();

        this.load();
    }
    
    @Override
    public RoomComponentContext getRoomComponentContext() {
        return super.getRoomComponentContext();
    }
    
    @Override
    public void dispose() {
        this.botNameToId.clear();
    }

    public void load() {
        try {
            List<IBotData> botData = this.room.getCachedData() != null ? this.room.getCachedData().getBots() : RoomBotDao.getBotsByRoomId(this.room.getId());
					
					botData.forEach(data -> {
						if (this.botNameToId.containsKey(data.getUsername())) {
							data.setUsername(this.getAvailableName(data.getUsername()));
						}
						BotEntity botEntity = new BotEntity(data, room.getEntities().getFreeId(), ((PlayerBotData) data).getPosition(), 2, 2, room);
						this.botNameToId.put(botEntity.getUsername(), botEntity.getBotId());
						botEntity.getPosition().setZ(this.getRoom().getMapping().getStepHeight(botEntity.getPosition()));
						this.getRoom().getEntities().addEntity(botEntity);
						this.getRoom().getItems().getItemsOnSquare(((PlayerBotData) data).getPosition().getX(), ((PlayerBotData) data).getPosition().getY()).forEach(roomItemFloor -> roomItemFloor.onEntityStepOn(botEntity));
					});
        } catch (Exception e) {
            room.log.error("Error while deploying bots", e);
        }
    }

    public String getAvailableName(String name) {
        int usedCount = (int) this.botNameToId.keySet().stream().filter(name::startsWith).count();
			
			if (usedCount == 0) return name;

        return name + usedCount;
    }

    public BotEntity addBot(IBotData bot, int x, int y, double height) {
        int virtualId = room.getEntities().getFreeId();
        String name = this.botNameToId.containsKey(bot.getUsername()) ? this.getAvailableName(bot.getUsername()) : bot.getUsername();
			
			this.botNameToId.put(bot.getUsername(), bot.getId());

        BotData botData = new PlayerBotData(bot.getId(), name, bot.getMotto(), bot.getFigure(), bot.getGender(), bot.getOwnerName(), bot.getOwnerId(), "[]", true, 7, bot.getBotType(), bot.getMode(), null);
        BotEntity botEntity = new BotEntity(botData, virtualId, new Position(x, y, height), 1, 1, room);

        if (botEntity.getPosition().getZ() < this.getRoom().getModel().getSquareHeight()[x][y]) {
            botEntity.getPosition().setZ(this.getRoom().getModel().getSquareHeight()[x][y]);
        }

        this.getRoom().getEntities().addEntity(botEntity);
        return botEntity;
    }

    public BotEntity getBotByName(String name) {
        if (this.botNameToId.containsKey(name)) {
            return this.getRoom().getEntities().getEntityByBotId(this.botNameToId.get(name));
        }

        return null;
    }

    public Room getRoom() {
        return this.room;
    }

    public void changeBotName(String currentName, String newName) {
        if (!this.botNameToId.containsKey(currentName)) return;

        int botId = this.botNameToId.get(currentName);

        this.botNameToId.remove(currentName);
        this.botNameToId.put(newName, botId);
    }

    public void removeBot(String name) {
        this.botNameToId.remove(name);
    }
}
