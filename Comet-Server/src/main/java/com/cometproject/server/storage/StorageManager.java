package com.cometproject.server.storage;

import com.cometproject.api.utilities.process.Initializable;
import com.cometproject.server.storage.cache.CacheManager;
import com.cometproject.storage.api.StorageContext;
import com.cometproject.storage.mysql.MySQLStorageInitializer;
import com.cometproject.storage.mysql.connections.HikariConnectionProvider;
import com.cometproject.storage.mysql.queues.MySQLQueueManager;
import com.cometproject.storage.mysql.queues.types.players.*;
import com.cometproject.storage.mysql.queues.types.items.*;
import com.cometproject.storage.mysql.queues.types.pets.PetStatsUpdateQueue;

public class StorageManager implements Initializable {
	private static StorageManager storageManagerInstance;
	private final HikariConnectionProvider hikariConnectionProvider;
	
	public StorageManager() {
		hikariConnectionProvider = new HikariConnectionProvider();
	}
	
	public static StorageManager getInstance() {
		if (storageManagerInstance == null) storageManagerInstance = new StorageManager();
		return storageManagerInstance;
	}
	
	@Override
	public void initialize() {
		final MySQLStorageInitializer initializer = new MySQLStorageInitializer(hikariConnectionProvider);
		final MySQLQueueManager queueManager = new MySQLQueueManager(hikariConnectionProvider);
		final StorageContext storageContext = new StorageContext();
		
		initializer.setup(storageContext);
		
		StorageContext.setCurrentContext(storageContext);
		this.registerQueues();
		MySQLQueueManager.getInstance().initialize();
		
		CacheManager.getInstance().initialize();
		SQLUtility.init(hikariConnectionProvider);
	}

	public void registerQueues() {
		MySQLQueueManager.getInstance().registerQueue("playerDataUpdateQueue", new PlayerDataUpdateQueue(1000, hikariConnectionProvider));
		MySQLQueueManager.getInstance().registerQueue("playerBadgeUpdateQueue", new PlayerBadgeUpdateQueue(1000, hikariConnectionProvider));
		MySQLQueueManager.getInstance().registerQueue("playerOfflineUpdateQueue", new PlayerOfflineUpdateQueue(1000, hikariConnectionProvider));
		MySQLQueueManager.getInstance().registerQueue("playerStatusUpdateQueue", new PlayerStatusUpdateQueue(1000, hikariConnectionProvider));
		MySQLQueueManager.getInstance().registerQueue("petStatsUpdateQueue", new PetStatsUpdateQueue(1000, hikariConnectionProvider));
	
		MySQLQueueManager.getInstance().registerBlockingQueue("itemDataUpdateQueue", new ItemDataUpdateQueue(25, hikariConnectionProvider));
		MySQLQueueManager.getInstance().registerBlockingQueue("itemUpdateQueue", new ItemUpdateQueue(25, hikariConnectionProvider));
	}

	public MySQLQueueManager getQueueManager() {
		return MySQLQueueManager.getInstance();
	}
	
	public void shutdown() {
		this.hikariConnectionProvider.shutdown();
	}
	
}
