package com.cometproject.storage.mysql.queues.types;

import com.cometproject.api.utilities.Pair;
import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BlockingMySQLStorageQueue<T, O> extends Thread {
    private static final Logger log = Logger.getLogger(BlockingMySQLStorageQueue.class);
    private final MySQLConnectionProvider connectionProvider;
    private final String batchQuery;
    private final int batchThreshold;
    private final Map<T, O> mapping;
    private final BlockingQueue<Pair<T, O>> queue;
    private final ScheduledExecutorService scheduler;
    private volatile boolean running = true;
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    public BlockingMySQLStorageQueue(final MySQLConnectionProvider connectionProvider, final String batchQuery, final int batchThreshold) {
        this.batchQuery = batchQuery;
        this.connectionProvider = connectionProvider;
        this.batchThreshold = batchThreshold;
        this.queue = new LinkedBlockingQueue<>();
        this.mapping = Maps.newConcurrentMap();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    protected abstract void processBatch(PreparedStatement preparedStatement, T id, O object) throws Exception;

    @Override
    public void run() {
        scheduler.scheduleAtFixedRate(this::flush, 0, 1, TimeUnit.MINUTES);

        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                final Set<Pair<T, O>> entriesToProcess = new HashSet<>();
                queue.drainTo(entriesToProcess, batchThreshold);

                if (!entriesToProcess.isEmpty()) {
                    processEntries(entriesToProcess);
                } else {
                    TimeUnit.MILLISECONDS.sleep(50);
                }
            }

            flush(); // Process remaining entries before shutdown
        } catch (InterruptedException e) {
            log.warn("Queue processing interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Failed to process batch", e);
        } finally {
            scheduler.shutdown();
        }
    }

    public O getQueued(T key) {
        return this.mapping.get(key);
    }

    private void processEntries(Set<Pair<T, O>> entriesToProcess) {
        try (Connection sqlConnection = connectionProvider.getConnection();
             PreparedStatement preparedStatement = sqlConnection.prepareStatement(this.batchQuery)) {

            for (Pair<T, O> obj : entriesToProcess) {
                try {
                    this.mapping.remove(obj.left());
                    this.processBatch(preparedStatement, obj.left(), obj.right());
                } catch (Exception e) {
                    log.error(String.format("Failed to process batch entry for key: %s", obj.left(), e));
                    errorCount.incrementAndGet();
                }
            }

            preparedStatement.executeBatch();
            processedCount.addAndGet(entriesToProcess.size());
        } catch (Exception e) {
            log.error("Failed to prepare batch process", e);
            errorCount.incrementAndGet();
            // Implement retry logic here
        } finally {
            entriesToProcess.clear();
        }
    }

    public void add(T key, O obj) {
        this.mapping.put(key, obj);
        this.queue.add(new Pair<>(key, obj));
    }

    public void addAll(Collection<Pair<T, O>> all) {
        all.forEach(obj -> this.add(obj.left(), obj.right()));
    }

    public void flush() {
        Set<Pair<T, O>> entriesToProcess = new HashSet<>();
        queue.drainTo(entriesToProcess);
        processEntries(entriesToProcess);
    }

    public void shutdown() {
        running = false;
        interrupt();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        flush(); // Ensure all remaining entries are processed
    }

    public long getProcessedCount() {
        return processedCount.get();
    }

    public long getErrorCount() {
        return errorCount.get();
    }

    public int getQueueSize() {
        return queue.size();
    }
}