package com.cometproject.server.storage.queries.player;

import com.cometproject.server.boot.Comet;
import com.cometproject.server.storage.SQLUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class PlayerAccessDao {

    public static void saveAccess(int playerId, String hardwareId, String ipAddress) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("INSERT INTO `player_access` (player_id, hardware_id, ip_address, timestamp) VALUES (?, ?, ?, ?);", sqlConnection);

            preparedStatement.setInt(1, playerId);
            preparedStatement.setString(2, hardwareId);
            preparedStatement.setString(3, ipAddress);
            preparedStatement.setInt(4, (int) Comet.getTime());

            preparedStatement.execute();
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }

}
