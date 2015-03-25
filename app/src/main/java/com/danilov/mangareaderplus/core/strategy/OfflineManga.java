package com.danilov.mangareaderplus.core.strategy;

import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.danilov.mangareaderplus.core.interfaces.MangaShowObserver;
import com.danilov.mangareaderplus.core.interfaces.MangaShowStrategy;
import com.danilov.mangareaderplus.core.model.LocalManga;
import com.danilov.mangareaderplus.core.model.MangaChapter;
import com.danilov.mangareaderplus.core.repository.RepositoryEngine;
import com.danilov.mangareaderplus.core.repository.RepositoryException;
import com.danilov.mangareaderplus.core.util.Pair;
import com.danilov.mangareaderplus.core.util.Promise;
import com.danilov.mangareaderplus.core.view.InAndOutAnim;
import com.danilov.mangareaderplus.core.view.MangaViewPager;

import java.io.File;
import java.util.List;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public class OfflineManga implements MangaShowStrategy, ViewPager.OnPageChangeListener {

    private static final String TAG = "OfflineManga";

    private LocalManga manga;
    private MangaViewPager mangaViewPager;
    private InAndOutAnim nextImageAnim;
    private InAndOutAnim prevImageAnim;

    private RepositoryEngine engine = RepositoryEngine.Repository.OFFLINE.getEngine();

    private List<String> uris = null;

    private boolean destroyed = false;

    private int currentImageNumber = -1;
    private int currentChapter = -1;

    private MangaShowObserver observer;
    private MangaStrategyListener initListener;

    private Handler handler;

    public OfflineManga(final LocalManga manga, final MangaViewPager mangaViewPager, final InAndOutAnim nextImageAnim, final InAndOutAnim prevImageAnim) {
        this.manga = manga;
        this.mangaViewPager = mangaViewPager;
        this.nextImageAnim = nextImageAnim;
        this.prevImageAnim = prevImageAnim;
        mangaViewPager.setOnline(false);
        mangaViewPager.setOnPageChangeListener(this);
        handler = new Handler();
    }

    @Override
    public Promise<Result> initStrategy() {
        final Promise<Result> promise = new Promise<>();
        if (manga.getChapters() != null) {
            promise.finish(Result.SUCCESS, true);
            return promise;
        }
        try {
            engine.queryForChapters(manga);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    promise.finish(Result.SUCCESS, true);
                    updateObserver();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to load chapters: " + e.getMessage());
            promise.finish(Result.ERROR, false);
        }
        return promise;
    }

    @Override
    public void restoreState(final List<String> uris, final int chapter, final int image) {
        this.currentChapter = chapter;
        this.uris = uris;
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

    @Override
    public Promise<Result> showChapter(final int chapterToShow) throws ShowMangaException {
        Promise<Result> promise = new Promise<Result>();
        Pair pair = null;
        if (chapterToShow == -1) {
            pair = manga.getFirstExistingChapterAndIsLast();
        } else {
            pair = manga.getChapterAndIsLastByNumber(chapterToShow);
        }
        if (pair == null) {
            promise.finish(Result.NOT_DOWNLOADED, true);
            return promise;
        }
        MangaChapter chapter = (MangaChapter) pair.first;
        boolean isLast = (boolean) pair.second;
        if (chapterToShow == -1) {
            this.currentChapter = chapter.getNumber();
        } else {
            this.currentChapter = chapterToShow;
        }
        this.currentImageNumber = -1;
        try {
            uris = engine.getChapterImages(chapter);
        } catch (RepositoryException e) {
            throw new ShowMangaException(e.getMessage());
        }
        mangaViewPager.setUris(uris);
        updateObserver();
        promise.finish(isLast ? Result.LAST_DOWNLOADED : Result.SUCCESS, true);
        return promise;
    }

    private void updateObserver() {
        if (observer != null) {
            observer.onUpdate(this);
        }
    }

    @Override
    public Promise<Result> next() throws ShowMangaException {
        if (currentImageNumber + 1 >= uris.size()) {
            //TODO: переход с 1 до 3 главы, если вторая не скачана - спрашивать юзера?
            return showChapterFromNext();
        }
        showImage(currentImageNumber + 1);
        return null;
    }


    private Promise<Result> showChapterFromNext() throws ShowMangaException {
        Promise<Result> promise = new Promise<Result>();
        List<MangaChapter> chapters = manga.getChapters();
        boolean nextIsNeeded = false;
        MangaChapter chapter = null;
        boolean isLast = false;
        for (int i = 0; i < chapters.size(); i++) {
            MangaChapter _chapter = chapters.get(i);
            if (nextIsNeeded) {
                chapter = _chapter;
                isLast = (i == (chapters.size() - 1));
                break;
            }
            if (_chapter.getNumber() == currentChapter) {
                nextIsNeeded = true;
            }
        }
        if (chapter == null) {
            promise.finish(Result.NOT_DOWNLOADED, true);
            return promise;
        }
        this.currentChapter = chapter.getNumber();
        this.currentImageNumber = -1;
        try {
            uris = engine.getChapterImages(chapter);
        } catch (RepositoryException e) {
            throw new ShowMangaException(e.getMessage());
        }
        mangaViewPager.setUris(uris);
        updateObserver();
        promise.finish(isLast ? Result.LAST_DOWNLOADED : Result.SUCCESS, true);
        return promise;
    }

    @Override
    public void previous() throws ShowMangaException {
        showImage(currentImageNumber - 1);
    }

    @Override
    public int getCurrentChapterNumber() {
        return currentChapter;
    }

    @Override
    public String getTotalChaptersNumber() {
        if (manga == null) {
            return "0";
        }
        return "? (" + manga.getChaptersQuantity() + ")";
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
        this.initListener = mangaStrategyListener;
    }

    @Override
    public void destroy() {
        this.destroyed = true;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public int getCurrentImageNumber() {
        return currentImageNumber;
    }

    @Override
    public int getTotalImageNumber() {
        if (uris == null) {
            return 0;
        }
        return uris.size();
    }

    @Override
    public void onPageSelected(final int position) {
        this.currentImageNumber = position;
        updateObserver();
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(final int state) {

    }

}