package com.cometproject.server.storage.queries.bots;

import com.cometproject.api.game.bots.BotMode;
import com.cometproject.api.game.bots.BotType;
import com.cometproject.api.game.bots.IBotData;
import com.cometproject.server.game.rooms.objects.entities.types.data.PlayerBotData;
import com.cometproject.server.storage.SQLUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class PlayerBotDao {
    public static Map<Integer, IBotData> getBotsByPlayerId(int playerId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<Integer, IBotData> data = new ConcurrentHashMap<>();

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT * FROM bots WHERE owner_id = ? AND room_id = 0", sqlConnection);
            preparedStatement.setInt(1, playerId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                final int id = resultSet.getInt("id");
                final String username = resultSet.getString("name");
                final String motto = resultSet.getString("motto");
                final String figure = resultSet.getString("figure");
                final String gender = resultSet.getString("gender");
                final String ownerName = resultSet.getString("owner");
                final int ownerId = resultSet.getInt("owner_id");
                final String messages = resultSet.getString("messages");
                final boolean automaticChat = resultSet.getBoolean("automatic_chat");
                final int chatDelay = resultSet.getInt("chat_delay");
                final BotType botType = BotType.valueOf(resultSet.getString("type").toUpperCase());
                final BotMode mode = BotMode.valueOf(resultSet.getString("mode").toUpperCase());
                final String storedData = resultSet.getString("data");

                data.put(id, new PlayerBotData(id, username, motto, figure, gender,
                        ownerName, ownerId, messages, automaticChat, chatDelay, botType, mode, storedData));
            }
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(resultSet);
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }

        return data;
    }

    public static int createBot(int playerId, String name, String figure, String gender, String motto, BotType type) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("INSERT into bots (`owner_id`, `room_id`, `name`, `figure`, `gender`, `motto`, `x`, `y`, `z`, `messages`, `automatic_chat`, `chat_delay`, `type`) VALUES(" +
																									 "?, 0, ?, ?, ?, ?, 0, 0, 0, '[]', '1', '14', ?);", sqlConnection, true);

            preparedStatement.setInt(1, playerId);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, figure);
            preparedStatement.setString(4, gender);
            preparedStatement.setString(5, motto);
            preparedStatement.setString(6, type.toString().toLowerCase());

            preparedStatement.execute();

            resultSet = preparedStatement.getGeneratedKeys();

            while (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(resultSet);
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }

        return 0;
    }

    public static void deleteBots(int playerId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("DELETE FROM bots WHERE owner_id = ? AND room_id = 0;", sqlConnection);
            preparedStatement.setInt(1, playerId);

            SQLUtility.executeStatementSilently(preparedStatement, false);
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }
}
