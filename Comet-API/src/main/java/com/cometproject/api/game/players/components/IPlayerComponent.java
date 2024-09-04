package com.cometproject.api.game.players.components;

public interface IPlayerComponent {
    
    PlayerComponentContext getComponentContext();
    void dispose();
    
}
