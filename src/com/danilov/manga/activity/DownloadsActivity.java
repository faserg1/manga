package com.danilov.manga.activity;

import android.app.Activity;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.danilov.manga.R;
import com.danilov.manga.core.service.MangaDownloadService;
import com.danilov.manga.core.service.MangaDownloadService.MangaDownloadRequest;
import com.danilov.manga.core.util.Pair;

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


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloads_activity);

        chaptersProgressBar = (ProgressBar) findViewById(R.id.chaptersProgressBar);
        imageProgressBar = (ProgressBar) findViewById(R.id.imageProgressBar);
        chaptersProgress = (TextView) findViewById(R.id.chaptersProgress);
        imagesProgress = (TextView) findViewById(R.id.imageProgress);

    }

    @Override
    protected void onResume() {
        super.onResume();
        ServiceConnection serviceConnection = new MangaDownloadService.MDownloadServiceConnection(new ServiceConnectionListener());
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
        String progressText = pair.first + "/" + pair.second;
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
        String progressText = passed + "/" + quantity;
        chaptersProgress.setText(progressText);
        chaptersProgressBar.setProgress(passed);
    }

    private void onCancel(final Message message) {

    }

    private void onError(final Message message) {

    }

    private void onStatus(final Message message) {
        MangaDownloadRequest request = (MangaDownloadRequest) message.obj;
        int currImage = message.arg1;
        int currImageQuantity = message.arg2;
        String progressText = currImage + "/" + currImageQuantity;
        imagesProgress.setText(progressText);
        int currentChapter = request.getCurrentChapter();
        int quantity = request.quantity;
        int passed = currentChapter - request.from;
        chaptersProgressBar.setMax(quantity);
        chaptersProgressBar.setProgress(passed);
        progressText = passed + "/" + quantity;
        chaptersProgress.setText(progressText);
    }

    private class ServiceConnectionListener implements MangaDownloadService.ServiceConnectionListener {

        @Override
        public void onServiceConnected(final MangaDownloadService service) {
            DownloadsActivity.this.service = service;
        }

        @Override
        public void onServiceDisconnected(final MangaDownloadService service) {

        }

    }

}