package com.danilov.manga;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.android.httpimage.BitmapMemoryCache;
import com.android.httpimage.FileSystemPersistence;
import com.android.httpimage.HttpImageManager;
import com.danilov.manga.activity.MangaInfoActivity;
import com.danilov.manga.core.application.ApplicationSettings;
import com.danilov.manga.core.cache.CacheDirectoryManagerImpl;
import com.danilov.manga.core.http.ExtendedHttpClient;
import com.danilov.manga.core.http.HttpBitmapReader;
import com.danilov.manga.core.http.HttpBytesReader;
import com.danilov.manga.core.http.HttpStreamReader;
import com.danilov.manga.core.service.DownloadManager;
import com.danilov.manga.core.util.ServiceContainer;
import com.danilov.manga.test.QueryTestActivity;
import com.danilov.manga.test.TouchImageViewActivityTest;

import java.io.File;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mydir = getBaseContext().getDir("mydir", Context.MODE_PRIVATE);

        fsp = new FileSystemPersistence(new CacheDirectoryManagerImpl(mydir, new ApplicationSettings(), "com.danilov.manga"));
        httpStreamReader = new HttpStreamReader(new ExtendedHttpClient(), getResources());
        httpBytesReader = new HttpBytesReader(httpStreamReader, getResources());
        httpBitmapReader = new HttpBitmapReader(httpBytesReader);
        httpImageManager = new HttpImageManager(new BitmapMemoryCache(), fsp, getResources(), httpBitmapReader);
        ServiceContainer.addService(httpBytesReader);
        ServiceContainer.addService(httpImageManager);
        DownloadManager downloadManager = new DownloadManager();
        downloadManager.startDownload("http://he.readmanga.ru/auto/11/52/35/03.png", mydir.getPath() + "/1.png");
        downloadManager.startDownload("http://hb.readmanga.ru/auto/11/52/35/04.png", mydir.getPath() + "/2.png");
    }

    public void firstTest(View view) {
        Intent intent = new Intent(this, TouchImageViewActivityTest.class);
        startActivity(intent);
    }

    public void secondTest(View view) {
        Intent intent = new Intent(this, QueryTestActivity.class);
        startActivity(intent);
    }

    public void thirdTest(View view) {
        Intent intent = new Intent(this, MangaInfoActivity.class);
        startActivity(intent);
    }

    File mydir = null; //Creating an internal dir;

    FileSystemPersistence fsp = null;
    HttpStreamReader httpStreamReader = null;
    HttpBytesReader httpBytesReader = null;
    HttpBitmapReader httpBitmapReader = null;

    private HttpImageManager httpImageManager = null;


}
