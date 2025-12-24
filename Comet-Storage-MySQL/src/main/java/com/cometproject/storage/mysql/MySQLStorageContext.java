package com.cometproject.storage.mysql;

import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;

public record MySQLStorageContext(MySQLConnectionProvider connectionProvider) {
	
	private static MySQLStorageContext currentContext;
	
	public static MySQLStorageContext getCurrentContext() {
		return currentContext;
	}
	
	public static void setCurrentContext(MySQLStorageContext storageContext) {
		currentContext = storageContext;
	}
	
}
