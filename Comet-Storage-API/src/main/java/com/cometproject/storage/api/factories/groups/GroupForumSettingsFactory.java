package com.cometproject.storage.api.factories.groups;

import com.cometproject.api.game.groups.types.components.forum.ForumPermission;
import com.cometproject.api.game.groups.types.components.forum.IForumSettings;
import com.cometproject.storage.api.data.groups.GroupForumSettingsData;

public class GroupForumSettingsFactory {
	
	public IForumSettings createSettings(int groupId, ForumPermission readPermission, ForumPermission postPermission, ForumPermission startThreadPermission, ForumPermission moderatePermission) {
		return new GroupForumSettingsData(groupId, readPermission, postPermission, startThreadPermission, moderatePermission);
	}
	
}
