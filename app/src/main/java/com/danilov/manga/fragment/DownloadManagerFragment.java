package com.danilov.manga.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.danilov.manga.R;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.repository.RepositoryEngine;
import com.danilov.manga.core.service.MangaDownloadService;
import com.danilov.manga.core.util.Constants;
import com.danilov.manga.core.util.Pair;
import com.danilov.manga.core.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon on 15.11.2014.
 */
public class DownloadManagerFragment extends BaseFragment {

    private static final String TAG = "DownloadManagerFragment";

    private Handler handler;
    private MangaDownloadService service;

    private ProgressBar chaptersProgressBar;
    private ProgressBar imageProgressBar;

    private TextView chaptersProgress;
    private TextView imagesProgress;

    private ServiceConnection serviceConnection;

    private Intent serviceIntent;

    private Button restartButton;
    private Button skipButton;

    private ListView listView;

    private ActionBarActivity actionBarActivity;

    private Context context;

    public static DownloadManagerFragment newInstance() {
        return new DownloadManagerFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.manga_downloadmanager_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBarActivity = (ActionBarActivity) getActivity();
        context = actionBarActivity;

        chaptersProgressBar = findViewById(R.id.chaptersProgressBar);
        imageProgressBar = findViewById(R.id.imageProgressBar);
        chaptersProgress = findViewById(R.id.chaptersProgress);
        imagesProgress = findViewById(R.id.imageProgress);
        listView = findViewById(R.id.download_queue);

        handler = new ServiceMessagesHandler();

        Intent i = actionBarActivity.getIntent();
        final Manga manga = i.getParcelableExtra(Constants.MANGA_PARCEL_KEY);
        final ArrayList<Integer> selectedChapters = i.getIntegerArrayListExtra(Constants.SELECTED_CHAPTERS_KEY);
        if (selectedChapters != null) {
            startDownload(manga, selectedChapters);
            i.removeExtra(Constants.SELECTED_CHAPTERS_KEY);
        }


        restartButton = (Button) findViewById(R.id.restart);
        skipButton = (Button) findViewById(R.id.skip);
        restartButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                service.restartDownload();
            }

        });
        skipButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                service.skipImage();
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        serviceConnection = new MangaDownloadService.MDownloadServiceConnection(new ServiceConnectionListener());
        serviceIntent = new Intent(actionBarActivity, MangaDownloadService.class);
        actionBarActivity.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
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
        imageProgressBar.setMax(1);
        imageProgressBar.setProgress(1);
        chaptersProgressBar.setMax(1);
        chaptersProgressBar.setProgress(1);
    }

    private void onChapterComplete(final Message message) {
        int currentChapter = message.arg1;
        int quantity = message.arg2;
        MangaDownloadService.MangaDownloadRequest request = (MangaDownloadService.MangaDownloadRequest) message.obj;
        chaptersProgressBar.setProgress(currentChapter);
        String progressText = ++currentChapter + "/" + quantity;
        chaptersProgress.setText(progressText);
    }

    private void onCancel(final Message message) {

    }

    private void onError(final Message message) {
        Pair p = (Pair) message.obj;
        Utils.showToast(context, String.valueOf(p.second));
    }

    private void onStatus(final Message message) {
        Pair p = (Pair) message.obj;
        MangaDownloadService.MangaDownloadRequest request = (MangaDownloadService.MangaDownloadRequest) p.first;
        List<MangaDownloadService.MangaDownloadRequest> requests = (List<MangaDownloadService.MangaDownloadRequest>) p.second;

        RequestsAdapter adapter = new RequestsAdapter(context, android.R.layout.simple_list_item_1, requests);
        listView.setAdapter(adapter);

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
    public void onPause() {
        actionBarActivity.unbindService(serviceConnection);
        service.removeObserver(handler);
        super.onPause();
    }

    private class ServiceConnectionListener implements MangaDownloadService.ServiceConnectionListener {

        @Override
        public void onServiceConnected(final MangaDownloadService service) {
            DownloadManagerFragment.this.service = service;
            service.addObserver(handler);
            actionBarActivity.startService(serviceIntent);
        }

        @Override
        public void onServiceDisconnected(final MangaDownloadService service) {
            service.removeObserver(handler);
        }

    }

    private void startDownload(final Manga manga, final ArrayList<Integer> selectedChapters) {
        Thread t = new Thread() {

            @Override
            public void run() {
                RepositoryEngine engine = manga.getRepository().getEngine();
                try {
                    engine.queryForChapters(manga);
                    service.addDownload(manga, selectedChapters);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
        t.start();
    }

    private class RequestsAdapter extends ArrayAdapter<MangaDownloadService.MangaDownloadRequest> {

        private List<MangaDownloadService.MangaDownloadRequest> requests = null;

        public RequestsAdapter(final Context context, final int resource, final List<MangaDownloadService.MangaDownloadRequest> objects) {
            super(context, resource, objects);
            requests = objects;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TextView view = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, null);
            MangaDownloadService.MangaDownloadRequest request = requests.get(position);
            view.setText(request.getManga().getTitle());
            return view;
        }

    }

}
