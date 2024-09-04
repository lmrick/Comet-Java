package com.cometproject.storage.api.factories.groups;

import com.cometproject.api.game.groups.types.components.forum.IForumThread;
import com.cometproject.api.game.groups.types.components.forum.IForumThreadReply;
import com.cometproject.storage.api.data.groups.GroupForumThreadData;
import com.cometproject.storage.api.data.groups.GroupForumThreadMessageData;

public class GroupForumMessageFactory {

    public IForumThread createThread(int id, String title, String message, int authorId, int authorTimestamp, int state, boolean locked, boolean pinned, int moderatorId, String moderatorUsername) {
        return new GroupForumThreadData(id, title, message, authorId, authorTimestamp, state, locked, pinned, moderatorId, moderatorUsername);
    }

    public IForumThreadReply createThreadReply(int id, int index, String message, int threadId, int authorId, int authorTimestamp, int state, int adminId, String adminUsername) {
        return new GroupForumThreadMessageData(id, index, message, threadId, authorId, authorTimestamp, state, adminId, adminUsername);
    }
}