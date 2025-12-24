package com.cometproject.server.boot.utils;

import com.cometproject.api.game.GameContext;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.logging.LogService;
import com.cometproject.server.logging.database.queries.LogQueries;
import com.cometproject.server.storage.StorageManager;
import com.cometproject.server.storage.queries.system.StatisticsDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShutdownProcess {
	
	private static final Logger log = LogManager.getLogger(ShutdownProcess.class.getName());
	
	public static void init() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(false)));
	}
	
	public static void shutdown(boolean exit) {
		log.info("Comet is now shutting down");
		
		Comet.isRunning = false;
		
		log.info("Resetting statistics");
		StatisticsDao.saveStatistics(0, 0, Comet.getBuild());
		
		if (LogService.ENABLED) {
			log.info("Updating room entry data");
			LogQueries.updateRoomEntries();
		}
		
		log.info("Closing all database connections");
		
		GameContext.setCurrent(null);
		StorageManager.getInstance().shutdown();
		
		if (exit) {
			System.exit(0);
		}
	}
	
}
