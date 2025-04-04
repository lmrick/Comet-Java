package com.cometproject.server.storage.queries.items;

import com.cometproject.server.storage.SQLUtility;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class TradeDao {

    public static void updateTradeItems(int userId, long itemId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("UPDATE items SET player_id = ? WHERE id = ?", sqlConnection);

            preparedStatement.setInt(1, userId);
            preparedStatement.setLong(2, itemId);

            SQLUtility.executeStatementSilently(preparedStatement, false);
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }

    public static void updateTradeItems(Map<Long, Integer> itemsToUpdate) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("UPDATE items SET player_id = ? WHERE id = ?;", sqlConnection);

            for (Map.Entry<Long, Integer> item : itemsToUpdate.entrySet()) {
                preparedStatement.setInt(1, item.getValue());
                preparedStatement.setLong(2, item.getKey());

                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }
}
