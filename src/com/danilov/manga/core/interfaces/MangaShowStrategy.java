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

}
