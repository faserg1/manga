package com.danilov.supermanga.core.repository;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public class RepositoryException extends Exception {

    public RepositoryException(final String message) {
        super(message);
    }

    public RepositoryException(final Throwable throwable) {
        super(throwable);
    }

}
