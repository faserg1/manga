package com.danilov.mangareaderplus.core.strategy;

import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.danilov.mangareaderplus.core.interfaces.MangaShowObserver;
import com.danilov.mangareaderplus.core.interfaces.MangaShowStrategy;
import com.danilov.mangareaderplus.core.model.Manga;
import com.danilov.mangareaderplus.core.model.MangaChapter;
import com.danilov.mangareaderplus.core.repository.RepositoryEngine;
import com.danilov.mangareaderplus.core.repository.RepositoryException;
import com.danilov.mangareaderplus.core.service.DownloadManager;
import com.danilov.mangareaderplus.core.util.Promise;
import com.danilov.mangareaderplus.core.view.InAndOutAnim;
import com.danilov.mangareaderplus.core.view.MangaViewPager;

import java.util.List;

/**
 * Created by Semyon Danilov on 21.06.2014.
 *
 * This class handles showing pictures from web
 *
 */
public class OnlineManga implements MangaShowStrategy, ViewPager.OnPageChangeListener {

    private static final String TAG = "OnlineManga";

    private MangaViewPager mangaViewPager;
    private InAndOutAnim nextImageAnim;
    private InAndOutAnim prevImageAnim;

    private DownloadManager downloadManager;
    private Handler handler;

    private Manga manga;
    private RepositoryEngine engine;
    private int currentImageNumber = -1;
    private int currentChapter;
    private int totalImages = 0;
    private List<String> uris = null;

    private MangaShowObserver observer;
    private MangaStrategyListener listener;

    private boolean destroyed = false;

    public OnlineManga(final Manga manga, final MangaViewPager mangaViewPager, final InAndOutAnim nextImageAnim, final InAndOutAnim prevImageAnim) {
        this.manga = manga;
        this.engine = manga.getRepository().getEngine();
        this.mangaViewPager = mangaViewPager;
        this.nextImageAnim = nextImageAnim;
        this.prevImageAnim = prevImageAnim;
        this.handler = new Handler();
        mangaViewPager.setOnline(true);
        mangaViewPager.setOnPageChangeListener(this);
        this.downloadManager = new DownloadManager();
    }

    @Override
    public void restoreState(final List<String> uris, final int chapter, final int image) {
        this.currentChapter = chapter;
        this.savedCurrentImageNumber = image;
        this.uris = uris;
        if (uris != null) {
            this.totalImages = uris.size();
        }
        mangaViewPager.setUris(uris);
    }

    @Override
    public void showImage(final int i) {
        if (i == currentImageNumber || i >= uris.size() || i < 0) {
            return;
        }
        this.currentImageNumber = i;
        updateObserver();
        mangaViewPager.setCurrentItem(i);
    }

    private void updateObserver() {
        if (observer != null) {
            observer.onUpdate(this);
        }
    }

    private int savedCurrentImageNumber = 0;

    @Override
    public Promise<Result> showChapter(final int i) throws ShowMangaException {
        int chapterNum = i < 0 ? 0 : i;
        int chaptersQuantity = manga.getChaptersQuantity();
        if (chaptersQuantity <= 0) {
            throw new ShowMangaException("No chapters to show");
        }
        final MangaChapter chapter = manga.getChapterByNumber(chapterNum);
        final Promise<Result> promise = new Promise<Result>();
        if (chapter == null) {
            boolean isOnLastChapterAndTappedNext = currentChapter == (chaptersQuantity - 1) && chapterNum == chaptersQuantity;
            promise.finish(isOnLastChapterAndTappedNext ? Result.ALREADY_FINAL_CHAPTER : Result.NO_SUCH_CHAPTER, true);
            return promise;
        }
        this.currentChapter = chapterNum;
        this.savedCurrentImageNumber = this.currentImageNumber;
        this.currentImageNumber = -1;
        listener.onChapterInfoLoadStart(this);
        Thread thread = new Thread() {

            @Override
            public void run() {
                boolean _success = true;
                try {
                    uris = engine.getChapterImages(chapter);
                    totalImages = uris.size();
                } catch (final Exception e) {
                    _success = false;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onChapterInfoLoadEnd(OnlineManga.this, false, e.getMessage());
                            promise.finish(Result.ERROR, true);
                        }
                    });
                }
                final boolean success = _success;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (destroyed) {
                            return;
                        }
                        listener.onChapterInfoLoadEnd(OnlineManga.this, true, "");
                        updateObserver();
                        mangaViewPager.setUris(uris);
                        promise.finish(success ? Result.SUCCESS : Result.ERROR, true);
                    }
                });
            }
        };
        thread.start();
        return promise;
    }

    @Override
    public Promise<Result> next() throws ShowMangaException {
        if (currentImageNumber + 1 >= uris.size()) {
            return showChapter(currentChapter + 1);
        }
        showImage(currentImageNumber + 1);
        return null;
    }

    @Override
    public void previous() throws ShowMangaException{
        showImage(currentImageNumber - 1);
    }

    @Override
    public Promise<Result> initStrategy() {
        final Promise<Result> promise = new Promise<Result>();
        if (manga.getChapters() != null) {
            promise.finish(Result.SUCCESS, true);
        } else {
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        engine.queryForChapters(manga);
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                if (destroyed) {
                                    return;
                                }
                                promise.finish(Result.SUCCESS, true);
                            }

                        });
                    } catch (RepositoryException e) {
                        Log.e(TAG, "Failed to load chapters: " + e.getMessage());
                        promise.finish(Result.ERROR, false);
                    }
                }

            };
            t.start();
        }
        return promise;
    }

    @Override
    public int getCurrentImageNumber() {
        if (currentImageNumber == -1) {
            return savedCurrentImageNumber;
        }
        return currentImageNumber;
    }

    @Override
    public int getTotalImageNumber() {
        return totalImages;
    }

    @Override
    public int getCurrentChapterNumber() {
        return currentChapter;
    }

    @Override
    public int getTotalChaptersNumber() {
        if (manga == null) {
            return 0;
        }
        return manga.getChaptersQuantity();
    }

    @Override
    public List<String> getChapterUris() {
        return uris;
    }

    @Override
    public void setObserver(final MangaShowObserver observer) {
        this.observer = observer;
    }

    @Override
    public void setOnStrategyListener(final MangaStrategyListener mangaStrategyListener) {
        this.listener = mangaStrategyListener;
    }

    @Override
    public void destroy() {
        this.destroyed = true;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(final int position) {
        this.currentImageNumber = position;
        updateObserver();
    }

    @Override
    public void onPageScrollStateChanged(final int state) {

    }

}
