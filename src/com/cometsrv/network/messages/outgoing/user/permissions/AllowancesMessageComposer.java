package com.cometsrv.network.messages.outgoing.user.permissions;

import com.cometsrv.game.GameEngine;
import com.cometsrv.game.permissions.types.Perk;
import com.cometsrv.network.messages.headers.Composers;
import com.cometsrv.network.messages.types.Composer;

import java.util.Map;

public class AllowancesMessageComposer {
    public static Composer compose(int rank) {
        Composer msg = new Composer(Composers.AllowancesMessageComposer);

        msg.writeInt(GameEngine.getPermissions().getPerks().size());

        for(Map.Entry<Integer, Perk> perk : GameEngine.getPermissions().getPerks().entrySet()) {
            msg.writeString(perk.getValue().getTitle());

            if(perk.getValue().doesOverride()) {
                msg.writeBoolean(perk.getValue().getDefault());
            } else {
                msg.writeBoolean(perk.getValue().getRank() <= rank);
            }

            msg.writeString(perk.getValue().getData());
        }


        return msg;
    }
}
