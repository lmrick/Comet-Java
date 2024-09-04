package com.cometproject.server.storage.queries.items;

import com.cometproject.api.game.rooms.objects.data.LimitedEditionItemData;
import com.cometproject.server.storage.SQLUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class LimitedEditionDao {
    public static void save(LimitedEditionItemData item) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("INSERT INTO items_limited_edition (`item_id`, `limited_id`, `limited_total`) VALUES(?, ?, ?);", sqlConnection);
            preparedStatement.setLong(1, item.getItemId());
            preparedStatement.setInt(2, item.getLimitedRare());
            preparedStatement.setInt(3, item.getLimitedRareTotal());

            preparedStatement.execute();
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }

    public static LimitedEditionItemData get(long itemId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT `limited_id`, `limited_total` FROM items_limited_edition WHERE item_id = ? LIMIT 1;", sqlConnection);
            preparedStatement.setLong(1, itemId);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return new LimitedEditionItemData(itemId, resultSet.getInt("limited_id"), resultSet.getInt("limited_total"));
            }
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(resultSet);
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }

        return null;
    }
}
