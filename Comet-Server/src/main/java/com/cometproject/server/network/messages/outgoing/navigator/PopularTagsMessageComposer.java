package com.cometproject.server.network.messages.outgoing.navigator;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;
import java.util.Map;


public class PopularTagsMessageComposer extends MessageComposer {
    private final Map<String, Integer> popularTags;

    public PopularTagsMessageComposer(final Map<String, Integer> popularTags) {
        this.popularTags = popularTags;
    }

    @Override
    public short getId() {
        return Composers.PopularRoomTagsResultMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(Math.min(popularTags.size(), 50));
			
			popularTags.forEach((key, value) -> {
				msg.writeString(key);
				msg.writeInt(value);
			});
    }

    @Override
    public void dispose() {
        this.popularTags.clear();
    }
}
