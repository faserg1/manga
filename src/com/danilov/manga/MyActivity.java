package com.danilov.manga;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.android.httpimage.BitmapMemoryCache;
import com.android.httpimage.FileSystemPersistence;
import com.android.httpimage.HttpImageManager;
import com.danilov.manga.activity.MangaInfoActivity;
import com.danilov.manga.core.application.ApplicationSettings;
import com.danilov.manga.core.cache.CacheDirectoryManagerImpl;
import com.danilov.manga.core.http.*;
import com.danilov.manga.core.util.IoUtils;
import com.danilov.manga.core.util.ServiceContainer;
import com.danilov.manga.test.DownloadTestActivity;
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

        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    long s = System.currentTimeMillis();
                    String uri = "http://readmanga.me/naruto_dj___animal_panic_paradox/vol1/1";
                    HttpStreamModel model = httpStreamReader.fromUri(uri);
                    LinesSearchInputStream linesSearchInputStream = new LinesSearchInputStream(model.stream, "pictures", "\n");
                    byte[] bytes = new byte[1024];
                    while(linesSearchInputStream.read(bytes) == LinesSearchInputStream.SEARCHING) {
                        Log.d("", "searching");
                    }
                    bytes = linesSearchInputStream.getResult();
                    String str = IoUtils.convertBytesToString(bytes);
                    str.isEmpty();
                    Log.d("MyActivity", "Time: " + (System.currentTimeMillis() - s) + " ms");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void firstTest(View view) {
        Intent intent = new Intent(this, TouchImageViewActivityTest.class);
        startActivity(intent);
    }

    public void secondTest(View view) {
        Intent intent = new Intent(this, QueryTestActivity.class);
        startActivity(intent);
    }

    public void fourthTest(View view) {
        Intent intent = new Intent(this, DownloadTestActivity.class);
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
