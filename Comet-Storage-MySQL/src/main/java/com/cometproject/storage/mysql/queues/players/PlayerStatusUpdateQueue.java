package com.cometproject.storage.mysql.queues.players;

import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import com.cometproject.storage.mysql.queues.MySQLStorageQueue;
import com.cometproject.storage.mysql.queues.players.objects.PlayerStatusUpdate;

import java.sql.PreparedStatement;
import java.util.concurrent.ScheduledExecutorService;

public class PlayerStatusUpdateQueue extends MySQLStorageQueue<Integer, PlayerStatusUpdate> {
    public PlayerStatusUpdateQueue(long delayMilliseconds, ScheduledExecutorService executorService, MySQLConnectionProvider connectionProvider) {
        super("UPDATE players SET online = ?, last_ip = ?, last_online = ? WHERE id = ?;", delayMilliseconds, executorService, connectionProvider);
    }

    @Override
    public void processBatch(PreparedStatement preparedStatement, Integer id, PlayerStatusUpdate object) throws Exception {
        preparedStatement.setString(1, object.playerOnline() ? "1" : "0");
        preparedStatement.setString(2, object.ipAddress());
        preparedStatement.setLong(3, System.currentTimeMillis() / 1000L);
        preparedStatement.setInt(4, object.playerId());

        preparedStatement.addBatch();
    }
}
