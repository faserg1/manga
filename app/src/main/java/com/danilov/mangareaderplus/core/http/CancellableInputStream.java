package com.danilov.mangareaderplus.core.http;

import android.util.Log;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class CancellableInputStream extends FilterInputStream {
    public static final String TAG = "CancellableInputStream";

    private final ICancelled mTask;

    public CancellableInputStream(InputStream in, ICancelled task) {
        super(in);
        this.mTask = task;
    }

    @Override
    public int read() throws IOException {
        if (this.checkCancelled()) {
            this.closeWithoutExceptions();
            return -1;
        }

        return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (this.checkCancelled()) {
            this.closeWithoutExceptions();
            return -1;
        }

        return super.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (this.checkCancelled()) {
            this.closeWithoutExceptions();
            return -1;
        }

        return super.read(b, off, len);
    }

    private boolean checkCancelled() {
        return this.mTask != null && this.mTask.isCancelled();
    }

    private void closeWithoutExceptions() {
        try {
            this.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

}