package com.danilov.mangareader.core.strategy;

import android.os.Handler;
import android.util.Log;

import com.danilov.mangareader.core.interfaces.MangaShowObserver;
import com.danilov.mangareader.core.interfaces.MangaShowStrategy;
import com.danilov.mangareader.core.model.LocalManga;
import com.danilov.mangareader.core.model.MangaChapter;
import com.danilov.mangareader.core.repository.RepositoryEngine;
import com.danilov.mangareader.core.repository.RepositoryException;
import com.danilov.mangareader.core.util.Pair;
import com.danilov.mangareader.core.util.Promise;
import com.danilov.mangareader.core.view.InAndOutAnim;
import com.danilov.mangareader.core.view.MangaImageSwitcher;

import java.io.File;
import java.util.List;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public class OfflineManga implements MangaShowStrategy {

    private static final String TAG = "OfflineManga";

    private LocalManga manga;
    private MangaImageSwitcher mangaImageSwitcher;
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

    public OfflineManga(final LocalManga manga, final MangaImageSwitcher mangaImageSwitcher, final InAndOutAnim nextImageAnim, final InAndOutAnim prevImageAnim) {
        this.manga = manga;
        this.mangaImageSwitcher = mangaImageSwitcher;
        this.nextImageAnim = nextImageAnim;
        this.prevImageAnim = prevImageAnim;
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
    }

    @Override
    public void showImage(final int i) {
        if (i == currentImageNumber || i >= uris.size() || i < 0) {
            return;
        }
        File imageFile = new File(uris.get(i));
        if (i < currentImageNumber) {
            mangaImageSwitcher.setInAndOutAnim(prevImageAnim);
            mangaImageSwitcher.setPreviousImageDrawable(imageFile.getPath());
        } else if (i > currentImageNumber) {
            mangaImageSwitcher.setInAndOutAnim(nextImageAnim);
            mangaImageSwitcher.setNextImageDrawable(imageFile.getPath());
        }
        currentImageNumber = i;
        updateObserver();
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

}
