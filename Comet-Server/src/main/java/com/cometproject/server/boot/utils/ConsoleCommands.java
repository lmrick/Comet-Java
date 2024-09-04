package com.cometproject.server.boot.utils;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.stats.CometStats;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.catalog.CatalogManager;
import com.cometproject.server.game.moderation.BanManager;
import com.cometproject.server.game.navigator.NavigatorManager;
import com.cometproject.server.game.permissions.PermissionsManager;
import com.cometproject.server.modules.ModuleManager;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.storage.SQLUtility;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class ConsoleCommands {
    private static final Logger log = Logger.getLogger("Console Command Handler");

    public static void init() {
        final Thread cmdThr = new Thread(() -> {
						while (Comet.isRunning) {
								if (!Comet.isRunning) {
										break;
								}

								try {
										BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
										String input = br.readLine();

										if (input != null) {
												handleCommand(input);
										}
								} catch (Exception e) {
										log.error("Error while parsing console command");
								}
						}
				});

        cmdThr.start();
    }

    public static void handleCommand(String line) {
        if (line.startsWith("/")) {
					switch (line.split(" ")[0]) {
						default -> log.error("Invalid command");
						case "/query-log" -> SQLUtility.queryLogEnabled = !SQLUtility.queryLogEnabled;
						case "/", "/help", "/commands" ->
										log.info("Commands available: /about, /reload_messages, /gc, /reload_permissions, /changemotd, /reload_catalog, /reload_bans, /reload_locale, /reload_permissions, /queries, /queries");
						case "/reload_modules" -> {
							ModuleManager.getInstance().initialize();
							log.info("Modules reloaded successfully.");
						}
						case "/about" -> {
							final CometStats stats = Comet.getStats();
							
							log.info("This server is powered by Comet (" + Comet.getBuild() + ")");
							log.info("    Players online: " + stats.getPlayers());
							log.info("    Active rooms: " + stats.getRooms());
							log.info("    Uptime: " + stats.getUptime());
							log.info("    Process ID: " + stats.getProcessId());
							log.info("    Memory allocated: " + stats.getAllocatedMemory() + "MB");
							log.info("    Memory usage: " + stats.getUsedMemory() + "MB");
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
							
							for (Map.Entry<String, AtomicInteger> query : SQLUtility.getQueryCounters().entrySet()) {
								log.info("Query:" + query.getKey());
								log.info("Count: " + query.getValue().get());
								log.info("");
							}
						}
						case "/clear_queries" -> {
							SQLUtility.getQueryCounters().clear();
							log.info("Query counters have been cleared.");
						}
					}
        } else {
            log.error("Invalid command");
        }
    }
}
