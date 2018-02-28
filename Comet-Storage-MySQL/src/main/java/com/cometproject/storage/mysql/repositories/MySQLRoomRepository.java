package com.cometproject.storage.mysql.repositories;

import com.cometproject.api.game.rooms.IRoom;
import com.cometproject.api.game.rooms.IRoomData;
import com.cometproject.api.game.rooms.RoomType;
import com.cometproject.api.game.rooms.models.RoomModelData;
import com.cometproject.api.game.rooms.settings.*;
import com.cometproject.storage.api.repositories.IRoomRepository;
import com.cometproject.storage.mysql.MySQLConnectionProvider;
import com.cometproject.storage.mysql.data.results.IResultReader;
import com.cometproject.storage.mysql.data.results.ResultSetReader;
import com.cometproject.storage.mysql.models.factories.rooms.RoomDataFactory;
import com.cometproject.storage.mysql.models.factories.rooms.RoomModelDataFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MySQLRoomRepository extends MySQLRepository implements IRoomRepository {
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

        select("SELECT * FROM room_models", (data) -> {
            final String id = data.readString("id");
            final String heightmap = data.readString("heightmap");
            final int doorX = data.readInteger("door_x");
            final int doorY = data.readInteger("door_y");
            final int doorRotation = data.readInteger("door_dir");

            roomModels.put(id, this.roomModelDataFactory.createData(id, heightmap, doorX, doorY, doorRotation));
        });

        modelConsumer.accept(roomModels);
    }

    @Override
    public void getRoomDataById(int roomId, Consumer<IRoomData> dataConsumer) {
        select("SELECT * FROM rooms WHERE id = ? LIMIT 1;", (data) -> {
            final IRoomData roomData = readRoomData(data);

            if(roomData != null) {
                dataConsumer.accept(roomData);
            }
        }, roomId);
    }

    @Override
    public void updateRoom(IRoomData data) {

    }

    private IRoomData readRoomData(final IResultReader room) throws Exception {
        final int id = room.readInteger("id");
        final RoomType type = RoomType.valueOf(room.readString("type"));
        final String name = room.readString("name");
        final String description = room.readString("description");
        final int ownerId = room.readInteger("owner_id");
        final String owner = room.readString("owner");
        final int category = room.readInteger("category");
        final int maxUsers = room.readInteger("max_users");
        final String thumbnail = room.readString("thumbnail");

        String accessTypeString = room.readString("access_type");

        if (!accessTypeString.equals("open") && !accessTypeString.equals("doorbell") && !accessTypeString.equals("password")) {
            accessTypeString = "open";
        }

        final String password = room.readString("password");
        final RoomAccessType access = RoomAccessType.valueOf(accessTypeString.toUpperCase());
        final String originalPassword = password;

        final int score = room.readInteger("score");

        final String[] tags = room.readString("tags").isEmpty() ? new String[0] :
                room.readString("tags").split(",");

        final Map<String, String> decorations = new HashMap<>();

        String[] decorationsArray = room.readString("decorations").split(",");

        for (int i = 0; i < decorationsArray.length; i++) {
            String[] decoration = decorationsArray[i].split("=");

            if (decoration.length == 2)
                decorations.put(decoration[0], decoration[1]);
        }

        final String model = room.readString("model");

        final boolean hideWalls = room.readString("hide_walls").equals("1");
        final int thicknessWall = room.readInteger("thickness_wall");
        final int thicknessFloor = room.readInteger("thickness_floor");
        final boolean allowWalkthrough = room.readString("allow_walkthrough").equals("1");
        final boolean allowPets = room.readString("allow_pets").equals("1");
        final String heightmap = room.readString("heightmap");
        final RoomTradeState tradeState = RoomTradeState.valueOf(room.readString("trade_state"));

        final RoomKickState kickState = RoomKickState.valueOf(room.readString("kick_state"));
        final RoomBanState banState = RoomBanState.valueOf(room.readString("ban_state"));
        final RoomMuteState muteState = RoomMuteState.valueOf(room.readString("mute_state"));

        final int bubbleMode = room.readInteger("bubble_mode");
        final int bubbleScroll = room.readInteger("bubble_scroll");
        final int bubbleType = room.readInteger("bubble_type");
        final int antiFloodSettings = room.readInteger("flood_level");
        final int chatDistance = room.readInteger("chat_distance");

        final List<String> disabledCommands = Lists.newArrayList(room.readString("disabled_commands").split(","));
        final int groupId = room.readInteger("group_id");
        final String requiredBadge = room.readString("required_badge");
        final boolean wiredHidden = room.readBoolean("hide_wired");

        return this.roomDataFactory.createRoomData(id, type, name, description, ownerId, owner, category, maxUsers, access, password,
                originalPassword, tradeState, score, tags, decorations, model, hideWalls, thicknessWall, thicknessFloor,
                allowWalkthrough, allowPets, heightmap, muteState, kickState, banState, bubbleMode, bubbleType,
                bubbleScroll, chatDistance, antiFloodSettings, disabledCommands, groupId, requiredBadge, thumbnail, wiredHidden);
    }
}
