package com.cometproject.server.network.messages.outgoing.user.achievements;

import com.cometproject.api.game.achievements.types.IAchievement;
import com.cometproject.api.game.achievements.types.IAchievementGroup;
import com.cometproject.api.game.players.data.components.achievements.IAchievementProgress;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.game.players.components.types.achievements.AchievementProgress;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class AchievementProgressMessageComposer extends MessageComposer {

    private final IAchievementGroup achievementGroup;
    private final IAchievementProgress achievementProgress;

    public AchievementProgressMessageComposer(IAchievementProgress achievementProgress, IAchievementGroup achievementGroup) {
        this.achievementProgress = achievementProgress == null ? null : new AchievementProgress(achievementProgress.getLevel(), achievementProgress.getProgress());
        this.achievementGroup = achievementGroup;
    }

    @Override
    public short getId() {
        return Composers.AchievementProgressedMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        final IAchievement achievement = this.achievementGroup.getAchievement(this.achievementProgress.getLevel());

        msg.writeInt(achievementGroup.id());
        msg.writeInt(achievement.level());
        msg.writeString(achievementGroup.groupName() + achievement.level());
        msg.writeInt(achievement.level() == 1 ? 0 : achievementGroup.getAchievement(achievement.level() - 1).progressNeeded());
        msg.writeInt(achievement.progressNeeded());
        msg.writeInt(achievement.rewardActivityPoints());
        msg.writeInt(0);
        msg.writeInt(achievementProgress.getProgress());

        if (achievementProgress.getLevel() >= achievementGroup.getLevelCount()) {
            msg.writeBoolean(true);
        } else {
            msg.writeBoolean(false);
        }

        msg.writeString(achievementGroup.category().toString().toLowerCase());
        msg.writeString("");
        msg.writeInt(achievementGroup.getLevelCount());
        msg.writeInt(0);
    }
}
