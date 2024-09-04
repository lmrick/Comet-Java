package com.cometproject.api.game.players.components;

import com.cometproject.api.game.players.data.components.*;

public interface IPlayerComponentFactory {
	
	IPlayerAchievements createPlayerAchievementsComponent();
	IPlayerBots createPlayerBotsComponent();
	IPlayerInventory createPlayerInventoryComponent();
	IPlayerMessenger createPlayerMessengerComponent();
	IPlayerNavigator createPlayerNavigatorComponent();
	IPlayerPermissions createPlayerPermissionsComponent();
	IPlayerPets createPlayerPetsComponent();
	IPlayerQuests createPlayerQuestsComponent();
	IPlayerRelationships createPlayerRelationshipsComponent();
	IPlayerSubscription createPlayerSubscriptionComponent();
	IPlayerWardrobe createPlayerWardrobeComponent();
	
}
