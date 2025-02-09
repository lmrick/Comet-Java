package com.cometproject.server.storage.queries.quests;

import com.cometproject.api.game.quests.IQuest;
import com.cometproject.server.game.quests.types.Quest;
import com.cometproject.server.storage.SQLUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class QuestsDao {
    public static Map<String, IQuest> getAllQuests() {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<String, IQuest> quests = new HashMap<>();

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT * FROM quests WHERE enabled = '1'", sqlConnection);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                quests.put(resultSet.getString("name"), new Quest(resultSet));
            }
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(resultSet);
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }

        return quests;
    }
}
