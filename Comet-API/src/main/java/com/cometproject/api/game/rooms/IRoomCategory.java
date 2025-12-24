package com.cometproject.api.game.rooms;

public interface IRoomCategory {

    int id();
    String category();
    String categoryId();
    String publicName();
    boolean canDoActions();
    int colour();

}
