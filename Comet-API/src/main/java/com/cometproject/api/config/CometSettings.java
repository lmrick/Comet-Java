package com.cometproject.api.config;

import com.cometproject.api.game.rooms.filter.FilterMode;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;


public class CometSettings {
    
    public static final int fastFoodGameThreads = 2;
    public static final String fastFoodGameHost = "0.0.0.0";
    public static final short fastFoodGamePort = 30010;
    
    public static final LocalDate date = LocalDate.now();
    public static final Calendar calendar = Calendar.getInstance();
    
    public static final int hour = calendar.get(Calendar.HOUR_OF_DAY);
    public static final int minute = calendar.get(Calendar.MINUTE);
    
    public static final boolean updateDaily = hour == 0 && minute == 0;
    public static final int dailyRespects = 3;
    public static final int dailyScratches = 3;
    
    public static boolean motdEnabled = false;
    public static String motdMessage = "";
    public static String hotelName = "";
    public static String hotelUrl = "";
    public static String aboutImg = "";

    public static boolean onlineRewardEnabled = false;
    public static int onlineRewardCredits = 0;
    public static int onlineRewardDuckets = 0;

    public static int onlineRewardDiamondsInterval = 45;
    public static int onlineRewardDiamonds = 0;
    public static int onlineRewardInterval = 15;
    public static Set<DayOfWeek> onlineRewardDoubleDays = Sets.newHashSet();

    public static int groupCost = 0;

    public static boolean aboutShowPlayersOnline = true;
    public static boolean aboutShowUptime = true;
    public static boolean aboutShowRoomsActive = true;

    public static int floorEditorMaxX = 0;
    public static int floorEditorMaxY = 0;
    public static int floorEditorMaxTotal = 0;

    public static int roomMaxPlayers = 50000;
    public static boolean roomEncryptPasswords = false;
    public static int roomPasswordEncryptionRounds = 10;
    public static boolean roomCanPlaceItemOnEntity = false;
    public static int roomMaxBots = 100;
    public static int roomMaxPets = 100;
    public static int roomIdleMinutes = 20;

    public static FilterMode wordFilterMode = FilterMode.DEFAULT;

    public static boolean useDatabaseIp = false;
    public static boolean saveLogins = false;

    public static boolean playerInfiniteBalance = false;
    public static int playerGiftCooldown = 30;

    public static final Map<String, String> strictFilterCharacters = Maps.newHashMap();
    public static boolean playerFigureValidation = false;
    public static int playerChangeFigureCooldown = 5;

    public static int messengerMaxFriends = 1100;
    public static boolean messengerLogMessages = false;

    public static int CAMERA_COINS_PRICE = 0;
    public static int CAMERA_DUCKETS_PROCEI = 0;
    public static int CAMERA_PHOTO_ITEM_ID = 50001;
    public static String CAMERA_PHOTO_URL = "http://localhost:8080/camera/photos/%photoId%";
    public static String CAMERA_UPLOAD_URL = "http://dev-comet.test/camera/thumbnails/%photoId%";

    public static int roomWiredRewardMinimumRank = 7;
    public static boolean asyncCatalogPurchase = false;

    public static boolean storagePlayerQueueEnabled = false;
    public static boolean storageItemQueueEnabled = false;

    public static boolean adaptiveEntityProcessDelay = false;

    public static int maxConnectionsPerIpAddress = 2;

    public static boolean playerRightsItemPlacement = true;

    public static boolean groupChatEnabled = true;
    public static boolean logCatalogPurchases = false;

    public static boolean hallOfFameEnabled = false;
    public static String hallOfFameCurrency = "";
    public static int hallOfFameRefreshMinutes = 5;
    public static String hallOfFameTextsKey = "";

    public static int wiredMaxEffects = 10;
    public static int wiredMaxTriggers = 10;
    public static int wiredMaxExecuteStacks = 5;
    public static boolean maxConnectionsBlockSuspicious = true;

    private static final Logger log = Logger.getLogger(CometSettings.class.getName());

    public static void setMotd(String motd) {
        motdEnabled = true;
        motdMessage = motd;
    }
    
}
