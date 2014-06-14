package com.danilov.manga.core.service;

import android.util.Log;
import com.danilov.manga.core.interfaces.Pool;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Semyon Danilov on 26.05.2014. Using new mode - 07.06
 */
public class DownloadManager {

    private final String TAG = "DownloadManager";

    protected DownloadManagerThread thread = new DownloadManagerThread();

    private static final int MAX_BUFFER_SIZE = 1024;

    private DownloadPool pool = new DownloadPool();

    private DownloadProgressListener listener;

    private Queue<Download> downloads = new ArrayDeque<Download>();

    //thread
    final Lock lock = new ReentrantLock();

    final Condition isWake = lock.newCondition();
    //!thread

    public DownloadManager() {
        thread.start();
    }

    //executing only one download at a time
    //on complete start another download
    public void startDownload(final String uri, final String filePath) {
        startDownload(uri, filePath, 0);
    }

    public void startDownload(final String uri, final String filePath, final int tag) {
        lock.lock();
        try {
            Download download = pool.obtain();
            download.setUri(uri);
            download.setTag(tag);
            download.setFilePath(filePath);
            downloads.add(download);
            isWake.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public class Download implements Runnable {

        private int tag = 0;

        private String uri;
        private String filePath;
        private int size = -1;
        private int downloaded = 0;
        private DownloadStatus status;

        public Download() {
        }

        public void recycle() {
            clear();
            DownloadManager.this.recycle(this);
        }

        public String getUri() {
            return uri;
        }

        public void setUri(final String uri) {
            this.uri = uri;
        }

        public String getFilePath() {
            return filePath;
        }

        //TODO: block if download is not in pool
        public void setFilePath(final String filePath) {
            this.filePath = filePath;
        }

        private void clear() {
            this.uri = null;
            this.filePath = null;
            this.size = -1;
            this.tag = 0;
            this.downloaded = 0;
            this.status = null;
        }

        @Override
        public void run() {
            RandomAccessFile file = null;
            InputStream stream = null;
            try {
                // Open connection to URL.
                URL url = new URL(uri);
                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                // Specify what portion of file to download.
                connection.setRequestProperty("Range",
                        "bytes=" + downloaded + "-");

                // Connect to server.
                connection.connect();

                // Make sure response code is in the 200 range.
                if (connection.getResponseCode() / 100 != 2) {
                    error();
                }

                // Check for valid content length.
                int contentLength = connection.getContentLength();
                if (contentLength < 1) {
                    error();
                }
                /* Set the size for this download if it
                hasn't been already set. */
                if (size == -1) {
                    synchronized (this) {
                        size = contentLength;
                    }
                }
                status = DownloadStatus.DOWNLOADING;
                stateChanged();

                // Open file and seek to the end of it.
                file = new RandomAccessFile(filePath, "rw");
                file.seek(downloaded);

                stream = connection.getInputStream();
                byte[] buffer;
                if (size - downloaded > MAX_BUFFER_SIZE) {
                    buffer = new byte[MAX_BUFFER_SIZE];
                } else {
                    buffer = new byte[size - downloaded];
                }
                while (status == DownloadStatus.DOWNLOADING) {
                    /* Size buffer according to how much of the
                    file is left to download. */
                    if (size - downloaded < MAX_BUFFER_SIZE) {
                        buffer = new byte[size - downloaded];
                    }
                    // Read from server into buffer.
                    int read = stream.read(buffer);
                    if (read == -1) {
                        break;
                    }
                    // Write buffer to file.
                    file.write(buffer, 0, read);
                    downloaded += read;
                    if (listener != null) {
                        listener.onProgress(this, downloaded);
                    }
                }
                /* Change status to complete if this point was
                reached because downloading has finished. */
                if (status == DownloadStatus.DOWNLOADING) {
                    status = DownloadStatus.COMPLETE;
                    downloads.remove(this);
                    stateChanged();
                }
            } catch (Exception e) {
                error();
            } finally {
                // Close file.
                if (file != null) {
                    try {
                        file.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error while closing file: " + e.getMessage());
                    }
                }

                // Close connection to server.
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error while closing stream: " + e.getMessage());
                    }
                }
            }
        }

        private void error() {
            status = DownloadStatus.ERROR;
            stateChanged();
        }

        private void stateChanged() {
            switch (status) {
                case DOWNLOADING:
                    if (listener != null) {
                        listener.onResume(this);
                    }
                    break;
                case PAUSED:
                    if (listener != null) {
                        listener.onPause(this);
                    }
                    break;
                case COMPLETE:
                    if (listener != null) {
                        listener.onComplete(this);
                        recycle();
                    }
                    break;
                case CANCELLED:
                    if (listener != null) {
                        listener.onCancel(this);
                    }
                    break;
                case ERROR:
                    if (listener != null) {
                        listener.onError(this);
                    }
                    break;
            }
        }

        public float getProgress() {
            return ((float) downloaded / size) * 100;
        }

        public DownloadStatus getStatus() {
            return status;
        }

        public synchronized int getSize() {
            return size;
        }

        public int getTag() {
            return tag;
        }

        public void setTag(final int tag) {
            this.tag = tag;
        }
    }

    public class DownloadPool implements Pool<Download> {

        private int poolMaxSize = 15;

        private Queue<Download> pool = new ArrayDeque<Download>(poolMaxSize);

        @Override
        public Download obtain() {
            Download download;
            if (!pool.isEmpty()) {
                download = pool.poll();
            } else {
                download = new Download();
            }
            download.clear();
            return download;
        }

        @Override
        public void retrieve(final Download object) {
            if (pool.size() < poolMaxSize) {
                pool.add(object);
            }
        }

    }

    private void recycle(final Download download) {
        pool.retrieve(download);
    }

    public interface DownloadProgressListener {

        public void onProgress(final Download download, final int progress);

        public void onPause(final Download download);

        public void onResume(final Download download);

        public void onComplete(final Download download);

        public void onCancel(final Download download);

        public void onError(final Download download);

    }

    public void setListener(final DownloadProgressListener listener) {
        this.listener = listener;
    }

    private class DownloadManagerThread extends Thread {

        private boolean isWorking;

        public DownloadManagerThread() {
            isWorking = true;
        }

        @Override
        public void run() {
            while (isWorking()) {
                Download download = null;
                lock.lock();
                try {
                    while (downloads.size() < 1) {
                        try {
                            isWake.await();
                        } catch (InterruptedException e) {
                            Log.d(TAG, "Thread is awake, but засыпай, баю-бай");
                        }
                    }
                    download = downloads.peek();
                    while (download.getStatus() == DownloadStatus.PAUSED || download.getStatus() == DownloadStatus.ERROR) {
                        try {
                            isWake.await();
                        } catch (InterruptedException e) {
                            Log.d(TAG, "Thread is awake, but засыпай, баю-бай");
                        }
                    }
                    if (download.getStatus() == DownloadStatus.CANCELLED) {
                        download = downloads.remove();
                        download.recycle();
                        continue;
                    }
                } finally {
                    lock.unlock();
                }
                download.run();
            }
        }

        public synchronized boolean isWorking() {
            return isWorking;
        }

        public synchronized void stopWorking() {
            this.isWorking = false;
        }

    }

    public enum DownloadStatus {
        DOWNLOADING,
        PAUSED,
        COMPLETE,
        CANCELLED,
        ERROR
    }

}
