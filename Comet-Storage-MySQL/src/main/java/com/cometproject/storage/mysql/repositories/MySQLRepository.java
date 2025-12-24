package com.cometproject.storage.mysql.repositories;

import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import com.cometproject.storage.mysql.data.statements.StatementConsumer;
import com.cometproject.storage.mysql.data.results.IResultReader;
import com.cometproject.storage.mysql.data.results.ResultReaderConsumer;
import com.cometproject.storage.mysql.data.results.ResultSetReader;
import com.cometproject.storage.mysql.data.transactions.MySQLTransaction;
import com.cometproject.storage.mysql.data.transactions.Transaction;
import com.cometproject.storage.mysql.data.transactions.TransactionConsumer;
import org.apache.log4j.Logger;
import java.math.BigDecimal;
import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MySQLRepository {
    protected final Logger log = Logger.getLogger(MySQLRepository.class);
    private final MySQLConnectionProvider connectionProvider;
    private final ConcurrentHashMap<String, PreparedStatement> statementCache;

    public MySQLRepository(MySQLConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        this.statementCache = new ConcurrentHashMap<>();
    }

    public void insertBatch(final String query, StatementConsumer params, ResultReaderConsumer keyConsumer) {
        insertBatch(query, null, params, keyConsumer);
    }

    public void insert(String query, ResultReaderConsumer keyConsumer, Object... parameters) {
        insert(query, keyConsumer, Transaction.NULL, parameters);
    }
      
    public void update(String query, Object... parameters) {
        update(query, null, parameters);
    }

    public void updateBatch(final String query, StatementConsumer params) {
        updateBatch(query, null, params);
    }

    private PreparedStatement getPreparedStatement(Connection connection, String query) throws SQLException {
        return getPreparedStatement(connection, query, Statement.NO_GENERATED_KEYS);
    }

    public void select(String query, ResultReaderConsumer resultConsumer, Object... parameters) {
        try (Connection connection = this.connectionProvider.getConnection();
                PreparedStatement preparedStatement = getPreparedStatement(connection, query);
                ResultSet resultSet = preparedStatement.executeQuery()) {

            this.addParameters(preparedStatement, parameters);

            final IResultReader reader = new ResultSetReader(resultSet);
            while (resultSet.next()) {
                resultConsumer.accept(reader);
            }
        } catch (Exception e) {
            log.error("Failed to select data", e);
        }
    }

    public void update(String query, Transaction transaction, Object... parameters) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean ownConnection = false;
    
        try {
            if (transaction != null) {
                connection = transaction.connection();
            } else {
                connection = this.connectionProvider.getConnection();
                ownConnection = true;
            }
    
            preparedStatement = getPreparedStatement(connection, query);
            this.addParameters(preparedStatement, parameters);
            preparedStatement.executeUpdate();
    
        } catch (Exception e) {
            log.error("Failed to update data", e);
        } finally {
            if (ownConnection) {
                this.connectionProvider.closeConnection(connection);
            }
        }
    }
    
    public void updateBatch(final String query, Transaction transaction, StatementConsumer params) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean ownConnection = false;
    
        try {
            if (transaction != null) {
                connection = transaction.connection();
            } else {
                connection = this.connectionProvider.getConnection();
                ownConnection = true;
            }
    
            preparedStatement = getPreparedStatement(connection, query);
            params.accept(preparedStatement);
            preparedStatement.executeBatch();
        } catch (Exception e) {
            log.error("Failed to update data", e);
        } finally {
            if (ownConnection) {
                this.connectionProvider.closeConnection(connection);
            }
        }
    }
    
    public void insertBatch(final String query, Transaction transaction, StatementConsumer params,
            ResultReaderConsumer keyConsumer) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        boolean ownConnection = false;
    
        try {
            if (transaction != null) {
                connection = transaction.connection();
            } else {
                connection = this.connectionProvider.getConnection();
                ownConnection = true;
            }
    
            preparedStatement = getPreparedStatement(connection, query, Statement.RETURN_GENERATED_KEYS);
            params.accept(preparedStatement);
            preparedStatement.executeBatch();
            resultSet = preparedStatement.getGeneratedKeys();
    
            final IResultReader resultReader = new ResultSetReader(resultSet);
            while (resultSet.next()) {
                keyConsumer.accept(resultReader);
            }
        } catch (Exception e) {
            log.error("Failed to update data", e);
        } finally {
            this.connectionProvider.closeResults(resultSet);
            if (ownConnection) {
                this.connectionProvider.closeConnection(connection);
            }
        }
    }
    
    public void insert(String query, ResultReaderConsumer keyConsumer, Transaction transaction, Object... parameters) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        boolean ownConnection = false;
    
        try {
            if (transaction == Transaction.NULL) {
                connection = this.connectionProvider.getConnection();
                ownConnection = true;
            } else {
                connection = transaction.connection();
            }
    
            // Use getPreparedStatement method for caching
            preparedStatement = getPreparedStatement(connection, query, Statement.RETURN_GENERATED_KEYS);
    
            this.addParameters(preparedStatement, parameters);
    
            preparedStatement.execute();
    
            resultSet = preparedStatement.getGeneratedKeys();
            final IResultReader resultReader = new ResultSetReader(resultSet);
    
            if (resultSet.next()) {
                keyConsumer.accept(resultReader);
            }
        } catch (Exception e) {
            log.error("Failed to insert data", e);
        } finally {
            this.connectionProvider.closeResults(resultSet);
            // Don't close the prepared statement as it's now cached
            if (ownConnection) {
                this.connectionProvider.closeConnection(connection);
            }
        }
    }    

    private PreparedStatement getPreparedStatement(Connection connection, String query, int autoGeneratedKeys) throws SQLException {
        String cacheKey = query + "_" + autoGeneratedKeys;
        PreparedStatement stmt = statementCache.get(cacheKey);
        if (stmt == null || stmt.isClosed()) {
            stmt = connection.prepareStatement(query, autoGeneratedKeys);
            statementCache.put(cacheKey, stmt);
        } else {
            stmt.clearParameters();
        }
        return stmt;
    }

    public void transaction(TransactionConsumer transactionConsumer) {
        Transaction transaction = null;
        try {
            transaction = new MySQLTransaction(this.connectionProvider.getConnection());
            transaction.startTransaction();
            transactionConsumer.accept(transaction);
            transaction.commit();
        } catch (Exception e) {
            log.error("Failed to run transaction, rolling back", e);
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    log.error("Failed to rollback transaction", rollbackEx);
                }
            }
            throw new RuntimeException("Transaction failed", e);
        } finally {
            if (transaction != null) {
                try {
                    transaction.close();
                } catch (Exception closeEx) {
                    log.error("Failed to close transaction", closeEx);
                }
            }
        }
    }

    public void clearStatementCache() {
        for (PreparedStatement stmt : statementCache.values()) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.error("Error closing cached statement", e);
            }
        }
        statementCache.clear();
    }

    public void close() {
        clearStatementCache();
    }

    private void addParameters(PreparedStatement preparedStatement, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            setParameter(preparedStatement, i + 1, parameters[i]);
        }
    }

    private void setParameter(PreparedStatement preparedStatement, int index, Object parameter) throws SQLException {
        if (parameter == null) {
            preparedStatement.setNull(index, Types.NULL);
        } else if (parameter instanceof Integer) {
            preparedStatement.setInt(index, (Integer) parameter);
        } else if (parameter instanceof String) {
            preparedStatement.setString(index, (String) parameter);
        } else if (parameter instanceof Long) {
            preparedStatement.setLong(index, (Long) parameter);
        } else if (parameter instanceof Boolean) {
            preparedStatement.setBoolean(index, (Boolean) parameter);
        } else if (parameter instanceof Double) {
            preparedStatement.setDouble(index, (Double) parameter);
        } else if (parameter instanceof Float) {
            preparedStatement.setFloat(index, (Float) parameter);
        } else if (parameter instanceof BigDecimal) {
            preparedStatement.setBigDecimal(index, (BigDecimal) parameter);
        } else if (parameter instanceof Date) {
            preparedStatement.setDate(index, (Date) parameter);
        } else if (parameter instanceof Timestamp) {
            preparedStatement.setTimestamp(index, (Timestamp) parameter);
        } else if (parameter instanceof byte[]) {
            preparedStatement.setBytes(index, (byte[]) parameter);
        } else {
            throw new SQLException("Unsupported parameter type: " + parameter.getClass().getName());
        }
    }

}
