package com.cometproject.storage.api.data.groups;

import com.cometproject.api.game.groups.types.GroupType;
import com.cometproject.api.game.groups.types.IGroupData;
import com.cometproject.api.game.players.data.IPlayerAvatar;


public class GroupData implements IGroupData {
    private int id;
    private String title;
    private String description;
    private String badge;
    private int ownerId;
    private int roomId;
    private int created;
    private GroupType type;
    private int colourA;
    private int colourB;
    private boolean canMembersDecorate;
    private boolean hasForum;
    private IPlayerAvatar ownerAvatar;

    public GroupData(int id, String title, String description, String badge, int ownerId, String ownerName, int roomId, int created, GroupType type, int colourA, int colourB, boolean canMembersDecorate, boolean hasForum, IPlayerAvatar playerAvatar) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.badge = badge;
        this.ownerId = ownerId;
        this.roomId = roomId;
        this.created = created;
        this.type = type;
        this.colourA = colourA;
        this.colourB = colourB;
        this.canMembersDecorate = canMembersDecorate;
        this.hasForum = hasForum;
        this.ownerAvatar = playerAvatar;
    }

    public GroupData(String title, String description, String badge, int ownerId, int roomId, int colourA, int colourB) {
        this.id = -1;
        this.title = title;
        this.description = description;
        this.badge = badge.replace("s00000", "");
        this.ownerId = ownerId;
        this.roomId = roomId;
        this.created = (int) (System.currentTimeMillis() / 1000);
        this.type = GroupType.REGULAR;
        this.colourA = colourA;
        this.colourB = colourB;
        this.canMembersDecorate = false;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int getOwnerId() {
        return this.ownerId;
    }

    @Override
    public void setOwnerId(int id) {
        this.ownerId = id;
    }

    @Override
    public String getOwnerName() {
        return this.ownerAvatar.getUsername();
    }

    @Override
    public String getBadge() {
        return badge;
    }

    @Override
    public void setBadge(String badge) {
        this.badge = badge.replace("s00000", "");
    }

    @Override
    public int getRoomId() {
        return roomId;
    }

    @Override
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    @Override
    public int getCreatedTimestamp() {
        return created;
    }

    @Override
    public boolean canMembersDecorate() {
        return canMembersDecorate;
    }

    @Override
    public void setCanMembersDecorate(boolean canMembersDecorate) {
        this.canMembersDecorate = canMembersDecorate;
    }

    @Override
    public GroupType getType() {
        return type;
    }

    @Override
    public void setType(GroupType type) {
        this.type = type;
    }

    @Override
    public int getColourA() {
        return colourA;
    }

    @Override
    public void setColourA(int colourA) {
        this.colourA = colourA;
    }

    @Override
    public int getColourB() {
        return colourB;
    }

    @Override
    public void setColourB(int colourB) {
        this.colourB = colourB;
    }

    @Override
    public boolean hasForum() {
        return this.hasForum;
    }

    @Override
    public void setHasForum(boolean hasForum) {
        this.hasForum = hasForum;
    }

    @Override
    public IPlayerAvatar getOwnerAvatar() {
        return this.ownerAvatar;
    }
}
