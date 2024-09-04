package com.cometproject.api.game.groups.types.components.forum;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;

public interface IForumThreadReply {
    void compose(IComposerDataWrapper msg);

    int getId();

    void setId(int id);

    String getMessage();

    void setMessage(String message);

    int getAuthorId();

    void setAuthorId(int authorId);

    int getAuthorTimestamp();

    void setAuthorTimestamp(int authorTimestamp);

    int getThreadId();

    int getIndex();

    void setIndex(int index);

    int getState();

    void setState(int state);

    int getAdminId();

    void setAdminId(int adminId);

    String getAdminUsername();

    void setAdminUsername(String adminUsername);
}
