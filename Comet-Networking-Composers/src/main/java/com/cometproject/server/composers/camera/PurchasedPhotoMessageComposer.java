package com.cometproject.server.composers.camera;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class PurchasedPhotoMessageComposer extends MessageComposer {
    @Override
    public short getId() {
        return Composers.PurchasedPhotoMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {

    }
}
