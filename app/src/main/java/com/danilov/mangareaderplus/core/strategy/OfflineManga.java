package com.danilov.mangareaderplus.core.strategy;

import com.danilov.mangareaderplus.core.interfaces.MangaShowObserver;
import com.danilov.mangareaderplus.core.interfaces.MangaShowStrategy;
import com.danilov.mangareaderplus.core.model.LocalManga;
import com.danilov.mangareaderplus.core.model.MangaChapter;
import com.danilov.mangareaderplus.core.repository.RepositoryEngine;
import com.danilov.mangareaderplus.core.repository.RepositoryException;
import com.danilov.mangareaderplus.core.util.Pair;
import com.danilov.mangareaderplus.core.util.Promise;
import com.danilov.mangareaderplus.core.view.CompatPager;
import com.danilov.mangareaderplus.core.view.InAndOutAnim;
import com.danilov.mangareaderplus.core.view.MangaViewPager;

import java.util.List;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public class OfflineManga implements MangaShowStrategy, CompatPager.OnPageChangeListener {

    private static final String TAG = "OfflineManga";

    private LocalManga manga;
    private MangaViewPager mangaViewPager;
    private InAndOutAnim nextImageAnim;
    private InAndOutAnim prevImageAnim;

    private RepositoryEngine engine = RepositoryEngine.Repository.OFFLINE.getEngine();

    private List<String> uris = null;

    private boolean destroyed = false;

    private int currentImageNumber = -1;
    private int currentChapter = -1;

    private MangaShowObserver observer;
    private StrategyDelegate.MangaShowListener listener;

    public OfflineManga(final LocalManga manga, final MangaViewPager mangaViewPager, final InAndOutAnim nextImageAnim, final InAndOutAnim prevImageAnim) {
        this.manga = manga;
        this.mangaViewPager = mangaViewPager;
        this.nextImageAnim = nextImageAnim;
        this.prevImageAnim = prevImageAnim;
        mangaViewPager.setOnline(false);
        mangaViewPager.setOnPageChangeListener(this);
    }

    @Override
    public Promise<Result> initStrategyOld() {
        return null;
    }

    @Override
    public Promise<Result> showChapterOld(final int chapterToShow) throws ShowMangaException {
        return null;
    }

    @Override
    public Promise<Result> nextOld() throws ShowMangaException {
        return null;
    }

    @Override
    public void showChapter(final int chapterToShow) {
        Pair pair = null;
        if (chapterToShow == -1) {
            pair = manga.getFirstExistingChapterAndIsLast();
        } else {
            pair = manga.getChapterAndIsLastByNumber(chapterToShow);
        }
        if (pair == null) {
            listener.onShowChapter(Result.NOT_DOWNLOADED, "");
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
            listener.onShowChapter(Result.ERROR, e.getMessage());
        }
        mangaViewPager.setUris(uris);
        updateObserver();
/*        Thread t = new Thread() {
            @Override
            public void run() {
                Thread.sleep(5000);
            }
        }*/
        listener.onShowChapter(isLast ? Result.LAST_DOWNLOADED : Result.SUCCESS, "");
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
        showChapter(chapter.getNumber());
    }

    @Override
    public void next() {
        if (currentImageNumber + 1 >= uris.size()) {
            //TODO: переход с 1 до 3 главы, если вторая не скачана - спрашивать юзера?
            showChapterFromNext();
        }
        showImage(currentImageNumber + 1);
    }

    @Override
    public void initStrategy() {
        if (manga.getChapters() != null) {
            listener.onInit(Result.SUCCESS, "");
        }
        try {
            engine.queryForChapters(manga);
            listener.onInit(Result.SUCCESS, "");
        } catch (Exception e) {
            listener.onInit(Result.ERROR, e.getMessage());
        }
    }

    @Override
    public void restoreState(final List<String> uris, final int chapter, final int image, final MangaViewPager mangaViewPager) {
        this.currentChapter = chapter;
        this.uris = uris;
        this.mangaViewPager = mangaViewPager;
        this.mangaViewPager.setUris(uris);
    }

    @Override
    public void showImage(final int i) {
        if (i == currentImageNumber || i >= uris.size() || i < 0) {
            return;
        }
        this.currentImageNumber = i;
        updateObserver();
        mangaViewPager.setCurrentItem(i);
    }

    private void updateObserver() {
        if (observer != null) {
            observer.onUpdate(this);
        }
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
    public void setObserver(final MangaShowObserver observer) {
        this.observer = observer;
    }

    @Override
    public void setOnStrategyListener(final StrategyDelegate.MangaShowListener listener) {
        this.listener = listener;
    }

    @Override
    public void destroy() {
        this.destroyed = true;
    }

    @Override
    public boolean isOnline() {
        return false;
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
        updateObserver();
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(final int state) {

    }

}