package com.danilov.manga.core.util;

/**
 * Created by Semyon Danilov on 21.09.2014.
 */
public class OldPromise<ARG> {

    private boolean isOver;

    private ARG data;

    private Action<ARG> action;

    private boolean success = false;

    public OldPromise() {

    }

    public synchronized void finish(final ARG data, final boolean success) {
        isOver = true;
        this.data = data;
        this.success = success;
        if (action != null) {
            action.action(data, success);
        }
    }

    public void after(final Action<ARG> action) {
        synchronized (this) {
            if (isOver) {
                action.action(data, success);
            }
        }
        this.action = action;
    }

    public interface Action<ARG> {

        public void action(final ARG arg, final boolean success);

    }

}
