package com.cometproject.server.storage.queries.player.relationships;

import com.cometproject.api.game.players.data.components.messenger.RelationshipLevel;
import com.cometproject.server.storage.SQLUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class RelationshipDao {
    public static Map<Integer, RelationshipLevel> getRelationshipsByPlayerId(int playerId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<Integer, RelationshipLevel> data = new ConcurrentHashMap<>();

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT `partner`, `level` FROM player_relationships WHERE player_id = ?", sqlConnection);
            preparedStatement.setInt(1, playerId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                data.put(resultSet.getInt("partner"), RelationshipLevel.valueOf(resultSet.getString("level").toUpperCase()));
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

    public static void deleteRelationship(int playerId, int partner) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("DELETE FROM player_relationships WHERE player_id = ? AND partner = ?", sqlConnection);
            preparedStatement.setInt(1, playerId);
            preparedStatement.setInt(2, partner);

            SQLUtility.executeStatementSilently(preparedStatement, false);
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }

    public static void updateRelationship(int playerId, int partner, String level) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("UPDATE player_relationships SET level = ? WHERE player_id = ? AND partner = ?", sqlConnection);

            preparedStatement.setString(1, level);
            preparedStatement.setInt(2, playerId);
            preparedStatement.setInt(3, partner);

            SQLUtility.executeStatementSilently(preparedStatement, false);
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }

    public static void emptyRelationship(int playerId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("DELETE FROM player_relationships WHERE player_id = ?", sqlConnection);
            preparedStatement.setInt(1, playerId);

            SQLUtility.executeStatementSilently(preparedStatement, false);
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }

    public static void createRelationship(int playerId, int partner, String status) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("INSERT into player_relationships (`player_id`, `level`, `partner`) VALUES(?, ?, ?);", sqlConnection);
            preparedStatement.setInt(1, playerId);
            preparedStatement.setString(2, status);
            preparedStatement.setInt(3, partner);

            SQLUtility.executeStatementSilently(preparedStatement, false);
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }
}
