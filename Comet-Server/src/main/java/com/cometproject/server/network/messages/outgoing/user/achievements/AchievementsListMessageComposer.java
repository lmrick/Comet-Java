package com.cometproject.server.network.messages.outgoing.user.achievements;

import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.achievements.IAchievementsService;
import com.cometproject.api.game.achievements.types.IAchievement;
import com.cometproject.api.game.players.data.components.achievements.IAchievementProgress;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.game.players.components.types.AchievementComponent;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class AchievementsListMessageComposer extends MessageComposer {
    private final AchievementComponent achievementComponent;

    public AchievementsListMessageComposer(final AchievementComponent achievementComponent) {
        this.achievementComponent = achievementComponent;
    }

    @Override
    public short getId() {
        return Composers.AchievementsMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        final var achievementsGroupsSize = GameContext.getCurrent().getService(IAchievementsService.class).getAchievementGroups().size();
        final var achievementsGroupsEntrySet = GameContext.getCurrent().getService(IAchievementsService.class).getAchievementGroups().entrySet();
        
        msg.writeInt(achievementsGroupsSize);

        for (var entry : achievementsGroupsEntrySet) {
            final IAchievementProgress achievementProgress = this.achievementComponent.getProgress(entry.getKey());
            final IAchievement achievement = achievementProgress == null ? entry.getValue().getAchievement(1) : entry.getValue().getAchievement(achievementProgress.getLevel());

            msg.writeInt(entry.getValue().id());
            msg.writeInt(achievement == null ? 0 : achievement.level());
            msg.writeString(achievement == null ? "" : entry.getKey().getGroupName() + achievement.level());
            msg.writeInt(achievement == null ? 0 : achievement.level() == 1 ? 0 : entry.getValue().getAchievement(achievement.level() - 1).progressNeeded());
            msg.writeInt(achievement == null ? 0 : achievement.progressNeeded());
            msg.writeInt(achievement == null ? 0 : achievement.rewardActivityPoints());
            msg.writeInt(0);
            msg.writeInt(achievementProgress != null ? achievementProgress.getProgress() : 0);

            if (achievementProgress == null) {
                msg.writeBoolean(false);
            } else if (achievementProgress.getLevel() >= entry.getValue().getLevelCount()) {
                msg.writeBoolean(true);
            } else {
                msg.writeBoolean(false);
            }

            msg.writeString(entry.getValue().category().toString().toLowerCase());
            msg.writeString("");
            msg.writeInt(entry.getValue().getLevelCount());
            msg.writeInt(0);
        }

        msg.writeString("");
    }

}
