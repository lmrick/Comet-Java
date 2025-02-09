package com.cometproject.server.network.messages.outgoing.user.details;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.game.players.types.PlayerSettings;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;


public class PlayerSettingsMessageComposer extends MessageComposer {
    private final PlayerSettings playerSettings;

    public PlayerSettingsMessageComposer(final PlayerSettings playerSettings) {
        this.playerSettings = playerSettings;
    }

    @Override
    public short getId() {
        return Composers.SoundSettingsMessageComposer;
    }

    @Override
    public void compose(IComposerDataWrapper msg) {
        msg.writeInt(this.playerSettings.getVolumes().getSystemVolume());
        msg.writeInt(this.playerSettings.getVolumes().getFurniVolume());
        msg.writeInt(this.playerSettings.getVolumes().getTraxVolume());
        msg.writeBoolean(this.playerSettings.isUseOldChat()); // old chat enabled?
        msg.writeBoolean(this.playerSettings.ignoreEvents()); // ignore room invites
        msg.writeBoolean(this.playerSettings.isCameraFollow()); //disable_room_camera_follow_checkbox
        msg.writeInt(0); //??
        msg.writeInt(0); //??
    }
}
