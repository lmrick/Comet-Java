package com.cometproject.storage.mysql.queues.types.players.objects;

public record PlayerStatusUpdate(int playerId, boolean playerOnline, String ipAddress) {

}
