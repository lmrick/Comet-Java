package com.cometproject.server.storage;

import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SQLUtility {
	
	public static boolean queryLogEnabled = false;
	public static Map<Integer, QueryLog> queryLog = new ConcurrentHashMap<>();
	private static MySQLConnectionProvider connectionProvider;
	private static final Logger log = Logger.getLogger(SQLUtility.class.getName());
	private static final Map<String, AtomicInteger> queryCounters = new ConcurrentHashMap<>();
	
	public static void init(MySQLConnectionProvider connectionProvider) {
		SQLUtility.connectionProvider = connectionProvider;
	}
	
	public static Connection getConnection() throws SQLException {
		Connection connection = null;
		
		try {
			connection = connectionProvider.getConnection();
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			log.error("Unexpected error while retrieving connection", e);
		}
		
		return connection;
	}
	
	public static void closeSilently(Connection connection) {
		try {
			if (connection == null) {
				return;
			}
			connection.close();
		} catch (SQLException e) {
			handleSqlException(e);
		}
	}
	
	public static void closeSilently(ResultSet resultSet) {
		try {
			if (resultSet == null) {
				return;
			}
			resultSet.close();
		} catch (SQLException e) {
			handleSqlException(e);
		}
	}
	
	public static void closeSilently(PreparedStatement statement) {
		try {
			if (statement == null) {
				return;
			}
			
			if (queryLogEnabled && queryLog.containsKey(statement.hashCode())) {
				final QueryLog queryLogger = queryLog.get(statement.hashCode());
				final long timeTaken = (System.currentTimeMillis() - queryLogger.startTime);
				
				log.info(MessageFormat.format("[QUERY] {0} took {1}MS", queryLogger.query, timeTaken));
				
				queryLog.remove(statement.hashCode());
			}
			
			statement.close();
		} catch (SQLException e) {
			handleSqlException(e);
		}
	}
	
	public static void executeStatementSilently(PreparedStatement statement, boolean autoClose) {
		try {
			if (statement == null) {
				return;
			}
			
			statement.execute();
			
			if (autoClose) {
				statement.close();
			}
		} catch (SQLException e) {
			handleSqlException(e);
		}
	}
	
	public static PreparedStatement prepare(String query, Connection con) throws SQLException {
		return prepare(query, con, false);
	}
	
	public static PreparedStatement prepare(String query, Connection connection, boolean returnKeys) throws SQLException {
		if (Thread.currentThread().getName().startsWith("Room-Processor")) {
			log.trace(MessageFormat.format("Executing query from room processor: {0}", query));
		}
		
		if (!queryCounters.containsKey(query)) {
			queryCounters.put(query, new AtomicInteger(1));
		} else {
			queryCounters.get(query).incrementAndGet();
		}
		
		final PreparedStatement statement = returnKeys ? connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(query);
		
		if (queryLogEnabled) {
			final QueryLog log = new QueryLog();
			log.query = query;
			queryLog.put(statement.hashCode(), log);
		}
		
		return statement;
	}
	
	public static void handleSqlException(SQLException e) {
		if (e.getMessage().equals("Pool has been shutdown") || e.getMessage().contains("DataWrapper too long for column")) {
			return;
		}
		log.error("Error while executing query", e);
	}
	
	public static String escapeWildcards(String text) {
		return text.replace("_", "\\\\_").replace("%", "\\\\%");
	}
	
	public static Map<String, AtomicInteger> getQueryCounters() {
		return queryCounters;
	}
	
	public static class QueryLog {
		
		public long startTime = System.currentTimeMillis();
		public String query;
		
	}
	
}
