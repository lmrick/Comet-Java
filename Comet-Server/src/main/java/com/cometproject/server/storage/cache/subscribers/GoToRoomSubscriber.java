package com.cometproject.server.storage.cache.subscribers;

import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.outgoing.room.engine.RoomForwardMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

public class GoToRoomSubscriber implements ISubscriber {
    private Jedis jedis = null;

    @Override
    public void setJedis(JedisPool jedis) {
        this.jedis = jedis.getResource();
    }

    @Override
    public void subscribe() {
        this.jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                handleMessage(message);
            }
        }, getChannel());
    }

    @Override
    public void handleMessage(String message) {
        GoToRoomSubscriberData data = new Gson().fromJson(message, GoToRoomSubscriberData.class);
        if (data == null || data.roomId() == null || data.roomId() == 0 || data.username() == null || data.username().isEmpty()) {
					return;
				}

        Session session = NetworkManager.getInstance().getSessions().getByPlayerUsername(data.username());
        if (session == null) {
					return;
				}

        session.send(new RoomForwardMessageComposer(data.roomId()));
    }

    @Override
    public String getChannel() {
        return "comet.goto.room";
    }
    
    private record GoToRoomSubscriberData(String username, Integer roomId) { }
}
