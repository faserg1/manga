package com.danilov.mangareaderplus.core.interfaces;

import com.danilov.mangareaderplus.core.strategy.ShowMangaException;
import com.danilov.mangareaderplus.core.strategy.StrategyDelegate;
import com.danilov.mangareaderplus.core.util.Promise;
import com.danilov.mangareaderplus.core.view.MangaViewPager;

import java.util.List;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public interface MangaShowStrategy {

    void restoreState(final List<String> uris, final int chapter, final int image, final MangaViewPager mangaViewPager);

    void showImage(final int i);

    Promise<Result> showChapterOld(final int i) throws ShowMangaException;

    Promise<Result> nextOld() throws ShowMangaException;

    Promise<Result> initStrategyOld();

    void showChapter(final int i);

    void next();

    void initStrategy();

    void previous() throws ShowMangaException;

    int getCurrentImageNumber();

    int getTotalImageNumber();

    int getCurrentChapterNumber();

    int getTotalChaptersNumber();

    List<String> getChapterUris();

    void setObserver(final MangaShowObserver observer);

    void setOnStrategyListener(final StrategyDelegate.MangaShowListener listener);

    void destroy();

    boolean isOnline();

    //TODO: return them actually!
    public enum Result {
        ERROR,
        SUCCESS,
        ALREADY_FINAL_CHAPTER,
        NO_SUCH_CHAPTER,
        LAST_DOWNLOADED,
        NOT_DOWNLOADED
    }

//    public interface MangaStrategyListener {
//
//        public void onImageLoadStart(final MangaShowStrategy strategy);
//
//        public void onImageLoadProgress(final MangaShowStrategy strategy, final int current, final int total);
//
//        public void onImageLoadEnd(final MangaShowStrategy strategy, final boolean success, final String message);
//
//        public void onChapterInfoLoadStart(final MangaShowStrategy strategy);
//
//        public void onChapterInfoLoadEnd(final MangaShowStrategy strategy, final boolean success, final String message);
//
//    }

}
