package com.cometproject.server.storage.queries.permissions;

import com.cometproject.server.game.permissions.types.CommandPermission;
import com.cometproject.server.game.permissions.types.OverrideCommandPermission;
import com.cometproject.server.game.permissions.types.Perk;
import com.cometproject.server.game.permissions.types.Rank;
import com.cometproject.server.storage.SQLUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class PermissionsDao {

    public static Map<Integer, Perk> getPerks() {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<Integer, Perk> data = new ConcurrentHashMap<>();

        try {
            sqlConnection = SQLUtility.getConnection();

            preparedStatement = SQLUtility.prepare("SELECT `id`, `title`, `data`, `override_rank`, `override_default`, `min_rank` FROM permission_perks", sqlConnection);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                final int id = resultSet.getInt("id");
                final String title = resultSet.getString("title");
                final String dataStr = resultSet.getString("data");
                final int rank = resultSet.getInt("min_rank");
                final boolean overrideRank = resultSet.getString("override_rank").equals("1");
                final boolean overrideDefault = resultSet.getString("override_default").equals("1");

                data.put(resultSet.getInt("id"), new Perk(id, title, dataStr, rank, overrideRank, overrideDefault));
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

    public static Map<Integer, Rank> getRankPermissions() {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<Integer, Rank> data = new ConcurrentHashMap<>();

        try {

            sqlConnection = SQLUtility.getConnection();
            preparedStatement = SQLUtility.prepare("SELECT * FROM server_permissions_ranks", sqlConnection);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                data.putIfAbsent(resultSet.getInt("id"), new Rank(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("flood_bypass").equals("1"),
                        resultSet.getInt("flood_time"),
                        resultSet.getString("disconnectable").equals("1"),
                        resultSet.getString("mod_tool").equals("1"),
                        resultSet.getString("bannable").equals("1"),
                        resultSet.getString("room_kickable").equals("1"),
                        resultSet.getString("room_full_control").equals("1"),
                        resultSet.getString("room_mute_bypass").equals("1"),
                        resultSet.getString("room_filter_bypass").equals("1"),
                        resultSet.getString("room_ignorable").equals("1"),
                        resultSet.getString("room_enter_full").equals("1"),
                        resultSet.getString("room_enter_locked").equals("1"),
                        resultSet.getString("room_staff_pick").equals("1"),
                        resultSet.getString("room_see_whispers").equals("1"),
                        resultSet.getString("messenger_staff_chat").equals("1"),
                        resultSet.getString("messenger_log_chat").equals("1"),
                        resultSet.getInt("messenger_max_friends"),
                        resultSet.getString("about_detailed").equals("1"),
                        resultSet.getString("about_stats").equals("1"),
                        resultSet.getString("login_notif").equals("1"),
                        resultSet.getString("name_prefix")));
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

    public static Map<String, CommandPermission> getCommandPermissions() {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<String, CommandPermission> data = new ConcurrentHashMap<>();

        try {
            sqlConnection = SQLUtility.getConnection();
            preparedStatement = SQLUtility.prepare("SELECT `command_id`, `minimum_rank`, `vip_only`, `rights_only` FROM permission_commands", sqlConnection);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                try {
                    final String commandId = resultSet.getString("command_id");
                    final int minimumRank = resultSet.getInt("minimum_rank");
                    final boolean vipOnly = resultSet.getString("vip_only").equals("1");
                    final boolean rightsOnly = resultSet.getString("rights_only").equals("1");

                    data.putIfAbsent(resultSet.getString("command_id"),
                            new CommandPermission(commandId, minimumRank, vipOnly, rightsOnly));
                } catch (Exception ignored) {

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

    public static Map<String, OverrideCommandPermission> getOverrideCommandPermissions() {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<String, OverrideCommandPermission> data = new ConcurrentHashMap<>();

        try {
            sqlConnection = SQLUtility.getConnection();
            preparedStatement = SQLUtility.prepare("SELECT `id`, `command_id`, `player_id`, `enabled` FROM permission_command_overrides", sqlConnection);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                try {
                    final String commandId = resultSet.getString("command_id");
                    final int playerId = resultSet.getInt("player_id");
                    final boolean enabled = resultSet.getString("enabled").equals("1");
                    data.putIfAbsent(resultSet.getString("command_id"), new OverrideCommandPermission(commandId, playerId, enabled));
                } catch (Exception ignored) {

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

    public static Map<Integer, Integer> getEffects() {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<Integer, Integer> data = new ConcurrentHashMap<>();

        try {
            sqlConnection = SQLUtility.getConnection();
            preparedStatement = SQLUtility.prepare("SELECT * FROM permission_effects", sqlConnection);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                data.put(resultSet.getInt("effect_id"), resultSet.getInt("minimum_rank"));
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

    public static Map<Integer, Integer> getChatBubbles() {
        Connection sqlConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Map<Integer, Integer> data = new ConcurrentHashMap<>();

        try {
            sqlConnection = SQLUtility.getConnection();
            preparedStatement = SQLUtility.prepare("SELECT `bubble_id`, `minimum_rank` FROM permission_chat_bubbles", sqlConnection);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                try {
                    data.put(resultSet.getInt("bubble_id"), resultSet.getInt("minimum_rank"));
                } catch (Exception ignored) {

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
}
