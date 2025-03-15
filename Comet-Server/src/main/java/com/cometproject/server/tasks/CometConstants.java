package com.cometproject.server.tasks;

import com.cometproject.api.config.Configuration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CometConstants {

    public static final int LOW_PRIORITY_ITEM_PROCESS_TIME_MS = 250;
    public static final long GUIDE_HELP_REQUEST_DELAY = 1000L;
    public static final int ROOM_CYCLE_PERIOD = 500;
    public static final int ROOM_CYCLE_FLAG = 2000;
    public static final int ITEM_PROCESS_COMPONENT_INTERVAL = 500;
    public static final int ITEM_PROCESS_COMPONENT_FLAG = 400;
    
    public static final ExecutorService PLAYER_LOGIN_EXECUTOR = Executors.newFixedThreadPool((int) Configuration.currentConfig().getOrDefault("comet.player.login.threads", 4));
    public static final ExecutorService NAVIGATOR_SEARCH_EXECUTOR = Executors.newFixedThreadPool((int) Configuration.currentConfig().getOrDefault("comet.navigator.search.threads", 8));
    public static final ExecutorService COMMAND_EXECUTOR = Executors.newFixedThreadPool((int) Configuration.currentConfig().getOrDefault("comet.command.threads", 2));
    public static final ExecutorService CATALOG_PURCHASE_EXECUTOR = Executors.newFixedThreadPool((int) Configuration.currentConfig().getOrDefault("comet.catalog.purchase.threads", 2));
    public static final int ROOM_LOAD_EXECUTOR = (int) Configuration.currentConfig().getOrDefault("comet.room.load.threads", 2);
    public static final ExecutorService BOT_PATHFINDER_EXECUTOR = Executors.newFixedThreadPool((int) Configuration.currentConfig().getOrDefault("comet.bot.pathfinder.threads", 2));

}
