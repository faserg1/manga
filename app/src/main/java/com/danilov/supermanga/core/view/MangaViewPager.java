package com.danilov.supermanga.core.view;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.adapter.ExtendedPagerAdapter;
import com.danilov.supermanga.core.cache.CacheDirectoryManager;
import com.danilov.supermanga.core.cache.CacheDirectoryManagerImpl;
import com.danilov.supermanga.core.service.DownloadManager;
import com.danilov.supermanga.core.util.IoUtils;
import com.danilov.supermanga.core.util.ServiceContainer;
import com.danilov.supermanga.core.util.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Semyon on 17.03.2015.
 */
public class MangaViewPager extends CompatPager {


    private DownloadManager downloadManager = new DownloadManager();
    private Adapter adapter = null;
    private CacheDirectoryManager cacheDirectoryManager = null;
    private String cachePath = null;
    private FragmentManager fragmentManager;
    private List<String> uris = null;
    private boolean isOnline = false;
    private Handler handler = new Handler();


    private List<CompatPager.OnPageChangeListener> listeners = new LinkedList<>();

    public MangaViewPager(final Context context) {
        super(context);
        init();
    }

    public MangaViewPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private boolean justChanged = false;

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        try {
            if (justChanged && ev.getAction() == MotionEvent.ACTION_MOVE) {
                setLastMotion(ev.getX() + 1, ev.getY());
                setInitialMotionX(ev.getX());
                justChanged = false;
            }
            return super.onInterceptTouchEvent(ev);
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (Exception e) {
        }
        return false;
    }

    public void setOnline(final boolean isOnline) {
        this.isOnline = isOnline;
    }

    public void setFragmentManager(final FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(final boolean disallowIntercept) {
        justChanged = true;
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    private void init() {
        requestDisallowInterceptTouchEvent(true);
        super.setOnPageChangeListener(internalListener);
        cacheDirectoryManager = ServiceContainer.getService(CacheDirectoryManagerImpl.class);
        this.cachePath = cacheDirectoryManager.getImagesCacheDirectory().toString() + "/";
    }

    public void setUris(final List<String> uris) {
        this.uris = uris;
        if (adapter != null) {
            adapter.unsubscribe();
        }
        adapter = new Adapter(getContext(), uris);
        adapter.subscribeToPageChangeEvent(this);
        setAdapter(adapter);
    }

    @Override
    public void setOnPageChangeListener(final CompatPager.OnPageChangeListener listener) {
        listeners.add(listener);
    }

    private OnPageChangeListener internalListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(final int position) {
            for (OnPageChangeListener listener : listeners) {
                listener.onPageSelected(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            for (OnPageChangeListener listener : listeners) {
                listener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
            for (OnPageChangeListener listener : listeners) {
                listener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

    };

    private class Adapter extends ExtendedPagerAdapter<String> {

        private Map<Integer, SubsamplingScaleImageView> views = new HashMap<>();

        public Adapter(final Context context, final List<String> models) {
            super(context, models);
        }

        public void unsubscribe() {
            listeners.remove(this.listener);
        }

        @Override
        protected View createView(final int position) {
            String url = getItem(position);

            View v = LayoutInflater.from(getContext()).inflate(R.layout.viewer_page, MangaViewPager.this, false);

            SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) v.findViewById(R.id.imageView);
            imageView.setMaxScale(4);
            TextView progressView = (TextView) v.findViewById(R.id.progress);
            Button restart = (Button) v.findViewById(R.id.restart);
            imageView.setVisibility(View.VISIBLE);
            loadImage(url, imageView, progressView, restart);
            views.put(position, imageView);
            return v;
        }

        @Override
        protected void onViewUnselected(final int position, final View view) {
//            SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) view;
//            imageView.reset();
            for (Map.Entry<Integer, SubsamplingScaleImageView> entry : views.entrySet()) {
                int pos = entry.getKey();
                if (Math.abs(position - pos) > 1) {
                    entry.getValue().reset();
                }
            }
            SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) view.findViewById(R.id.imageView);
            if (imageView.isLargePicture()) {
                imageView.reset();
            }
            imageView.setShouldDrawLarge(false);
        }

        @Override
        protected void onViewSelected(final int position, final View view) {
            SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) view.findViewById(R.id.imageView);
            imageView.setShouldDrawLarge(true);
        }
    }


    public void loadImage(final String url, final SubsamplingScaleImageView imageView, final TextView textView, final Button button) {
        if (!isOnline) {
            imageView.setImageFile(url);
            textView.setVisibility(View.GONE);
            return;
        }
        final String path = cachePath + IoUtils.createPathForURL(url);
        final String donePath = path + "_done";
        File file = new File(donePath);
        if (file.exists()) {
            imageView.setImageFile(donePath);
            textView.setVisibility(View.GONE);
            return;
        }
        ImageBundle bundle = new ImageBundle();
        bundle.iv = imageView;
        bundle.tv = textView;
        bundle.path = path;
        bundle.donePath = donePath;
        bundle.restart = button;

        String tag = path + Utils.getRandomString(15);

        imageBundleMap.put(tag, bundle);
        downloadManager.startImportantDownload(url, path, tag);

        downloadManager.setListener(downloadProgressListener);
    }

    private class ImageBundle {

        TextView tv;
        SubsamplingScaleImageView iv;
        Button restart;
        String path;
        String donePath;

    }

    private Map<String, ImageBundle> imageBundleMap = new HashMap<>();

    private DownloadManager.DownloadProgressListener downloadProgressListener = new DownloadManager.DownloadProgressListener() {

        private ImageBundle imageBundle = null;

        @Override
        public void onProgress(final DownloadManager.Download download, final int progress) {
            final TextView tv = imageBundle.tv;
            final int _progress = (int) (((float) progress /  download.getSize()) * 100);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    tv.setText(_progress + "%");
                }
            });
        }

        @Override
        public void onPause(final DownloadManager.Download download) {

        }

        @Override
        public void onResume(final DownloadManager.Download download) {
            imageBundle = imageBundleMap.get(download.getTag());
        }

        @Override
        public void onComplete(final DownloadManager.Download download) {

            final ImageBundle imageBundle1 = imageBundleMap.remove(download.getTag());

            File file = new File(imageBundle1.path);
            File newPath = new File(imageBundle1.donePath);
            boolean renamed = file.renameTo(newPath);
            if (!renamed) {
                Log.e("MangaViewPager", "Failed to rename file");
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    imageBundle1.iv.setImageFile(imageBundle1.donePath);
                    imageBundle1.tv.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public void onCancel(final DownloadManager.Download download) {

        }

        @Override
        public void onError(final DownloadManager.Download download, final String errorMsg) {

            ImageBundle imageBundle = imageBundleMap.get(download.getTag());

            if (imageBundle == null) {
                return;
            }

            final String url = download.getUri();
            final SubsamplingScaleImageView iv = imageBundle.iv;
            final TextView tv = imageBundle.tv;
            final Button button = imageBundle.restart;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    tv.setVisibility(View.INVISIBLE);
                    button.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            tv.setVisibility(View.VISIBLE);
                            button.setVisibility(View.INVISIBLE);
                            loadImage(url, iv, tv, button);
                        }
                    });
                    button.setVisibility(View.VISIBLE);
                    downloadManager.cancelDownload(download);
                }
            });
        }

    };

    private boolean compareUrlAndTag(final String url, final Object tag) {
        if (tag != null) {
            if (tag instanceof String) {
                String tagString = (String) tag;
                if (tagString.equals(url)) {
                    return true;
                }
            }
        }
        return false;
    }

}