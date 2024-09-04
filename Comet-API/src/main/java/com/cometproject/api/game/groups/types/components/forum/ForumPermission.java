package com.cometproject.api.game.groups.types.components.forum;

public enum ForumPermission {
	EVERYBODY(0), MEMBERS(1), ADMINISTRATORS(2), OWNER(3);
	
	private final int permissionId;
	
	ForumPermission(int permissionId) {
		this.permissionId = permissionId;
	}
	
	public int getPermissionId() {
		return permissionId;
	}
	
	public static ForumPermission getById(int id) {
		return switch (id) {
			case 0 -> EVERYBODY;
			case 2 -> ADMINISTRATORS;
			case 3 -> OWNER;
			default -> MEMBERS;
		};
	}
}

