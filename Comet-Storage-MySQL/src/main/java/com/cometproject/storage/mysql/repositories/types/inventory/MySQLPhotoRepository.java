package com.cometproject.storage.mysql.repositories.types.inventory;

import com.cometproject.storage.api.repositories.IPhotoRepository;
import com.cometproject.storage.mysql.connections.MySQLConnectionProvider;
import com.cometproject.storage.mysql.repositories.MySQLRepository;

public class MySQLPhotoRepository extends MySQLRepository implements IPhotoRepository {

    public MySQLPhotoRepository(MySQLConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public void savePhoto(int playerId, int roomId, String photoId, int timestamp) {
        update("INSERT into player_photos (player_id, room_id, photo, timestamp) VALUES(?, ?, ?, ?);", playerId, roomId, photoId, timestamp);
    }
}
