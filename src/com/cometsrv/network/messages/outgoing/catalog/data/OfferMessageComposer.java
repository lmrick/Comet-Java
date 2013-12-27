package com.cometsrv.network.messages.outgoing.catalog.data;

import com.cometsrv.network.messages.headers.Composers;
import com.cometsrv.network.messages.types.Composer;

public class OfferMessageComposer {
    public static Composer compose() {
        Composer msg = new Composer(Composers.OfferMessageComposer);

        msg.writeInt(100);
        msg.writeInt(6);
        msg.writeInt(1);
        msg.writeInt(1);
        msg.writeInt(2);
        msg.writeInt(40);
        msg.writeInt(99);

        return msg;
    }
}
