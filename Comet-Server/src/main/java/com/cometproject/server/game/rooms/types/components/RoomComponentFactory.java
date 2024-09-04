package com.cometproject.server.game.rooms.types.components;

import com.cometproject.api.game.rooms.components.IRoomComponent;
import com.cometproject.api.game.rooms.components.IRoomComponentFactory;
import com.cometproject.api.game.rooms.components.RoomComponentContext;
import com.cometproject.api.game.rooms.components.types.*;
import com.cometproject.server.game.rooms.types.components.types.*;
import java.util.ArrayList;

public class RoomComponentFactory implements IRoomComponentFactory {
	private static final ArrayList<IRoomComponent> components = new ArrayList<>();
	private final RoomComponentContext roomComponentContext;
	
	public RoomComponentFactory(RoomComponentContext roomComponentContext) {
		this.roomComponentContext = roomComponentContext;
	}
	
	@Override
	public IEntityComponent createEntityComponent() {
		var entityComponent = new EntityComponent(roomComponentContext);
		components.add(entityComponent);
		return entityComponent;
	}
	
	@Override
	public IFilterComponent createFilterComponent() {
		var filterComponent = new FilterComponent(roomComponentContext);
		components.add(filterComponent);
		return filterComponent;
	}
	
	@Override
	public IGameComponent createGameComponent() {
		var gameComponent = new GameComponent(roomComponentContext);
		components.add(gameComponent);
		return gameComponent;
	}
	
	@Override
	public IItemsProcessComponent createItemsProcessComponent() {
		var itemsProcessComponent = new ItemProcessComponent(roomComponentContext);
		components.add(itemsProcessComponent);
		return itemsProcessComponent;
	}
	
	@Override
	public IItemsComponent createItemsComponent() {
		var itemsComponent = new ItemsComponent(roomComponentContext);
		components.add(itemsComponent);
		return itemsComponent;
	}
	
	@Override
	public IPetComponent createPetComponent() {
		var petComponent = new PetComponent(roomComponentContext);
		components.add(petComponent);
		return petComponent;
	}
	
	@Override
	public IProcessComponent createProcessComponent() {
		var processComponent = new ProcessComponent(roomComponentContext);
		components.add(processComponent);
		return processComponent;
	}
	
	@Override
	public IRightsComponent createRightsComponent() {
		var rightsComponent = new RightsComponent(roomComponentContext);
		components.add(rightsComponent);
		return rightsComponent;
	}
	
	@Override
	public IRoomBotComponent createRoomBotComponent() {
		var roomBotComponent = new RoomBotComponent(roomComponentContext);
		components.add(roomBotComponent);
		return roomBotComponent;
	}
	
	@Override
	public ITradeComponent createTradeComponent() {
		var tradeComponent = new TradeComponent(roomComponentContext);
		components.add(tradeComponent);
		return tradeComponent;
	}
	
	public static ArrayList<IRoomComponent> getComponents() {
		return components;
	}
	
}
