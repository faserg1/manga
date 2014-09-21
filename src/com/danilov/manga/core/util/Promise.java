package com.danilov.manga.core.util;

/**
 * Created by Semyon Danilov on 21.09.2014.
 */
public class Promise<ARG> {

    private boolean isOver;

    private ARG arg;

    private Action<ARG> action;

    private boolean success = false;

    public Promise(final ARG arg) {
        this.arg = arg;
    }

    public synchronized void finish(final boolean success) {
        isOver = true;
        this.success = success;
        if (action != null) {
            action.action(arg, success);
        }
    }

    public void after(final Action<ARG> action) {
        synchronized (this) {
            if (isOver) {
                action.action(arg, success);
            }
        }
        this.action = action;
    }

    public interface Action<ARG> {

        public void action(final ARG arg, final boolean success);

    }

}
