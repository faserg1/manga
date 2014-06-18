package com.danilov.manga.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.danilov.manga.R;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.repository.RepositoryEngine;
import com.danilov.manga.core.service.MangaDownloadService;
import com.danilov.manga.core.service.MangaDownloadService.MangaDownloadRequest;
import com.danilov.manga.core.util.Pair;
import com.danilov.manga.test.Mock;

/**
 * Created by Semyon Danilov on 14.06.2014.
 */
public class DownloadsActivity extends Activity {

    private Handler handler;
    private MangaDownloadService service;

    private ProgressBar chaptersProgressBar;
    private ProgressBar imageProgressBar;

    private TextView chaptersProgress;
    private TextView imagesProgress;

    private ServiceConnection serviceConnection;

    private Intent serviceIntent;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloads_activity);

        chaptersProgressBar = (ProgressBar) findViewById(R.id.chaptersProgressBar);
        imageProgressBar = (ProgressBar) findViewById(R.id.imageProgressBar);
        chaptersProgress = (TextView) findViewById(R.id.chaptersProgress);
        imagesProgress = (TextView) findViewById(R.id.imageProgress);

        handler = new ServiceMessagesHandler();

    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceConnection = new MangaDownloadService.MDownloadServiceConnection(new ServiceConnectionListener());
        serviceIntent = new Intent(this, MangaDownloadService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private class ServiceMessagesHandler extends Handler {

        @Override
        public void handleMessage(final Message message) {
            int what = message.what;
            switch (what) {
                case MangaDownloadService.PROGRESS:
                    onProgress(message);
                    break;
                case MangaDownloadService.PAUSE:
                    onPause(message);
                    break;
                case MangaDownloadService.RESUME:
                    onResume(message);
                    break;
                case MangaDownloadService.REQUEST_COMPLETE:
                    onRequestComplete(message);
                    break;
                case MangaDownloadService.CHAPTER_COMPLETE:
                    onChapterComplete(message);
                    break;
                case MangaDownloadService.CANCEL:
                    onCancel(message);
                    break;
                case MangaDownloadService.ERROR:
                    onError(message);
                    break;
                case MangaDownloadService.STATUS:
                    onStatus(message);
                    break;
            }
        }
    }

    private void onProgress(final Message message) {
        int progress = message.arg1;
        int max = message.arg2;
        imageProgressBar.setMax(max);
        imageProgressBar.setProgress(progress);
    }

    private void onPause(final Message message) {
    }

    private void onResume(final Message message) {
        int max = message.arg1;
        imageProgressBar.setMax(max);
        imageProgressBar.setProgress(0);
        Pair pair = (Pair) message.obj;
        int currentImage = (Integer) pair.first + 1; //what image is processed now starting with 1 not zero
        String progressText = currentImage + "/" + pair.second;
        imagesProgress.setText(progressText);
        pair.retrieve();
    }

    private void onRequestComplete(final Message message) {

    }

    private void onChapterComplete(final Message message) {
        int currentChapter = message.arg1;
        int quantity = message.arg2;
        MangaDownloadRequest request = (MangaDownloadRequest) message.obj;
        int passed = currentChapter - request.from;
        String progressText = ++passed + "/" + quantity;
        chaptersProgress.setText(progressText);
        chaptersProgressBar.setProgress(passed);
    }

    private void onCancel(final Message message) {

    }

    private void onError(final Message message) {

    }

    private void onStatus(final Message message) {
        MangaDownloadRequest request = (MangaDownloadRequest) message.obj;
        int currImage = message.arg1 + 1; //what image is processed now starting with 1 not zero
        int currImageQuantity = message.arg2;
        String progressText = currImage + "/" + currImageQuantity;
        imagesProgress.setText(progressText);
        int currentChapter = request.getCurrentChapter();
        int quantity = request.quantity;
        int passed = currentChapter - request.from;
        chaptersProgressBar.setMax(quantity);
        chaptersProgressBar.setProgress(passed);
        progressText = ++passed + "/" + quantity;
        chaptersProgress.setText(progressText);
    }

    @Override
    protected void onPause() {
        unbindService(serviceConnection);
        service.removeObserver(handler);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class ServiceConnectionListener implements MangaDownloadService.ServiceConnectionListener {

        @Override
        public void onServiceConnected(final MangaDownloadService service) {
            DownloadsActivity.this.service = service;
            service.addObserver(handler);
            startService(serviceIntent);
        }

        @Override
        public void onServiceDisconnected(final MangaDownloadService service) {
            service.removeObserver(handler);
        }

    }

    public void test(View view) {
        Thread t = new Thread() {

            @Override
            public void run() {
                Manga manga = Mock.getMockManga();
                RepositoryEngine engine = manga.getRepository().getEngine();
                try {
                    engine.queryForChapters(manga);
                    while (service == null) {
                        synchronized (DownloadsActivity.this) {
                            try {
                                DownloadsActivity.this.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    service.addDownload(manga, 2, 4);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
        };
        t.start();
    }

}