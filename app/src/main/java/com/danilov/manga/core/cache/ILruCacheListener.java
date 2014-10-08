package com.danilov.manga.core.cache;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public interface ILruCacheListener<K, V> {

    public void onEntryRemoved(final K key, final V value);

}
