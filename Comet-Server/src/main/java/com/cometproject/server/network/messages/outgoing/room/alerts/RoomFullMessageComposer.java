//package com.cometproject.server.network.messages.outgoing.room.alerts;
//
//import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
//import com.cometproject.server.network.messages.composers.MessageComposer;
//import com.cometproject.server.protocol.headers.Composers;
//
//
//public class RoomFullMessageComposer extends MessageComposer {
//    @Override
//    public short getId() {
//        return Composers.RoomErrorNotifMessageComposer;
//    }
//
//    @Override
//    public void compose(IComposerDataWrapper msg) {
//        msg.writeInt(1);
//        msg.writeString("/x363");
//
//    }
//}
