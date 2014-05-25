package com.danilov.manga.core.service;

/**
 * Created by Semyon Danilov on 26.05.2014.
 */
public class DownloadManager {

    private static final int MAX_BUFFER_SIZE = 1024;



    public class Download implements Runnable {

        private String uri;
        private int size;
        private int downloaded;
        private DownloadStatus status;

        @Override
        public void run() {

        }

    }

    public interface DownloadProgressListener {

        public void onProgress(final Download download);

        public void onPause(final Download download);

        public void onResume(final Download download);

        public void onComplete(final Download download);

        public void onCancel(final Download download);

        public void onError(final Download download);

    }

    public enum DownloadStatus {
        DOWNLOADING,
        PAUSED,
        COMPLETE,
        CANCELLED,
        ERROR
    }

}
