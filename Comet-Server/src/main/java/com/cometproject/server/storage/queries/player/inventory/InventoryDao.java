package com.cometproject.server.storage.queries.player.inventory;

import com.cometproject.api.game.players.data.components.inventory.IPlayerItem;
import com.cometproject.server.game.players.components.types.inventory.InventoryItem;
import com.cometproject.server.storage.SQLUtility;
import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class InventoryDao {
    public static String ITEMS_USERID_INDEX = "";
    private static Logger log = Logger.getLogger(InventoryDao.class.getName());

    public static Map<Long, IPlayerItem> getInventoryByPlayerId(int playerId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<Long, IPlayerItem> data = new HashMap<>();

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = ITEMS_USERID_INDEX.equals("") ?
																SQLUtility.prepare("SELECT i.*, ltd.limited_id, ltd.limited_total FROM items i LEFT JOIN items_limited_edition ltd ON ltd.item_id = i.id WHERE room_id = 0 AND player_id = ? ORDER by id DESC;", sqlConnection)
                    : SQLUtility.prepare("SELECT i.*, ltd.limited_id, ltd.limited_total FROM items i LEFT JOIN items_limited_edition ltd ON ltd.item_id = i.id USE INDEX (" + ITEMS_USERID_INDEX + ") WHERE room_id = 0 AND player_id = ? ORDER by id DESC;", sqlConnection);

            preparedStatement.setInt(1, playerId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                IPlayerItem playerItem = new InventoryItem(resultSet);

                if (playerItem.getDefinition() != null) {
                    data.put(resultSet.getLong("id"), playerItem);
                } else {
                    log.warn("InventoryItem: " + playerItem.getId() + " with invalid definition ID: " + playerItem.getBaseId());
                }
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

    public static String[] getWornBadgesByPlayerId(int playerId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String[] data = new String[5];

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT * FROM player_badges WHERE player_id = ? AND slot != 0 LIMIT 5", sqlConnection);
            preparedStatement.setInt(1, playerId);

            resultSet = preparedStatement.executeQuery();

            int i = 0;

            while (resultSet.next()) {
                data[i++] = resultSet.getString("badge_code");
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

    public static Map<String, Integer> getBadgesByPlayerId(int playerId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<String, Integer> data = new ConcurrentHashMap<>();

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT * FROM player_badges WHERE player_id = ? ORDER BY slot;", sqlConnection);
            preparedStatement.setInt(1, playerId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                data.put(resultSet.getString("badge_code"), resultSet.getInt("slot"));
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

    public static void addBadge(String badge, int playerId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("INSERT INTO player_badges (`player_id`, `badge_code`) VALUES (?, ?)", sqlConnection);
            preparedStatement.setInt(1, playerId);
            preparedStatement.setString(2, badge);

            SQLUtility.executeStatementSilently(preparedStatement, false);
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }

    public static void addBadges(String badge, List<Integer> playerIds) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("INSERT INTO player_badges (`player_id`, `badge_code`) VALUES (?, ?)", sqlConnection);

            for (Integer playerId : playerIds) {
                preparedStatement.setInt(1, playerId);
                preparedStatement.setString(2, badge);

                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);

            playerIds.clear();
        }
    }

    public static void removeBadge(String badge, int playerId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("DELETE FROM player_badges WHERE player_id = ? AND badge_code = ?", sqlConnection);
            preparedStatement.setInt(1, playerId);
            preparedStatement.setString(2, badge);

            SQLUtility.executeStatementSilently(preparedStatement, false);
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }

    public static void updateBadge(String badge, int slot, int playerId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("UPDATE player_badges SET slot = ? WHERE badge_code = ? AND player_id = ?;", sqlConnection);
            preparedStatement.setInt(1, slot);
            preparedStatement.setString(2, badge);
            preparedStatement.setInt(3, playerId);

            SQLUtility.executeStatementSilently(preparedStatement, false);
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }

    public static void clearInventory(int userId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("DELETE FROM items WHERE player_id = ? AND room_id = 0", sqlConnection);
            preparedStatement.setInt(1, userId);

            SQLUtility.executeStatementSilently(preparedStatement, false);
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }
}
