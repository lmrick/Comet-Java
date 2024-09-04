package com.cometproject.server.game.players.components;

import com.cometproject.api.game.players.components.IPlayerComponent;
import com.cometproject.api.game.players.components.PlayerComponentContext;
import com.cometproject.api.game.players.components.IPlayerComponentFactory;
import com.cometproject.api.game.players.data.components.*;
import com.cometproject.server.game.players.components.types.*;
import java.util.ArrayList;

public class PlayerComponentFactory implements IPlayerComponentFactory {
	
	private static final ArrayList<IPlayerComponent> components = new ArrayList<>();
	private final PlayerComponentContext playerComponentContext;
	
	public PlayerComponentFactory(PlayerComponentContext playerComponentContext) {
		this.playerComponentContext = playerComponentContext;
	}
	
	@Override
	public IPlayerAchievements createPlayerAchievementsComponent() {
		var playerAchievements = new AchievementComponent(playerComponentContext);
		components.add(playerAchievements);
		return playerAchievements;
	}
	
	@Override
	public IPlayerBots createPlayerBotsComponent() {
		var playerBots = new InventoryBotComponent(playerComponentContext);
		components.add(playerBots);
		return playerBots;
	}
	
	@Override
	public IPlayerInventory createPlayerInventoryComponent() {
		var playerInventory = new InventoryComponent(playerComponentContext);
		components.add(playerInventory);
		return playerInventory;
	}
	
	@Override
	public IPlayerMessenger createPlayerMessengerComponent() {
		var playerMessenger = new MessengerComponent(playerComponentContext);
		components.add(playerMessenger);
		return playerMessenger;
	}
	
	@Override
	public IPlayerNavigator createPlayerNavigatorComponent() {
		var playerNavigator = new NavigatorComponent(playerComponentContext);
		components.add(playerNavigator);
		return playerNavigator;
	}
	
	@Override
	public IPlayerPermissions createPlayerPermissionsComponent() {
		var playerPermissions = new PermissionComponent(playerComponentContext);
		components.add(playerPermissions);
		return playerPermissions;
	}
	
	@Override
	public IPlayerPets createPlayerPetsComponent() {
		var playerPets = new PetComponent(playerComponentContext);
		components.add(playerPets);
		return playerPets;
	}
	
	@Override
	public IPlayerQuests createPlayerQuestsComponent() {
		var playerQuests = new QuestComponent(playerComponentContext);
		components.add(playerQuests);
		return playerQuests;
	}
	
	@Override
	public IPlayerRelationships createPlayerRelationshipsComponent() {
		var playerRelationships = new RelationshipComponent(playerComponentContext);
		components.add(playerRelationships);
		return playerRelationships;
	}
	
	@Override
	public IPlayerSubscription createPlayerSubscriptionComponent() {
		var playerSubscription = new SubscriptionComponent(playerComponentContext);
		components.add(playerSubscription);
		return playerSubscription;
	}
	
	@Override
	public IPlayerWardrobe createPlayerWardrobeComponent() {
		var playerWardrobe = new WardrobeComponent(playerComponentContext);
		components.add(playerWardrobe);
		return playerWardrobe;
	}
	
	public static ArrayList<IPlayerComponent> getComponents() {
		return components;
	}
	
}
