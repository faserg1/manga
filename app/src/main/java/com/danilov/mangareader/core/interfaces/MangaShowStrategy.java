package com.danilov.mangareader.core.interfaces;

import com.danilov.mangareader.core.strategy.ShowMangaException;
import com.danilov.mangareader.core.util.OldPromise;

import java.util.List;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public interface MangaShowStrategy {

    void restoreState(final List<String> uris, final int chapter, final int image);

    void showImage(final int i) throws ShowMangaException;

    OldPromise<MangaShowStrategy> showChapter(final int i) throws ShowMangaException;

    OldPromise<MangaShowStrategy> next() throws ShowMangaException;

    void previous() throws ShowMangaException;

    OldPromise<MangaShowStrategy> initStrategy() throws ShowMangaException;

    int getCurrentImageNumber();

    int getTotalImageNumber();

    int getCurrentChapterNumber();

    String getTotalChaptersNumber();

    List<String> getChapterUris();

    void setObserver(final MangaShowObserver observer);

    void setOnStrategyListener(MangaStrategyListener mangaStrategyListener);

    void destroy();

    public interface MangaStrategyListener {

        public void onImageLoadStart(final MangaShowStrategy strategy);

        public void onImageLoadProgress(final MangaShowStrategy strategy, final int current, final int total);

        public void onImageLoadEnd(final MangaShowStrategy strategy, final boolean success, final String message);

        public void onChapterInfoLoadStart(final MangaShowStrategy strategy);

        public void onChapterInfoLoadEnd(final MangaShowStrategy strategy, final boolean success, final String message);

    }

}
