package com.android.httpimage;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import com.danilov.mangareader.core.http.AsyncDrawable;
import com.danilov.mangareader.core.http.HttpBitmapReader;
import com.danilov.mangareader.core.util.BitmapUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



/**
 * HttpImageManager uses 3-level caching to download and store network images.
 * <p>
 * ---------------<br>
 * memory cache<br>
 * ---------------<br>
 * persistent storage (DB/FS)<br>
 * ---------------<br>
 * network loader<br>
 * ---------------
 *
 * <p>
 * HttpImageManager will first look up the memory cache, return the image bitmap
 * if it was already cached in memory. Upon missing, it will further look at the
 * 2nd level cache, which is the persistence layer. It only goes to network if
 * the resource has never been downloaded.
 *
 * <p>
 * The downloading process is handled in asynchronous manner. To get
 * notification of the response, one can add an OnLoadResponseListener to the
 * LoadRequest object.
 *
 * <p>
 * HttpImageManager is usually used for ImageView to display a network image. To
 * simplify the code, One can register an ImageView object as target to the
 * LoadRequest instead of an OnLoadResponseListener. HttpImageManager will try
 * to feed the loaded resource to the target ImageView upon successful download.
 * Following code snippet shows how it is used in a customer list adapter.
 *
 * <p>
 *
 * <pre>
 *         ...
 *         String imageUrl = userInfo.getUserImage();
 *         ImageView imageView = holder.image;
 *
 *         imageView.setImageResource(R.drawable.default_image);
 *
 *         if(!TextUtils.isEmpty(imageUrl)){
 *             Bitmap bitmap = mHttpImageManager.loadImage(new HttpImageManager.LoadRequest(Uri.parse(imageUrl), imageView));
 *            if (bitmap != null) {
 *                imageView.setImageBitmap(bitmap);
 *            }
 *        }
 *
 * </pre>
 *
 * @author zonghai@gmail.com
 */
public class HttpImageManager {

    private static final String TAG = "HttpImageManager";

    private final BitmapMemoryCache mCache;
    private final FileSystemPersistence mPersistence;
    private final HttpBitmapReader mNetworkResourceLoader;
    private final Resources mResources;

    private final Handler mHandler = new Handler();
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(4);

    private final Set<LoadRequest> mActiveRequests = new HashSet<LoadRequest>();

    public static class LoadRequest {

        private ImageView imageView = null;
        private boolean isCancelled = false;
        private int newSize;

        private static Queue<LoadRequest> pool = new ArrayDeque<LoadRequest>();

        public static LoadRequest obtain(final Uri uri) {
            LoadRequest r = null;
            if (!pool.isEmpty()) {
                r = pool.remove();
                r.mUri = uri;
                r.mHashedUri = r.computeHashedName(uri.toString());
                r.mListener = null;
            } else {
                r = new LoadRequest(uri);
            }
            return r;
        }

        public static LoadRequest obtain(final Uri uri, final OnLoadResponseListener l) {
            LoadRequest r = null;
            if (!pool.isEmpty()) {
                r = pool.remove();
                r.mUri = uri;
                r.mListener = l;
                r.mHashedUri = r.computeHashedName(uri.toString());
            } else {
                r = new LoadRequest(uri, l);
            }
            return r;
        }

        public static LoadRequest obtain(final Uri uri, final ImageView imageView, final int newSize) {
            LoadRequest r = null;
            if (!pool.isEmpty()) {
                r = pool.remove();
                r.mUri = uri;
                r.mHashedUri = r.computeHashedName(uri.toString()) + newSize;
                r.imageView = imageView;
                r.newSize = newSize;
            } else {
                r = new LoadRequest(uri, imageView, newSize);
                Log.d(TAG, "Creating new loadrequest, pool is empty");
            }
            r.mListener = r.new LoadListener();

            //отменяем старый реквест здесь, потому что до onBeforeLoad может не дойти, если в кэше есть:)
            if (imageView.getDrawable() instanceof AsyncDrawable) {
                AsyncDrawable ad = (AsyncDrawable) imageView.getDrawable();
                if (!r.equals(ad.getRequest())) {
                    ad.getRequest().cancel();
                    Log.d(TAG, "Request with URI " + ad.getRequest().getUri() + " was cancelled");
                }
            }
            return r;
        }

        private LoadRequest(final Uri uri) {
            this(uri, (OnLoadResponseListener) null);
        }

        public LoadRequest(final Uri uri, final OnLoadResponseListener l) {
            if (uri == null) {
                throw new NullPointerException("uri must not be null");
            }

            this.mUri = uri;
            this.mHashedUri = this.computeHashedName(uri.toString());
            this.mListener = l;
        }

        public LoadRequest(final Uri uri, final OnLoadResponseListener l, final int newSize) {
            if (uri == null) {
                throw new NullPointerException("uri must not be null");
            }

            this.mUri = uri;
            this.newSize = newSize;
            this.mHashedUri = this.computeHashedName(uri.toString());
            this.mListener = l;
        }


        public LoadRequest(final Uri uri, final ImageView imageView, final int newSize) {
            if (uri == null) {
                throw new NullPointerException("uri must not be null");
            }

            this.mUri = uri;
            this.newSize = newSize;
            this.mHashedUri = this.computeHashedName(uri.toString()) + newSize;
            this.imageView = imageView;
        }



        class LoadListener implements OnLoadResponseListener {

            @Override
            public void beforeLoad(final LoadRequest r) {
                Log.d(TAG, "Starting request for URI " + getUri());
                imageView.setImageDrawable(new AsyncDrawable(LoadRequest.this));
            }

            @Override
            public void onLoadResponse(final LoadRequest r, final Bitmap data) {
                if (!(imageView.getDrawable() instanceof AsyncDrawable)) {
                    return;
                }
                AsyncDrawable ad = (AsyncDrawable) imageView.getDrawable();
                if (ad.getRequest().equals(r)) {
                    imageView.setImageBitmap(data);
                }
            }

            @Override
            public void onLoadError(final LoadRequest r, final Throwable e) {

            }

        }

        public ImageView getImageView() {
            return imageView;
        }

        public Uri getUri() {
            return this.mUri;
        }

        public void cancel() {
            this.isCancelled = true;
        }

        public int getNewSize() {
            return newSize;
        }

        public void setNewSize(final int newSize) {
            this.newSize = newSize;
        }

        public String getHashedUri() {
            return this.mHashedUri;
        }

        @Override
        public int hashCode() {
            return this.mUri.hashCode();
        }

        public void retrieve() {
            mListener = null;
            mUri = null;
            mHashedUri = null;
            imageView = null;
            newSize = 0;
            isCancelled = false;
            pool.add(this);
        }

        @Override
        public boolean equals(Object b) {
            if (b instanceof LoadRequest) {
                LoadRequest _b = (LoadRequest) b;
                boolean a = this.mHashedUri.equals(_b.getHashedUri());
                return a && this.mUri.equals(((LoadRequest) b).getUri());
            }

            return false;
        }

        /* B64 encoded Hash over the input name */
        private String computeHashedName(String name) {
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

        private Uri mUri;
        private String mHashedUri;

        private OnLoadResponseListener mListener;
    }

    public static interface OnLoadResponseListener {
        public void beforeLoad(LoadRequest r);

        public void onLoadResponse(LoadRequest r, Bitmap data);

        public void onLoadError(LoadRequest r, Throwable e);
    }

    public HttpImageManager(BitmapMemoryCache cache, FileSystemPersistence persistence, Resources resources, HttpBitmapReader httpBitmapReader) {
        this.mCache = cache;
        this.mPersistence = persistence;
        this.mNetworkResourceLoader = httpBitmapReader;
        this.mResources = resources;
    }

    public Bitmap loadImage(Uri uri) {
        return this.loadImage(new LoadRequest(uri));
    }

    public boolean isCached(String uriString) {
        LoadRequest r = new LoadRequest(Uri.parse(uriString));
        String key = r.getHashedUri();

        return this.mCache.exists(key);
    }

    /**
     * Nonblocking call, return null if the bitmap is not in cache.
     *
     * @param r
     * @return
     */
    public Bitmap loadImage(LoadRequest r) {
        if (r == null || r.getUri() == null || TextUtils.isEmpty(r.getUri().toString())) {
            throw new IllegalArgumentException("null or empty request");
        }

        String key = r.getHashedUri();

        Bitmap cachedBitmap = this.mCache.loadData(key);
        if (cachedBitmap == null) {
            // not ready yet, try to retrieve it asynchronously.
            if (r.mListener != null) {
                r.mListener.beforeLoad(r);
            }

            this.mExecutor.submit(this.newRequestCall(r));

            return null;
        }

        r.retrieve();
        return cachedBitmap;
    }

    // //PRIVATE
    private Callable<LoadRequest> newRequestCall(final LoadRequest request) {
        return new Callable<LoadRequest>() {
            @Override
            public LoadRequest call() {
                StringBuilder log = new StringBuilder();
                log.append("Starting request: " + request.getUri());
                synchronized (HttpImageManager.this.mActiveRequests) {
                    // If there's been already request pending for the same URL,
                    // we just wait until it is handled.
                    while (HttpImageManager.this.mActiveRequests.contains(request)) {
                        try {
                            log.append("; waiting; ");
                            HttpImageManager.this.mActiveRequests.wait();
                        } catch (InterruptedException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }

                    HttpImageManager.this.mActiveRequests.add(request);
                }

                Bitmap data;

                try {
                    String key = request.getHashedUri();

                    // first we lookup memory cache
                    data = HttpImageManager.this.mCache.loadData(key);
                    if (data == null) {
                        // then check the persistent storage
                        data = HttpImageManager.this.mPersistence.loadData(key);
                        if (data != null) {
                            log.append("; Loaded from persistent cache");
                            // load it into memory
                            data = BitmapUtils.reduceBitmapSize(HttpImageManager.this.mResources, data, request.getNewSize());
                            HttpImageManager.this.mCache.storeData(key, data);
                        } else {
                            log.append("; going to network");
                            // we go to network
                            HttpImageManager.this.mNetworkResourceLoader.removeIfModifiedForUri(request.getUri().toString());
                            data = HttpImageManager.this.mNetworkResourceLoader.fromUri(request.getUri().toString());

                            // load it into memory
                            data = BitmapUtils.reduceBitmapSize(HttpImageManager.this.mResources, data, request.getNewSize());
                            HttpImageManager.this.mCache.storeData(key, data);

                            // persist it
                            HttpImageManager.this.mPersistence.storeData(key, data);
                        }
                    } else {
                        log.append("; Loaded from RAM cache");
                    }

                    if (data != null && request.mListener != null) {
                        final Bitmap theData = data;
                        log.append("; posting to handler");

                        HttpImageManager.this.mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                request.mListener.onLoadResponse(request, theData);
                                request.retrieve();
                            }
                        });
                    }

                } catch (final Throwable e) {
                    log.append("; got error: " + e.getMessage());
                    if (request.mListener != null) {
                        HttpImageManager.this.mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                request.mListener.onLoadError(request, e);
                                request.retrieve();
                            }
                        });
                    }
                    Log.e(TAG, e.getMessage());
                } finally {
                    Log.d(TAG, log.toString());
                    synchronized (HttpImageManager.this.mActiveRequests) {
                        HttpImageManager.this.mActiveRequests.remove(request);
                        HttpImageManager.this.mActiveRequests.notifyAll(); // wake up pending requests
                        // who's querying the same
                        // URL.
                    }
                }

                return request;
            }
        };
    }
}