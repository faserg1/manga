package com.danilov.mangareaderplus.core.interfaces;

import com.danilov.mangareaderplus.core.strategy.ShowMangaException;
import com.danilov.mangareaderplus.core.strategy.StrategyDelegate;
import com.danilov.mangareaderplus.core.view.MangaViewPager;

import java.util.List;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public interface MangaShowStrategy {

    void restoreState(final List<String> uris, final int chapter, final int image, final MangaViewPager mangaViewPager);

    void showImage(final int i);

    void showChapter(final int i);

    void showChapterAndImage(final int chapterNumber, final int imageNumber);

    void onCallbackDelivered(final StrategyDelegate.ActionType actionType);

    void next();

    void initStrategy(final int chapter, final int image);

    void previous() throws ShowMangaException;

    int getCurrentImageNumber();

    int getTotalImageNumber();

    int getCurrentChapterNumber();

    int getTotalChaptersNumber();

    List<String> getChapterUris();

    void setOnStrategyListener(final StrategyDelegate.MangaShowListener listener);

    boolean isOnline();

    //TODO: return them actually!
    public enum Result {
        ERROR,
        SUCCESS,
        ALREADY_FINAL_CHAPTER,
        NO_SUCH_CHAPTER,
        LAST_DOWNLOADED,
        NOT_DOWNLOADED
    }

}
