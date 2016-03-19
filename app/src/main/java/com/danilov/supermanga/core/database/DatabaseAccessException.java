package com.danilov.supermanga.core.database;

/**
 * Created by Semyon Danilov on 04.07.2014.
 */
public class DatabaseAccessException extends Exception {

    public DatabaseAccessException(final String message) {
        super(message);
    }

    public DatabaseAccessException() {
    }

    public DatabaseAccessException(final Throwable throwable) {
        super(throwable);
    }

    public DatabaseAccessException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
    }

}
