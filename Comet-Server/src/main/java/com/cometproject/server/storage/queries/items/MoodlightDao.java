package com.cometproject.server.storage.queries.items;

import com.cometproject.api.utilities.JsonUtil;
import com.cometproject.server.game.rooms.objects.items.data.MoodLightData;
import com.cometproject.server.game.rooms.objects.items.data.MoodLightPresetData;
import com.cometproject.server.game.rooms.objects.items.types.wall.MoodLightWallItem;
import com.cometproject.server.storage.SQLUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class MoodlightDao {
    public static MoodLightData getMoodlightData(long itemId) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        List<MoodLightPresetData> presets = new ArrayList<>();
        MoodLightData data = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT * FROM items_moodlight WHERE item_id = ?", sqlConnection);
            preparedStatement.setLong(1, itemId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.isBeforeFirst()) {
                if (resultSet.next()) {
                    String preset1 = resultSet.getString("preset_1");
                    String preset2 = resultSet.getString("preset_2");
                    String preset3 = resultSet.getString("preset_3");

                    if (!preset1.equals("")) {
                        presets.add(JsonUtil.getInstance().fromJson(preset1, MoodLightPresetData.class));
                    }
                    if (!preset2.equals("")) {
                        presets.add(JsonUtil.getInstance().fromJson(preset2, MoodLightPresetData.class));
                    }
                    if (!preset3.equals("")) {
                        presets.add(JsonUtil.getInstance().fromJson(preset3, MoodLightPresetData.class));
                    }

                    data = new MoodLightData(resultSet.getString("enabled").equals("1"), resultSet.getInt("active_preset"), presets);
                }
            } else {
                presets.add(new MoodLightPresetData(true, "#000000", 255));
                presets.add(new MoodLightPresetData(true, "#000000", 255));
                presets.add(new MoodLightPresetData(true, "#000000", 255));

                preparedStatement = SQLUtility.prepare("INSERT INTO items_moodlight (item_id,enabled,active_preset,preset_1,preset_2,preset_3) VALUES (?,?,?,?,?,?);", sqlConnection);
                preparedStatement.setLong(1, itemId);
                preparedStatement.setString(2, "0");
                preparedStatement.setString(3, "1");
                preparedStatement.setString(4, JsonUtil.getInstance().toJson(presets.get(0)));
                preparedStatement.setString(5, JsonUtil.getInstance().toJson(presets.get(1)));
                preparedStatement.setString(6, JsonUtil.getInstance().toJson(presets.get(2)));

                preparedStatement.execute();

                data = new MoodLightData(false, 1, presets);
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

    public static void updateMoodlight(MoodLightWallItem item) {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("UPDATE items_moodlight SET enabled = ?, active_preset = ?, preset_1 = ?, preset_2 = ?, preset_3 = ? WHERE item_id = ?", sqlConnection);
            preparedStatement.setString(1, item.getMoodlightData().isEnabled() ? "1" : "0");
            preparedStatement.setInt(2, item.getMoodlightData().getActivePreset());
            preparedStatement.setString(3, JsonUtil.getInstance().toJson(item.getMoodlightData().getPresets().get(0)));
            preparedStatement.setString(4, JsonUtil.getInstance().toJson(item.getMoodlightData().getPresets().get(1)));
            preparedStatement.setString(5, JsonUtil.getInstance().toJson(item.getMoodlightData().getPresets().get(2)));
            preparedStatement.setLong(6, item.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } finally {
            SQLUtility.closeSilently(preparedStatement);
            SQLUtility.closeSilently(sqlConnection);
        }
    }
}
