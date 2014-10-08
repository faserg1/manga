package com.danilov.manga.core.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.InputStream;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class HttpStreamModel {
    public InputStream stream;
    public HttpRequestBase request;
    public HttpResponse response;
    public boolean notModifiedResult;
}
