package com.cometproject.storage.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cometproject.storage.api.repositories.*;

public final class StorageContext {
	private static StorageContext storageContext;
	private final Map<Class<?>, Object> repositories = new ConcurrentHashMap<>();

	public StorageContext() {
		
	}

	public <T> void setRepository(Class<T> repositoryClass, T repository) {
		repositories.put(repositoryClass, repository);
	}

	@SuppressWarnings("unchecked")
	public <T> T getRepository(Class<T> repositoryClass) {
		return (T) repositories.get(repositoryClass);
	}

	public static void setCurrentContext(StorageContext ctx) {
		storageContext = ctx;
	}

	public static StorageContext getCurrentContext() {
		return storageContext;
	}

	public IGroupRepository getGroupRepository() {
		return getRepository(IGroupRepository.class);
	}

	public IGroupMemberRepository getGroupMemberRepository() {
		return getRepository(IGroupMemberRepository.class);
	}

	public IGroupForumRepository getGroupForumRepository() {
		return getRepository(IGroupForumRepository.class);
	}

	public IRoomItemRepository getRoomItemRepository() {
		return getRepository(IRoomItemRepository.class);
	}

	public IInventoryRepository getInventoryRepository() {
		return getRepository(IInventoryRepository.class);
	}

	public IRoomRepository getRoomRepository() {
		return getRepository(IRoomRepository.class);
	}

	public IRewardRepository getRewardRepository() {
		return getRepository(IRewardRepository.class);
	}

	public IPhotoRepository getPhotoRepository() {
		return getRepository(IPhotoRepository.class);
	}

}
