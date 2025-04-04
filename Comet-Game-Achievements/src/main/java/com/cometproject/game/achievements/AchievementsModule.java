package com.cometproject.game.achievements;

import com.cometproject.api.modules.BaseModule;
import com.cometproject.api.modules.ModuleConfig;
import com.cometproject.api.server.IGameService;
import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.achievements.IAchievementsService;
import com.cometproject.game.achievements.services.AchievementService;
import com.cometproject.storage.api.StorageContext;
import com.cometproject.storage.api.repositories.IAchievementRepository;

public class AchievementsModule extends BaseModule {
    private AchievementService achievementService;
    
    public AchievementsModule(ModuleConfig config, IGameService gameService) {
        super(config, gameService);
    }

    @Override
    public void setup() {
        this.achievementService = new AchievementService(StorageContext.getCurrentContext().getRepository(IAchievementRepository.class));
    }

    @Override
    public void initializeServices(GameContext gameContext) {
        this.achievementService.initialize();
        GameContext.getCurrent().setService(IAchievementsService.class, this.achievementService);
    }

}
