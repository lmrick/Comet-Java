package com.cometproject.server.boot.utils;

import com.cometproject.api.config.CometSettings;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.catalog.CatalogManager;
import com.cometproject.server.game.moderation.BanManager;
import com.cometproject.server.game.navigator.NavigatorManager;
import com.cometproject.server.game.permissions.PermissionsManager;
import com.cometproject.server.modules.ModuleManager;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.storage.SQLUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleCommands {
	
	private static final Logger log = LogManager.getLogger("Console Command Handler");
	
	public static void init() {
		final var cmdThr = new Thread(ConsoleCommands::run);
		cmdThr.start();
	}
	
	public static void handleCommand(String line) {
		if (line.startsWith("/")) {
			switch (line.split(" ")[0]) {
				case "/query-log" -> SQLUtility.queryLogEnabled = !SQLUtility.queryLogEnabled;
				
				case "/", "/help", "/commands" ->
								log.info("Commands available: /about, /reload_messages, /gc, /reload_permissions, /changemotd, /reload_catalog, /reload_bans, /reload_locale, /reload_permissions, /queries, /queries");
				
				case "/reload_modules" -> {
					ModuleManager.getInstance().initialize();
					log.info("Modules reloaded successfully.");
				}
				
				case "/about" -> {
					final var stats = Comet.getStats();
					final var aboutLog = """
														This server is powered by Comet ({})
														Players online: {}
														Active rooms: {}
														Uptime: {}
														Process ID: {}
														Memory allocated: {} MB
														Memory usage: {} MB
														CPU Cores: {}
														OS: {}
														""";
					log.info(aboutLog, Comet.getBuild(),
									stats.getPlayers(), stats.getRooms(),
									stats.getUptime(), stats.getProcessId(),
									stats.getAllocatedMemory(), stats.getUsedMemory(),
									stats.getCpuCores(), stats.getOperatingSystem()
					);
				}
				case "/reload_messages" -> {
					NetworkManager.getInstance().getMessages().load();
					log.info("Message handlers were reloaded");
				}
				case "/gc" -> {
					System.gc();
					log.info("GC was run");
				}
				case "/changemotd" -> {
					String motd = line.replace("/changemotd ", "");
					CometSettings.setMotd(motd);
					log.info("Message of the day was set.");
				}
				case "/reload_permissions" -> {
					PermissionsManager.getInstance().loadCommands();
					PermissionsManager.getInstance().loadOverrideCommands();
					PermissionsManager.getInstance().loadPerks();
					PermissionsManager.getInstance().loadRankPermissions();
					log.info("Permissions cache was reloaded.");
				}
				case "/reload_catalog" -> {
					CatalogManager.getInstance().loadItemsAndPages();
					CatalogManager.getInstance().loadGiftBoxes();
					log.info("Catalog cache was reloaded.");
				}
				case "/reload_bans" -> {
					BanManager.getInstance().loadBans();
					log.info("Bans were reloaded.");
				}
				case "/reload_navigator" -> {
					NavigatorManager.getInstance().loadPublicRooms();
					NavigatorManager.getInstance().loadCategories();
					log.info("Navigator was reloaded.");
				}
				case "/reload_locale" -> {
					Locale.initialize();
					log.info("Locale configuration was reloaded.");
				}
				case "/queries" -> {
					
					log.info("Queries");
					log.info("================================================");
					
					SQLUtility.getQueryCounters().forEach((key, value) -> {
						log.info("Query:{}", key);
						log.info("Count: {}", value.get());
						log.info("");
					});
				}
				case "/clear_queries" -> {
					SQLUtility.getQueryCounters().clear();
					log.info("Query counters have been cleared.");
				}
				default -> log.error("Invalid command");
				
			}
		} else {
			log.error("Invalid command");
		}
	}
	
	private static void run() {
		while (Comet.isRunning) {
			if (!Comet.isRunning) {
				break;
			}
			
			try {
				var br = new BufferedReader(new InputStreamReader(System.in));
				var input = br.readLine();
				
				if (input != null) {
					handleCommand(input);
				}
			} catch (Exception e) {
				log.error("Error while parsing console command");
			}
		}
	}
	
}
