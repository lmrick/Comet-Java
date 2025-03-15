package com.cometproject.api.game.rooms;

public interface IRoomCategory {

    int getId();
    String getCategory();
    String getCategoryId();
    String getPublicName();
    boolean canDoActions();
    int getColour();

}
