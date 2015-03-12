package com.danilov.mangareaderplus.core.cache;

import android.net.Uri;

import java.io.File;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public interface CacheDirectoryManager {

    public File getInternalCacheDir();

    public File getExternalCacheDir();

    public File getCurrentCacheDirectory();

    public abstract File getPagesCacheDirectory();

    public abstract File getThumbnailsCacheDirectory();

    public abstract void trimCacheIfNeeded();

    public abstract File getImagesCacheDirectory();

    public abstract File getCachedImageFileForWrite(Uri uri);

    public abstract File getCachedImageFileForRead(Uri uri);

}
