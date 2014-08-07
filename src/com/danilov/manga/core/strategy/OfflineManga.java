package com.danilov.manga.core.strategy;

import com.danilov.manga.core.interfaces.MangaShowStrategy;
import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.view.MangaImageSwitcher;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public class OfflineManga implements MangaShowStrategy {

    private LocalManga manga;
    private MangaImageSwitcher mangaImageSwitcher;

    public OfflineManga(final LocalManga manga, final MangaImageSwitcher mangaImageSwitcher) {
        this.manga = manga;
        this.mangaImageSwitcher = mangaImageSwitcher;
    }

    @Override
    public void showImage(final int i) {
    }

    @Override
    public void next() {

    }

    @Override
    public void previous() {

    }

}
