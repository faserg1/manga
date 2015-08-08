package com.danilov.mangareaderplus.core.strategy;

import android.os.Handler;
import android.util.Log;

import com.danilov.mangareaderplus.core.interfaces.MangaShowObserver;
import com.danilov.mangareaderplus.core.interfaces.MangaShowStrategy;
import com.danilov.mangareaderplus.core.model.Manga;
import com.danilov.mangareaderplus.core.model.MangaChapter;
import com.danilov.mangareaderplus.core.repository.RepositoryEngine;
import com.danilov.mangareaderplus.core.repository.RepositoryException;
import com.danilov.mangareaderplus.core.service.DownloadManager;
import com.danilov.mangareaderplus.core.view.CompatPager;
import com.danilov.mangareaderplus.core.view.InAndOutAnim;
import com.danilov.mangareaderplus.core.view.MangaViewPager;

import java.util.List;

/**
 * Created by Semyon Danilov on 21.06.2014.
 *
 * This class handles showing pictures from web
 *
 */
public class OnlineManga implements MangaShowStrategy, CompatPager.OnPageChangeListener {

    private static final String TAG = "OnlineManga";

    private MangaViewPager mangaViewPager;

    private Manga manga;
    private RepositoryEngine engine;
    private int currentImageNumber = 0;
    private int currentChapter;
    private int totalImages = 0;
    private List<String> uris = null;

    private Handler handler = new Handler();

    private StrategyDelegate.MangaShowListener listener;

    public OnlineManga(final Manga manga, final MangaViewPager mangaViewPager) {
        this.manga = manga;
        this.engine = manga.getRepository().getEngine();
        this.mangaViewPager = mangaViewPager;
        mangaViewPager.setOnline(true);
        mangaViewPager.setOnPageChangeListener(this);
    }

    @Override
    public void restoreState(final List<String> uris, final int chapter, final int image, final MangaViewPager mangaViewPager) {
        this.currentChapter = chapter;
        this.mangaViewPager = mangaViewPager;
        this.mangaViewPager.setOnline(true);
        this.mangaViewPager.setOnPageChangeListener(this);
        this.uris = uris;
        if (uris != null) {
            this.totalImages = uris.size();
            this.mangaViewPager.setUris(uris);
        }
    }

    @Override
    public void showImage(final int i) {
        if (i >= uris.size() || i < 0) {
            return;
        }
        this.currentImageNumber = i;
        mangaViewPager.setCurrentItem(i);
        this.listener.onShowImage(i);
    }

    @Override
    public void showChapter(final int i) {
        this.uris = null;
        int chapterNum = i < 0 ? 0 : i;
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
        this.currentChapter = chapterNum;
        this.currentImageNumber = 0;
        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    uris = engine.getChapterImages(chapter);
                    totalImages = uris.size();
                } catch (final Exception e) {
                    listener.onShowChapter(Result.ERROR, e.getMessage());
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (uris != null) {
                            mangaViewPager.setUris(uris);
                        }
                        listener.onShowChapter(Result.SUCCESS, "");
                    }
                });
            }
        };
        thread.start();
    }

    @Override
    public void next() {
        if (currentImageNumber + 1 >= uris.size()) {
            showChapter(currentChapter + 1);
            return;
        }
        showImage(currentImageNumber + 1);
    }

    @Override
    public void initStrategy(final int chapter, final int image) {
        if (manga.getChapters() != null) {
            listener.onInit(Result.SUCCESS, "");

            if (uris != null) {
                showImage(image);
            } else {
                showChapter(chapter);
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
                            showChapter(chapter);
                        }

                    } catch (RepositoryException e) {
                        Log.e(TAG, "Failed to load chapters: " + e.getMessage());
                        listener.onInit(Result.ERROR,  e.getMessage());
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
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(final int position) {
        this.currentImageNumber = position;
        this.listener.onShowImage(position);
    }

    @Override
    public void onPageScrollStateChanged(final int state) {

    }

}
