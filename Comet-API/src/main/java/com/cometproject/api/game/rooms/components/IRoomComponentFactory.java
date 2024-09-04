package com.cometproject.api.game.rooms.components;

import com.cometproject.api.game.rooms.components.types.*;

public interface IRoomComponentFactory {
	
	IEntityComponent createEntityComponent();
	IFilterComponent createFilterComponent();
	IGameComponent createGameComponent();
	IItemsProcessComponent createItemsProcessComponent();
	IItemsComponent createItemsComponent();
	IPetComponent createPetComponent();
	IProcessComponent createProcessComponent();
	IRightsComponent createRightsComponent();
	IRoomBotComponent createRoomBotComponent();
	ITradeComponent createTradeComponent();
	
}
