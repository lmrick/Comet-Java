package com.cometproject.storage.mysql.queues.types.players;

import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import com.cometproject.storage.mysql.queues.types.players.objects.PlayerStatusUpdate;
import com.cometproject.storage.mysql.queues.types.MySQLStorageQueue;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PlayerStatusUpdateQueue extends MySQLStorageQueue<Integer, PlayerStatusUpdate> {
    
    public PlayerStatusUpdateQueue(long delayMilliseconds, MySQLConnectionProvider connectionProvider) {
        super("UPDATE players SET online = ?, last_ip = ?, last_online = ? WHERE id = ?;", delayMilliseconds, connectionProvider);
    }

    @Override
    public void processBatch(PreparedStatement preparedStatement, Integer id, PlayerStatusUpdate object) throws SQLException {
        preparedStatement.setString(1, object.playerOnline() ? "1" : "0");
        preparedStatement.setString(2, object.ipAddress());
        preparedStatement.setLong(3, System.currentTimeMillis() / 1000L);
        preparedStatement.setInt(4, object.playerId());

        preparedStatement.addBatch();
    }

}
