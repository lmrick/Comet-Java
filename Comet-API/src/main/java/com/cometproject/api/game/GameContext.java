package com.cometproject.api.game;

import com.cometproject.api.game.catalog.ICatalogService;
import com.cometproject.api.game.furniture.IFurnitureService;
import com.cometproject.api.game.groups.IGroupService;
import com.cometproject.api.game.players.IPlayerService;
import com.cometproject.api.game.rooms.IRoomService;
import com.cometproject.api.game.rooms.models.IRoomModelService;
import org.apache.log4j.Logger;

public class GameContext {
	
	private static GameContext gameContext;
	
	private ICatalogService catalogService;
	private IFurnitureService furnitureService;
	private IGroupService groupService;
	private IPlayerService playerService;
	private IRoomService roomService;
	private IRoomModelService roomModelService;
	
	private final Logger logger = Logger.getLogger(GameContext.class);
	
	public ICatalogService getCatalogService() {
		return catalogService;
	}
	
	public void setCatalogService(ICatalogService catalogService) {
		this.catalogService = catalogService;
	}
	
	public IFurnitureService getFurnitureService() {
		return this.furnitureService;
	}
	
	public void setFurnitureService(IFurnitureService furnitureService) {
		this.furnitureService = furnitureService;
	}
	
	public IGroupService getGroupService() {
		return this.groupService;
	}
	
	public void setGroupService(IGroupService groupService) {
		this.groupService = groupService;
	}
	
	public IPlayerService getPlayerService() {
		return this.playerService;
	}
	
	public void setPlayerService(IPlayerService playerService) {
		this.playerService = playerService;
	}
	
	public static GameContext getCurrent() {
		if (gameContext == null) {
			System.out.println("Game Context not configured!");
			System.exit(0);
		}
		
		return gameContext;
	}
	
	public static void setCurrent(GameContext instance) {
		GameContext.gameContext = instance;
	}
	
	public IRoomService getRoomService() {
		return roomService;
	}
	
	public void setRoomService(IRoomService roomService) {
		this.roomService = roomService;
	}
	
	public IRoomModelService getRoomModelService() {
		return roomModelService;
	}
	
	public void setRoomModelService(IRoomModelService roomModelService) {
		this.roomModelService = roomModelService;
	}
	
}
