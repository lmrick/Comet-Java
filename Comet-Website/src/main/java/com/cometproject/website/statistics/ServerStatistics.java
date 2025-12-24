package com.cometproject.website.statistics;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ServerStatistics {
    private final int activePlayers;
    private final int activeRooms;
    private final String serverVersion;

    public ServerStatistics(ResultSet data) throws SQLException {
        this.activePlayers = data.getInt("active_players");
        this.activeRooms = data.getInt("active_rooms");
        this.serverVersion = data.getString("server_version");
    }

    public int getActivePlayers() {
        return activePlayers;
    }

    public int getActiveRooms() {
        return activeRooms;
    }

    public String getServerVersion() {
        return serverVersion;
    }
}
