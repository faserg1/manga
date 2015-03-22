package com.danilov.mangareaderplus.core.view;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ViewSwitcher;

import com.danilov.mangareaderplus.core.adapter.ExtendedPagerAdapter;
import com.danilov.mangareaderplus.core.cache.CacheDirectoryManager;
import com.danilov.mangareaderplus.core.cache.CacheDirectoryManagerImpl;
import com.danilov.mangareaderplus.core.service.DownloadManager;
import com.danilov.mangareaderplus.core.util.IoUtils;
import com.danilov.mangareaderplus.core.util.ServiceContainer;

import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Semyon on 17.03.2015.
 */
public class MangaViewPager extends ViewPager {


    private DownloadManager downloadManager = new DownloadManager();
    private DownloadManager.Download download = null;
    private ViewSwitcher.ViewFactory viewFactory = null;
    private int size = 0;
    private Adapter adapter = null;
    private CacheDirectoryManager cacheDirectoryManager = null;
    private String cachePath = null;
    private FragmentManager fragmentManager;
    private List<String> uris = null;
    private boolean isOnline = false;
    private Handler handler = new Handler();


    private List<ViewPager.OnPageChangeListener> listeners = new LinkedList<>();

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
        if (justChanged && ev.getAction() == MotionEvent.ACTION_MOVE) {
            setMLastMotion(ev.getX() + 1, ev.getY());
            setMInitialMotionX(ev.getX());
            justChanged = false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setFactory(final ViewSwitcher.ViewFactory factory) {
        this.viewFactory = factory;
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

    public void setSize(final int size) {
        this.size = size;
        adapter.notifyDataSetChanged();
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
    public void setOnPageChangeListener(final OnPageChangeListener listener) {
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


        public Adapter(final Context context, final List<String> models) {
            super(context, models);
        }

        public void unsubscribe() {
            listeners.remove(this.listener);
        }

        @Override
        protected View createView(final int position) {
            String url = getItem(position);
//            SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) viewFactory.makeView();
            SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) viewFactory.makeView();
            imageView.setVisibility(View.VISIBLE);
            loadImage(url, imageView);
            return imageView;
        }

        @Override
        protected void onViewUnselected(final int position, final View view) {
//            SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) view;
//            imageView.reset();
        }

        @Override
        protected void onViewSelected(final int position, final View view) {
//            SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) view;
        }
    }

    public void setMLastMotion(final float x, final float y) {

        Class<?> klass = ViewPager.class;

        Field field = null;
        try {
            field = klass.getDeclaredField("mLastMotionX");
            field.setAccessible(true);
            field.set(this, x);

            field = klass.getDeclaredField("mLastMotionY");
            field.setAccessible(true);
            field.set(this, y);

            field = klass.getDeclaredField("mVelocityTracker");
            field.setAccessible(true);
            VelocityTracker velocityTracker = (VelocityTracker) field.get(this);
            if (velocityTracker != null) {
                velocityTracker.clear();
                velocityTracker.recycle();
                field.set(this, null);
            }

        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void setMInitialMotionX(final float x) {

        Class<?> klass = ViewPager.class;

        Field field = null;
        try {
            field = klass.getDeclaredField("mInitialMotionX");
            field.setAccessible(true);
            field.set(this, x);

        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void loadImage(final String url, final SubsamplingScaleImageView imageView) {
        if (!isOnline) {
            imageView.setImageFile(url);
            return;
        }
        final String path = cachePath + IoUtils.createPathForURL(url);
        final String donePath = path + "_done";
        File file = new File(donePath);
        if (file.exists()) {
            imageView.setImageFile(donePath);
            return;
        }
        download = downloadManager.startDownload(url, path);
        downloadManager.setListener(new DownloadManager.DownloadProgressListener() {
            @Override
            public void onProgress(final DownloadManager.Download download, final int progress) {

            }

            @Override
            public void onPause(final DownloadManager.Download download) {

            }

            @Override
            public void onResume(final DownloadManager.Download download) {

            }

            @Override
            public void onComplete(final DownloadManager.Download download) {
                File file = new File(path);
                File newPath = new File(donePath);
                file.renameTo(newPath);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageFile(donePath);
                    }
                });
            }

            @Override
            public void onCancel(final DownloadManager.Download download) {

            }

            @Override
            public void onError(final DownloadManager.Download download, final String errorMsg) {

            }
        });
    }

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