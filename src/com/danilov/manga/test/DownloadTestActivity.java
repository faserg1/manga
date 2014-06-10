package com.danilov.manga.test;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.danilov.manga.R;
import com.danilov.manga.core.service.DownloadManager;

import java.io.File;

/**
 * Created by Semyon Danilov on 11.06.2014.
 */
public class DownloadTestActivity extends Activity {

    private static final String TAG = "DownloadTestActivity";

    private Handler downloadHandler;
    private DownloadManager downloadManager = new DownloadManager();

    private TextView progressText;
    private ProgressBar progressBar;
    private EditText urlEditText;

    private File mydir;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_download_activity);
        progressText = (TextView) findViewById(R.id.progressAsText);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        urlEditText = (EditText) findViewById(R.id.url);
        mydir = getBaseContext().getDir("mydir", Context.MODE_PRIVATE);
        downloadHandler = new DownloadHandler();
        downloadManager.setListener(new DownloadListener());
        downloadManager.startDownload("http://he.readmanga.ru/auto/11/52/35/03.png", mydir.getPath() + "/1.png");
        downloadManager.startDownload("http://hb.readmanga.ru/auto/11/52/35/04.png", mydir.getPath() + "/2.png");
    }

    public void load(View view) {
        String url = urlEditText.getText().toString();
        String[] splittedUrl = url.split("/");
        String fileName = splittedUrl[splittedUrl.length - 1];
        downloadManager.startDownload(url, mydir.getPath() + "/" + fileName);
    }

    public class DownloadHandler extends Handler {

        private static final int ON_PROGRESS = 0;
        private static final int ON_RESUME = 1;

        @Override
        public void handleMessage(final Message msg) {
            int what = msg.what;
            switch (what) {
                case ON_PROGRESS:
                    int progress = msg.arg1;
                    String prgrs = (String) msg.obj;
                    progressBar.setProgress(progress);
                    progressText.setText(prgrs);
                    break;
                case ON_RESUME:
                    int max = msg.arg1;
                    progressBar.setMax(max);
                    progressBar.setProgress(0);
                    break;
                default:
                    Log.d(TAG, "Nothing to do here");
                    break;
            }
        }
    }

    private class DownloadListener implements DownloadManager.DownloadProgressListener {

        int max = 0;

        @Override
        public void onProgress(final DownloadManager.Download download, final int progress) {
            Message message = Message.obtain();
            message.what = DownloadHandler.ON_PROGRESS;
            int progressPercent = (int) (((float) progress / max) * 100);
            String prgrs = progressPercent + "%";
            Log.d(TAG, prgrs);
            message.arg1 = progress;
            message.obj = prgrs;
            downloadHandler.sendMessage(message);
        }

        @Override
        public void onPause(final DownloadManager.Download download) {

        }

        @Override
        public void onResume(final DownloadManager.Download download) {
            Message message = Message.obtain();
            message.what = DownloadHandler.ON_RESUME;
            max = download.getSize();
            message.arg1 = max;
            downloadHandler.sendMessage(message);
        }

        @Override
        public void onComplete(final DownloadManager.Download download) {

        }

        @Override
        public void onCancel(final DownloadManager.Download download) {

        }

        @Override
        public void onError(final DownloadManager.Download download) {

        }

    }

}