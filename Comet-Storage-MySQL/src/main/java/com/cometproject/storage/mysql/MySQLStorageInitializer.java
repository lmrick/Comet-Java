package com.cometproject.storage.mysql;

import com.cometproject.game.items.inventory.InventoryItemFactory;
import com.cometproject.storage.api.IStorageInitializer;
import com.cometproject.storage.api.StorageContext;
import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import com.cometproject.storage.api.factories.groups.GroupDataFactory;
import com.cometproject.storage.api.factories.groups.GroupForumMessageFactory;
import com.cometproject.storage.api.factories.groups.GroupForumSettingsFactory;
import com.cometproject.storage.api.factories.groups.GroupMemberFactory;
import com.cometproject.storage.api.factories.rooms.RoomDataFactory;
import com.cometproject.storage.api.factories.rooms.RoomModelDataFactory;
import com.cometproject.storage.mysql.repositories.types.groups.MySQLGroupForumRepository;
import com.cometproject.storage.mysql.repositories.types.groups.MySQLGroupMemberRepository;
import com.cometproject.storage.mysql.repositories.types.groups.MySQLGroupRepository;
import com.cometproject.storage.mysql.repositories.types.inventory.MySQLInventoryRepository;
import com.cometproject.storage.mysql.repositories.types.inventory.MySQLPhotoRepository;
import com.cometproject.storage.mysql.repositories.types.inventory.MySQLRewardRepository;
import com.cometproject.storage.mysql.repositories.types.rooms.MySQLRoomItemRepository;
import com.cometproject.storage.mysql.repositories.types.rooms.MySQLRoomRepository;

public class MySQLStorageInitializer implements IStorageInitializer {
	
	private final MySQLConnectionProvider connectionProvider;
	
	public MySQLStorageInitializer(MySQLConnectionProvider connectionProvider) {
		this.connectionProvider = connectionProvider;
		MySQLStorageContext.setCurrentContext(new MySQLStorageContext(connectionProvider));
	}
	
	@Override
	public void setup(StorageContext storageContext) {
		storageContext.setGroupRepository(new MySQLGroupRepository(new GroupDataFactory(), connectionProvider));
		storageContext.setGroupMemberRepository(new MySQLGroupMemberRepository(new GroupMemberFactory(), connectionProvider));
		storageContext.setGroupForumRepository(new MySQLGroupForumRepository(new GroupForumSettingsFactory(), new GroupForumMessageFactory(), connectionProvider));
		storageContext.setInventoryRepository(new MySQLInventoryRepository(new InventoryItemFactory(), connectionProvider));
		storageContext.setRoomItemRepository(new MySQLRoomItemRepository(connectionProvider));
		storageContext.setRoomRepository(new MySQLRoomRepository(new RoomDataFactory(), new RoomModelDataFactory(), connectionProvider));
		storageContext.setRewardRepository(new MySQLRewardRepository(connectionProvider));
		storageContext.setPhotoRepository(new MySQLPhotoRepository(connectionProvider));
	}
	
}
