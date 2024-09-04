package com.cometproject.api.networking.messages;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import io.netty.buffer.ByteBuf;

public interface IMessageComposer {
    
    IComposerDataWrapper writeMessage(ByteBuf buffer);
    short getId();
    void compose(IComposerDataWrapper msg);
    void dispose();
    
}
