package com.danilov.manga.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.danilov.manga.R;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.repository.RepositoryEngine;
import com.danilov.manga.core.service.MangaDownloadService;
import com.danilov.manga.core.service.MangaDownloadService.MangaDownloadRequest;
import com.danilov.manga.core.util.Constants;
import com.danilov.manga.core.util.Pair;
import com.danilov.manga.core.util.Utils;

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

    private EditText from;
    private EditText to;

    private ServiceConnection serviceConnection;

    private Intent serviceIntent;

    private Button restartButton;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloads_activity);

        chaptersProgressBar = (ProgressBar) findViewById(R.id.chaptersProgressBar);
        imageProgressBar = (ProgressBar) findViewById(R.id.imageProgressBar);
        chaptersProgress = (TextView) findViewById(R.id.chaptersProgress);
        imagesProgress = (TextView) findViewById(R.id.imageProgress);

        from = (EditText) findViewById(R.id.from);
        to = (EditText) findViewById(R.id.to);

        handler = new ServiceMessagesHandler();

        Intent i = getIntent();
        final Manga manga = i.getParcelableExtra(Constants.MANGA_PARCEL_KEY);
        restartButton = (Button) findViewById(R.id.restart);
        restartButton.setEnabled(false);
        restartButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                service.restartDownload();
            }

        });
        (findViewById(R.id.test_button)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                test(manga);
            }

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceConnection = new MangaDownloadService.MDownloadServiceConnection(new ServiceConnectionListener());
        serviceIntent = new Intent(this, MangaDownloadService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Connecting to service");
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
        chaptersProgressBar.setProgress(currentChapter);
        String progressText = ++currentChapter + "/" + quantity;
        chaptersProgress.setText(progressText);
    }

    private void onCancel(final Message message) {

    }

    private void onError(final Message message) {
        Pair p = (Pair) message.obj;
        Utils.showToast(this, String.valueOf(p.second));
        restartButton.setEnabled(true);
    }

    private void onStatus(final Message message) {
        MangaDownloadRequest request = (MangaDownloadRequest) message.obj;
        int currImage = message.arg1 + 1; //what image is processed now starting with 1 not zero
        int currImageQuantity = message.arg2;
        String progressText = currImage + "/" + currImageQuantity;
        imagesProgress.setText(progressText);
        int currentChapter = request.getCurrentChapterInList();
        int quantity = request.quantity;
        chaptersProgressBar.setMax(quantity);
        chaptersProgressBar.setProgress(currentChapter);
        progressText = ++currentChapter + "/" + quantity;
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

    private static final String TAG = "DownloadsActivity";

    public void test(final Manga manga) {
        Thread t = new Thread() {

            @Override
            public void run() {
                RepositoryEngine engine = manga.getRepository().getEngine();
                try {
                    engine.queryForChapters(manga);
                    while (service == null) {
                        Log.d(TAG, "service == null, waiting");
                        synchronized (DownloadsActivity.this) {
                            try {
                                DownloadsActivity.this.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    String fromVal = from.getText().toString();
                    String toVal = to.getText().toString();
                    Integer from = Integer.valueOf(fromVal);
                    Integer to = Integer.valueOf(toVal);
                    int sz = manga.getChapters().size();
                    if (from < 0 || to >= sz) {
                        return;
                    }
                    service.addDownload(manga, from, to);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
        };
        t.start();
    }

}