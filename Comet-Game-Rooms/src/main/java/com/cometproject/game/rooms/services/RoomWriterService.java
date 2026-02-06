package com.cometproject.game.rooms.services;

import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.groups.types.IGroupData;
import com.cometproject.api.game.rooms.IRoomData;
import com.cometproject.api.game.rooms.IRoomWriter;
import com.cometproject.api.game.rooms.settings.RoomAccessType;
import com.cometproject.api.game.rooms.writer.IPublicRoomData;
import com.cometproject.api.game.rooms.writer.IRoomPromotionData;
import com.cometproject.api.game.rooms.writer.IRoomQueryService;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;

public class RoomWriterService implements IRoomWriter {
    @Override
    public void write(IRoomData room, IComposerDataWrapper msg) {
        this.write(room, msg, false);
    }

    @Override
    public void write(IRoomData room, IComposerDataWrapper msg, boolean skipAuth) {
        IRoomQueryService roomQueryService = GameContext.getCurrent().getRoomQueryService();
        boolean isActive = roomQueryService != null && roomQueryService.isActive(room.getId());
        IPublicRoomData publicRoom = roomQueryService != null ? roomQueryService.getPublicRoom(room.getId()) : null;

        msg.writeInt(room.getId());
        msg.writeString(publicRoom != null ? publicRoom.caption() : room.getName());
        msg.writeInt(room.getOwnerId());
        msg.writeString(room.getOwner());
        msg.writeInt(skipAuth ? 0 : RoomAccessType.roomAccessToNumber(room.getAccess()));
        msg.writeInt(!isActive ? 0 : roomQueryService.playerCount(room.getId()));
        msg.writeInt(room.getMaxUsers());
        msg.writeString(publicRoom != null ? publicRoom.description() : room.getDescription());
        msg.writeInt(room.getTradeState().getState());
        msg.writeInt(room.getScore());
        msg.writeInt(0);
        msg.writeInt(room.getCategoryId());

        msg.writeInt(0);

        IRoomPromotionData promotion = roomQueryService != null ? roomQueryService.getPromotion(room.getId()) : null;
        IGroupData group = GameContext.getCurrent().getGroupService().getData(room.getGroupId());

        composeRoomSpecials(msg, room, promotion, group, publicRoom);
    }

    @Override
    public void entryData(IRoomData room, IComposerDataWrapper msg, boolean isLoading, boolean checkEntry, boolean skipAuth, boolean canMute) {
        IRoomQueryService roomQueryService = GameContext.getCurrent().getRoomQueryService();

        msg.writeBoolean(isLoading);
        write(room, msg, skipAuth);

        msg.writeBoolean(checkEntry);
        msg.writeBoolean(roomQueryService != null && roomQueryService.isStaffPicked(room.getId()));
        msg.writeBoolean(false);
        msg.writeBoolean(roomQueryService != null && roomQueryService.isActive(room.getId()) && roomQueryService.hasRoomMute(room.getId()));

        msg.writeInt(room.getMuteState().getState());
        msg.writeInt(room.getKickState().getState());
        msg.writeInt(room.getBanState().getState());

        msg.writeBoolean(canMute);

        msg.writeInt(room.getBubbleMode());
        msg.writeInt(room.getBubbleType());
        msg.writeInt(room.getBubbleScroll());
        msg.writeInt(room.getChatDistance());
        msg.writeInt(room.getAntiFloodSettings());
    }

    private void composeRoomSpecials(IComposerDataWrapper msg, IRoomData roomData, IRoomPromotionData promotion, IGroupData group, IPublicRoomData publicRoom) {
        int specialsType = publicRoom == null ? 8 : 1;

        if (group != null) {
            specialsType |= 2;
        }

        if (promotion != null) {
            specialsType |= 4;
        }

        if (roomData.isAllowPets()) {
            specialsType |= 16;
        }

        msg.writeInt(specialsType);

        if (publicRoom != null) {
            msg.writeString(publicRoom.imageUrl());
        }

        if (group != null) {
            composeGroup(group, msg);
        }

        if (promotion != null) {
            composePromotion(promotion, msg);
        }
    }

    private void composePromotion(IRoomPromotionData promotion, IComposerDataWrapper msg) {
        msg.writeString(promotion.promotionName());
        msg.writeString(promotion.promotionDescription());
        msg.writeInt(promotion.minutesLeft());
    }

    private void composeGroup(IGroupData group, IComposerDataWrapper msg) {
        msg.writeInt(group.getId());
        msg.writeString(group.getTitle());
        msg.writeString(group.getBadge());
    }
}
