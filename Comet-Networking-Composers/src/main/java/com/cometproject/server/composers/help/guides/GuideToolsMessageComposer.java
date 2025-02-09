package com.cometproject.server.composers.help.guides;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class GuideToolsMessageComposer extends MessageComposer {

    private final boolean onDuty;
    private final int activeGuideCount;
    private final int activeGuardianCount;

    public GuideToolsMessageComposer(final boolean onDuty, int activeGuideCount, int activeGuardianCount) {
        this.onDuty = onDuty;
        this.activeGuardianCount = activeGuardianCount;
        this.activeGuideCount = activeGuideCount;
    }

    @Override
    public short getId() {
        return Composers.GuideToolsMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeBoolean(this.onDuty);
        msg.writeInt(0);
        msg.writeInt(this.activeGuideCount);//active guides
        msg.writeInt(this.activeGuardianCount);//active guardians
    }
}
