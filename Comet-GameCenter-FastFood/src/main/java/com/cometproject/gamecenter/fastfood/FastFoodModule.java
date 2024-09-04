package com.cometproject.gamecenter.fastfood;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.modules.ModuleConfig;
import com.cometproject.api.game.GameContext;
import com.cometproject.api.modules.BaseModule;
import com.cometproject.api.server.IGameService;
import com.cometproject.gamecenter.fastfood.net.FastFoodMessageHandler;
import com.cometproject.gamecenter.fastfood.net.SessionFactory;
import com.cometproject.gamecenter.fastfood.storage.MySQLFastFoodRepository;
import com.cometproject.networking.api.INetworkingServer;
import com.cometproject.networking.api.NetworkingContext;
import com.cometproject.networking.api.config.NetworkingServerConfig;
import com.cometproject.storage.mysql.MySQLStorageContext;
import com.google.common.collect.Sets;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class FastFoodModule extends BaseModule {
	private MySQLFastFoodRepository fastFoodRepository;
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(CometSettings.fastFoodGameThreads);
	
	public FastFoodModule(ModuleConfig config, IGameService gameService) {
		super(config, gameService);
	}
	
	@Override
	public void setup() {
		this.fastFoodRepository = new MySQLFastFoodRepository(MySQLStorageContext.getCurrentContext().getConnectionProvider());
	}
	
	@Override
	public void initialiseServices(GameContext gameContext) {
		INetworkingServer fastFoodServer = NetworkingContext.getCurrentContext().serverFactory()
						.createServer(new NetworkingServerConfig(CometSettings.fastFoodGameHost, Sets.newHashSet(CometSettings.fastFoodGamePort)),
													new SessionFactory(
													new FastFoodMessageHandler(this.executorService, gameContext.getPlayerService(),
													this.fastFoodRepository)));
		
		fastFoodServer.start();
	}
	
}
