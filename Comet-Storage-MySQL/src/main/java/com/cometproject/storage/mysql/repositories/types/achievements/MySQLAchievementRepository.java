package com.cometproject.storage.mysql.repositories.types.achievements;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import com.cometproject.api.game.achievements.types.AchievementCategory;
import com.cometproject.api.game.achievements.types.AchievementType;
import com.cometproject.api.game.achievements.types.IAchievement;
import com.cometproject.api.game.achievements.types.IAchievementGroup;
import com.cometproject.storage.api.factories.achievements.AchievementFactory;
import com.cometproject.storage.api.repositories.IAchievementRepository;
import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import com.cometproject.storage.mysql.data.results.IResultReader;
import com.cometproject.storage.mysql.repositories.MySQLRepository;

public class MySQLAchievementRepository extends MySQLRepository implements IAchievementRepository {
    private final AchievementFactory achievementFactory;

    public MySQLAchievementRepository(AchievementFactory achievementFactory, MySQLConnectionProvider connectionProvider) {
        super(connectionProvider);

        this.achievementFactory = achievementFactory;
    }
    

    @Override
    public void getAchievements(Map<AchievementType, IAchievementGroup> achievementGroups, Consumer<Integer> consumer) {

        select("SELECT * FROM achievements WHERE enabled = '1' ORDER by group_name ASC", data -> {
            while(data.hasNext()) {
            int count = 0;
            final AchievementType groupName = AchievementType.getTypeByName(data.readString("group_name"));
            final int achievementLevel = data.readInteger("level");
            count++;

            if (groupName == null) continue;            
            if (!achievementGroups.containsKey(groupName)) {
                    achievementGroups.put(groupName, readAchievementGroup(data));
                }

                if (!achievementGroups.get(groupName).achievements().containsKey(achievementLevel)) {
                    achievementGroups.get(groupName).achievements().put(achievementLevel, readAchievement(data));
                }

                consumer.accept(count);
            }
        });
    }

    private IAchievementGroup readAchievementGroup(IResultReader data) throws Exception {
        int id = data.readInteger("id");
        String groupName = data.readString("group_name");
        AchievementCategory category = AchievementCategory.valueOf(data.readString("category").toUpperCase());
        Map<Integer, IAchievement> achievements = new HashMap<>();

        return this.achievementFactory.createAchievementGroup(id, achievements, groupName, category);
    }

    private IAchievement readAchievement(IResultReader data) throws Exception {
        int level = data.readInteger("level");
        int rewardActivityPoints = data.readInteger("reward_activity_points");
        int rewardAchievement = data.readInteger("reward_achievement_points");
        int progressNeeded = data.readInteger("progress_requirement");

        return this.achievementFactory.createAchievement(level, rewardActivityPoints, rewardAchievement, progressNeeded);
    }

}
