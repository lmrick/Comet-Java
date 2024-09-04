package com.cometproject.server.game.permissions.types;

public record OverrideCommandPermission(String commandId, int playerId, boolean enabled) {

}
