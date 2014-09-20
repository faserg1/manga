package com.danilov.manga.core.interfaces;

import com.danilov.manga.core.strategy.ShowMangaException;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public interface MangaShowStrategy {

    void showImage(final int i) throws ShowMangaException;

    void showChapter(final int i) throws ShowMangaException;

    void next() throws ShowMangaException;

    void previous() throws ShowMangaException;

    void initStrategy() throws ShowMangaException;

    int getCurrentImageNumber();

    int getTotalImageNumber();

    int getCurrentChapterNumber();

    String getTotalChaptersNumber();

    void setObserver(final MangaShowObserver observer);

    void setOnStrategyListener(MangaStrategyListener mangaStrategyListener);

    public interface MangaStrategyListener {

        public void onInit(final MangaShowStrategy strategy);

        public void onImageLoadStart(final MangaShowStrategy strategy);

        public void onImageLoadEnd(final MangaShowStrategy strategy, final boolean success, final String message);

        public void onChapterInfoLoadStart(final MangaShowStrategy strategy);

        public void onChapterInfoLoadEnd(final MangaShowStrategy strategy, final boolean success, final String message);

    }

}
