package com.danilov.mangareaderplus.core.service;

/**
 * Created by Semyon on 28.06.2015.
 */
public class UpdateException extends Exception {

    public UpdateException() {
    }

    public UpdateException(final String detailMessage) {
        super(detailMessage);
    }

    public UpdateException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UpdateException(final Throwable throwable) {
        super(throwable);
    }

}