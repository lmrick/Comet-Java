package com.cometproject.api.game.players.data.components;

import com.cometproject.api.game.players.data.IPlayerAvatar;
import com.cometproject.api.game.players.data.components.messenger.IMessengerFriend;
import com.cometproject.api.networking.messages.IMessageComposer;

import java.util.List;
import java.util.Map;

public interface IPlayerMessenger {
    
    IMessageComposer search(String query);
    void addRequest(int playerId);
    void addFriend(IMessengerFriend friend);
    void removeFriend(int userId);
    Integer getRequestBySender(int sender);
    void broadcast(IMessageComposer msg);
    void broadcast(List<Integer> friends, IMessageComposer msg);
    boolean hasRequestFrom(int playerId);
    List<IPlayerAvatar> getRequestAvatars();
    void clearRequests();
    void sendOffline(int friend, boolean online, boolean inRoom);
    void sendStatus(boolean online, boolean inRoom);
    IMessengerFriend getFriendById(int id);
    Map<Integer, IMessengerFriend> getFriends();
    List<Integer> getRequests();
    void removeRequest(Integer request);
    void setInitialized(boolean initialised);
    boolean isInitialized();
    
}
