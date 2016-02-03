package com.danilov.supermanga.core.application;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.httpimage.BitmapMemoryCache;
import com.android.httpimage.FileSystemPersistence;
import com.android.httpimage.HttpImageManager;
import com.danilov.supermanga.R;
import com.danilov.supermanga.core.cache.CacheDirectoryManagerImpl;
import com.danilov.supermanga.core.database.HistoryDAO;
import com.danilov.supermanga.core.database.MangaDAO;
import com.danilov.supermanga.core.database.UpdatesDAO;
import com.danilov.supermanga.core.http.ExtendedHttpClient;
import com.danilov.supermanga.core.http.HttpBitmapReader;
import com.danilov.supermanga.core.http.HttpBytesReader;
import com.danilov.supermanga.core.http.HttpStreamReader;
import com.danilov.supermanga.core.receiver.AlarmReceiver;
import com.danilov.supermanga.core.repository.RepositoryException;
import com.danilov.supermanga.core.repository.RepositoryHolder;
import com.danilov.supermanga.core.repository.special.JSCrud;
import com.danilov.supermanga.core.repository.special.JavaScriptEngine;
import com.danilov.supermanga.core.repository.special.JavaScriptRepository;
import com.danilov.supermanga.core.repository.special.test.JSTestEngine;
import com.danilov.supermanga.core.service.LocalImageManager;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.ServiceContainer;
import com.danilov.supermanga.test.DBTest;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.io.File;
import java.util.Calendar;
import java.util.Collection;

/**
 * Created by Semyon Danilov on 02.08.2014.
 */
@ReportsCrashes(formKey = "", // will not be used
        mailTo = "senya.danilov@gmail.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class MangaApplication extends Application {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
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
        MangaDAO mangaDAO = new MangaDAO();
        JSCrud jsCrud = new JSCrud();
        ServiceContainer.addService(cacheDirectoryManager);
        ServiceContainer.addService(httpBytesReader);
        ServiceContainer.addService(httpStreamReader);
        ServiceContainer.addService(httpImageManager);
        ServiceContainer.addService(localImageManager);
        ServiceContainer.addService(historyDAO);
        ServiceContainer.addService(updatesDAO);
        ServiceContainer.addService(mangaDAO);
        ServiceContainer.addService(jsCrud);
        cacheDirectoryManager.trimCacheIfNeeded();

        RepositoryHolder repositoryHolder = new RepositoryHolder();
        repositoryHolder.init();
        ServiceContainer.addService(repositoryHolder);
        ///TEST
        JavaScriptRepository repository = jsCrud.create(new JavaScriptRepository("/mnt/sdcard/mangafox.js", "MangaFOX"));
        Collection<JavaScriptRepository> resultSet = jsCrud.select(jsCrud.getByNameSelector("MangaFOX"));
        if (repository != null && resultSet != null) {

        }
        ///TEST


//        AccountManager accountManager = AccountManager.get(this);
//        Account[] accounts = accountManager.getAccounts();
//        int length = accounts.length;

        AlarmReceiver.setUpdateAlarm(this);

        //Google Play Service ты офигел!
        try {
            Class.forName("android.os.AsyncTask");
        } catch(Throwable ignore) {
        }

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread thread, final Throwable ex) {
                Log.e("MangaFAIL", ex.getMessage());
                ex.printStackTrace();
                defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
            }
        });
    }



    public static Context getContext() {
        return context;
    }

}