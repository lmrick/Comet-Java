package com.cometproject.api.game.groups.types.components;

import com.cometproject.api.game.groups.types.IGroupComponent;
import com.cometproject.api.game.groups.types.IGroupData;
import com.cometproject.api.game.groups.types.components.forum.IForumSettings;
import com.cometproject.api.game.groups.types.components.forum.IForumThread;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;

import java.util.List;
import java.util.Map;

public interface IForumComponent extends IGroupComponent {
    void composeData(IComposerDataWrapper msg, IGroupData groupData);

    List<IForumThread> getForumThreads(int start);

    IForumSettings forumSettings();

    Map<Integer, IForumThread> forumThreads();

    List<Integer> pinnedThreads();
}