package com.cometproject.storage.mysql.queues.types.players;

import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import com.cometproject.storage.mysql.queues.types.players.objects.PlayerBadgeUpdate;
import com.cometproject.storage.mysql.queues.types.MySQLStorageQueue;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PlayerBadgeUpdateQueue extends MySQLStorageQueue<Integer, PlayerBadgeUpdate> {

    public PlayerBadgeUpdateQueue(long delayMilliseconds, MySQLConnectionProvider connectionProvider) {
        super("UPDATE player_badges SET slot = ? WHERE badge_code = ? AND player_id = ?;", delayMilliseconds, connectionProvider);
    }

    @Override
    public void processBatch(PreparedStatement preparedStatement, Integer id, PlayerBadgeUpdate object) throws SQLException {
        preparedStatement.setInt(1, object.slot());
        preparedStatement.setString(2, object.badgeId());
        preparedStatement.setInt(3, id);

        preparedStatement.addBatch();
    }

}
