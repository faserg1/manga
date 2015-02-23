package com.danilov.mangareader.core.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.android.httpimage.HttpImageManager;
import com.danilov.mangareader.core.database.DatabaseAccessException;
import com.danilov.mangareader.core.database.MangaDAO;
import com.danilov.mangareader.core.model.LocalManga;
import com.danilov.mangareader.core.model.Manga;
import com.danilov.mangareader.core.model.MangaChapter;
import com.danilov.mangareader.core.notification.NotificationHelper;
import com.danilov.mangareader.core.repository.RepositoryEngine;
import com.danilov.mangareader.core.repository.RepositoryException;
import com.danilov.mangareader.core.util.IoUtils;
import com.danilov.mangareader.core.util.Pair;
import com.danilov.mangareader.core.util.SafeHandler;
import com.danilov.mangareader.core.util.ServiceContainer;

import java.io.File;
import java.util.ArrayList;
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

    private MangaDAO mangaDAO = null;

    private final List<SafeHandler> observerHandlers = new LinkedList<SafeHandler>();

    private Queue<MangaDownloadRequest> requests = new LinkedList<MangaDownloadRequest>();

    private MangaDownloadRequest currentRequest;

    private int currentImageQuantity;
    private int currentImage;

    private HttpImageManager httpImageManager = ServiceContainer.getService(HttpImageManager.class);

    private NotificationHelper helper = null;


    @Override
    public void onCreate() {
        super.onCreate();
        helper = new NotificationHelper(this);
        downloadManager = new DownloadManager();
        downloadManager.setListener(new MangaDownloadListener());
        mangaDAO = ServiceContainer.getService(MangaDAO.class);
        serviceHandler = new DownloadServiceHandler();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return new MDownloadServiceBinder();
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
        public static final int RESTART_ERROR = 3;
        public static final int SKIP_PICTURE = 4;
        public static final int PAUSE = 5;
        public static final int RESUME = 6;

        @Override
        public void handleMessage(final Message msg) {
            int what = msg.what;
            switch (what) {
                case ADD_DOWNLOAD:
                    MangaDownloadRequest mangaDownloadRequest = (MangaDownloadRequest) msg.obj;
                    if (requests.isEmpty()) {
                        showNotification(mangaDownloadRequest.getManga());
                        currentRequest = mangaDownloadRequest;
                        updateNotificationProgress(currentRequest.quantity, 0);
                        requests.add(mangaDownloadRequest);
                        (new MangaDownloadThread(mangaDownloadRequest)).start();
                    } else {
                        requests.add(mangaDownloadRequest);
                        sendStatus();
                    }
                    break;
                case START_NEXT_CHAPTER:
                    MangaDownloadRequest request = requests.peek();
                    updateNotificationProgress(request.quantity, request.getCurrentChapterInList());
                    (new MangaDownloadThread(request)).start();
                    break;
                case START_NEXT_REQUEST:
                    requests.remove();
                    if (requests.isEmpty()) {
                        currentRequest = null;
                        helper.finish();
                        return;
                    }
                    MangaDownloadRequest nextRequest = requests.peek();
                    currentRequest = nextRequest;
                    showNotification(currentRequest.getManga());
                    (new MangaDownloadThread(nextRequest)).start();
                    break;
                case RESTART_ERROR:
                    currentRequest.setHasError(false);
                    downloadManager.restartError();
                    break;
                case SKIP_PICTURE:
                    downloadManager.skipDownload();
                    break;
                case PAUSE:
                    downloadManager.pauseDownload();
                    break;
                case RESUME:
                    downloadManager.resumeDownload();
                    break;
                default:
                    break;
            }
        }
    }

    private void showNotification(final Manga manga) {
        helper.buildNotification(manga);

        Uri coverUri = Uri.parse(manga.getCoverUri());
        HttpImageManager.LoadRequest request = HttpImageManager.LoadRequest.obtain(coverUri, helper.getIconView(), 110);
        Bitmap bitmap = httpImageManager.loadImage(request);
        if (bitmap != null) {
            helper.setIcon(bitmap);
        }

    }

    private void updateNotificationProgress(final int max, final int progress) {
        helper.updateProgress(max, progress);
    }

    private void sendStatus() {
        if (currentRequest == null) {
            return;
        }

        List<MangaDownloadRequest> rqs = new ArrayList<MangaDownloadRequest>(requests.size());
        for (MangaDownloadRequest request : requests) {
            if (currentRequest == request) {
                continue;
            }
            rqs.add(request);
        }

        Pair pair = Pair.obtain(currentRequest, rqs);

        Message message = Message.obtain();
        message.arg1 = currentImage;
        message.arg2 = currentImageQuantity;
        message.obj = pair;
        message.what = STATUS;
        notifyObservers(message);
    }

    private void sendError(final MangaDownloadRequest request, final String error) {
        Message message = Message.obtain();
        message.what = ERROR;
        Pair pair = Pair.obtain();
        pair.first = request;
        pair.second = error;
        message.obj = pair;
        notifyObservers(message);
    }

    public void addDownload(final Manga manga, final int from, final int to) {
        Message message = Message.obtain();
        message.what = DownloadServiceHandler.ADD_DOWNLOAD;
        message.obj = new MangaDownloadRequest(manga, from, to);
        serviceHandler.sendMessage(message);
    }

    public void addDownload(final Manga manga, final List<Integer> chapters) {
        Message message = Message.obtain();
        message.what = DownloadServiceHandler.ADD_DOWNLOAD;
        message.obj = new MangaDownloadRequest(manga, chapters);
        serviceHandler.sendMessage(message);
    }

    public void restartDownload() {
        Message message = Message.obtain();
        message.what = DownloadServiceHandler.RESTART_ERROR;
        serviceHandler.sendMessage(message);
    }

    public void skipImage() {
        Message message = Message.obtain();
        message.what = DownloadServiceHandler.SKIP_PICTURE;
        serviceHandler.sendMessage(message);
    }

    public void pause() {
        Message message = Message.obtain();
        message.what = DownloadServiceHandler.PAUSE;
        serviceHandler.sendMessage(message);
    }

    public void resume() {
        Message message = Message.obtain();
        message.what = DownloadServiceHandler.RESUME;
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
            MangaChapter chapter = request.getCurrentChapter();
            List<String> urls = null;
            try {
                urls = engine.getChapterImages(chapter);
            } catch (RepositoryException e) {
                e.printStackTrace();
                return;
            }
            for (int i = 0; i < urls.size(); i++) {
                String url = urls.get(i);
                if (!url.contains("http")) {
                    Log.d(TAG, "Added repo uri");
                    url = manga.getRepository().getEngine().getBaseUri() + "/" + url;
                }
                urls.set(i, url);
            }
            int curChapterNumber = request.getCurrentChapterNumber();
            String mangaPath = IoUtils.createPathForManga(manga, MangaDownloadService.this) + "/";
            String coverUri = mangaPath + "/cover";
            try {

                LocalManga _local = new LocalManga(manga.getTitle(), manga.getUri(), manga.getRepository());
                _local.setLocalUri(mangaPath);
                _local.setAuthor(manga.getAuthor());
                _local.setChaptersQuantity(manga.getChaptersQuantity());
                _local.setDescription(manga.getDescription());
                _local.setCoverUri(manga.getCoverUri());
                _local.setFavorite(manga.isFavorite());

                Manga localManga = mangaDAO.updateFromDownloadService(_local, manga.getChaptersQuantity());
                if (localManga != null) {
                    mangaPath = ((LocalManga)localManga).getLocalUri();
                    coverUri = mangaPath + "/cover";
                }
            } catch (DatabaseAccessException e) {
                //TODO: decide what do we need to do if can't store manga
                //I suppose, that we better cancel request, due to the fact
                // that manga won't be accessible from the app in case of failed ~DAO operation
                sendError(currentRequest, e.getMessage());
                Log.d(TAG, e.getMessage());
            }
            String chapterPath = IoUtils.createPathForMangaChapter(mangaPath, curChapterNumber) + "/";
            int i = 0;
            currentImage = 0;
            currentImageQuantity = urls.size();
            if (!new File(coverUri).exists()) {
                downloadManager.startDownload(manga.getCoverUri(), mangaPath + "/cover");
                currentImageQuantity++;
            }
            sendStatus();
            for (String url : urls) {
                downloadManager.startDownload(url, chapterPath + i + ".png", curChapterNumber);
                i++;
            }
        }

    }

    public class MangaDownloadRequest {

        private int currentChapterInList;

        private int currentChapter;

        public int quantity;

        public Manga manga;

        public List<Integer> whichChapters;

        private boolean hasError;
        private boolean isPaused = false;

        public synchronized void incCurChapter() {
            currentChapterInList++;
        }

        public synchronized int getCurrentChapterInList() {
            return currentChapterInList;
        }

        public MangaDownloadRequest(final Manga manga, final int from, final int to) {
            int size = (from == to) ? (1) : (to - from + 1);
            List<Integer> which = new ArrayList<Integer>(size);
            int _from = from;
            for (int i = 0; i < size; i++) {
                which.add(_from);
                _from++;
            }
            this.manga = manga;
            this.whichChapters = which;
            this.currentChapterInList = 0;
            this.quantity = which.size();
            this.currentChapter = which.get(0);
        }

        public MangaDownloadRequest(final Manga manga, final List<Integer> whichChapters) {
            this.manga = manga;
            this.whichChapters = whichChapters;
            this.currentChapterInList = 0;
            this.quantity = whichChapters.size();
            this.currentChapter = whichChapters.get(0);
        }

        public MangaChapter getCurrentChapter() {
            return manga.getChapters().get(whichChapters.get(currentChapterInList));
        }

        public int getCurrentChapterNumber() {
            return whichChapters.get(currentChapterInList);
        }

        public Manga getManga() {
            return manga;
        }

        public synchronized boolean isHasError() {
            return hasError;
        }

        public synchronized void setHasError(final boolean hasError) {
            this.hasError = hasError;
        }

        public synchronized boolean isPaused() {
            return isPaused;
        }

        public synchronized void setIsPaused(final boolean isPaused) {
            this.isPaused = isPaused;
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
            currentRequest.setIsPaused(true);
            Message message = Message.obtain();
            message.what = PAUSE;
            notifyObservers(message);
        }

        @Override
        public void onResume(final DownloadManager.Download download) {
            currentRequest.setIsPaused(false);
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
            if (download.getTag() == -1) {
                //TODO: tag = -1 will be applied for manga covers
                return;
            }
            currentImage++;
            if (currentImage == currentImageQuantity) {
                if (currentRequest.currentChapterInList == currentRequest.quantity - 1) {
                    //go to next request

                    //old bug: currentRequest  становился нулём после хэндла этого сообшения и ДО отправки следующего, и возника NPE (там было currentRequest.manga - NPE!)
                    //а уже в DownloadManager это просто ловилось и передавалось в OnError
                    //ппц
                    Manga manga = currentRequest.getManga();
                    Message message = Message.obtain();
                    message.what = DownloadServiceHandler.START_NEXT_REQUEST;
                    serviceHandler.sendMessage(message);

                    message = Message.obtain(); //that's a new message
                    message.what = REQUEST_COMPLETE;
                    message.obj = manga;
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
                    message.arg1 = currentRequest.currentChapterInList;
                    message.arg2 = currentRequest.quantity;
                    notifyObservers(message);
                }
            }
        }

        @Override
        public void onCancel(final DownloadManager.Download download) {

        }

        @Override
        public void onError(final DownloadManager.Download download, final String errorMsg) {
            if (currentRequest != null) {
                currentRequest.setHasError(true);
            } else {
                Log.d(TAG, download.getUri());
                currentRequest.setHasError(true);
            }
            sendError(currentRequest, errorMsg);
        }

    }

    public void addObserver(final SafeHandler handler) {
        synchronized (observerHandlers) {
            observerHandlers.add(handler);
        }
        sendStatus();
    }

    private void notifyObservers(final Message message) {
        Message currentMessage = message;
        synchronized (observerHandlers) {
            for (SafeHandler handler : observerHandlers) {
                Message tmp = Message.obtain();
                tmp.copyFrom(currentMessage);
                handler.sendMessage(currentMessage);
                currentMessage = tmp;
            }
        }
    }

    public void removeObserver(final SafeHandler handler) {
        synchronized (observerHandlers) {
            observerHandlers.remove(handler);
        }
    }


}
