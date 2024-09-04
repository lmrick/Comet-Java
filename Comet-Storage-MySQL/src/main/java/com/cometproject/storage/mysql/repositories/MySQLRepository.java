package com.cometproject.storage.mysql.repositories;

import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import com.cometproject.storage.mysql.data.statements.StatementConsumer;
import com.cometproject.storage.mysql.data.results.IResultReader;
import com.cometproject.storage.mysql.data.results.ResultReaderConsumer;
import com.cometproject.storage.mysql.data.results.ResultSetReader;
import com.cometproject.storage.mysql.data.transactions.MySQLTransaction;
import com.cometproject.storage.mysql.data.transactions.Transaction;
import com.cometproject.storage.mysql.data.transactions.TransactionConsumer;
import com.cometproject.storage.mysql.repositories.exceptions.UnexpectedTypeException;
import org.apache.log4j.Logger;
import java.sql.*;

public abstract class MySQLRepository {
    protected final Logger log = Logger.getLogger(MySQLRepository.class);
    private final MySQLConnectionProvider connectionProvider;

    public MySQLRepository(MySQLConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public void select(String query, ResultReaderConsumer resultConsumer, Object... parameters) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.connectionProvider.getConnection();
            preparedStatement = connection.prepareStatement(query);

            this.addParameters(preparedStatement, parameters);

            resultSet = preparedStatement.executeQuery();

            final IResultReader reader = new ResultSetReader(resultSet);

            while (resultSet.next()) {
                resultConsumer.accept(reader);
            }
        } catch (Exception e) {
            log.error("Failed to select data", e);
        } finally {
            this.connectionProvider.closeResults(resultSet);
            this.connectionProvider.closeStatement(preparedStatement);
            this.connectionProvider.closeConnection(connection);
        }
    }

    public void update(String query, Object... parameters) {
        update(query, Transaction.NULL, parameters);
    }

    public void update(String query, Transaction transaction, Object... parameters) {
        Connection connection = transaction != null ? transaction.getConnection() : null;
        PreparedStatement preparedStatement = null;

        try {
            if (connection == null)
                connection = this.connectionProvider.getConnection();

            preparedStatement = connection.prepareStatement(query);

            this.addParameters(preparedStatement, parameters);

            // We could return or accept a consumer of the affected rows or something?
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            log.error("Failed to update data", e);
        } finally {
            this.connectionProvider.closeStatement(preparedStatement);

            if (transaction == null)
                this.connectionProvider.closeConnection(connection);
        }
    }

    public void updateBatch(final String query, Transaction transaction, StatementConsumer params) {
        Connection connection = transaction != null ? transaction.getConnection() : null;
        PreparedStatement preparedStatement = null;

        try {
            if (connection == null)
                connection = this.connectionProvider.getConnection();

            preparedStatement = connection.prepareStatement(query);

            params.accept(preparedStatement);

            // We could return or accept a consumer of the affected rows or something?
            preparedStatement.executeBatch();
        } catch (Exception e) {
            log.error("Failed to update data", e);
        } finally {
            this.connectionProvider.closeStatement(preparedStatement);

            if (transaction == null)
                this.connectionProvider.closeConnection(connection);
        }
    }

    public void updateBatch(final String query, StatementConsumer params) {
        updateBatch(query, null, params);
    }

    public void insertBatch(final String query, Transaction transaction, StatementConsumer params, ResultReaderConsumer keyConsumer) {
        Connection connection = transaction != null ? transaction.getConnection() : null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            if (connection == null)
                connection = this.connectionProvider.getConnection();

            preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

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
            this.connectionProvider.closeStatement(preparedStatement);

            if (transaction == null)
                this.connectionProvider.closeConnection(connection);
        }
    }

    public void insertBatch(final String query, StatementConsumer params, ResultReaderConsumer keyConsumer) {
        insertBatch(query, null, params, keyConsumer);
    }

    public void insert(String query, ResultReaderConsumer keyConsumer, Object... parameters) {
        insert(query, keyConsumer, Transaction.NULL, parameters);
    }

    public void insert(String query, ResultReaderConsumer keyConsumer, Transaction transaction, Object... parameters) {
        Connection connection = transaction == Transaction.NULL ? null : transaction.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            if (connection == null) {
                connection = this.connectionProvider.getConnection();
            }

            preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            this.addParameters(preparedStatement, parameters);

            preparedStatement.execute();

            resultSet = preparedStatement.getGeneratedKeys();
            final IResultReader resultReader = new ResultSetReader(resultSet);

            if (resultSet.next()) {
                keyConsumer.accept(resultReader);
            }
        } catch (Exception e) {
            log.error("Failed to update data", e);
        } finally {
            this.connectionProvider.closeResults(resultSet);
            this.connectionProvider.closeStatement(preparedStatement);

            if (transaction == null) {
                this.connectionProvider.closeConnection(connection);
            }
        }
    }

    public void transaction(TransactionConsumer transactionConsumer) {
        Transaction transaction = null;

        try {
            transaction = new MySQLTransaction(this.connectionProvider.getConnection());

            // Start the transaction (configure the connection to not autoCommit)
            transaction.startTransaction();

            transactionConsumer.accept(transaction);
        } catch (Exception e) {
            try {
                if (transaction != null) {
                    transaction.rollback();

                    // TODO: make sure we perform any checks here for if the connection is closed, we can't have
                    //       connection leaks!
                }
            } catch (Exception ex) {
                log.error("Failed to rollback transaction", ex);
            }

            log.error("Failed to run transaction, rolling back", e);
        } finally {
            try {
                if (transaction != null)
                    transaction.getConnection().close();
            } catch (SQLException e) {
                log.error("Failed to close connection");
            }
        }
    }
    
    /**
     * Dynamically sets parameters to the prepared statement
     *
     * @param preparedStatement The statement of which to set the parameters
     * @param parameters        List of parameters defined as objects
     * @throws Exception Exception when setting the parameters
     */
    private void addParameters(PreparedStatement preparedStatement, Object... parameters) throws Exception {
        int parameterIndex = 1;
        for (Object obj : parameters) {
            if (obj instanceof Integer) {
                preparedStatement.setInt(parameterIndex++, (Integer) obj);
            } else if (obj instanceof String) {
                preparedStatement.setString(parameterIndex++, (String) obj);
            } else if (obj instanceof Long) {
                preparedStatement.setLong(parameterIndex++, (Long) obj);
            } else if (obj instanceof Boolean) {
                preparedStatement.setBoolean(parameterIndex++, (Boolean) obj);
            } else if (obj instanceof Double) {
                preparedStatement.setDouble(parameterIndex++, (Double) obj);
            } else {
                if (obj == null) {
                    preparedStatement.setString(parameterIndex++, null);
                    continue;
                }
                throw new UnexpectedTypeException("You can only bind types: Integer, String, Boolean, Long and Double to a statement!");
            }
        }
    }
}
