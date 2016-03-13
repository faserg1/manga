package com.danilov.supermanga;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.danilov.supermanga.activity.DownloadsActivity;
import com.danilov.supermanga.activity.HistoryActivity;
import com.danilov.supermanga.activity.MangaInfoActivity;
import com.danilov.supermanga.activity.MangaQueryActivity;
import com.danilov.supermanga.core.service.MangaDownloadService;
import com.danilov.supermanga.core.view.AnimatedActionView;
import com.danilov.supermanga.test.LocalMangaActivity;
import com.danilov.supermanga.test.QueryTestActivity;

public class MyActivity extends AppCompatActivity {

    private MangaDownloadService service = null;


    private AnimatedActionView aav = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (aav.isAnimating()) {
            aav.stopAnimation();
        } else {
            aav.startAnimation();
        }
        return super.onOptionsItemSelected(item);
    }

    public void firstTest(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    public void secondTest(View view) {
        Intent intent = new Intent(this, QueryTestActivity.class);
        startActivity(intent);
    }

    public void fourthTest(View view) {
        Intent intent = new Intent(this, LocalMangaActivity.class);
        startActivity(intent);
    }

    public void fifthTest(View view) {
        Intent intent = new Intent(this, DownloadsActivity.class);
        startActivity(intent);
    }

    public void sixthTest(View view) {
        Intent intent = new Intent(this, MangaQueryActivity.class);
        startActivity(intent);
    }

    public void thirdTest(View view) {
        Intent intent = new Intent(this, MangaInfoActivity.class);
        startActivity(intent);
    }

}
