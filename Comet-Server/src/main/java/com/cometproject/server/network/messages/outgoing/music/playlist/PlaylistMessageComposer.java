package com.cometproject.server.network.messages.outgoing.music.playlist;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.game.items.music.SongItemData;
import com.cometproject.server.game.rooms.objects.items.types.floor.SoundMachineFloorItem;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

import java.util.List;

public class PlaylistMessageComposer extends MessageComposer {
    private List<SongItemData> songItemDatas;

    public PlaylistMessageComposer(List<SongItemData> songItemDatas) {
        this.songItemDatas = songItemDatas;
    }

    @Override
    public short getId() {
        return Composers.PlaylistMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(SoundMachineFloorItem.MAX_CAPACITY);
        msg.writeInt(songItemDatas.size());

        for (SongItemData songItemData : this.songItemDatas) {
            msg.writeInt(songItemData.getItemSnapshot().baseItemId());
            msg.writeInt(songItemData.getSongId());
        }
    }
}
