package com.danilov.supermanga.core.module;

import android.content.Context;
import android.support.annotation.NonNull;

import com.android.httpimage.BitmapCache;
import com.android.httpimage.BitmapMemoryCache;
import com.android.httpimage.FileSystemPersistence;
import com.danilov.supermanga.core.application.ApplicationSettings;
import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.cache.CacheDirectoryManager;
import com.danilov.supermanga.core.cache.CacheDirectoryManagerImpl;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Semyon on 18.03.2016.
 */
@Module
public class CacheModule {

    @Provides
    @Singleton
    public CacheDirectoryManager provideCacheDirectoryManager(@NonNull final MangaApplication mangaApplication) {
        File mydir = mangaApplication.getBaseContext().getDir("mydir", Context.MODE_PRIVATE);
        CacheDirectoryManagerImpl cacheDirectoryManager = new CacheDirectoryManagerImpl(mydir, ApplicationSettings.get(mangaApplication), ApplicationSettings.PACKAGE_NAME);
        cacheDirectoryManager.trimCacheIfNeeded();
        return cacheDirectoryManager;
    }

    @Provides
    @Singleton
    public BitmapCache provideBitmapCache() {
        return new BitmapMemoryCache(0.4f);
    }

}
