package com.cometproject.server.game.permissions.types;

public record CommandPermission(String commandId, int minimumRank, boolean vipOnly, boolean rightsOnly) {

}
