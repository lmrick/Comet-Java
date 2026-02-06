package com.cometproject.server.game.rooms.services;

import com.cometproject.api.game.rooms.writer.IPublicRoomData;
import com.cometproject.api.game.rooms.writer.IRoomPromotionData;
import com.cometproject.api.game.rooms.writer.IRoomQueryService;
import com.cometproject.server.game.navigator.NavigatorManager;
import com.cometproject.server.game.navigator.types.publics.PublicRoom;
import com.cometproject.server.game.rooms.RoomManager;
import com.cometproject.server.game.rooms.types.components.types.promotion.RoomPromotion;

public class RoomQueryService implements IRoomQueryService {
    @Override
    public boolean isActive(int roomId) {
        return RoomManager.getInstance().isActive(roomId);
    }

    @Override
    public int playerCount(int roomId) {
        return this.isActive(roomId) ? RoomManager.getInstance().get(roomId).getEntities().playerCount() : 0;
    }

    @Override
    public boolean hasRoomMute(int roomId) {
        return this.isActive(roomId) && RoomManager.getInstance().get(roomId).hasRoomMute();
    }

    @Override
    public boolean isStaffPicked(int roomId) {
        return NavigatorManager.getInstance().isStaffPicked(roomId);
    }

    @Override
    public IPublicRoomData getPublicRoom(int roomId) {
        PublicRoom room = NavigatorManager.getInstance().getPublicRoom(roomId);

        if (room == null) {
            return null;
        }

        return new IPublicRoomData() {
            @Override
            public String caption() {
                return room.caption();
            }

            @Override
            public String description() {
                return room.description();
            }

            @Override
            public String imageUrl() {
                return room.imageUrl();
            }
        };
    }

    @Override
    public IRoomPromotionData getPromotion(int roomId) {
        RoomPromotion promotion = RoomManager.getInstance().getRoomPromotions().get(roomId);

        if (promotion == null) {
            return null;
        }

        return new IRoomPromotionData() {
            @Override
            public String promotionName() {
                return promotion.getPromotionName();
            }

            @Override
            public String promotionDescription() {
                return promotion.getPromotionDescription();
            }

            @Override
            public int minutesLeft() {
                return promotion.minutesLeft();
            }
        };
    }
}
