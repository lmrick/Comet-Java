package com.cometproject.server.network.sessions;

import com.cometproject.api.networking.messages.IMessageComposer;
import com.cometproject.api.networking.sessions.ISession;
import com.cometproject.api.networking.sessions.ISessionService;
import com.cometproject.api.networking.sessions.SessionContext;
import com.cometproject.server.game.players.PlayerManager;
import com.cometproject.server.network.ws.messages.WsMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class SessionManager implements ISessionService {
    public static final AttributeKey<Session> SESSION_ATTR = AttributeKey.valueOf("Session.attr");
    public static final AttributeKey<Integer> CHANNEL_ID_ATTR = AttributeKey.valueOf("ChannelId.attr");
    public static boolean isLocked = false;
    private final AtomicInteger idGenerator = new AtomicInteger();
    private final Map<Integer, ISession> sessions = new ConcurrentHashMap<>();
    private final Map<String, SessionAccessLog> accessLog = new ConcurrentHashMap<>();
    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public SessionManager() {
        SessionContext.getInstance().setSessionService(this);
    }

    public boolean add(ChannelHandlerContext channel) {
        Session session = new Session(channel);

        session.initialise();

        channel.attr(SessionManager.SESSION_ATTR).set(session);
        this.channelGroup.add(channel.channel());
        channel.attr(CHANNEL_ID_ATTR).set(this.idGenerator.incrementAndGet());

        return (this.sessions.putIfAbsent(channel.attr(CHANNEL_ID_ATTR).get(), session) == null);
    }

    public boolean remove(ChannelHandlerContext channel) {
        if (channel.attr(CHANNEL_ID_ATTR).get() == null) {
            return false;
        }

        if (this.sessions.containsKey(channel.attr(CHANNEL_ID_ATTR).get())) {
            this.channelGroup.remove(channel.channel());
            this.sessions.remove(channel.attr(CHANNEL_ID_ATTR).get());

            return true;
        }

        return false;
    }

    public Session getByPlayerId(int id) {
        if (PlayerManager.getInstance().getSessionIdByPlayerId(id) != -1) {
            int sessionId = PlayerManager.getInstance().getSessionIdByPlayerId(id);

            return (Session) sessions.get(sessionId);
        }

        return null;
    }

    public Session getByPlayerUsername(String username) {
        int playerId = PlayerManager.getInstance().getPlayerIdByUsername(username);

        if (playerId == -1)
            return null;

        int sessionId = PlayerManager.getInstance().getSessionIdByPlayerId(playerId);

        if (sessionId == -1)
            return null;

        if (this.sessions.containsKey(sessionId))
            return (Session) this.sessions.get(sessionId);

        return null;
    }

    public int getUsersOnlineCount() {
        return PlayerManager.getInstance().size();
    }

    public Map<Integer, ISession> getSessions() {
        return this.sessions;
    }

    public void broadcast(IMessageComposer msg) {
        this.getChannelGroup().writeAndFlush(msg);
    }

    @Override
    public void broadcastTo(Set<Integer> players, IMessageComposer messageComposer, int sender) {
        players.stream().filter(id -> id != sender).map(this::getByPlayerId).forEach(session -> {
            if (session != null) {
                session.send(messageComposer);
            }
        });
    }

    public void broadcastWs(WsMessage message) {
        for (ISession client : sessions.values()) {
            if (((Session) client).getWsChannel() != null) {
                ((Session) client).sendWs(message);
            }
        }
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    public void broadcastToModerators(IMessageComposer messageComposer) {
			this.sessions.values().stream().filter(session -> session.getPlayer() != null && session.getPlayer().getPermissions() != null && session.getPlayer().getPermissions().getRank().modTool()).forEachOrdered(session -> session.send(messageComposer));
    }

    public Map<String, SessionAccessLog> getAccessLog() {
        return accessLog;
    }
}
