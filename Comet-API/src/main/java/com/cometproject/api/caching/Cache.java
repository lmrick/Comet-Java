package com.cometproject.api.caching;

import java.util.function.BiConsumer;

public interface Cache<K, V> {

    V get(K key);
    void remove(K key);
    void add(K key, V obj);
    boolean contains(K key);
    void forEach(BiConsumer<K, V> consumer);
    
}
