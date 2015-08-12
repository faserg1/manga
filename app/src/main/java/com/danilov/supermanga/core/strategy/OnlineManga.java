package com.danilov.supermanga.core.strategy;

import android.os.Handler;
import android.util.Log;

import com.danilov.supermanga.core.interfaces.MangaShowStrategy;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryException;
import com.danilov.supermanga.core.view.CompatPager;
import com.danilov.supermanga.core.view.MangaViewPager;

import java.util.List;

/**
 * Created by Semyon Danilov on 21.06.2014.
 *
 * This class handles showing pictures from web
 *
 */
public class OnlineManga implements MangaShowStrategy, CompatPager.OnPageChangeListener {

    private static final String TAG = "OnlineManga";

    private Manga manga;
    private RepositoryEngine engine;
    private int currentImageNumber = 0;
    private int currentChapter;
    private int totalImages = 0;
    private List<String> uris = null;

    private StrategyDelegate.MangaShowListener listener;

    public OnlineManga(final Manga manga) {
        this.manga = manga;
        this.engine = manga.getRepository().getEngine();
    }

    @Override
    public boolean restoreState() {
        if (uris != null) {
            this.totalImages = uris.size();
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

    @Override
    public void showChapter(final int i) {
        showChapterInternal(i, null);
    }

    @Override
    public void showChapterAndImage(final int chapterNumber, final int imageNumber) {
        showChapterInternal(chapterNumber, new Runnable() {
            @Override
            public void run() {
                showImage(imageNumber);
            }
        });
    }

    private boolean isShowChapterInProgress = false;

    private void showChapterInternal(final int i, final Runnable runnable) {
        if (isShowChapterInProgress) {
            return;
        }
        isShowChapterInProgress = true;
        final int chapterNum = i < 0 ? 0 : i;
        int chaptersQuantity = manga.getChaptersQuantity();
        if (chaptersQuantity <= 0) {
            listener.onShowChapter(Result.ERROR, "No chapters to show"); //TODO: replace with getString
            return;
        }
        final MangaChapter chapter = manga.getChapterByNumber(chapterNum);
        if (chapter == null) {
            boolean isOnLastChapterAndTappedNext = currentChapter == (chaptersQuantity - 1) && chapterNum == chaptersQuantity;
            listener.onShowChapter(isOnLastChapterAndTappedNext ? Result.ALREADY_FINAL_CHAPTER : Result.NO_SUCH_CHAPTER, "");
            return;
        }
        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    uris = engine.getChapterImages(chapter);
                    totalImages = uris.size();
                    OnlineManga.this.currentChapter = chapterNum;
                    OnlineManga.this.currentImageNumber = 0;
                } catch (final Exception e) {
                    listener.onShowChapter(Result.ERROR, e.getMessage());
                    return;
                }
                listener.onShowChapter(Result.SUCCESS, "");
                if (runnable != null) {
                    runnable.run();
                }
            }
        };
        thread.start();
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

    @Override
    public void next() {
        if (currentImageNumber + 1 >= uris.size()) {
            showChapter(currentChapter + 1);
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
                showChapterAndImage(chapter, image);
            }

        } else {
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        engine.queryForChapters(manga);
                        listener.onInit(Result.SUCCESS, "");

                        if (uris != null) {
                            showImage(image);
                        } else {
                            showChapterAndImage(chapter, image);
                        }

                    } catch (RepositoryException e) {
                        Log.e(TAG, "Failed to load chapters: " + e.getMessage());
                        listener.onInit(Result.ERROR, e.getMessage());
                    }
                }

            };
            t.start();
        }
    }

    @Override
    public void previous() throws ShowMangaException{
        showImage(currentImageNumber - 1);
    }

    @Override
    public int getCurrentImageNumber() {
        return currentImageNumber;
    }

    @Override
    public int getTotalImageNumber() {
        return totalImages;
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
        return true;
    }

    @Override
    public boolean isInitInProgress() {
        return isInitStrategyInProgress;
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(final int position) {
        this.currentImageNumber = position;
    }

    @Override
    public void onPageScrollStateChanged(final int state) {

    }

}
