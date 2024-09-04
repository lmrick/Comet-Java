package com.cometproject.server.game.rooms.objects.entities.types.data.types;

import com.cometproject.server.game.rooms.objects.entities.types.data.IBotDataObject;

import java.util.List;

public class SpyBotData implements IBotDataObject {
    private final List<String> visitors;

    public SpyBotData(List<String> visitors) {
        this.visitors = visitors;
    }

    public List<String> getVisitors() {
        return visitors;
    }
}
