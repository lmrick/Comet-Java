package com.cometproject.api.networking.messages.wrappers;

public interface IEventDataWrapper {
    
    short readShort();
    int readInt();
    boolean readBoolean();
    String readString();
    byte[] readBytes(int length);
    byte[] toRawBytes();
    short getId();
    int getLength();
    
}
