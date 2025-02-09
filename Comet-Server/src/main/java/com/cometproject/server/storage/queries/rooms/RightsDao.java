package com.cometproject.server.storage.queries.rooms;

import com.cometproject.server.game.rooms.types.components.types.ban.RoomBan;
import com.cometproject.server.storage.SQLUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class RightsDao {
    public static List<Integer> getRightsByRoomId(int roomId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        List<Integer> data = new CopyOnWriteArrayList<>();

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT player_id FROM room_rights WHERE room_id = ?", sqlConnection);
            preparedStatement.setInt(1, roomId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                data.add(resultSet.getInt("player_id"));
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

    public static void delete(int playerId, int roomId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("DELETE from room_rights WHERE room_id = ? AND player_id = ? ", sqlConnection);
            preparedStatement.setInt(1, roomId);
            preparedStatement.setInt(2, playerId);

            SQLUtility.executeStatementSilently(preparedStatement, false);
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }

    public static void add(int playerId, int roomId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("INSERT into room_rights (`room_id`, `player_id`) VALUES(?, ?);", sqlConnection);
            preparedStatement.setInt(1, roomId);
            preparedStatement.setInt(2, playerId);

            SQLUtility.executeStatementSilently(preparedStatement, false);
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }

    public static void addRoomBan(int playerId, int roomId, int expireTimestamp) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("INSERT into room_bans (`room_id`, `player_id`, `expire_timestamp`) VALUES(?, ?, ?);", sqlConnection);
            preparedStatement.setInt(1, roomId);
            preparedStatement.setInt(2, playerId);
            preparedStatement.setInt(3, expireTimestamp);

            SQLUtility.executeStatementSilently(preparedStatement, false);
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }

    public static void deleteRoomBan(int playerId, int roomId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("DELETE FROM `room_bans` WHERE `player_id` = ? AND `room_id` = ?", sqlConnection);
            preparedStatement.setInt(1, playerId);
            preparedStatement.setInt(2, roomId);

            SQLUtility.executeStatementSilently(preparedStatement, false);
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }


    public static Map<Integer, RoomBan> getRoomBansByRoomId(int roomId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<Integer, RoomBan> data = new ConcurrentHashMap<>();

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT b.`room_id`, b.`player_id`, b.`expire_timestamp`, p.`username` AS player_name FROM `room_bans` b LEFT JOIN `players` AS p ON p.`id` = b.`player_id` WHERE b.`room_id` = ? AND b.`expire_timestamp` >= UNIX_TIMESTAMP() OR b.`room_id` = ? AND b.`expire_timestamp` = -1", sqlConnection);
            preparedStatement.setInt(1, roomId);
            preparedStatement.setInt(2, roomId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                data.put(resultSet.getInt("player_id"), new RoomBan(resultSet.getInt("player_id"), resultSet.getString("player_name"), resultSet.getInt("expire_timestamp")));
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
}
