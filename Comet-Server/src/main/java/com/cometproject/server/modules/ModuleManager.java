package com.cometproject.server.modules;

import com.cometproject.api.modules.ModuleConfig;
import com.cometproject.api.events.IEventHandler;
import com.cometproject.api.game.GameContext;
import com.cometproject.api.modules.BaseModule;
import com.cometproject.api.server.IGameService;
import com.cometproject.api.utilities.Initializable;
import com.cometproject.game.groups.GroupsModule;
import com.cometproject.game.rooms.RoomsModule;
import com.cometproject.server.modules.events.EventHandlerService;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleManager implements Initializable {
	
	private static final Logger log = Logger.getLogger(ModuleManager.class.getName());
	private static ModuleManager moduleManagerInstance;
	private final IEventHandler eventHandler;
	private final CometGameService gameService;
	private Map<String, BaseModule> modules;
	
	public ModuleManager() {
		this.eventHandler = new EventHandlerService();
		this.gameService = new CometGameService(this.eventHandler);
	}
	
	public static ModuleManager getInstance() {
		if (moduleManagerInstance == null) moduleManagerInstance = new ModuleManager();
		return moduleManagerInstance;
	}
	
	@Override
	public void initialize() {
		if (this.modules != null) {
			this.modules.clear();
		} else {
			this.modules = new ConcurrentHashMap<>();
		}
		
		ModuleManager.getInstance().getEventHandler().initialize();
		
		this.loadCoreModule(GroupsModule.class);
		this.loadCoreModule(RoomsModule.class);
	}
	
	private void loadCoreModule(Class<? extends BaseModule> moduleClass) {
		try {
			var constructor = moduleClass.getConstructor(ModuleConfig.class, IGameService.class);
			var cometModule = constructor.newInstance(null, this.gameService);
			cometModule.loadModule();
			this.modules.put(moduleClass.getSimpleName(), cometModule);
		} catch (Exception e) {
			log.error("Failed to load system module: " + moduleClass.getName(), e);
		}
	}
	
	public void setupModules() {
		this.modules.values().forEach(baseModule -> {
			baseModule.setup();
			baseModule.initialiseServices(GameContext.getCurrent());
		});
	}
	
	public IEventHandler getEventHandler() {
		return eventHandler;
	}
	
	private record ModulesConfig(List<CometModule> modules) {
	
	}
	
}
