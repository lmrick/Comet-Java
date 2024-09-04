package com.cometproject.api.game.groups.types.components.forum;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;

import java.util.List;

public interface IForumThread {
    void compose(IComposerDataWrapper msg);

    List<IForumThreadReply> getReplies(int start);

    IForumThreadReply getReplyById(int id);

    IForumThreadReply getMostRecentPost();

    void addReply(IForumThreadReply reply);

    void dispose();

    int getId();

    void setId(int id);

    String getTitle();

    void setTitle(String title);

    List<IForumThreadReply> getReplies();

    void setReplies(List<IForumThreadReply> replies);

    int getAuthorId();

    int getAuthorTimestamp();

    boolean isLocked();

    void setIsLocked(boolean isLocked);

    int getState();

    void setState(int state);

    boolean isPinned();

    void setIsPinned(boolean isPinned);
}
