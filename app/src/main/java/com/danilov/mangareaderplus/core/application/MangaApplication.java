package com.danilov.mangareaderplus.core.application;

import android.app.Application;
import android.content.Context;

import com.android.httpimage.BitmapMemoryCache;
import com.android.httpimage.FileSystemPersistence;
import com.android.httpimage.HttpImageManager;
import com.danilov.mangareaderplus.R;
import com.danilov.mangareaderplus.core.cache.CacheDirectoryManagerImpl;
import com.danilov.mangareaderplus.core.database.DownloadedMangaDAO;
import com.danilov.mangareaderplus.core.database.HistoryDAO;
import com.danilov.mangareaderplus.core.database.MangaDAO;
import com.danilov.mangareaderplus.core.database.UpdatesDAO;
import com.danilov.mangareaderplus.core.http.ExtendedHttpClient;
import com.danilov.mangareaderplus.core.http.HttpBitmapReader;
import com.danilov.mangareaderplus.core.http.HttpBytesReader;
import com.danilov.mangareaderplus.core.http.HttpStreamReader;
import com.danilov.mangareaderplus.core.service.LocalImageManager;
import com.danilov.mangareaderplus.core.util.ServiceContainer;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.io.File;

/**
 * Created by Semyon Danilov on 02.08.2014.
 */
@ReportsCrashes(formKey = "", // will not be used
        mailTo = "senya.danilov@gmail.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class MangaApplication extends Application {

    @Override
    public void onCreate() {
        File mydir = getBaseContext().getDir("mydir", Context.MODE_PRIVATE);
        CacheDirectoryManagerImpl cacheDirectoryManager = new CacheDirectoryManagerImpl(mydir, ApplicationSettings.get(this), ApplicationSettings.PACKAGE_NAME);
        FileSystemPersistence fsp = new FileSystemPersistence(cacheDirectoryManager);
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
        ServiceContainer.addService(cacheDirectoryManager);
        ServiceContainer.addService(httpBytesReader);
        ServiceContainer.addService(httpStreamReader);
        ServiceContainer.addService(httpImageManager);
        ServiceContainer.addService(localImageManager);
        ServiceContainer.addService(downloadedMangaDAO);
        ServiceContainer.addService(historyDAO);
        ServiceContainer.addService(updatesDAO);
        ServiceContainer.addService(mangaDAO);


        //Google Play Service ты офигел!
        try {
            Class.forName("android.os.AsyncTask");
        } catch(Throwable ignore) {
        }

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}
