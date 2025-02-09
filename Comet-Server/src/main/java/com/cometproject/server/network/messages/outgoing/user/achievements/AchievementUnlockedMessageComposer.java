package com.cometproject.server.network.messages.outgoing.user.achievements;

import com.cometproject.api.game.achievements.types.IAchievement;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class AchievementUnlockedMessageComposer extends MessageComposer {
    private final String achievementCategory;
    private final String achievementName;

    private final int currentAchievementId;
    private final IAchievement newAchievement;

    public AchievementUnlockedMessageComposer(final String achievementCategory, String achievementName, int currentAchievementId, IAchievement newAchievement) {
        this.achievementCategory = achievementCategory;
        this.achievementName = achievementName;
        this.currentAchievementId = currentAchievementId;
        this.newAchievement = newAchievement;
    }

    @Override
    public short getId() {
        return Composers.AchievementUnlockedMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(currentAchievementId);
        msg.writeInt(newAchievement == null ? 1 : newAchievement.level());
        msg.writeInt(144);
        msg.writeString(achievementName + (newAchievement == null ? 1 : (newAchievement.level() - 1)));
        msg.writeInt(newAchievement == null ? 0 : newAchievement.rewardAchievement());
        msg.writeInt(newAchievement == null ? 0 : newAchievement.rewardActivityPoints());
        msg.writeInt(0);
        msg.writeInt(10);
        msg.writeInt(21);
        msg.writeString(newAchievement != null ? newAchievement.level() > 1 ? achievementName + (newAchievement.level() - 1) : "" : "");

        msg.writeString(achievementCategory.toLowerCase());
        msg.writeBoolean(true);
    }
}
