package com.danilov.mangareaderplus.core.util;

import android.util.Log;

/**
 * Created by Semyon on 05.08.2015.
 */
public class Logger {

    private String classTag = "";

    public Logger(final Class clazz) {
        this.classTag = clazz.getSimpleName();
    }

    public Logger(final String tag) {
        this.classTag = tag;
    }

    public void d(final String message) {
        Log.d(classTag, message);
    }

    public void d(final String message, final Throwable throwable) {
        Log.d(classTag, message, throwable);
    }

    public void e(final String errorMessage) {
        Log.e(classTag, errorMessage);
    }

    public void e(final String errorMessage, final Throwable throwable) {
        Log.e(classTag, errorMessage, throwable);
    }

    public void i(final String message) {
        Log.i(classTag, message);
    }

    public void i(final String message, final Throwable throwable) {
        Log.i(classTag, message, throwable);
    }

}
