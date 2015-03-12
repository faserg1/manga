package com.android.httpimage;

import android.graphics.Bitmap;
import com.danilov.mangareaderplus.core.cache.LruCache;
import com.danilov.mangareaderplus.core.util.BitmapUtils;

public class BitmapMemoryCache implements BitmapCache {

    private final LruCache<String, Bitmap> cache;

    private final int cacheSize;

    public BitmapMemoryCache(final float maxMemoryPercentage) {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        if (maxMemoryPercentage >= 1) {
            throw new IllegalStateException("Can't allocate more than 100% of memory");
        }
        // Use 1/8th of the available memory for this memory cache.
        cacheSize = (int) (maxMemory * maxMemoryPercentage);
        this.cache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(final String key, final Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return BitmapUtils.getBitmapSize(bitmap) / 1024;
            }

            @Override
            protected void entryRemoved(final boolean evicted, final String key, Bitmap oldValue, final Bitmap newValue) {
                if (oldValue != null) {
                    oldValue.recycle();
                    oldValue = null;
                }
            }
        };
    }

    @Override
    public synchronized boolean exists(String key) {
        return cache.hasKey(key);
    }

    @Override
    public boolean exists(final Bitmap bitmap) {
        return cache.hasValue(bitmap);
    }


    @Override
    public synchronized void clear() {
        this.cache.evictAll();
    }

    @Override
    public synchronized Bitmap loadData(String key) {
        Bitmap res = this.cache.get(key);

        return res;
    }

    @Override
    public synchronized void storeData(String key, Bitmap data) {
        if (this.exists(key)) {
            return;
        }
        if (cacheSize <= this.cache.size()  + (BitmapUtils.getBitmapSize(data) / 1024)) {
            return;
        }
        this.cache.put(key, data);
    }

    @Override
    public String toString() {
        int size = cache.size();
        return "size: " + size + ", free memory: " + (cacheSize - size);
    }
}
