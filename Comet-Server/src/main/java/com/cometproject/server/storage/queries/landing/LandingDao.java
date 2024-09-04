package com.cometproject.server.storage.queries.landing;

import com.cometproject.api.game.players.data.IPlayerAvatar;
import com.cometproject.server.game.landing.types.PromoArticle;
import com.cometproject.server.game.players.data.PlayerAvatarData;
import com.cometproject.server.storage.SQLUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class LandingDao {
    public static Map<Integer, PromoArticle> getArticles() {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<Integer, PromoArticle> data = new HashMap<>();

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT * FROM server_articles WHERE visible = '1'", sqlConnection);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                data.put(resultSet.getInt("id"), new PromoArticle(resultSet));
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

    public static Map<IPlayerAvatar, Integer> getHallOfFame(String currency, int limit) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<IPlayerAvatar, Integer> data = new LinkedHashMap<>();

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT id, username, figure, " + currency + ", gender, motto FROM players WHERE rank <= 3 ORDER BY " + currency + " DESC LIMIT " + limit, sqlConnection);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                data.put(new PlayerAvatarData(resultSet.getInt("id"), resultSet.getString("username"), resultSet.getString("figure"), resultSet.getString("gender"), resultSet.getString("motto"),0), resultSet.getInt(currency));
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
