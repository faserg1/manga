package com.danilov.mangareader.core.application;

import android.app.Application;
import android.content.Context;
import com.android.httpimage.BitmapMemoryCache;
import com.android.httpimage.FileSystemPersistence;
import com.android.httpimage.HttpImageManager;
import com.danilov.mangareader.core.cache.CacheDirectoryManagerImpl;
import com.danilov.mangareader.core.database.DownloadedMangaDAO;
import com.danilov.mangareader.core.database.HistoryDAO;
import com.danilov.mangareader.core.database.MangaDAO;
import com.danilov.mangareader.core.database.UpdatesDAO;
import com.danilov.mangareader.core.http.ExtendedHttpClient;
import com.danilov.mangareader.core.http.HttpBitmapReader;
import com.danilov.mangareader.core.http.HttpBytesReader;
import com.danilov.mangareader.core.http.HttpStreamReader;
import com.danilov.mangareader.core.service.LocalImageManager;
import com.danilov.mangareader.core.util.ServiceContainer;

import java.io.File;

/**
 * Created by Semyon Danilov on 02.08.2014.
 */
public class MangaApplication extends Application {

    @Override
    public void onCreate() {
        File mydir = getBaseContext().getDir("mydir", Context.MODE_PRIVATE);
        FileSystemPersistence fsp = new FileSystemPersistence(new CacheDirectoryManagerImpl(mydir, ApplicationSettings.get(this), "com.danilov.mangareader"));
        HttpStreamReader httpStreamReader = new HttpStreamReader(new ExtendedHttpClient(), getResources());
        HttpBytesReader httpBytesReader = new HttpBytesReader(httpStreamReader, getResources());
        HttpBitmapReader httpBitmapReader = new HttpBitmapReader(httpBytesReader);
        BitmapMemoryCache bmc = new BitmapMemoryCache(0.4f);
        HttpImageManager httpImageManager = new HttpImageManager(bmc, fsp, getResources(), httpBitmapReader);
        LocalImageManager localImageManager = new LocalImageManager(bmc, getResources());
        HistoryDAO historyDAO = new HistoryDAO();
        UpdatesDAO updatesDAO = new UpdatesDAO();
        DownloadedMangaDAO downloadedMangaDAO = new DownloadedMangaDAO();
        MangaDAO mangaDAO = new MangaDAO();
        ServiceContainer.addService(httpBytesReader);
        ServiceContainer.addService(httpStreamReader);
        ServiceContainer.addService(httpImageManager);
        ServiceContainer.addService(localImageManager);
        ServiceContainer.addService(downloadedMangaDAO);
        ServiceContainer.addService(historyDAO);
        ServiceContainer.addService(updatesDAO);
        ServiceContainer.addService(mangaDAO);
    }
}
