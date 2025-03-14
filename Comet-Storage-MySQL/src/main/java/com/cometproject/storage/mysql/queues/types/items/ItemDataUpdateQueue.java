package com.cometproject.storage.mysql.queues.types.items;

import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import com.cometproject.storage.mysql.queues.types.BlockingMySQLStorageQueue;

import java.sql.PreparedStatement;

public class ItemDataUpdateQueue extends BlockingMySQLStorageQueue<Long, String> {

    public ItemDataUpdateQueue(int batchThreshold, MySQLConnectionProvider connectionProvider) {
        super(connectionProvider, "UPDATE items SET extra_data = ? WHERE id = ?;", batchThreshold);
    }

    @Override
    public void processBatch(PreparedStatement preparedStatement, Long id, String object) throws Exception {
        preparedStatement.setString(1, object);
        preparedStatement.setLong(2, id);

        preparedStatement.addBatch();
    }
    
}
