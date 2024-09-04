package com.cometproject.server.game.rooms.types;

import com.cometproject.server.game.players.types.Player;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

public class RoomReloadListener {
    private final Set<Player> players = new HashSet<>();
    private final BiConsumer<Set<Player>, Room> consumer;

    public RoomReloadListener(final Room room, BiConsumer<Set<Player>, Room> consumer) {
        this.consumer = consumer;

        this.addPlayers(room);
    }

    public void onReloaded(Room room) {
        this.consumer.accept(this.players, room);
    }

    private void addPlayers(Room room) {
			room.getEntities().getPlayerEntities().stream().map(PlayerEntity::getPlayer).filter(Objects::nonNull).forEachOrdered(this.players::add);
    }
}
