package com.danilov.manga.core.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.model.MangaChapter;
import com.danilov.manga.core.repository.RepositoryEngine;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Semyon Danilov on 12.06.2014.
 */
public class MangaDownloadService extends Service {

    private static final String TAG = "MangaDownloadService";

    private Handler serviceHandler = null;

    private DownloadManager downloadManager = null;

    private List<Handler> observerHandlers = null;

    private Queue<MangaDownloadRequest> requests = new LinkedList<MangaDownloadRequest>();

    @Override
    public void onCreate() {
        super.onCreate();
        downloadManager = new DownloadManager();
        serviceHandler = new DownloadServiceHandler();
        observerHandlers = new LinkedList<Handler>();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(final Intent intent, final int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        return Service.START_STICKY;
    }

    private class MDownloadServiceBinder extends Binder {

        public MangaDownloadService getService() {
            return MangaDownloadService.this;
        }

    }

    public static class MDownloadServiceConnection implements ServiceConnection {

        private ServiceConnectionListener listener;
        private MangaDownloadService service;


        public MDownloadServiceConnection(final ServiceConnectionListener listener) {
            this.listener = listener;
        }

        @Override
        public void onServiceConnected(final ComponentName componentName, final IBinder iBinder) {
            Log.d(TAG, "Service connected");
            if (listener != null) {
                MDownloadServiceBinder binder = (MDownloadServiceBinder) iBinder;
                service = binder.getService();
                listener.onServiceConnected(service);
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName componentName) {
            Log.d(TAG, "Service disconnected");
            if (listener != null) {
                listener.onServiceDisconnected(service);
            }
        }

    }

    public class DownloadServiceHandler extends Handler {

        // whats:

        public static final int ADD_DOWNLOAD = 0;
        public static final int START_NEXT_CHAPTER = 1;

        @Override
        public void handleMessage(final Message msg) {
            int what = msg.what;
            switch (what) {
                case ADD_DOWNLOAD:
                    MangaDownloadRequest mangaDownloadRequest = (MangaDownloadRequest) msg.obj;
                    (new MangaDownloadThread(mangaDownloadRequest)).start();
                    break;
                case START_NEXT_CHAPTER:
                    MangaDownloadRequest request = requests.poll();
                    (new MangaDownloadThread(request)).start();
                    break;
                default:
                    break;
            }
        }
    }

    public interface ServiceConnectionListener {

        void onServiceConnected(final MangaDownloadService service);

        void onServiceDisconnected(final MangaDownloadService service);

    }

    private class MangaDownloadThread extends Thread {

        private MangaDownloadRequest request;

        public MangaDownloadThread(final MangaDownloadRequest request) {
            this.request = request;
        }

        @Override
        public void run() {
            final Manga manga = request.manga;
            final RepositoryEngine engine = manga.getRepository().getEngine();
            List<MangaChapter> chapters = manga.getChapters();
            MangaChapter chapter = chapters.get(request.current);

        }

    }

    public class MangaDownloadRequest {

        protected int to;

        protected int current;

        protected Manga manga;

        public MangaDownloadRequest(final Manga manga, final int from, final int to) {
            this.manga = manga;
            this.current = from;
            this.to = to;
        }

    }

}
