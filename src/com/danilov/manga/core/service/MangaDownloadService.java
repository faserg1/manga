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
import com.danilov.manga.core.util.Pair;
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
    public static final int REQUEST_COMPLETE = 3;
    public static final int CHAPTER_COMPLETE = 4;
    public static final int CANCEL = 5;
    public static final int ERROR = 6;
    public static final int STATUS = 7;

    private Handler serviceHandler = null;

    private DownloadManager downloadManager = null;

    private final List<Handler> observerHandlers = new LinkedList<Handler>();

    private Queue<MangaDownloadRequest> requests = new LinkedList<MangaDownloadRequest>();

    private MangaDownloadRequest currentRequest;

    private int currentImageQuantity;
    private int currentImage;

    @Override
    public void onCreate() {
        super.onCreate();
        downloadManager = new DownloadManager();
        downloadManager.setListener(new MangaDownloadListener());
        serviceHandler = new DownloadServiceHandler();
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return new MDownloadServiceBinder();
    }

    @Override
    public void onStart(final Intent intent, final int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
                        currentRequest = null;
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

    private void sendStatus() {
        if (currentRequest == null) {
            return;
        }
        Message message = Message.obtain();
        message.arg1 = currentImage;
        message.arg2 = currentImageQuantity;
        message.obj = currentRequest;
        message.what = STATUS;
        notifyObservers(message);
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
            MangaChapter chapter = chapters.get(request.currentChapter);
            List<String> urls = null;
            try {
                urls = engine.getChapterImages(chapter);
            } catch (HttpRequestException e) {
                e.printStackTrace();
                return;
            }
            String path = Utils.createPathForMangaChapter(manga, request.currentChapter, MangaDownloadService.this) + "/";
            int i = 0;
            currentImage = 0;
            currentImageQuantity = urls.size();
            sendStatus();
            for (String url : urls) {
                downloadManager.startDownload(url, path + i + ".png", request.currentChapter);
                i++;
            }
        }

    }

    public class MangaDownloadRequest {

        public int to;

        public int from;

        private int currentChapter;

        public int quantity;

        public Manga manga;

        public synchronized void incCurChapter() {
            currentChapter++;
        }

        public synchronized int getCurrentChapter() {
            return currentChapter;
        }

        public MangaDownloadRequest(final Manga manga, final int from, final int to) {
            this.manga = manga;
            this.from = from;
            this.currentChapter = from;
            this.to = to;
            this.quantity = to - from + 1;
        }

    }

    private class MangaDownloadListener implements DownloadManager.DownloadProgressListener {

        private int currentSize = -1;

        @Override
        public void onProgress(final DownloadManager.Download download, final int progress) {
            Message message = Message.obtain();
            message.what = PROGRESS;
            message.arg1 = progress;
            message.arg2 = currentSize;
            notifyObservers(message);
        }

        @Override
        public void onPause(final DownloadManager.Download download) {

        }

        @Override
        public void onResume(final DownloadManager.Download download) {
            Message message = Message.obtain();
            message.what = RESUME;
            currentSize = download.getSize();
            message.arg1 = currentSize;
            Pair pair = Pair.obtain();
            pair.first = currentImage;
            pair.second = currentImageQuantity;
            message.obj = pair;
            notifyObservers(message);
        }

        @Override
        public void onComplete(final DownloadManager.Download download) {
            currentImage++;
            if (currentImage == currentImageQuantity) {
                if (currentRequest.currentChapter == currentRequest.to) {
                    //go to next request
                    Message message = Message.obtain();
                    message.what = DownloadServiceHandler.START_NEXT_REQUEST;
                    serviceHandler.sendMessage(message);

                    message = Message.obtain(); //that's a new message
                    message.what = REQUEST_COMPLETE;
                    message.obj = currentRequest.manga;
                    notifyObservers(message);
                } else {
                    //go to next chapter
                    currentRequest.incCurChapter();
                    Message message = Message.obtain();
                    message.what = DownloadServiceHandler.START_NEXT_CHAPTER;
                    serviceHandler.sendMessage(message);

                    message = Message.obtain(); //that's a new message too
                    message.what = CHAPTER_COMPLETE;
                    message.obj = currentRequest;
                    message.arg1 = currentRequest.currentChapter;
                    message.arg2 = currentRequest.quantity;
                    notifyObservers(message);
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
        sendStatus();
    }

    private void notifyObservers(final Message message) {
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
