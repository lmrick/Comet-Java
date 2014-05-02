package com.cometproject.server.storage;

import com.cometproject.server.boot.Comet;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.concurrent.TimeUnit;

public class SqlStorageEngine {
    private static Logger log = Logger.getLogger(SqlStorageEngine.class.getName());
    //private BoneCP connections = null;
    private HikariDataSource connections = null;

    public SqlStorageEngine() {
        checkDriver();

        boolean isConnectionFailed = false;

        try {
            /*BoneCPConfig config = new BoneCPConfig();

            config.setJdbcUrl("jdbc:mysql://" + Comet.getServer().getConfig().get("comet.db.host") + "/" + Comet.getServer().getConfig().get("comet.db.name"));
            config.setUsername(Comet.getServer().getConfig().get("comet.db.username"));
            config.setPassword(Comet.getServer().getConfig().get("comet.db.password"));

            config.setMinConnectionsPerPartition(Integer.parseInt(Comet.getServer().getConfig().get("comet.db.pool.min")));
            config.setMaxConnectionsPerPartition(Integer.parseInt(Comet.getServer().getConfig().get("comet.db.pool.max")));
            config.setPartitionCount(Integer.parseInt(Comet.getServer().getConfig().get("comet.db.pool.count")));

            log.info("Connecting to the MySQL server");
            this.connections = new BoneCP(config);*/

            String[] connectionDetails = Comet.getServer().getConfig().get("comet.db.host").split(":");

            HikariConfig config = new HikariConfig();
            config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
            config.addDataSourceProperty("serverName", connectionDetails[0]);
            config.addDataSourceProperty("port", connectionDetails.length > 1 ? Integer.parseInt(connectionDetails[1]) : 3306);
            config.addDataSourceProperty("databaseName", Comet.getServer().getConfig().get("comet.db.name"));
            config.addDataSourceProperty("user", Comet.getServer().getConfig().get("comet.db.username"));
            config.addDataSourceProperty("password", Comet.getServer().getConfig().get("comet.db.password"));
            config.setMaximumPoolSize(Integer.parseInt(Comet.getServer().getConfig().get("comet.db.pool.max")));
            config.setLeakDetectionThreshold(10000);

            this.connections = new HikariDataSource(config);
        } catch (Exception e) {
            isConnectionFailed = true;
            log.error("Failed to connect to MySQL server", e);
            System.exit(0);
        } finally {
            if (!isConnectionFailed) {
                log.info("Connection to MySQL server was successful");
            }
        }

        SqlHelper.init(this);
    }

    public int getConnectionCount() {
        return 0;
    }

    public HikariDataSource getConnections() {
        return this.connections;
    }

    public PreparedStatement prepare(String query) throws SQLException {
        return prepare(query, false);
    }

    public PreparedStatement prepare(String query, boolean returnKeys) throws SQLException {
        Connection conn = null;

        try {
            conn = this.connections.getConnection();

            if (returnKeys) {
                return conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            } else {
                return conn.prepareStatement(query);
            }

        } catch (SQLException e) {
            log.error("Error while creating prepared statement", e);
        } finally {
            conn.close();
        }

        return null;
    }

    public void execute(String query) {
        PreparedStatement statement = null;
        try {
            statement = this.prepare(query);
            statement.execute();
        } catch (SQLException e) {
            log.error("Error while executing MySQL query", e);
        } finally {
            try {
                if (statement != null) {
                    statement.getConnection().close();
                }
            } catch (SQLException e) { }
        }
    }

    public boolean exists(String query) throws SQLException {
        Connection conn = null;

        try {
            conn = this.connections.getConnection();
            return conn.createStatement().executeQuery(query).next();
        } catch (SQLException e) {
            log.error("Error while executing MySQL query", e);
        } finally {
            conn.close();
        }

        return false;
    }

    public int count(String query) throws SQLException {
        Connection conn = null;

        try {
            conn = this.connections.getConnection();
            PreparedStatement statement = conn.prepareStatement(query);
            return this.count(statement);
        } catch (SQLException e) {
            log.error("Error while creating prepared statement", e);
        } finally {
            conn.close();
        }

        return 0;
    }

    public int count(PreparedStatement statement) {
        int i = 0;

        try {
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                i++;
            }

            try {
                result.close();
            } catch (SQLException e) { }

            return i;
        } catch (SQLException e) {
            log.error("Error while counting entries", e);
        }

        return 0;
    }

    public ResultSet getRow(String query) throws SQLException {
        Connection conn = null;
        try {
            conn = this.connections.getConnection();
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return result;
            }
        } catch (SQLException e) {
            log.error("Error while getting row", e);
        } finally {
            conn.close();
        }

        return null;
    }

    public ResultSet getTable(String query) throws SQLException {
        Connection conn = null;
        try {
            conn = this.connections.getConnection();
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet result = statement.executeQuery();

            return result;
        } catch (SQLException e) {
            log.error("Error while getting table", e);
        } finally {
            conn.close();
        }

        return null;
    }

    public String getString(String query) {
        ResultSet result = null;

        try {
            result = this.prepare(query).executeQuery();
            result.first();

            String str = query.split(" ")[1];

            if (str.startsWith("`")) {
                str = str.substring(1, str.length() - 1);
            }

            return result.getString(str);
        } catch (SQLException e) {
            log.error("Error while getting string", e);
        } finally {
            try {
                if (result != null) { result.close(); }
            } catch (SQLException e) { }
        }

        return null;
    }

    public void checkDriver() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            Comet.exit("The JDBC driver is missing.");
        }
    }
}
