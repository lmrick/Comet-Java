package com.cometproject.server.game.rooms.objects.entities.types.data.types;

import com.cometproject.server.game.rooms.objects.entities.types.data.IBotDataObject;
import java.util.List;

public record SpyBotData(List<String> visitors) implements IBotDataObject {

}
