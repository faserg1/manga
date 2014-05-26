package com.danilov.manga.core.service;

import android.util.Log;
import com.danilov.manga.core.interfaces.Pool;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by Semyon Danilov on 26.05.2014.
 */
public class DownloadManager {

    private final String TAG = "DownloadManager";

    private DownloadManagerThread thread = new DownloadManagerThread();

    private static final int MAX_BUFFER_SIZE = 1024;

    private DownloadPool pool = new DownloadPool();

    private DownloadProgressListener listener;

    private Queue<Download> downloads = new ArrayDeque<Download>();

    //executing only one download at a time
    //on complete start another download
    //todo: decide if this is good
    public void startDownload(final String uri, final String filePath) throws DownloadManagerException {
        if (thread.isBusy()) {
            throw new DownloadManagerException("Trying to execute another download");
        }
        Download download = pool.obtain();
        download.setUri(uri);
        download.setFilePath(filePath);
    }

    public class Download implements Runnable {

        private String uri;
        private String filePath;
        private int size;
        private int downloaded;
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

        public void setFilePath(final String filePath) {
            this.filePath = filePath;
        }

        private void clear() {
            this.uri = null;
            this.filePath = null;
            this.size = 0;
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
                status = DownloadStatus.DOWNLOADING;
                /* Set the size for this download if it
                hasn't been already set. */
                if (size == -1) {
                    size = contentLength;
                    stateChanged();
                }

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
                        listener.onProgress(this);
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

        public void onProgress(final Download download);

        public void onPause(final Download download);

        public void onResume(final Download download);

        public void onComplete(final Download download);

        public void onCancel(final Download download);

        public void onError(final Download download);

    }

    private class DownloadManagerThread extends Thread {

        private Download download;

        public final short IDLE =  0;
        public final short BUSY =  1;

        private short status = IDLE;

        public DownloadManagerThread(final Download download) {
            this.download = download;
        }

        public DownloadManagerThread() {

        }

        @Override
        public void run() {
            setStatus(BUSY);
            download.run();
            setStatus(IDLE);
        }

        private synchronized void setStatus(final short status) {
            this.status = status;
        }

        public synchronized boolean isBusy() {
            return status == BUSY;
        }

        public Download getDownload() {
            return download;
        }

        public void setDownload(final Download download) {
            this.download = download;
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
