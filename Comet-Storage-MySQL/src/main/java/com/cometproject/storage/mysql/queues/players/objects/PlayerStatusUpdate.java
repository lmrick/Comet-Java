package com.cometproject.storage.mysql.queues.players.objects;

public record PlayerStatusUpdate(int playerId, boolean playerOnline, String ipAddress) {

}
