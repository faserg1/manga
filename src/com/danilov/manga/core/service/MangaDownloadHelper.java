package com.danilov.manga.core.service;

import android.os.Handler;
import android.os.Message;
import com.danilov.manga.core.application.ApplicationSettings;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.repository.RepositoryEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon Danilov on 27.05.2014.
 */
public class MangaDownloadHelper {

    private DownloadManager downloadManager = new DownloadManager();
    private ApplicationSettings settings;

    private RepositoryEngine engine;

    private Manga manga;
    private List<Handler> handlers = new ArrayList<Handler>();

    public MangaDownloadHelper(final Manga manga, final Handler handler) {
        this.manga = manga;
        this.handlers.add(handler);
        this.downloadManager.setListener(new DownloadListener());
    }

    public void addHandler(final Handler handler) {
        this.handlers.add(handler);
    }

    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    public void setDownloadManager(final DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }

    public ApplicationSettings getSettings() {
        return settings;
    }

    public void setSettings(final ApplicationSettings settings) {
        this.settings = settings;
    }

    public RepositoryEngine getEngine() {
        return engine;
    }

    public void setEngine(final RepositoryEngine engine) {
        this.engine = engine;
    }

    public Manga getManga() {
        return manga;
    }

    public void setManga(final Manga manga) {
        this.manga = manga;
    }

    public class DownloadListener implements DownloadManager.DownloadProgressListener {

        @Override
        public void onProgress(final DownloadManager.Download download) {
            sendMessages(PROGRESS);
        }

        @Override
        public void onPause(final DownloadManager.Download download) {
            sendMessages(PAUSE);
        }

        @Override
        public void onResume(final DownloadManager.Download download) {
            sendMessages(RESUME);
        }

        @Override
        public void onComplete(final DownloadManager.Download download) {
            sendMessages(COMPLETE);
        }

        @Override
        public void onCancel(final DownloadManager.Download download) {
            sendMessages(CANCEL);
        }

        @Override
        public void onError(final DownloadManager.Download download) {
            sendMessages(ERROR);
        }

    }

    private void sendMessages(final int what) {
        for (Handler handler : handlers) {
            Message message = handler.obtainMessage(what);
            handler.sendMessage(message);
        }
    }

    public static final int PAUSE = 0;
    public static final int RESUME = 1;
    public static final int CANCEL = 2;
    public static final int COMPLETE = 3;
    public static final int ERROR = 4;
    public static final int PROGRESS = 5;

}
