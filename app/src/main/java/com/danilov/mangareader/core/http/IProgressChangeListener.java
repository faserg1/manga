package com.danilov.mangareader.core.http;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public interface IProgressChangeListener {
    void setContentLength(long contentLength);

    void progressChanged(long mTotalNumBytesRead);
}
