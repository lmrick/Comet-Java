package com.cometproject.server.network.messages.outgoing.music;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class SongIdMessageComposer extends MessageComposer {

    private String songName;
    private int songId;

    public SongIdMessageComposer(String songName, int songId) {
        this.songName = songName;
        this.songId = songId;
    }

    @Override
    public short getId() {
        return Composers.SongIdMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeString(this.songName);
        msg.writeInt(this.songId);
    }
}
