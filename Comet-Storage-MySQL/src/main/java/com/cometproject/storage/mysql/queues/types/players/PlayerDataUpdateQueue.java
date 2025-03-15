package com.cometproject.storage.mysql.queues.types.players;

import com.cometproject.api.game.players.data.IPlayerData;
import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import com.cometproject.storage.mysql.queues.types.MySQLStorageQueue;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PlayerDataUpdateQueue extends MySQLStorageQueue<Integer, IPlayerData> {
    
    public PlayerDataUpdateQueue(long delayMilliseconds, MySQLConnectionProvider connectionProvider) {
        super("UPDATE players SET username = ?, motto = ?, figure = ?, credits = ?, vip_points = ?, gender = ?, favourite_group = ?, activity_points = ?, quest_id = ?, achievement_points = ? WHERE id = ?;", delayMilliseconds, connectionProvider);
    }

    @Override
    public void processBatch(PreparedStatement preparedStatement, Integer id, IPlayerData player) throws SQLException {
        preparedStatement.setString(1, player.getUsername());
        preparedStatement.setString(2, player.getMotto());
        preparedStatement.setString(3, player.getFigure());
        preparedStatement.setInt(4, player.getCredits());
        preparedStatement.setInt(5, player.getVipPoints());
        preparedStatement.setString(6, player.getGender());
        preparedStatement.setInt(7, player.getFavouriteGroup());
        preparedStatement.setInt(8, player.getActivityPoints());
        preparedStatement.setInt(9, player.getQuestId());
        preparedStatement.setInt(10, player.getAchievementPoints());
        preparedStatement.setInt(11, player.getId());

        preparedStatement.addBatch();
    }
}
