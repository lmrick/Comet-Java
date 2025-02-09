package com.cometproject.server.storage.queries.items;

import com.cometproject.server.storage.SQLUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class TeleporterDao {

    public static long getPairId(long id) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT * FROM items_teles WHERE id_one = ? LIMIT 1;", sqlConnection);
            preparedStatement.setLong(1, id);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getLong("id_two");
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

    public static void savePair(long item1, long item2) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("INSERT into items_teles (id_one, id_two) VALUES(?, ?);", sqlConnection);
            preparedStatement.setLong(1, item1);
            preparedStatement.setLong(2, item2);

            preparedStatement.addBatch();

            preparedStatement.setLong(1, item2);
            preparedStatement.setLong(2, item1);

            preparedStatement.addBatch();

            preparedStatement.executeBatch();
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }
}
