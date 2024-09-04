package com.cometproject.api.modules;

import com.cometproject.api.events.Event;
import com.cometproject.api.events.IEventListenerContainer;
import com.cometproject.api.game.GameContext;
import com.cometproject.api.networking.messages.IMessageEventHandler;
import com.cometproject.api.networking.sessions.ISession;
import com.cometproject.api.server.IGameService;

import java.util.UUID;
import java.util.function.BiConsumer;

public abstract class BaseModule implements IEventListenerContainer {
    private final ModuleConfig config;
    private final UUID moduleId;
    private final IGameService gameService;

    public BaseModule(ModuleConfig config, IGameService gameService) {
        this.moduleId = UUID.randomUUID();
        this.gameService = gameService;
        this.config = config;
    }

    protected void registerEvent(Event<?> event) {
        this.getGameService().eventHandler().registerEvent(event);
    }

    public void registerMessage(IMessageEventHandler messageEventHandler) {

    }

    protected void registerChatCommand(String commandExecutor, BiConsumer<ISession, String[]> consumer) {
        this.getGameService().eventHandler().registerChatCommand(commandExecutor, consumer);
    }

    public void setup() {

    }

    public void initialiseServices(GameContext gameContext) {

    }

    public void loadModule() {
        if(this.getConfig() != null && this.getConfig().commands() != null) {
					this.getConfig().commands().forEach((key, value) -> this.getGameService().eventHandler().registerCommandInfo(key, value));
        }
    }

    public UUID getModuleId() {
        return moduleId;
    }

    public IGameService getGameService() {
        return this.gameService;
    }

    public ModuleConfig getConfig() {
        return config;
    }

}
