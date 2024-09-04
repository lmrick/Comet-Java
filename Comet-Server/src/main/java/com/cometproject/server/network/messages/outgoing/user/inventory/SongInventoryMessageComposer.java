package com.cometproject.server.network.messages.outgoing.user.inventory;

import com.cometproject.api.game.furniture.types.ISongItem;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

import java.util.List;

public class SongInventoryMessageComposer extends MessageComposer {

    private List<ISongItem> songItems;

    public SongInventoryMessageComposer(List<ISongItem> songItems) {
        this.songItems = songItems;
    }

    @Override
    public short getId() {
        return Composers.SongInventoryMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(this.songItems.size());

        for (ISongItem songItem : this.songItems) {
            msg.writeInt(ItemManager.getInstance().getItemVirtualId(songItem.getItemSnapshot().id()));
            msg.writeInt(songItem.getSongId());
        }
    }
}
