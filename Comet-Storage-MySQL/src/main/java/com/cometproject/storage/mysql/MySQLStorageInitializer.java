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
import com.cometproject.storage.api.repositories.IGroupForumRepository;
import com.cometproject.storage.api.repositories.IGroupMemberRepository;
import com.cometproject.storage.api.repositories.IGroupRepository;
import com.cometproject.storage.api.repositories.IInventoryRepository;
import com.cometproject.storage.api.repositories.IPhotoRepository;
import com.cometproject.storage.api.repositories.IRewardRepository;
import com.cometproject.storage.api.repositories.IRoomItemRepository;
import com.cometproject.storage.api.repositories.IRoomRepository;
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
		storageContext.setRepository(IGroupRepository.class, new MySQLGroupRepository(new GroupDataFactory(), connectionProvider));
		storageContext.setRepository(IGroupMemberRepository.class, new MySQLGroupMemberRepository(new GroupMemberFactory(), connectionProvider));
		storageContext.setRepository(IGroupForumRepository.class, new MySQLGroupForumRepository(new GroupForumSettingsFactory(), new GroupForumMessageFactory(), connectionProvider));
		storageContext.setRepository(IInventoryRepository.class, new MySQLInventoryRepository(new InventoryItemFactory(), connectionProvider));
		storageContext.setRepository(IRoomItemRepository.class, new MySQLRoomItemRepository(connectionProvider));
		storageContext.setRepository(IRoomRepository.class, new MySQLRoomRepository(new RoomDataFactory(), new RoomModelDataFactory(), connectionProvider));
		storageContext.setRepository(IRewardRepository.class, new MySQLRewardRepository(connectionProvider));
		storageContext.setRepository(IPhotoRepository.class, new MySQLPhotoRepository(connectionProvider));
	}
	
}
