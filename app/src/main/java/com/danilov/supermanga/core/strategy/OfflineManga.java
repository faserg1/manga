package com.danilov.supermanga.core.strategy;

import android.os.Handler;

import com.danilov.supermanga.core.interfaces.MangaShowStrategy;
import com.danilov.supermanga.core.model.LocalManga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryException;
import com.danilov.supermanga.core.util.Pair;
import com.danilov.supermanga.core.view.CompatPager;

import java.util.List;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public class OfflineManga implements MangaShowStrategy, CompatPager.OnPageChangeListener {

    private static final String TAG = "OfflineManga";

    private LocalManga manga;

    private RepositoryEngine engine = RepositoryEngine.DefaultRepository.OFFLINE.getEngine();

    private List<String> uris = null;

    private int currentImageNumber = 0;
    private int currentChapter = 0;
    private StrategyDelegate.MangaShowListener listener;

    public OfflineManga(final LocalManga manga) {
        this.manga = manga;
    }

    @Override
    public void showChapter(final int chapterToShow, final boolean fromNext) {
        showChapterInternal(chapterToShow, null, fromNext);
    }

    @Override
    public void showChapterAndImage(final int chapterNumber, final int imageNumber, final boolean fromNext) {
        showChapterInternal(chapterNumber, () -> showImage(imageNumber), fromNext);
    }

    private boolean isShowChapterInProgress = false;

    private void showChapterInternal(final int chapterToShow, final Runnable runnable, final boolean fromNext) {
        if (isShowChapterInProgress) {
            return;
        }
        isShowChapterInProgress = true;
        Pair pair = null;
        if (chapterToShow == -1) {
            pair = manga.getFirstExistingChapterAndIsLast();
        } else {
            pair = manga.getChapterAndIsLastByNumber(chapterToShow);
        }
        if (pair == null) {
            if (fromNext) {
                listener.onNext(-1);
            }
            listener.onShowChapter(Result.NOT_DOWNLOADED, "");
            return;
        }
        MangaChapter chapter = (MangaChapter) pair.first;
        boolean isLast = (boolean) pair.second;
        try {
            uris = engine.getChapterImages(chapter);
        } catch (RepositoryException e) {
            listener.onShowChapter(Result.ERROR, e.getMessage());
            return;
        }
        if (chapterToShow == -1) {
            this.currentChapter = chapter.getNumber();
        } else {
            this.currentChapter = chapterToShow;
        }
        if (fromNext) {
            listener.onNext(currentChapter);
        }
        this.currentImageNumber = 0;
        listener.onShowChapter(isLast ? Result.LAST_DOWNLOADED : Result.SUCCESS, "");
        if (runnable != null) {
            runnable.run();
        }
    }

    @Override
    public void onCallbackDelivered(final StrategyDelegate.ActionType actionType) {
        switch (actionType) {
            case ON_SHOW_CHAPTER:
                isShowChapterInProgress = false;
                break;
            case ON_INIT:
                isInitStrategyInProgress = false;
                break;
        }
    }

    private void showChapterFromNext() {
        List<MangaChapter> chapters = manga.getChapters();
        boolean nextIsNeeded = false;
        MangaChapter chapter = null;
        for (int i = 0; i < chapters.size(); i++) {
            MangaChapter _chapter = chapters.get(i);
            if (nextIsNeeded) {
                chapter = _chapter;
                break;
            }
            if (_chapter.getNumber() == currentChapter) {
                nextIsNeeded = true;
            }
        }
        if (chapter == null) {
            listener.onShowChapter(Result.NOT_DOWNLOADED, "");
            return;
        }
        showChapter(chapter.getNumber(), true);
    }

    @Override
    public void next() {
        if (currentImageNumber + 1 >= uris.size()) {
            //TODO: переход с 1 до 3 главы, если вторая не скачана - спрашивать юзера?
            showChapterFromNext();
            return;
        }
        showImage(currentImageNumber + 1);
    }

    private boolean isInitStrategyInProgress = false;

    @Override
    public void initStrategy(final int chapter, final int image) {
        if (isInitStrategyInProgress) {
            return;
        }
        isInitStrategyInProgress = true;
        if (manga.getChapters() != null) {
            listener.onInit(Result.SUCCESS, "");
            if (uris != null) {
                showImage(image);
            } else {
                showChapterAndImage(chapter, image, false);
            }
        } else {
            try {
                engine.queryForChapters(manga);
                listener.onInit(Result.SUCCESS, "");
            } catch (Exception e) {
                listener.onInit(Result.ERROR, e.getMessage());
                return;
            }
        }
        if (uris != null) {
            showImage(image);
        } else {
            showChapterAndImage(chapter, image, false);
        }
    }

    @Override
    public boolean restoreState() {
        if (uris != null) {
            showImage(currentImageNumber);
            return true;
        }
        return false;
    }

    @Override
    public void showImage(final int i) {
        if (i >= uris.size() || i < 0) {
            return;
        }
        this.currentImageNumber = i;
        this.listener.onShowImage(i);
    }

    private Handler handler = new Handler();

    @Override
    public void previous() throws ShowMangaException {
        showImage(currentImageNumber - 1);
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
    public void setOnStrategyListener(final StrategyDelegate.MangaShowListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public boolean isInitInProgress() {
        return isInitStrategyInProgress;
    }

    @Override
    public RepositoryEngine getEngine() {
        return manga.getRepository().getEngine();
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
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(final int state) {

    }

}