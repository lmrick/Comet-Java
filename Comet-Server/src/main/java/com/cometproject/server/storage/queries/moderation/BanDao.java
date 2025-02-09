package com.cometproject.server.storage.queries.moderation;

import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.moderation.types.Ban;
import com.cometproject.server.game.moderation.types.BanType;
import com.cometproject.server.storage.SQLUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class BanDao {
    public static Map<String, Ban> getActiveBans() {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<String, Ban> data = new ConcurrentHashMap<>();

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT * FROM bans WHERE expire = 0 OR expire > " + Comet.getTime(), sqlConnection);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                data.put(resultSet.getString("data"), new Ban(resultSet));
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

    public static int createBan(BanType type, long length, long expire, String data, int addedBy, String reason) {

        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("INSERT into bans (`type`, `expire`, `data`, `reason`, `added_by`) VALUES(?, ?, ?, ?, ?);", sqlConnection, true);

            preparedStatement.setString(1, type.toString().toLowerCase());
            preparedStatement.setLong(2, length == 0 ? 0 : expire);
            preparedStatement.setString(3, data);
            preparedStatement.setString(4, reason);
            preparedStatement.setInt(5, addedBy);

            SQLUtility.executeStatementSilently(preparedStatement, false);
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

    public static void deleteBan(String data) {

        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("DELETE FROM bans WHERE data = ?", sqlConnection, true);

            preparedStatement.setString(1, data);

            SQLUtility.executeStatementSilently(preparedStatement, false);
            resultSet = preparedStatement.getGeneratedKeys();

        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(resultSet);
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }

    }
}
