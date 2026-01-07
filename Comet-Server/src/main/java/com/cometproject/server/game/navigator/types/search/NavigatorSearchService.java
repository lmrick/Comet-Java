package com.cometproject.server.game.navigator.types.search;

import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.groups.types.IGroupData;
import com.cometproject.api.game.players.data.components.messenger.IMessengerFriend;
import com.cometproject.api.game.rooms.IRoomData;
import com.cometproject.api.game.rooms.settings.RoomAccessType;
import com.cometproject.server.game.navigator.NavigatorManager;
import com.cometproject.server.game.navigator.types.categories.Category;
import com.cometproject.server.game.players.types.Player;
import com.cometproject.server.game.rooms.RoomManager;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.network.messages.outgoing.navigator.updated.NavigatorSearchResultSetMessageComposer;
import com.cometproject.server.tasks.CometConstants;
import com.cometproject.server.tasks.ICometTask;
import com.cometproject.common.caching.LastReferenceCache;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public class NavigatorSearchService implements ICometTask {
    private static NavigatorSearchService instance;

    // Executor para requests
    private final Executor searchExecutor = CometConstants.NAVIGATOR_SEARCH_EXECUTOR;

    // Caches
    private final LastReferenceCache<Integer, List<IRoomData>> friendsRoomsCache;
    private final LastReferenceCache<Integer, List<IRoomData>> myGroupsCache;
    private final LastReferenceCache<String, List<Category>> categoryCache;
    private final LastReferenceCache<String, List<IRoomData>> generalRoomsCache;

    // Executor para limpar expirados
    private final ScheduledExecutorService cacheExecutor = Executors.newSingleThreadScheduledExecutor();

    private NavigatorSearchService() {
        friendsRoomsCache = new LastReferenceCache<>(30_000, 5_000, null, cacheExecutor);
        myGroupsCache = new LastReferenceCache<>(30_000, 5_000, null, cacheExecutor);
        categoryCache = new LastReferenceCache<>(30_000, 5_000, null, cacheExecutor);
        generalRoomsCache = new LastReferenceCache<>(30_000, 5_000, null, cacheExecutor);
    }

    public static NavigatorSearchService getInstance() {
        if (instance == null) instance = new NavigatorSearchService();
        return instance;
    }

    @Override
    public void run() {
        // Pode ser usado para atualização periódica de caches globais
    }

    // ===========================
    // Submit Request
    // ===========================
    public void submitRequest(Player player, String category, String data) {
        searchExecutor.execute(() -> {
            String key = category + ":" + data + ":" + player.getId();
            List<Category> categories = categoryCache.get(key);
            if (categories == null) {
                categories = computeCategories(player, category, data);
                categoryCache.add(key, categories);
            }
            player.getSession().send(new NavigatorSearchResultSetMessageComposer(category, data, categories, player));
        });
    }

    private List<Category> computeCategories(Player player, String category, String data) {
        List<Category> categoryList = new ArrayList<>();

        if (!data.isEmpty()) {
            categoryList.addAll(NavigatorManager.getInstance().getCategories().values());
            return categoryList;
        }

        for (Category cat : NavigatorManager.getInstance().getCategories().values()) {
            String type = cat.getCategoryType().toString().toLowerCase();
            if ("myworld_view".equals(category)) {
                switch (type) {
                    case "my_friends_rooms" -> {
                        if (!getFriendsRooms(player).isEmpty()) categoryList.add(cat);
                    }
                    case "with_friends" -> {
                        if (!getFriendsOnlineRooms(player).isEmpty()) categoryList.add(cat);
                    }
                    case "my_groups" -> {
                        if (!getMyGroupsRooms(player).isEmpty()) categoryList.add(cat);
                    }
                    case "with_rights" -> {
                        if (!player.getRoomsWithRights().isEmpty()) categoryList.add(cat);
                    }
                    default -> {
                        if (cat.isVisible()) categoryList.add(cat);
                    }
                }
            } else if (cat.getCategory().equals(category) && cat.isVisible()) {
                categoryList.add(cat);
            }
        }

        return categoryList;
    }

    // ===========================
    // Caches granulares por jogador
    // ===========================
    private List<IRoomData> getFriendsRooms(Player player) {
        List<IRoomData> cached = friendsRoomsCache.get(player.getId());
        if (cached != null) return cached;

        List<IRoomData> rooms = new ArrayList<>();
        if (player.getMessenger() != null) {
            for (IMessengerFriend f : player.getMessenger().getFriends().values()) {
                if (!f.isInRoom()) continue;
                PlayerEntity entity = (PlayerEntity) f.getSession().getPlayer().getEntity();
                if (entity != null && isRoomVisibleToPlayer(entity, player)) rooms.add(entity.getRoom().getData());
            }
        }

        friendsRoomsCache.add(player.getId(), rooms);
        return rooms;
    }

    private List<IRoomData> getFriendsOnlineRooms(Player player) {
        List<IRoomData> cached = friendsRoomsCache.get(player.getId() + 1000000); // key offset
        if (cached != null) return cached;

        List<IRoomData> rooms = new ArrayList<>();
        if (player.getMessenger() != null) {
            for (IMessengerFriend f : player.getMessenger().getFriends().values()) {
                if (!f.isInRoom()) continue;
                PlayerEntity entity = (PlayerEntity) f.getSession().getPlayer().getEntity();
                if (entity != null && !entity.getPlayer().getSettings().getHideOnline() && isRoomVisibleToPlayer(entity, player))
                    rooms.add(entity.getRoom().getData());
            }
        }

        friendsRoomsCache.add(player.getId() + 1000000, rooms);
        return rooms;
    }

    private List<IRoomData> getMyGroupsRooms(Player player) {
        List<IRoomData> cached = myGroupsCache.get(player.getId());
        if (cached != null) return cached;

        List<IRoomData> rooms = new ArrayList<>();
        for (int gid : player.getGroups()) {
            IGroupData groupData = GameContext.getCurrent().getGroupService().getData(gid);
            if (groupData == null) continue;
            IRoomData room = GameContext.getCurrent().getRoomService().getRoomData(groupData.getRoomId());
            if (room != null) rooms.add(room);
        }

        myGroupsCache.add(player.getId(), rooms);
        return rooms;
    }

    // ===========================
    // Room Visibility Check
    // ===========================
    private boolean isRoomVisibleToPlayer(PlayerEntity entity, Player player) {
        if (entity.getRoom().getData().getAccess() == RoomAccessType.INVISIBLE && player.getData().getRank() < 3) {
            if (entity.getRoom().getGroup() != null) return false;
            return entity.getRoom().getRights().hasRights(player.getId());
        }
        return true;
    }

    // ===========================
    // Busca final (search)
    // ===========================
    public List<IRoomData> search(Category category, Player player, boolean expanded) {
        String key = category.getCategoryId() + ":" + player.getId() + ":" + expanded;
        List<IRoomData> cached = generalRoomsCache.get(key);
        if (cached != null) return cached;

        List<IRoomData> rooms = new ArrayList<>();

        switch (category.getCategoryType()) {
            case MY_FRIENDS_ROOMS -> rooms.addAll(getFriendsRooms(player));
            case WITH_FRIENDS -> rooms.addAll(getFriendsOnlineRooms(player));
            case MY_GROUPS -> rooms.addAll(getMyGroupsRooms(player));
            case MY_ROOMS -> {
                if (player.getRooms() != null) {
                    for (int id : player.getRooms()) {
                        IRoomData room = GameContext.getCurrent().getRoomService().getRoomData(id);
                        if (room != null) rooms.add(room);
                    }
                }
            }
            case WITH_RIGHTS -> {
                if (player.getRoomsWithRights() != null) {
                    for (int id : player.getRoomsWithRights()) {
                        IRoomData room = GameContext.getCurrent().getRoomService().getRoomData(id);
                        if (room != null) rooms.add(room);
                    }
                }
            }
            case MY_FAVORITES -> {
                if (player.getNavigator() != null) {
                    for (int id : player.getNavigator().getFavouriteRooms()) {
                        if (rooms.size() >= 50) break;
                        IRoomData room = GameContext.getCurrent().getRoomService().getRoomData(id);
                        if (room != null) rooms.add(room);
                    }
                }
            }
            case POPULAR, CATEGORY, TOP_PROMOTIONS, PUBLIC, STAFF_PICKS -> {
                rooms.addAll(NavigatorManager.getInstance().getRoomsByCategoryAndType(category, player, expanded));
            }
        }

        rooms = orderRooms(rooms, expanded ? category.getRoomCountExpanded() : category.getRoomCount());
        generalRoomsCache.add(key, rooms);
        return rooms;
    }

    private List<IRoomData> orderRooms(List<IRoomData> rooms, int limit) {
        rooms.sort((r1, r2) -> Integer.compare(
                RoomManager.getInstance().isActive(r2.getId()) ? RoomManager.getInstance().get(r2.getId()).getEntities().playerCount() : 0,
                RoomManager.getInstance().isActive(r1.getId()) ? RoomManager.getInstance().get(r1.getId()).getEntities().playerCount() : 0
        ));
        if (rooms.size() > limit) return rooms.subList(0, limit);
        return rooms;
    }
}
