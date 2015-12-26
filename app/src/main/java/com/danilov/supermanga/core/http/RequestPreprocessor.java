package com.danilov.supermanga.core.http;

import android.support.annotation.NonNull;

import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Semyon on 26.12.2015.
 */
public interface RequestPreprocessor {

    HttpURLConnection process(@NonNull final URL url) throws IOException;

    void process(@NonNull DefaultHttpClient httpClient);
}