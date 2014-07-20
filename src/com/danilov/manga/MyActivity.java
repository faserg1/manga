package com.danilov.manga;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.android.httpimage.BitmapMemoryCache;
import com.android.httpimage.FileSystemPersistence;
import com.android.httpimage.HttpImageManager;
import com.danilov.manga.activity.DownloadsActivity;
import com.danilov.manga.activity.MangaInfoActivity;
import com.danilov.manga.core.application.ApplicationSettings;
import com.danilov.manga.core.cache.CacheDirectoryManagerImpl;
import com.danilov.manga.core.database.DownloadedMangaDAO;
import com.danilov.manga.core.http.ExtendedHttpClient;
import com.danilov.manga.core.http.HttpBitmapReader;
import com.danilov.manga.core.http.HttpBytesReader;
import com.danilov.manga.core.http.HttpStreamReader;
import com.danilov.manga.core.service.LocalImageManager;
import com.danilov.manga.core.service.MangaDownloadService;
import com.danilov.manga.core.util.ServiceContainer;
import com.danilov.manga.core.view.AnimatedActionView;
import com.danilov.manga.test.DownloadTestActivity;
import com.danilov.manga.test.LocalImageActivity;
import com.danilov.manga.test.MangaViewTestActivity;
import com.danilov.manga.test.QueryTestActivity;

import java.io.File;

public class MyActivity extends Activity {

    private MangaDownloadService service = null;


    private AnimatedActionView aav = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mydir = getBaseContext().getDir("mydir", Context.MODE_PRIVATE);

        fsp = new FileSystemPersistence(new CacheDirectoryManagerImpl(mydir, ApplicationSettings.get(this), "com.danilov.manga"));
        httpStreamReader = new HttpStreamReader(new ExtendedHttpClient(), getResources());
        httpBytesReader = new HttpBytesReader(httpStreamReader, getResources());
        httpBitmapReader = new HttpBitmapReader(httpBytesReader);
        BitmapMemoryCache bmc = new BitmapMemoryCache(0.4f);
        httpImageManager = new HttpImageManager(bmc, fsp, getResources(), httpBitmapReader);
        localImageManager = new LocalImageManager(bmc, getResources());
        ServiceContainer.addService(httpBytesReader);
        ServiceContainer.addService(httpStreamReader);
        ServiceContainer.addService(httpImageManager);
        ServiceContainer.addService(localImageManager);
        DownloadedMangaDAO dao = new DownloadedMangaDAO();
        final MangaDownloadService.MDownloadServiceConnection serviceConnection = new MangaDownloadService.MDownloadServiceConnection(new MangaDownloadService.ServiceConnectionListener() {

            @Override
            public void onServiceConnected(final MangaDownloadService service) {
                MyActivity.this.service = service;
                synchronized (MyActivity.this) {
                    MyActivity.this.notifyAll();
                }
            }

            @Override
            public void onServiceDisconnected(final MangaDownloadService service) {

            }

        });

//        Intent intent = new Intent(this, MangaDownloadService.class);
//        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
//
//        Thread thread = new Thread() {
//
//            @Override
//            public void run() {
//                Manga manga = Mock.getMockManga();
//                RepositoryEngine engine = manga.getRepository().getEngine();
//                try {
//                    engine.queryForChapters(manga);
//                    while (service == null) {
//                        synchronized (MyActivity.this) {
//                            try {
//                                MyActivity.this.wait();
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                    service.addDownload(manga, 0, 1);
//                } catch (HttpRequestException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        thread.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.myactivity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        aav = new AnimatedActionView(this, menu, R.id.refresh, R.drawable.ic_action_refresh, R.anim.rotation);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
        if (aav.isAnimating()) {
            aav.stopAnimation();
        } else {
            aav.startAnimation();
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public void firstTest(View view) {
        Intent intent = new Intent(this, MangaViewTestActivity.class);
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

    public void fifthTest(View view) {
        Intent intent = new Intent(this, DownloadsActivity.class);
        startActivity(intent);
    }

    public void sixthTest(View view) {
        Intent intent = new Intent(this, LocalImageActivity.class);
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
    LocalImageManager localImageManager = null;

    private HttpImageManager httpImageManager = null;
}
