package com.cometproject.game.groups.types.components;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.game.groups.types.IGroupData;
import com.cometproject.api.game.groups.types.components.IForumComponent;
import com.cometproject.api.game.groups.types.components.forum.IForumSettings;
import com.cometproject.api.game.groups.types.components.forum.IForumThread;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;

public class ForumComponent implements IForumComponent {
    private final IForumSettings forumSettings;
    private final Map<Integer, IForumThread> forumThreads;
    private final List<Integer> pinnedThreads;

    public ForumComponent(IForumSettings forumSettings, 
                          List<Integer> pinnedThreads,
                          Map<Integer, IForumThread> forumThreads) {
        this.forumSettings = forumSettings;
        this.pinnedThreads = pinnedThreads;
        this.forumThreads = forumThreads;
    }

    @Override
    public void dispose() {
        for(IForumThread forumThread : this.forumThreads.values()) {
            forumThread.dispose();
        }

        this.forumThreads.clear();
        this.pinnedThreads.clear();
    }

    @Override
    public void composeData(IComposerDataWrapper msg, IGroupData groupData) {
        msg.writeInt(groupData.getId());
        msg.writeString(groupData.getTitle());
        msg.writeString(groupData.getDescription());
        msg.writeString(groupData.getBadge());

        msg.writeInt(this.forumThreads.size());//total threads
        msg.writeInt(0);//leaderboard score
        msg.writeInt(this.getForumThreadsAndRepliesSize().size());//count of all messages (threads+replies)
        msg.writeInt(0);//unread messages

        msg.writeInt(0);//last message id
        msg.writeInt(0);//last message author id
        msg.writeString("");//last message author name
        msg.writeInt(0);//last message time
    }

    public List<Integer> getForumThreadsAndRepliesSize() {
        List<Integer> threadsAndRepliesSize = Lists.newArrayList();

        int totalThreads = 0;
        int totalReplies = 0;

        for(IForumThread forumThread : this.forumThreads.values()) {
            totalThreads++;
            totalReplies += forumThread.getReplies().size();
        }

        threadsAndRepliesSize.add(totalThreads);
        threadsAndRepliesSize.add(totalReplies);

        return threadsAndRepliesSize;
    }

    @Override
    public List<IForumThread> getForumThreads(int start) {
        List<IForumThread> threads = Lists.newArrayList();

        if(start == 0) {
            for(int pinnedThread : this.pinnedThreads) {
                IForumThread forumThread = this.getForumThreads().get(pinnedThread);

                if(forumThread != null && threads.size() < CometSettings.GROUP_LIMIT_MSG_PAGE) {
                    threads.add(forumThread);
                }
            }

            for (IForumThread forumThread : this.getForumThreads().values()) {
                if (forumThread.isPinned() || threads.size() >= CometSettings.GROUP_LIMIT_MSG_PAGE) continue;
                threads.add(forumThread);
            }

            return threads;
        }

        int currentThreadIndex = 0;

        for(IForumThread forumThread : this.forumThreads.values()) {
            if(currentThreadIndex >= start && threads.size() < GROUP_LIMIT_MSG_PAGE) {
                threads.add(forumThread);
            }

            currentThreadIndex++;
        }

        return threads;
    }

    @Override
    public IForumSettings getForumSettings() {
        return this.forumSettings;
    }

    @Override
    public Map<Integer, IForumThread> getForumThreads() {
        return this.forumThreads;
    }

    @Override
    public List<Integer> getPinnedThreads() {
        return this.pinnedThreads;
    }
}
