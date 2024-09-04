package com.cometproject.server.storage.queries.system;

import com.cometproject.server.boot.Comet;
import com.cometproject.server.storage.SQLUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class StatisticsDao {
    
    public static void saveStatistics(int players, int rooms, String version, int onlineRecord) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();
            preparedStatement = SQLUtility.prepare("UPDATE server_status SET active_players = ?, active_rooms = ?, server_version = ?, player_record = ?, player_record_timestamp = ?", sqlConnection);

            preparedStatement.setInt(1, players);
            preparedStatement.setInt(2, rooms);
            preparedStatement.setString(3, version);
            preparedStatement.setInt(4, onlineRecord);
            preparedStatement.setInt(5, (int) Comet.getTime());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }

    public static void saveStatistics(int players, int rooms, String version) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();
            preparedStatement = SQLUtility.prepare("UPDATE server_status SET active_players = ?, active_rooms = ?, server_version = ?", sqlConnection);

            preparedStatement.setInt(1, players);
            preparedStatement.setInt(2, rooms);
            preparedStatement.setString(3, version);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }

    public static int getPlayerRecord() {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            sqlConnection = SQLUtility.getConnection();
            preparedStatement = SQLUtility.prepare("SELECT player_record FROM server_status LIMIT 1", sqlConnection);

            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) return 0;

            return resultSet.getInt("player_record");
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
            SQLUtility.closeSilently(resultSet);
        }

        return 0;
    }
    
}
