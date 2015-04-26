package com.danilov.mangareaderplus.core.interfaces;

import com.danilov.mangareaderplus.core.strategy.ShowMangaException;
import com.danilov.mangareaderplus.core.util.Promise;

import java.util.List;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public interface MangaShowStrategy {

    void restoreState(final List<String> uris, final int chapter, final int image);

    void showImage(final int i);

    Promise<Result> showChapter(final int i) throws ShowMangaException;

    Promise<Result> next() throws ShowMangaException;

    void previous() throws ShowMangaException;

    Promise<Result> initStrategy();

    int getCurrentImageNumber();

    int getTotalImageNumber();

    int getCurrentChapterNumber();

    int getTotalChaptersNumber();

    List<String> getChapterUris();

    void setObserver(final MangaShowObserver observer);

    void setOnStrategyListener(MangaStrategyListener mangaStrategyListener);

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

    public interface MangaStrategyListener {

        public void onImageLoadStart(final MangaShowStrategy strategy);

        public void onImageLoadProgress(final MangaShowStrategy strategy, final int current, final int total);

        public void onImageLoadEnd(final MangaShowStrategy strategy, final boolean success, final String message);

        public void onChapterInfoLoadStart(final MangaShowStrategy strategy);

        public void onChapterInfoLoadEnd(final MangaShowStrategy strategy, final boolean success, final String message);

    }

}
