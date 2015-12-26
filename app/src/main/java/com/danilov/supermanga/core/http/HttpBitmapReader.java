package com.danilov.supermanga.core.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.danilov.supermanga.core.util.IoUtils;

import org.apache.http.Header;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class HttpBitmapReader {
    public static final String TAG = "HttpBitmapReader";

    private final HttpBytesReader mHttpBytesReader;

    public HttpBitmapReader(HttpBytesReader httpBytesReader) {
        this.mHttpBytesReader = httpBytesReader;
    }

    public Bitmap fromUri(String uri, @Nullable final RequestPreprocessor preprocessor) throws HttpRequestException {
        return this.fromUri(uri, preprocessor, null);
    }

    public Bitmap fromUri(String uri, @Nullable final RequestPreprocessor preprocessor, Header[] customHeaders) throws HttpRequestException {
        byte[] bytes = this.mHttpBytesReader.fromUri(uri, preprocessor, customHeaders);
        InputStream stream = new FlushedInputStream(new ByteArrayInputStream(bytes));

        Bitmap bmp = BitmapFactory.decodeStream(stream);

        IoUtils.closeStream(stream);

        return bmp;
    }

    public void removeIfModifiedForUri(String uri) {
        this.mHttpBytesReader.removeIfModifiedForUri(uri);
    }
}
