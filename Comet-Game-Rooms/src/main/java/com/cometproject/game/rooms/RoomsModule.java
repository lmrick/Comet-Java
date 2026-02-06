package com.cometproject.game.rooms;

import com.cometproject.api.modules.ModuleConfig;
import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.rooms.IRoomData;
import com.cometproject.api.game.rooms.IRoomService;
import com.cometproject.api.game.rooms.IRoomWriter;
import com.cometproject.api.game.rooms.models.IRoomModelFactory;
import com.cometproject.api.game.rooms.models.IRoomModelService;
import com.cometproject.api.modules.BaseModule;
import com.cometproject.api.server.IGameService;
import com.cometproject.api.utilities.caching.Cache;
import com.cometproject.common.caching.LastReferenceCache;
import com.cometproject.game.rooms.factories.RoomModelFactory;
import com.cometproject.game.rooms.services.RoomModelService;
import com.cometproject.game.rooms.services.RoomService;
import com.cometproject.game.rooms.services.RoomWriterService;
import com.cometproject.storage.api.StorageContext;

public class RoomsModule extends BaseModule {
	
	private IRoomModelService roomModelService;
	private IRoomService roomService;
	private IRoomWriter roomWriterService;
	
	public RoomsModule(ModuleConfig config, IGameService gameService) {
		super(config, gameService);
	}
	
	@Override
	public void setup() {
		final IRoomModelFactory roomModelFactory = new RoomModelFactory();
		final Cache<Integer, IRoomData> roomDataCache = new LastReferenceCache<>(60000, (86400 * 1000), null, this.getGameService().getExecutorService());
		
		this.roomService = new RoomService(StorageContext.getCurrentContext().getRoomRepository(), roomDataCache);
		this.roomModelService = new RoomModelService(roomModelFactory, StorageContext.getCurrentContext().getRoomRepository());
		this.roomWriterService = new RoomWriterService();
	}
	
	@Override
	public void initializeServices(GameContext gameContext) {
		this.roomModelService.loadModels();
		
		gameContext.setRoomService(this.roomService);
		gameContext.setRoomModelService(this.roomModelService);
		gameContext.setService(IRoomWriter.class, this.roomWriterService);
	}
	
}
