package com.cometproject.api.game.players.data.components.permissions;

public interface IPlayerRank {

    int getId();
    String getName();
    boolean floodBypass();
    int floodTime();
    boolean disconnectable();
    boolean bannable();
    boolean modTool();
    boolean roomKickable();
    boolean roomFullControl();
    boolean roomMuteBypass();
    boolean roomFilterBypass();
    boolean roomIgnorable();
    boolean roomEnterFull();
    boolean roomEnterLocked();
    boolean roomStaffPick();
    boolean messengerStaffChat();
    boolean messengerLogChat();
    int messengerMaxFriends();
    boolean aboutDetailed();
    boolean aboutStats();
    boolean roomSeeWhispers();
    boolean sendLoginNotif();
    String namePrefix();
    boolean roomMPU();

}
