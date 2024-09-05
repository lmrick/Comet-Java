package com.cometproject.server.network.messages.outgoing.room.items;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.WiredFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.addons.WiredAddonRandomEffect;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.addons.WiredAddonUnseenEffect;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FloorItemsMessageComposer extends MessageComposer {
    private final Room room;

    public FloorItemsMessageComposer(final Room room) {
        this.room = room;
    }

    @Override
    public short getId() {
        return Composers.ObjectsMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        if (!room.getItems().getFloorItems().isEmpty()) {
            //if (room.getGroup() == null) {
            msg.writeInt(room.getItems().getItemOwners().size());
					
					room.getItems().getItemOwners().forEach((key, value) -> {
						msg.writeInt(key);
						msg.writeString(value);
					});
            /* } else {
                final Group group = room.getGroup();

                if (group.getData().canMembersDecorate()) {
                    msg.writeInt(group.getAll().getAll().size() + 1);

                    msg.writeInt(room.getData().getOwnerId());
                    msg.writeString(room.getData().getOwner());

                    for (GroupMember groupMember : group.getAll().getAll().values()) {
                        msg.writeInt(groupMember.getPlayerId());
                        msg.writeString(PlayerDao.getUsernameByPlayerId(groupMember.getPlayerId()));
                    }
                } else {
                    msg.writeInt(group.getAll().getAdministrators().size() + 1);

                    msg.writeInt(room.getData().getOwnerId());
                    msg.writeString(room.getData().getOwner());

                    for (Integer groupMember : group.getAll().getAdministrators()) {
                        msg.writeInt(groupMember);
                        msg.writeString(PlayerDao.getUsernameByPlayerId(groupMember));
                    }
                }
            }*/

            if (room.getData().isWiredHidden()) {
                List<RoomItemFloor> items = room.getItems().getFloorItems().values().stream().filter(item -> !(item instanceof WiredFloorItem) && !(item instanceof WiredAddonRandomEffect) && !(item instanceof WiredAddonUnseenEffect)).collect(Collectors.toList());
							
							msg.writeInt(items.size());
							
							items.forEach(item -> item.serialize(msg));
            } else {
                msg.writeInt(room.getItems().getFloorItems().size());
							
							room.getItems().getFloorItems().values().forEach(item -> item.serialize((msg)));
            }

        } else {
            msg.writeInt(0);
            msg.writeInt(0);
        }

    }
}
