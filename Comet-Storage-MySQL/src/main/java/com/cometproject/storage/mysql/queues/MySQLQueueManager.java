package com.cometproject.storage.mysql.queues;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cometproject.storage.mysql.MySQLStorageContext;
import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import com.cometproject.storage.mysql.queues.types.BlockingMySQLStorageQueue;
import com.cometproject.storage.mysql.queues.types.MySQLStorageQueue;

public class MySQLQueueManager {
    private static MySQLQueueManager instance;
    private final Map<String, MySQLStorageQueue<?, ?>> storageQueues;
    private final Map<String, BlockingMySQLStorageQueue<?, ?>> blockingStorageQueues;
    private final MySQLConnectionProvider connectionProvider;

    public MySQLQueueManager(MySQLConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        this.storageQueues = new ConcurrentHashMap<>();
        this.blockingStorageQueues = new ConcurrentHashMap<>();
    }

    private static MySQLQueueManager getInstance(MySQLConnectionProvider connectionProvider) {
        if(instance == null) {
           return new MySQLQueueManager(connectionProvider);
        }

        return instance;
    }

    public static MySQLQueueManager getInstance() {
        MySQLConnectionProvider connectionProvider = MySQLStorageContext.getCurrentContext().getConnectionProvider();
        return getInstance(connectionProvider);
    }

    public <T, O> void registerQueue(String queueName, MySQLStorageQueue<T, O> queue) {
        storageQueues.put(queueName, queue);
    }

    public <T, O> void removeQueue(T queueName, MySQLStorageQueue<T, O> queue) {
        storageQueues.remove(queueName, queue);
    }

    public <T, O> void registerBlockingQueue(String queueName, BlockingMySQLStorageQueue<T, O> queue) {
        blockingStorageQueues.put(queueName, queue);
        queue.start();
    }

    public MySQLStorageQueue<?, ?> getQueue(String queueName) {
        return storageQueues.get(queueName);
    }

    public BlockingMySQLStorageQueue<?, ?> getBlockingQueue(String queueName) {
        return blockingStorageQueues.get(queueName);
    }

    public void shutdown() {
        storageQueues.values().forEach(MySQLStorageQueue::stop);
        blockingStorageQueues.values().forEach(BlockingMySQLStorageQueue::shutdown);
    }

    public void flushAll() {
        storageQueues.values().forEach(MySQLStorageQueue::processBatch);
        blockingStorageQueues.values().forEach(BlockingMySQLStorageQueue::flush);
    }

    public void initialize() {
        storageQueues.values().forEach(queue -> queue.processBatch());
        blockingStorageQueues.values().forEach(queue -> queue.start());
    }

    public Map<String, Integer> getQueueSizes() {
        Map<String, Integer> sizes = new ConcurrentHashMap<>();
        storageQueues.forEach((key, value) -> sizes.put(key, value.getQueueSize()));
        blockingStorageQueues.forEach((key, value) -> sizes.put(key, value.getQueueSize()));
        
        return sizes;
    }

    public Map<String, Long> getProcessedCounts() {
        Map<String, Long> counts = new ConcurrentHashMap<>();
        storageQueues.forEach((key, value) -> counts.put(key, value.getProcessedCount()));
        blockingStorageQueues.forEach((key, value) -> counts.put(key, value.getProcessedCount()));

        return counts;
    }

    public Map<String, Long> getErrorCounts() {
        Map<String, Long> counts = new ConcurrentHashMap<>();
        storageQueues.forEach((key, value) -> counts.put(key, value.getErrorCount()));
        blockingStorageQueues.forEach((key, value) -> counts.put(key, value.getErrorCount()));

        return counts;
    }

}
