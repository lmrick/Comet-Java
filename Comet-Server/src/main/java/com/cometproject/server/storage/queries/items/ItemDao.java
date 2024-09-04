package com.cometproject.server.storage.queries.items;

import com.cometproject.api.game.furniture.types.CrackableReward;
import com.cometproject.api.game.furniture.types.CrackableRewardType;
import com.cometproject.api.game.furniture.types.CrackableType;
import com.cometproject.api.game.furniture.types.IFurnitureDefinition;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.game.items.types.ItemDefinition;
import com.cometproject.server.storage.SQLUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class ItemDao {
    public static Map<Integer, IFurnitureDefinition> getDefinitions() {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<Integer, IFurnitureDefinition> data = new HashMap<>();

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT * FROM furniture", sqlConnection);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
//                if(resultSet.getString("id").length() > 9) continue;
                try {
                    data.put(resultSet.getInt("id"), new ItemDefinition(resultSet));
                } catch (Exception e) {
                    ItemManager.getInstance().getLogger().warn("Error while loading item definition for ID: " + resultSet.getInt("id"), e);
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

    public static Map<Integer, CrackableReward> getCrackableRewards() {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<Integer, CrackableReward> data = new HashMap<>();

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT * FROM items_crackable_rewards", sqlConnection);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                data.put(resultSet.getInt("item_id"),
                        new CrackableReward(resultSet.getInt("hit_requirement"),
                                CrackableRewardType.valueOf(resultSet.getString("reward_type")),
                                CrackableType.valueOf(resultSet.getString("crackable_type")),
                                resultSet.getString("reward_data"), resultSet.getInt("reward_data_int")));
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
