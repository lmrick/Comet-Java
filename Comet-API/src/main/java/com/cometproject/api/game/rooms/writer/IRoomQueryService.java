package com.cometproject.api.game.rooms.writer;

public interface IRoomQueryService {
    boolean isActive(int roomId);

    int playerCount(int roomId);

    boolean hasRoomMute(int roomId);

    boolean isStaffPicked(int roomId);

    IPublicRoomData getPublicRoom(int roomId);

    IRoomPromotionData getPromotion(int roomId);
}
