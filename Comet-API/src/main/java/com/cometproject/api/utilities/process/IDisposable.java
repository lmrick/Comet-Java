package com.cometproject.api.utilities.process;

public interface IDisposable {
    
    default boolean isDisposed() {
        return false;
    }

    default void dispose() {
        
    }

}
