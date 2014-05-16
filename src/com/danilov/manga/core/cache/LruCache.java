package com.danilov.manga.core.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class LruCache<K, V> extends LinkedHashMap<K, V> {

    public static final int MAX_CAPACITY = 50;

    private final ILruCacheListener<K, V> mListener;

    public LruCache(ILruCacheListener<K, V> listener) {
        super(16, 1, true);

        this.mListener = listener;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
        if (this.size() <= MAX_CAPACITY) {
            return false;
        } else {
            if (this.mListener != null) {
                this.mListener.onEntryRemoved(entry.getKey(), entry.getValue());
            }

            return true;
        }
    }

}