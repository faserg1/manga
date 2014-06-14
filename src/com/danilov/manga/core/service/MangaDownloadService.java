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
import com.danilov.manga.core.http.HttpRequestException;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.model.MangaChapter;
import com.danilov.manga.core.repository.RepositoryEngine;
import com.danilov.manga.core.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Semyon Danilov on 12.06.2014.
 */
public class MangaDownloadService extends Service {

    private static final String TAG = "MangaDownloadService";

    public static final int PROGRESS = 0;
    public static final int PAUSE = 1;
    public static final int RESUME = 2;
    public static final int COMPLETE = 3;
    public static final int CANCEL = 4;
    public static final int ERROR = 5;

    private Handler serviceHandler = null;

    private DownloadManager downloadManager = null;

    private final List<Handler> observerHandlers = new LinkedList<Handler>();

    private Queue<MangaDownloadRequest> requests = new LinkedList<MangaDownloadRequest>();

    private MangaDownloadRequest currentRequest;

    @Override
    public void onCreate() {
        super.onCreate();
        downloadManager = new DownloadManager();
        serviceHandler = new DownloadServiceHandler();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MDownloadServiceBinder();
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
        public static final int START_NEXT_REQUEST = 2;

        @Override
        public void handleMessage(final Message msg) {
            int what = msg.what;
            switch (what) {
                case ADD_DOWNLOAD:
                    MangaDownloadRequest mangaDownloadRequest = (MangaDownloadRequest) msg.obj;
                    if (requests.isEmpty()) {
                        currentRequest = mangaDownloadRequest;
                        requests.add(mangaDownloadRequest);
                        (new MangaDownloadThread(mangaDownloadRequest)).start();
                    } else {
                        requests.add(mangaDownloadRequest);
                    }
                    break;
                case START_NEXT_CHAPTER:
                    MangaDownloadRequest request = requests.poll();
                    (new MangaDownloadThread(request)).start();
                    break;
                case START_NEXT_REQUEST:
                    requests.remove();
                    if (requests.isEmpty()) {
                        return;
                    }
                    MangaDownloadRequest nextRequest = requests.poll();
                    currentRequest = nextRequest;
                    (new MangaDownloadThread(nextRequest)).start();
                    break;
                default:
                    break;
            }
        }
    }

    public void addDownload(final Manga manga, final int from, final int to) {
        Message message = Message.obtain();
        message.what = DownloadServiceHandler.ADD_DOWNLOAD;
        message.obj = new MangaDownloadRequest(manga, from, to);
        serviceHandler.sendMessage(message);
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
            List<String> urls = null;
            try {
                urls = engine.getChapterImages(chapter);
            } catch (HttpRequestException e) {
                e.printStackTrace();
                return;
            }
            String path = Utils.createPathForMangaChapter(manga, request.current, MangaDownloadService.this) + "/";
            int i = 0;
            currentQuantity = urls.size();
            for (String url : urls) {
                downloadManager.startDownload(url, path + i + ".png", request.current);
                i++;
            }
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

    private int currentQuantity;



    private class MangaDownloadListener implements DownloadManager.DownloadProgressListener {

        @Override
        public void onProgress(final DownloadManager.Download download, final int progress) {

        }

        @Override
        public void onPause(final DownloadManager.Download download) {

        }

        @Override
        public void onResume(final DownloadManager.Download download) {

        }

        @Override
        public void onComplete(final DownloadManager.Download download) {
            currentQuantity--;
            if (currentQuantity == 0) {
                if (currentRequest.current == currentRequest.to) {
                    Message message = Message.obtain();
                    message.what = DownloadServiceHandler.START_NEXT_REQUEST;
                    serviceHandler.sendMessage(message);
                } else {
                    currentRequest.current++;
                    Message message = Message.obtain();
                    message.what = DownloadServiceHandler.START_NEXT_CHAPTER;
                    serviceHandler.sendMessage(message);
                }
            }
        }

        @Override
        public void onCancel(final DownloadManager.Download download) {

        }

        @Override
        public void onError(final DownloadManager.Download download) {

        }
    }

    public void addObserver(final Handler handler) {
        synchronized (observerHandlers) {
            observerHandlers.add(handler);
        }
    }

    public void notifyObservers(final Message message) {
        synchronized (observerHandlers) {
            for (Handler handler : observerHandlers) {
                handler.sendMessage(message);
            }
        }
    }

    public void removeObserver(final Handler handler) {
        synchronized (observerHandlers) {
            observerHandlers.remove(handler);
        }
    }


}
