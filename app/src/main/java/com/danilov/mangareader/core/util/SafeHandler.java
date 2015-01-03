package com.danilov.mangareader.core.util;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;

/**
 * Created by Semyon on 03.01.2015.
 */
public class SafeHandler {

    private Handler handler;

    private boolean isHandling = true;

    public SafeHandler(final Looper looper, final Callback callback) {
        handler = new Handler(looper, callback);
    }

    public SafeHandler(final Looper looper) {
        handler = new Handler(looper);
    }

    public SafeHandler(final Callback callback) {
        handler = new Handler(callback);
    }

    public SafeHandler() {
        handler = new Handler();
    }

    public void stopHandling() {
        isHandling = false;
    }

    public boolean sendMessage(final Message message) {
        return isHandling && handler.sendMessage(message);
    }

    public boolean post(final Runnable runnable) {
        return isHandling && handler.post(new SafeRunnable(runnable));
    }

    public void handleMessage(final Message message) {

    }

    private class SafeRunnable implements Runnable {

        private Runnable runnable;

        public SafeRunnable(final Runnable runnable) {
            this.runnable = runnable;
        }


        @Override
        public void run() {
            if (isHandling) {
                runnable.run();
            }
        }

    }

}
