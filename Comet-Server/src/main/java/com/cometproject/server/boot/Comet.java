package com.cometproject.server.boot;

import com.cometproject.api.stats.CometStats;
import com.cometproject.server.boot.utils.ConsoleCommands;
import com.cometproject.server.boot.utils.ShutdownProcess;
import com.cometproject.server.game.rooms.RoomManager;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.tasks.CometRuntime;
import com.cometproject.server.utilities.TimeSpan;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Comet {
	
	public static String instanceId = UUID.randomUUID().toString();
	public static long start;
	public static volatile boolean isDebugging = false;
	public static volatile boolean isRunning = true;
	public static boolean daemon = false;
	private static final Logger log = Logger.getLogger(Comet.class.getName());
	private static CometServer server;
	
	public static void run(String[] args) {
		start = System.currentTimeMillis();
		
		try {
			PropertyConfigurator.configure(new FileInputStream("./config/log4j.properties"));
		} catch (Exception e) {
			log.error("Error while loading log4j configuration", e);
			return;
		}
		
		log.info("Comet Server - " + getBuild());
		log.info("  /$$$$$$                                      /$$    ");
		log.info(" /$$__  $$                                    | $$    ");
		log.info("| $$  \\__/  /$$$$$$  /$$$$$$/$$$$   /$$$$$$  /$$$$$$  ");
		log.info("| $$       /$$__  $$| $$_  $$_  $$ /$$__  $$|_  $$_/  ");
		log.info("| $$      | $$  \\ $$| $$ \\ $$ \\ $$| $$$$$$$$  | $$    ");
		log.info("| $$    $$| $$  | $$| $$ | $$ | $$| $$_____/  | $$ /$$");
		log.info("|  $$$$$$/|  $$$$$$/| $$ | $$ | $$|  $$$$$$$  |  $$$/");
		log.info(" \\______/  \\______/ |__/ |__/ |__/ \\_______/   \\___/  ");
		
		if (ManagementFactory.getRuntimeMXBean().getInputArguments().stream().anyMatch(arg -> arg.contains("dt_"))) {
			isDebugging = true;
		}
		
		Level logLevel = Level.INFO;
		
		if (args.length < 1) {
			log.debug("No config args found, falling back to default configuration!");
			server = new CometServer(null);
		} else {
			var cometConfiguration = new HashMap<String, String>();
			var arguments = new ArrayList<String>();
			Arrays.stream(args).forEachOrdered(arg -> {
				if (arg.contains(" ")) {
					final String[] splitString = arg.split(" ");
					arguments.addAll(Arrays.asList(splitString));
				} else {
					arguments.add(arg);
				}
			});
			
			for (String arg : arguments) {
				if (arg.equals("--debug-logging")) logLevel = Level.TRACE;
				if (arg.equals("--daemon")) daemon = true;
				if (arg.startsWith("--instance-name=")) instanceId = arg.replace("--instance-name=", "");
				if (!arg.contains("=")) continue;
				String[] splitArgs = arg.split("=");
				cometConfiguration.put(splitArgs[0], splitArgs.length != 1 ? splitArgs[1] : "");
			}
			
			server = new CometServer(cometConfiguration);
		}
		
		Logger.getRootLogger().setLevel(logLevel);
		server.init();
		
		if (!daemon) {
			ConsoleCommands.init();
		}
		
		ShutdownProcess.init();
	}
	
	public static void exit(String message) {
		log.error(MessageFormat.format("Comet has shutdown. Reason: \"{0}\"", message));
		System.exit(0);
	}
	
	public static long getTime() {
		return (System.currentTimeMillis() / 1000L);
	}
	
	public static String getDate() {
		return new SimpleDateFormat("HH:mm:ss").format(new Date());
	}
	
	public static String getBuild() {
		return Comet.class.getPackage().getImplementationVersion() == null ? "Comet-DEV" : Comet.class.getPackage().getImplementationVersion();
	}
	
	public static CometStats getStats() {
		CometStats statsInstance = new CometStats();
		
		statsInstance.setPlayers(NetworkManager.getInstance().getSessions().getUsersOnlineCount());
		statsInstance.setRooms(RoomManager.getInstance().getRoomInstances().size());
		statsInstance.setUptime(TimeSpan.millisecondsToDate(System.currentTimeMillis() - Comet.start));
		
		statsInstance.setProcessId(CometRuntime.processId);
		statsInstance.setAllocatedMemory((Runtime.getRuntime().totalMemory() / 1024) / 1024);
		statsInstance.setUsedMemory(statsInstance.getAllocatedMemory() - (Runtime.getRuntime().freeMemory() / 1024) / 1024);
		statsInstance.setOperatingSystem(MessageFormat.format("{0} ({1})", CometRuntime.OPERATING_SYSTEM, CometRuntime.OPERATING_SYSTEM_ARCHITECTURE));
		statsInstance.setCpuCores(Runtime.getRuntime().availableProcessors());
		
		return statsInstance;
	}
	
	public static CometServer getServer() {
		return server;
	}
	
}
