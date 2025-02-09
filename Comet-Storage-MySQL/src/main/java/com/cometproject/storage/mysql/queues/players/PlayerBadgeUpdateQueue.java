package com.cometproject.storage.mysql.queues.players;

import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import com.cometproject.storage.mysql.queues.MySQLStorageQueue;
import com.cometproject.storage.mysql.queues.players.objects.PlayerBadgeUpdate;

import java.sql.PreparedStatement;
import java.util.concurrent.ScheduledExecutorService;

public class PlayerBadgeUpdateQueue extends MySQLStorageQueue<Integer, PlayerBadgeUpdate> {

    public PlayerBadgeUpdateQueue(long delayMilliseconds, ScheduledExecutorService executorService, MySQLConnectionProvider connectionProvider) {
        super("UPDATE player_badges SET slot = ? WHERE badge_code = ? AND player_id = ?;", delayMilliseconds, executorService, connectionProvider);
    }

    @Override
    protected void processBatch(PreparedStatement preparedStatement, Integer id, PlayerBadgeUpdate object) throws Exception {
        preparedStatement.setInt(1, object.slot());
        preparedStatement.setString(2, object.badgeId());
        preparedStatement.setInt(3, id);

        preparedStatement.addBatch();
    }
}
