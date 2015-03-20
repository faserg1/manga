package com.danilov.mangareaderplus.core.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

import com.danilov.mangareaderplus.core.adapter.ExtendedPagerAdapter;
import com.danilov.mangareaderplus.core.cache.CacheDirectoryManager;
import com.danilov.mangareaderplus.core.cache.CacheDirectoryManagerImpl;
import com.danilov.mangareaderplus.core.service.DownloadManager;
import com.danilov.mangareaderplus.core.util.IoUtils;
import com.danilov.mangareaderplus.core.util.ServiceContainer;
import com.danilov.mangareaderplus.core.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon on 17.03.2015.
 */
public class MangaViewPager extends ViewPager implements Switchable {


    private DownloadManager downloadManager = new DownloadManager();
    private DownloadManager.Download download = null;
    private ViewSwitcher.ViewFactory viewFactory = null;
    private int size = 0;
    private Adapter adapter = null;
    private CacheDirectoryManager cacheDirectoryManager = null;
    private String cachePath = null;

    public MangaViewPager(final Context context) {
        super(context);
        init();
    }

    public MangaViewPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void setNextImageDrawable(final String filePath) {
//        int curItem = getCurrentItem();
//        int prevItem = curItem - 1;
//
//        SubsamplingScaleImageView prevImage = adapter.getImageView(prevItem);
//        SubsamplingScaleImageView image = adapter.getImageView(curItem);
//        ImageViewState state = null;
//        if (prevImage != null) {
//            state = prevImage.getState();
//            if (state != null) {
//                state.setCenter(10000, 0);
//            }
//        }
//        image.setImageFile(filePath, state);
    }

    @Override
    public void setPreviousImageDrawable(final String filePath) {
//        int curItem = getCurrentItem();
//        int prevItem = curItem + 1;
//
//        SubsamplingScaleImageView prevImage = adapter.getImageView(prevItem);
//        SubsamplingScaleImageView image = adapter.getImageView(curItem);
//        ImageViewState state = null;
//        if (prevImage != null) {
//            state = prevImage.getState();
//            if (state != null) {
//                state.setCenter(0, 10000);
//            }
//        }
//        image.setImageFile(filePath, state);
    }

    @Override
    public void setInAndOutAnim(final InAndOutAnim inAndOutAnim) {

    }

    @Override
    public void setFactory(final ViewSwitcher.ViewFactory factory) {
        this.viewFactory = factory;
    }

    private FragmentManager fragmentManager;

    @Override
    public void setFragmentManager(final FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }


    private void init() {
        cacheDirectoryManager = ServiceContainer.getService(CacheDirectoryManagerImpl.class);
        this.cachePath = cacheDirectoryManager.getImagesCacheDirectory().toString() + "/";
    }

    @Override
    public void setSize(final int size) {
        this.size = size;
        adapter.notifyDataSetChanged();
    }

    private List<String> uris = null;

    @Override
    public void setUris(final List<String> uris) {
        this.uris = uris;
        adapter = new Adapter(getContext(), uris);
        adapter.subscribeToPageChangeEvent(this);
        setAdapter(adapter);
    }

    @Override
    public void setOnPageChangeListener(final OnPageChangeListener listener) {
        super.setOnPageChangeListener(listener);
    }

    private class Adapter extends ExtendedPagerAdapter<String> {


        public Adapter(final Context context, final List<String> models) {
            super(context, models);
        }

        @Override
        protected View createView(final int position) {
            String url = getItem(position);
            SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) viewFactory.makeView();
            imageView.setVisibility(View.VISIBLE);
            imageView.setDebug(true);
            loadImage(url, imageView);
            return imageView;
        }

        @Override
        protected void onViewUnselected(final int position, final View view) {
            SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) view;
//            imageView.reset();
        }

        @Override
        protected void onViewSelected(final int position, final View view) {
            SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) view;
        }
    }

    public void loadImage(final String url, final SubsamplingScaleImageView imageView) {
        final String path = cachePath + IoUtils.createPathForURL(url);
        final String donePath = path + "_done";
        File file = new File(donePath);
        if (file.exists()) {
            imageView.setImageFile(donePath);
            return;
        }
        if (download != null) {
            downloadManager.cancelDownload(download);
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

                if (compareUrlAndTag(url, imageView.getTag())) {
                    imageView.setImageFile(donePath);
                }
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