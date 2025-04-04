package com.cometproject.server.logging.database;

import com.cometproject.server.logging.database.queries.LogQueries;
import com.cometproject.server.storage.SQLUtility;
import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LogDatabaseHelper {
	private static final Logger log = Logger.getLogger(LogDatabaseHelper.class.getName());
	
	public static void init() {
		LogQueries.updateRoomEntries();
	}
	
	public static Connection getConnection() throws SQLException {
		return SQLUtility.getConnection();
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
	
	public static PreparedStatement prepare(String query, Connection connection) throws SQLException {
		return prepare(query, connection, false);
	}
	
	public static PreparedStatement prepare(String query, Connection connection, boolean returnKeys) throws SQLException {
		return returnKeys ? connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(query);
	}
	
	public static void handleSqlException(SQLException e) {
		log.error("Error while executing query", e);
	}
	
}
