package com.cometproject.storage.mysql.queues;

import com.cometproject.api.game.GameContext;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class BlockingMySQLStorageQueue<T, O> extends Thread {
	private static final Logger log = Logger.getLogger(BlockingMySQLStorageQueue.class);
	private final MySQLConnectionProvider connectionProvider;
	private final String batchQuery;
	private final int batchThreshold;
	private final Map<T, O> mapping;
	private final BlockingQueue<Pair<T, O>> queue;
	
	public BlockingMySQLStorageQueue(final MySQLConnectionProvider connectionProvider, final String batchQuery, final int batchThreshold) {
		this.batchQuery = batchQuery;
		this.connectionProvider = connectionProvider;
		this.batchThreshold = batchThreshold;
		this.queue = new ArrayBlockingQueue<>(25000);
		this.mapping = Maps.newConcurrentMap();
	}
	
	protected abstract void processBatch(PreparedStatement preparedStatement, T id, O object) throws Exception;
	
	@Override
	public void run() {
		try {
			Thread.sleep(10000);
			
			final Set<Pair<T, O>> entriesToProcess = new HashSet<>();
			while (GameContext.getCurrent() != null) {
				
				for (int i = 0; i < batchThreshold; i++) {
					final Pair<T, O> entry = this.queue.poll(50, TimeUnit.MILLISECONDS);
					if (entry != null) {
						entriesToProcess.add(entry);
					}
				}
				
				this.processEntries(entriesToProcess);
			}
			
			while (!this.queue.isEmpty()) {
				final Pair<T, O> entry = this.queue.poll(50, TimeUnit.MILLISECONDS);
				
				if (entry != null) {
					entriesToProcess.add(entry);
				}
				
				this.processEntries(entriesToProcess);
			}
		} catch (Exception e) {
			log.error("Failed to process batch", e);
		}
	}
	
	public O getQueued(T key) {
		return this.mapping.get(key);
	}
	
	private void processEntries(Set<Pair<T, O>> entriesToProcess) {
		Connection sqlConnection = null;
		PreparedStatement preparedStatement = null;
		
		try {
			sqlConnection = this.connectionProvider.getConnection();
			
			preparedStatement = sqlConnection.prepareStatement(this.batchQuery);
			
			for (Pair<T, O> obj : entriesToProcess) {
				try {
					this.mapping.remove(obj.left());
					this.processBatch(preparedStatement, obj.left(), obj.right());
				} catch (Exception e) {
					log.error("Failed to process batch entry", e);
				}
			}
			
			preparedStatement.executeBatch();
			entriesToProcess.clear();
		} catch (Exception e) {
			log.error("Failed to prepare batch process");
		} finally {
			this.connectionProvider.closeStatement(preparedStatement);
			this.connectionProvider.closeConnection(sqlConnection);
		}
	}
	
	public void add(T key, O obj) {
		this.mapping.put(key, obj);
		this.queue.add(new Pair<>(key, obj));
	}
	
	public void addAll(Collection<Pair<T, O>> all) {
		all.forEach(obj -> this.add(obj.left(), obj.right()));
	}
	
}
