package com.danilov.manga.core.strategy;

import com.danilov.manga.core.interfaces.MangaShowObserver;
import com.danilov.manga.core.interfaces.MangaShowStrategy;
import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.repository.RepositoryEngine;
import com.danilov.manga.core.service.DownloadManager;
import com.danilov.manga.core.view.InAndOutAnim;
import com.danilov.manga.core.view.MangaImageSwitcher;

/**
 * Created by Semyon Danilov on 21.06.2014.
 *
 * This class handles showing pictures from web
 *
 */
public class OnlineManga implements MangaShowStrategy {

    private MangaImageSwitcher mangaImageSwitcher;
    private InAndOutAnim nextImageAnim;
    private InAndOutAnim prevImageAnim;

    private DownloadManager downloadManager;

    private Manga manga;
    private RepositoryEngine engine;
    private int currentImageNumber;
    private int currentChapter;

    private MangaShowObserver observer;

    public OnlineManga(final LocalManga manga, final MangaImageSwitcher mangaImageSwitcher, final InAndOutAnim nextImageAnim, final InAndOutAnim prevImageAnim) {
        this.manga = manga;
        this.engine = manga.getRepository().getEngine();
        this.mangaImageSwitcher = mangaImageSwitcher;
        this.nextImageAnim = nextImageAnim;
        this.prevImageAnim = prevImageAnim;
    }

    @Override
    public void showImage(final int i) {
//        if (i == currentImageNumber || i >= uris.size() || i < 0) {
//            return;
//        }
//        File imageFile = new File(uris.get(i));
//        if (i < currentImageNumber) {
//            mangaImageSwitcher.setInAndOutAnim(prevImageAnim);
//            mangaImageSwitcher.setPreviousImageDrawable(imageFile.getPath());
//        } else if (i > currentImageNumber) {
//            mangaImageSwitcher.setInAndOutAnim(nextImageAnim);
//            mangaImageSwitcher.setNextImageDrawable(imageFile.getPath());
//        }
//        currentImageNumber = i;
//        updateObserver();
    }

    private void updateObserver() {
        if (observer != null) {
            observer.onUpdate(this);
        }
    }

    @Override
    public void showChapter(final int i) {

    }

    @Override
    public void next() {

    }

    @Override
    public void previous() {

    }

    @Override
    public void initStrategy() throws ShowMangaException {
        try {
            engine.queryForChapters(manga);
        } catch (Exception e) {
            throw new ShowMangaException(e.getMessage());
        }
    }

    @Override
    public int getCurrentImageNumber() {
        return currentImageNumber;
    }

    @Override
    public int getTotalImageNumber() {
        return 0;
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
    public void setObserver(final MangaShowObserver observer) {
        this.observer = observer;
    }

}
