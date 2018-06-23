package com.kaixin.core.util;

import java.util.HashMap;

/**
 * Simple extension to {@link java.util.HashMap} which does not enforce case sensitivity
 */
public class CaseInsensitiveHashMap<V> extends HashMap<String, V> {

    public CaseInsensitiveHashMap(){
        super();
    }

    @Override
    public V put(String key, V value) {
        return super.put(key.toLowerCase(), value);
    }

    @Override
    public V get(Object key) {
        return super.get(key.toString().toLowerCase());
    }
}
