package com.danilov.manga.core.http;

import android.graphics.drawable.BitmapDrawable;
import com.android.httpimage.HttpImageManager;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class AsyncDrawable extends BitmapDrawable {

    private HttpImageManager.LoadRequest request;

    public AsyncDrawable(final HttpImageManager.LoadRequest request) {
        this.request = request;
    }

    public HttpImageManager.LoadRequest getRequest() {
        return request;
    }

}
