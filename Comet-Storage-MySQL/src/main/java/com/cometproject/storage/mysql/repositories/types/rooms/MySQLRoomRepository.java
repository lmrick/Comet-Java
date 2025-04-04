package com.cometproject.storage.mysql.repositories.types.rooms;

import com.cometproject.api.game.rooms.IRoomData;
import com.cometproject.api.game.rooms.models.RoomModelData;
import com.cometproject.api.game.rooms.settings.*;
import com.cometproject.api.utilities.JsonUtil;
import com.cometproject.storage.api.repositories.IRoomRepository;
import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import com.cometproject.storage.mysql.data.results.IResultReader;
import com.cometproject.storage.api.factories.rooms.RoomDataFactory;
import com.cometproject.storage.api.factories.rooms.RoomModelDataFactory;
import com.cometproject.storage.mysql.repositories.MySQLRepository;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MySQLRoomRepository extends MySQLRepository implements IRoomRepository {
    private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>() { }.getType();
    private final RoomDataFactory roomDataFactory;
    private final RoomModelDataFactory roomModelDataFactory;

    public MySQLRoomRepository(RoomDataFactory roomDataFactory, RoomModelDataFactory roomModelDataFactory, MySQLConnectionProvider connectionProvider) {
        super(connectionProvider);

        this.roomDataFactory = roomDataFactory;
        this.roomModelDataFactory = roomModelDataFactory;
    }

    @Override
    public void getAllModels(Consumer<Map<String, RoomModelData>> modelConsumer) {
        final Map<String, RoomModelData> roomModels = Maps.newHashMap();

        select("SELECT * FROM room_models", data -> {
            final var id = data.readString("id");
            final var heightmap = data.readString("heightmap");
            final var doorX = data.readInteger("door_x");
            final var doorY = data.readInteger("door_y");
            final var doorRotation = data.readInteger("door_dir");

            roomModels.put(id, this.roomModelDataFactory.createData(id, heightmap, doorX, doorY, doorRotation));
        });

        modelConsumer.accept(roomModels);
    }

    @Override
    public void getRoomDataById(int roomId, Consumer<IRoomData> dataConsumer) {
        select("SELECT * FROM rooms WHERE id = ? LIMIT 1;", data -> {
            final IRoomData roomData = readRoomData(data);

            if (roomData != null) {
                dataConsumer.accept(roomData);
            }
        }, roomId);
    }

    @Override
    public void updateRoom(IRoomData data) {
        var tagString = new StringBuilder();

        for (var i = 0; i < data.getTags().length; i++) {
            if (i != 0) {
                tagString.append(",");
            }
            tagString.append(data.getTags()[i]);
        }

        update("UPDATE rooms SET name = ?, " +
                        "description = ?, " +
                        "owner_id = ?, " +
                        "owner = ?, " +
                        "category = ?, " +
                        "max_users = ?, " +
                        "max_bots = ?, " +
                        "max_pets = ?, " +
                        "access_type = ?, " +
                        "password = ?, " +
                        "score = ?, " +
                        "tags = ?, " +
                        "decorations = ?, " +
                        "model = ?, " +
                        "hide_walls = ?, " +
                        "thickness_wall = ?, " +
                        "thickness_floor = ?, " +
                        "allow_walkthrough = ?, " +
                        "allow_pets = ?, " +
                        "heightmap = ?, " +
                        "mute_state = ?, " +
                        "ban_state = ?, " +
                        "kick_state = ?, " +
                        "bubble_mode = ?, " +
                        "bubble_type = ?, " +
                        "bubble_scroll = ?, " +
                        "chat_distance = ?, " +
                        "flood_level = ?, " +
                        "trade_state = ?, " +
                        "disabled_commands = ?, " +
                        "group_id = ?, " +
                        "required_badge = ?, " +
                        "thumbnail = ?, " +
                        "hide_wired = ? " +
                        "WHERE id = ?;",
                data.getName(),
                data.getDescription(),
                data.getOwnerId(),
                data.getOwner(),
                data.getCategoryId(),
                data.getMaxUsers(),
                data.getMaxBots(),
                data.getMaxPets(),
                data.getAccess().toString().toLowerCase(),
                data.getPassword(),
                data.getScore(),
                tagString.toString(),
                data.getDecorationString(),
                data.getModel(),
                data.getHideWalls() ? "1" : "0",
                data.getWallThickness(),
                data.getFloorThickness(),
                data.isAllowWalkthrough() ? "1" : "0",
                data.isAllowPets() ? "1" : "0",
                data.getHeightmap(),
                data.getMuteState().toString(),
                data.getBanState().toString(),
                data.getKickState().toString(),
                data.getBubbleMode(),
                data.getBubbleType(),
                data.getBubbleScroll(),
                data.getChatDistance(),
                data.getAntiFloodSettings(),
                data.getTradeState().toString(),
                data.getDisabledCommands().isEmpty() ? null : JsonUtil.getInstance().toJson(data.getDisabledCommands()),
                data.getGroupId(),
                data.getRequiredBadge(),
                data.getThumbnail(),
                data.isWiredHidden() ? "1" : "0",
                data.getId());
    }

    private IRoomData readRoomData(final IResultReader room) throws Exception {
        final var id = room.readInteger("id");
        final var type = RoomType.valueOf(room.readString("type"));
        final var name = room.readString("name");
        final var description = room.readString("description");
        final var ownerId = room.readInteger("owner_id");
        final var owner = room.readString("owner");
        final var category = room.readInteger("category");
        final var maxUsers = room.readInteger("max_users");
        final var maxBots = room.readInteger("max_bots");
        final var maxPets = room.readInteger("max_pets");
        final var thumbnail = room.readString("thumbnail");

        var accessTypeString = room.readString("access_type");

        if (!accessTypeString.equals("open") 
        && !accessTypeString.equals("doorbell") 
        && !accessTypeString.equals("password")
        && !accessTypeString.equals("invisible")) {
            accessTypeString = "open";
        }

        final var password = room.readString("password");
        final var access = RoomAccessType.valueOf(accessTypeString.toUpperCase());
        final var originalPassword = password;

        final var score = room.readInteger("score");

        final String[] tags = room.readString("tags").isEmpty() 
        ? new String[0] : room.readString("tags").split(",");

        final Map<String, String> decorations = new HashMap<>();

        String[] decorationsArray = room.readString("decorations").split(",");
        for (var i = 0; i < decorationsArray.length; i++) {
            String[] decoration = decorationsArray[i].split("=");
            if (decoration.length == 2) decorations.put(decoration[0], decoration[1]);
        }

        final var model = room.readString("model");

        final var hideWalls = room.readString("hide_walls").equals("1");
        final var thicknessWall = room.readInteger("thickness_wall");
        final var thicknessFloor = room.readInteger("thickness_floor");
        final var allowWalkthrough = room.readString("allow_walkthrough").equals("1");
        final var allowPets = room.readString("allow_pets").equals("1");
        final var heightmap = room.readString("heightmap");
        final var tradeState = RoomTradeState.valueOf(room.readString("trade_state"));

        final var kickState = RoomKickState.valueOf(room.readString("kick_state"));
        final var banState = RoomBanState.valueOf(room.readString("ban_state"));
        final var muteState = RoomMuteState.valueOf(room.readString("mute_state"));

        final var bubbleMode = room.readInteger("bubble_mode");
        final var bubbleScroll = room.readInteger("bubble_scroll");
        final var bubbleType = room.readInteger("bubble_type");
        final var antiFloodSettings = room.readInteger("flood_level");
        final var chatDistance = room.readInteger("chat_distance");

        final List<String> disabledCommands = JsonUtil.getInstance().fromJson(room.readString("disabled_commands"), STRING_LIST_TYPE);
        final var groupId = room.readInteger("group_id");
        final var requiredBadge = room.readString("required_badge");
        final var wiredHidden = room.readBoolean("hide_wired");

        return this.roomDataFactory.createRoomData(id, type, name, description, ownerId, 
        owner, category, maxUsers, maxBots, maxPets,
         access, password, originalPassword, tradeState, score, tags, decorations, model, 
         hideWalls, thicknessWall, thicknessFloor,
         allowWalkthrough, allowPets, heightmap, muteState, kickState, 
         banState, bubbleMode, bubbleType,
         bubbleScroll, chatDistance, antiFloodSettings, 
         disabledCommands == null ? Lists.newArrayList() : disabledCommands,
         groupId, requiredBadge, thumbnail, wiredHidden);
    }

}
