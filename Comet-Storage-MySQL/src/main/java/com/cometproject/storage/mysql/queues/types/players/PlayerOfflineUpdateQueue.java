package com.cometproject.storage.mysql.queues.types.players;

import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import com.cometproject.storage.mysql.queues.types.MySQLStorageQueue;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PlayerOfflineUpdateQueue extends MySQLStorageQueue<Integer, Object> {

    public PlayerOfflineUpdateQueue(long delayMilliseconds, MySQLConnectionProvider connectionProvider) {
        super("UPDATE players SET online = '0' WHERE id = ?;", delayMilliseconds, connectionProvider);
    }

    @Override
    public void processBatch(PreparedStatement preparedStatement, Integer id, Object object) throws SQLException {
        preparedStatement.setInt(1, id);

        preparedStatement.addBatch();
    }
}
