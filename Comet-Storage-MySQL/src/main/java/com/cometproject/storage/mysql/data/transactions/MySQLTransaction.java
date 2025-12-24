package com.cometproject.storage.mysql.data.transactions;

import java.sql.Connection;

public record MySQLTransaction(Connection connection) implements Transaction {
	
	public void startTransaction() throws Exception {
		this.connection.setAutoCommit(false);
	}
	
	public void commit() throws Exception {
		this.connection.commit();
	}
	
	@Override
	public void rollback() throws Exception {
		this.connection.rollback();
	}
	
	@Override
	public void close() throws Exception {
		this.connection.close();
	}
	
}
