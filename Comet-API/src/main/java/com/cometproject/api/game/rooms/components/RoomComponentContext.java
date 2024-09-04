package com.cometproject.api.game.rooms.components;

import com.cometproject.api.game.rooms.IRoom;
import com.cometproject.api.game.rooms.RoomContext;
import com.cometproject.api.game.rooms.components.types.*;

public class RoomComponentContext extends RoomContext {
	private IEntityComponent entityComponent;
	private IFilterComponent filterComponent;
	private IGameComponent gameComponent;
	private IItemsComponent itemsComponent;
	private IItemsProcessComponent itemsProcessComponent;
	private IPetComponent petComponent;
	private IProcessComponent processComponent;
	private IRightsComponent rightsComponent;
	private IRoomBotComponent roomBotComponent;
	private ITradeComponent tradeComponent;
	
	public RoomComponentContext(IRoom room) {
		super(room);
	}
	
	public IEntityComponent getEntityComponent() {
		return entityComponent;
	}
	
	public void setEntityComponent(IEntityComponent entityComponent) {
		this.entityComponent = entityComponent;
	}
	
	public IFilterComponent getFilterComponent() {
		return filterComponent;
	}
	
	public void setFilterComponent(IFilterComponent filterComponent) {
		this.filterComponent = filterComponent;
	}
	
	public IGameComponent getGameComponent() {
		return gameComponent;
	}
	
	public void setGameComponent(IGameComponent gameComponent) {
		this.gameComponent = gameComponent;
	}
	
	public IItemsComponent getItemsComponent() {
		return itemsComponent;
	}
	
	public void setItemsComponent(IItemsComponent itemsComponent) {
		this.itemsComponent = itemsComponent;
	}
	
	public IItemsProcessComponent getItemsProcessComponent() {
		return itemsProcessComponent;
	}
	
	public void setItemsProcessComponent(IItemsProcessComponent itemsProcessComponent) {
		this.itemsProcessComponent = itemsProcessComponent;
	}
	
	public IPetComponent getPetComponent() {
		return petComponent;
	}
	
	public void setPetComponent(IPetComponent petComponent) {
		this.petComponent = petComponent;
	}
	
	public IProcessComponent getProcessComponent() {
		return processComponent;
	}
	
	public void setProcessComponent(IProcessComponent processComponent) {
		this.processComponent = processComponent;
	}
	
	public IRoomBotComponent getRoomBotComponent() {
		return roomBotComponent;
	}
	
	public void setRoomBotComponent(IRoomBotComponent roomBotComponent) {
		this.roomBotComponent = roomBotComponent;
	}
	
	public ITradeComponent getTradeComponent() {
		return tradeComponent;
	}
	
	public void setTradeComponent(ITradeComponent tradeComponent) {
		this.tradeComponent = tradeComponent;
	}
	
	public IRightsComponent getRightsComponent() {
		return rightsComponent;
	}
	
	public void setRightsComponent(IRightsComponent rightsComponent) {
		this.rightsComponent = rightsComponent;
	}
	
	@Override
	public IRoom getRoom() {
		return super.getRoom();
	}
	
}
