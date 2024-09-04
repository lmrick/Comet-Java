package com.cometproject.api.networking.messages.wrappers;

public interface IComposerDataWrapper {
    
    int getId();
    void clear();
    boolean isFinalized();
    void writeString(Object obj);
    void writeEmptyString();
    void writeDouble(double d);
    void writeInt(int i);
    void writeLong(long i);
    void writeBoolean(boolean b);
    void writeByte(int b);
    void writeShort(int s);
    
}
