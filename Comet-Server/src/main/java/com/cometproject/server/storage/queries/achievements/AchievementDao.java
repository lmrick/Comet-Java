package com.cometproject.server.storage.queries.achievements;

import com.cometproject.api.game.achievements.types.AchievementCategory;
import com.cometproject.api.game.achievements.types.AchievementType;
import com.cometproject.api.game.achievements.types.IAchievementGroup;
import com.cometproject.server.game.achievements.AchievementGroup;
import com.cometproject.server.game.achievements.types.Achievement;
import com.cometproject.server.storage.SQLUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AchievementDao {

    public static int getAchievements(Map<AchievementType, IAchievementGroup> achievementGroups) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        int count = 0;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT * FROM achievements WHERE enabled = '1' ORDER by group_name ASC", sqlConnection);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                count++;

                final AchievementType groupName = AchievementType.getTypeByName(resultSet.getString("group_name"));

                if (groupName == null) continue;

                if (!achievementGroups.containsKey(groupName)) {
                    achievementGroups.put(groupName, new AchievementGroup(resultSet.getInt("id"), new HashMap<>(), resultSet.getString("group_name"), AchievementCategory.valueOf(resultSet.getString("category").toUpperCase())));
                }

                if (!achievementGroups.get(groupName).achievements().containsKey(resultSet.getInt("level"))) {
                    achievementGroups.get(groupName).achievements().put(resultSet.getInt("level"), create(resultSet));
                }
            }
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(resultSet);
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }

        return count;
    }

    private static Achievement create(ResultSet resultSet) throws SQLException {
        return new Achievement(resultSet.getInt("level"), resultSet.getInt("reward_activity_points"), resultSet.getInt("reward_achievement_points"), resultSet.getInt("progress_requirement"));
    }

}
