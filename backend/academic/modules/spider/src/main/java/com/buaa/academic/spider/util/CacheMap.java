package com.buaa.academic.spider.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CacheMap<K, V> {

    private final int capacity;

    private final Map<K, V> cache = new HashMap<>();

    public CacheMap(int capacity) {
        this.capacity = capacity;
    }
    
    public void put(K key, V value) {
        synchronized (cache) {
            if (cache.containsKey(key)) {
                cache.put(key, value);
                return;
            }
            if (cache.size() >= capacity) {
                cache.clear();
            }
            cache.put(key, value);
        }
    }

    public V get(K key) {
        synchronized (cache) {
            return cache.get(key);
        }
    }
    
}
