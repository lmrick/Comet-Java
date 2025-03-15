package com.cometproject.storage.mysql.queues.types;

import com.cometproject.api.utilities.Pair;
import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public abstract class MySQLStorageQueue<T, O> {
    private static final Logger log = Logger.getLogger(MySQLStorageQueue.class);

    private final String batchQuery;
    private final Map<T, O> storageQueue;
    private final ScheduledExecutorService executorService;
    private final MySQLConnectionProvider dataSource;
    private final AtomicBoolean running;
    private final AtomicLong processedCount;
    private final AtomicLong errorCount;

    public MySQLStorageQueue(String batchQuery, long delayMilliseconds, final MySQLConnectionProvider dataSource) {
        this.batchQuery = batchQuery;
        this.storageQueue = new ConcurrentHashMap<>();
        this.dataSource = dataSource;
        this.running = new AtomicBoolean(false);
        this.processedCount = new AtomicLong(0);
        this.errorCount = new AtomicLong(0);

        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(this::processBatch, 
            delayMilliseconds + ThreadLocalRandom.current().nextLong(1000, 5000),
            delayMilliseconds, 
            TimeUnit.MILLISECONDS);
    }

    public abstract void processBatch(PreparedStatement preparedStatement, T id, O object) throws SQLException;

    public void add(T key, O obj) {
        this.storageQueue.put(key, obj);
    }

    public void addAll(Collection<Pair<T, O>> all) {
        all.forEach(obj -> this.add(obj.left(), obj.right()));
    }

    public void processBatch() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        try (Connection sqlConnection = this.dataSource.getConnection();
             PreparedStatement preparedStatement = sqlConnection.prepareStatement(this.batchQuery)) {

            for (Map.Entry<T, O> obj : this.storageQueue.entrySet()) {
                try {
                    this.processBatch(preparedStatement, obj.getKey(), obj.getValue());
                    processedCount.incrementAndGet();
                } catch (SQLException e) {
                    log.error("Error processing batch entry", e);
                    errorCount.incrementAndGet();
                }
            }

            preparedStatement.executeBatch();
            this.storageQueue.clear();

        } catch (Exception e) {
            log.error("Error executing batch", e);
            errorCount.incrementAndGet();
        } finally {
            running.set(false);
        }
    }

    public O getQueued(T obj) {
        return this.storageQueue.get(obj);
    }

    public void stop() {
        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                this.executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.executorService.shutdownNow();
        }
        
        if (!storageQueue.isEmpty()) {
            processBatch();
        }
    }

    public long getProcessedCount() {
        return processedCount.get();
    }

    public long getErrorCount() {
        return errorCount.get();
    }

    public int getQueueSize() {
        return storageQueue.size();
    }
}