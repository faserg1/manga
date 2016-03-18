package com.danilov.supermanga.core.service;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.android.httpimage.BitmapCache;
import com.android.httpimage.BitmapMemoryCache;
import com.danilov.supermanga.core.util.BitmapUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Semyon Danilov on 07.07.2014.
 */
public class LocalImageManager {

    private static final String TAG = "LocalImageManager";

    private final BitmapCache mCache;
    private final Resources resources;

    public Handler handler = new Handler();

    private final ExecutorService mExecutor = Executors.newFixedThreadPool(4);

    private final Set<ImageRequest> mActiveRequests = new HashSet<>();

    public LocalImageManager(final BitmapCache mCache, final Resources resources) {
        this.mCache = mCache;
        this.resources = resources;
    }

    public Bitmap loadBitmap(final ImageView imageView, final String uri, final int newSize) {

        if (uri == null || TextUtils.isEmpty(uri)) {
            throw new IllegalArgumentException("null or empty request");
        }

        if (imageView == null) {
            throw new IllegalArgumentException("imageview is null");
        }

        ImageRequest r = new ImageRequest();
        r.init(imageView, uri, newSize);
        String key = r.getHashedUri();
        Bitmap cachedBitmap = this.mCache.loadData(key);

        Drawable oldBitmap = imageView.getDrawable();
        if (oldBitmap instanceof RequestDrawable) {
            RequestDrawable requestDrawable = (RequestDrawable) oldBitmap;
            ImageRequest oldRequest = requestDrawable.getRequest();
            if (!r.equals(oldRequest)) {
                oldRequest.cancel();
            } else {
                if (cachedBitmap == null) {
                    return null;
                }
            }
        }

        if (cachedBitmap == null) {
            imageView.setImageDrawable(new RequestDrawable(r));
            this.mExecutor.submit(this.newRequestCall(r));
            return null;
        }
        return cachedBitmap;
    }

    public boolean hasImageInCache(final String uri, final int newSize) {
        String key = computeHashedName(uri) + newSize;
        return mCache.exists(key);
    }

    public boolean hasImageInCache(final Bitmap bitmap) {
        return mCache.exists(bitmap);
    }

    private Callable<Bitmap> newRequestCall(final ImageRequest imageRequest) {
        return () -> {

            synchronized (LocalImageManager.this.mActiveRequests) {
                // If there's been already request pending for the same URL,
                // we just wait until it is handled.
                while (LocalImageManager.this.mActiveRequests.contains(imageRequest)) {
                    try {
                        LocalImageManager.this.mActiveRequests.wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }

                LocalImageManager.this.mActiveRequests.add(imageRequest);
            }

            Bitmap data = null;
            try {
                String key = imageRequest.getHashedUri();
                //first - ram cache
                data = mCache.loadData(key);
                if (data == null) {
                    //now lets go persistent (ballin, ballin, yarl ballin)
                    data = BitmapUtils.loadLocal(imageRequest.getUri(), imageRequest.getNewSize(), 1);
                    int newSize = imageRequest.getNewSize();
                    if (newSize >= 0) {
                        data = BitmapUtils.reduceBitmapSize(resources, data, imageRequest.getNewSize());
                    }
                    if (data != null) {
                        mCache.storeData(key, data);
                    }
                }
                final Bitmap b = data;
                handler.post(() -> {
                    if (!imageRequest.isCancelled()) {
                        ImageView imageView = imageRequest.getImageView();
                        Drawable d = imageView.getDrawable();
                        if (d != null && d instanceof RequestDrawable) {
                            RequestDrawable oldRequest = (RequestDrawable) d;
                            if (imageRequest.equals(oldRequest.getRequest())) {
                                imageRequest.getImageView().setImageBitmap(b);
                            }
                        }
                    }
                });
            } catch (Throwable t) {
                Log.e(TAG, t.getMessage());
            } finally {
                synchronized (LocalImageManager.this.mActiveRequests) {
                    LocalImageManager.this.mActiveRequests.remove(imageRequest);
                    LocalImageManager.this.mActiveRequests.notifyAll(); // wake up pending requests
                    // who's querying the same
                    // URL.
                }
            }
            return data;
        };
    }

    private static class ImageRequest {

        private ImageView imageView;
        private String uri;
        private String hashedUri;
        private int newSize;
        private boolean isCancelled = false;

        private ImageRequest() {
        }

        public void init(final ImageView imageView, final String uri, final int newSize) {
            this.imageView = imageView;
            this.uri = uri;
            this.newSize = newSize;
            this.hashedUri = computeHashedName(uri) + newSize;
        }

        public void cancel() {
            this.isCancelled = true;
        }

        public int getNewSize() {
            return newSize;
        }

        public boolean isCancelled() {
            return isCancelled;
        }

        public String getHashedUri() {
            return hashedUri;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public String getUri() {
            return uri;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ImageRequest that = (ImageRequest) o;

            return !(uri != null ? !uri.equals(that.uri) : that.uri != null);

        }

        @Override
        public int hashCode() {
            return uri != null ? uri.hashCode() : 0;
        }

    }

    private static class RequestDrawable extends BitmapDrawable {

        private ImageRequest request;

        public RequestDrawable(final ImageRequest request) {
            this.request = request;
        }

        public ImageRequest getRequest() {
            return request;
        }

    }

    private static String computeHashedName(String name) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(name.getBytes());
            byte[] hash = digest.digest();
            BigInteger bi = new BigInteger(1, hash);
            return bi.toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
