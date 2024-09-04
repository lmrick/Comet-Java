package com.cometproject.api.game.players.components;

import com.cometproject.api.game.players.IPlayer;
import com.cometproject.api.game.players.PlayerContext;
import com.cometproject.api.game.players.data.components.*;

public class PlayerComponentContext extends PlayerContext {
	private IPlayerAchievements playerAchievements;
	private IPlayerBots playerBots;
	private IPlayerInventory playerInventory;
	private IPlayerMessenger playerMessenger;
	private IPlayerNavigator playerNavigator;
	private IPlayerPermissions playerPermissions;
	private IPlayerPets playerPets;
	private IPlayerQuests playerQuests;
	private IPlayerRelationships playerRelationships;
	private IPlayerSubscription playerSubscription;
	private IPlayerWardrobe playerWardrobe;
	
	public PlayerComponentContext(IPlayer player) {
		super(player);
	}
	
	public IPlayerSubscription getPlayerSubscription() {
		return playerSubscription;
	}
	
	public void setPlayerSubscription(IPlayerSubscription playerSubscription) {
		this.playerSubscription = playerSubscription;
	}
	
	public IPlayerWardrobe getPlayerWardrobe() {
		return playerWardrobe;
	}
	
	public void setPlayerWardrobe(IPlayerWardrobe playerWardrobe) {
		this.playerWardrobe = playerWardrobe;
	}
	
	public IPlayerAchievements getPlayerAchievements() {
		return playerAchievements;
	}
	
	public void setPlayerAchievements(IPlayerAchievements playerAchievements) {
		this.playerAchievements = playerAchievements;
	}
	
	public IPlayerBots getPlayerBots() {
		return playerBots;
	}
	
	public void setPlayerBots(IPlayerBots playerBots) {
		this.playerBots = playerBots;
	}
	
	public IPlayerInventory getPlayerInventory() {
		return playerInventory;
	}
	
	public void setPlayerInventory(IPlayerInventory playerInventory) {
		this.playerInventory = playerInventory;
	}
	
	public IPlayerMessenger getPlayerMessenger() {
		return playerMessenger;
	}
	
	public void setPlayerMessenger(IPlayerMessenger playerMessenger) {
		this.playerMessenger = playerMessenger;
	}
	
	public IPlayerNavigator getPlayerNavigator() {
		return playerNavigator;
	}
	
	public void setPlayerNavigator(IPlayerNavigator playerNavigator) {
		this.playerNavigator = playerNavigator;
	}
	
	public IPlayerPermissions getPlayerPermissions() {
		return playerPermissions;
	}
	
	public void setPlayerPermissions(IPlayerPermissions playerPermissions) {
		this.playerPermissions = playerPermissions;
	}
	
	public IPlayerPets getPlayerPets() {
		return playerPets;
	}
	
	public void setPlayerPets(IPlayerPets playerPets) {
		this.playerPets = playerPets;
	}
	
	public IPlayerQuests getPlayerQuests() {
		return playerQuests;
	}
	
	public void setPlayerQuests(IPlayerQuests playerQuests) {
		this.playerQuests = playerQuests;
	}
	
	public IPlayerRelationships getPlayerRelationships() {
		return playerRelationships;
	}
	
	public void setPlayerRelationships(IPlayerRelationships playerRelationships) {
		this.playerRelationships = playerRelationships;
	}
	
	@Override
	public IPlayer getPlayer() {
		return super.getPlayer();
	}
	
}
