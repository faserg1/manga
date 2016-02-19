package com.danilov.supermanga.core.strategy;

import android.os.Handler;
import android.support.annotation.Nullable;

import com.danilov.supermanga.core.interfaces.MangaShowStrategy;
import com.danilov.supermanga.core.view.CompatPager;
import com.danilov.supermanga.core.view.MangaViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon on 07.08.2015.
 */
public class StrategyDelegate implements CompatPager.OnPageChangeListener {

    private MangaShowStrategy strategy;

    private MangaShowListener listener;

    private boolean canHandle = false;

    private List<DelayedAction> delayedActions = new ArrayList<>();

    private Handler handler = new Handler();

    private boolean isStrategyInitialized = false;

    private MangaViewPager mangaViewPager;

    private boolean isOnline;

    public StrategyDelegate(final MangaViewPager mangaViewPager, final MangaShowStrategy strategy, final boolean isOnline) {
        this.strategy = strategy;
        this.isOnline = isOnline;
        strategy.setOnStrategyListener(listenerWrapper);
        this.mangaViewPager = mangaViewPager;
        this.mangaViewPager.setOnline(isOnline);
        this.mangaViewPager.setOnPageChangeListener(this);
    }

    public boolean restoreState(final MangaViewPager mangaViewPager) {
        this.mangaViewPager = mangaViewPager;
        this.mangaViewPager.setOnline(isOnline);
        this.mangaViewPager.setOnPageChangeListener(this);

        List<String> chapterUris = strategy.getChapterUris();
        if (chapterUris != null) {
            mangaViewPager.setEngine(strategy.getEngine());
            mangaViewPager.setUris(chapterUris);
        }

        return strategy.restoreState();
    }

    public void showImage(final int i) {
        strategy.showImage(i);
    }

    public int getCurrentImageNumber() {
        if (_initTempImage != -1) {
            return _initTempImage;
        }
        return strategy.getCurrentImageNumber();
    }

    public int getCurrentChapterNumber() {
        if (_initTempChapter != -1) {
            return _initTempChapter;
        }
        return strategy.getCurrentChapterNumber();
    }

    public boolean isOnline() {
        return strategy.isOnline();
    }

    public void previous() throws ShowMangaException {
        strategy.previous();
    }

    public int getTotalChaptersNumber() {
        return strategy.getTotalChaptersNumber();
    }

    public int getTotalImageNumber() {
        return strategy.getTotalImageNumber();
    }

    public List<String> getChapterUris() {
        return strategy.getChapterUris();
    }

    private int _initTempChapter = -1;
    private int _initTempImage = -1;

    public void initStrategy(final int chapter, final int image) {
        _initTempChapter = chapter;
        _initTempImage = image;
        strategy.initStrategy(chapter, image);
    }

    public void reInit() {
        strategy.initStrategy(_initTempChapter, _initTempImage);
    }

    public void next() {
        strategy.next();
    }

    public void showChapter(final int i, final boolean fromNext) {
        strategy.showChapter(i, fromNext);
    }

    public boolean isInitializationInProgress() { //shepard
        return strategy.isInitInProgress();
    }

    private MangaShowListener listenerWrapper = new MangaShowListener() {

        @Override
        public void onShowImage(final int number) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (canHandle && listener != null) {
                            mangaViewPager.setCurrentItem(number);
                            listener.onShowImage(number);
                    } else {
                        delayedActions.add(new DelayedAction(ActionType.ON_SHOW_IMAGE, number));
                    }
                }
            });
        }

        @Override
        public void onPreviousPicture() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (canHandle && listener != null) {
                            listener.onPreviousPicture();
                    } else {
                        delayedActions.add(new DelayedAction(ActionType.ON_PREVIOUS_PICTURE));
                    }
                }
            });
        }

        @Override
        public void onShowChapter(final MangaShowStrategy.Result result, final String message) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (canHandle && listener != null) {
                        if (result == MangaShowStrategy.Result.SUCCESS || result == MangaShowStrategy.Result.LAST_DOWNLOADED) {
                            List<String> chapterUris = strategy.getChapterUris();
                            if (chapterUris != null) {
                                mangaViewPager.setEngine(strategy.getEngine());
                                mangaViewPager.setUris(chapterUris);
                            }
                        }
                        listener.onShowChapter(result, message);
                        strategy.onCallbackDelivered(ActionType.ON_SHOW_CHAPTER);
                    } else {
                        delayedActions.add(new DelayedAction(ActionType.ON_SHOW_CHAPTER, result, message));
                    }
                }
            });
        }

        @Override
        public void onNext(@Nullable final Integer chapterNum) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (canHandle && listener != null) {
                            listener.onNext(chapterNum);
                    } else {
                        delayedActions.add(new DelayedAction(ActionType.ON_NEXT, chapterNum));
                    }
                }
            });
        }

        @Override
        public void onInit(final MangaShowStrategy.Result result, final String message) {
            if (result == MangaShowStrategy.Result.SUCCESS) {
                isStrategyInitialized = true;
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (canHandle && listener != null) {
                            listener.onInit(result, message);
                            strategy.onCallbackDelivered(ActionType.ON_INIT);
                            if (result == MangaShowStrategy.Result.SUCCESS) {
                                _initTempChapter = -1;
                                _initTempImage = -1;
                            }
                    } else {
                        delayedActions.add(new DelayedAction(ActionType.ON_INIT, result, message));
                    }
                }
            });
        }

    };

    public boolean isStrategyInitialized() {
        return isStrategyInitialized;
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(final int position) {
        strategy.onPageSelected(position);
        if (listener != null) {
            listener.onShowImage(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(final int state) {

    }

    public interface MangaShowListener {

        void onShowImage(final int pageNum);

        void onPreviousPicture();

        void onShowChapter(final MangaShowStrategy.Result result, final String message);

        void onNext(@Nullable final Integer chapterNum);

        void onInit(final MangaShowStrategy.Result result, final String message);

    }

    private class DelayedAction {

        private ActionType type;

        private Integer number;

        private MangaShowStrategy.Result result;
        private String message;

        private DelayedAction(final ActionType type) {
            this.type = type;
        }

        private DelayedAction(final ActionType type, final MangaShowStrategy.Result result, final String message) {
            this.type = type;
            this.result = result;
            this.message = message;
        }

        private DelayedAction(final ActionType type, final Integer number) {
            this.type = type;
            this.number = number;
        }

        public void run() {
            switch (type) {
                case ON_SHOW_IMAGE:
                    listenerWrapper.onShowImage(number);
                    break;
                case ON_PREVIOUS_PICTURE:
                    listenerWrapper.onPreviousPicture();
                    break;
                case ON_SHOW_CHAPTER:
                    listenerWrapper.onShowChapter(result, message);
                    break;
                case ON_NEXT:
                    listenerWrapper.onNext(number);
                    break;
                case ON_INIT:
                    listenerWrapper.onInit(result, message);
                    break;
            }
        }

    }

    public void showChapterAndImage(final int chapterNumber, final int imageNumber, final boolean fromNext) {
        strategy.showChapterAndImage(chapterNumber, imageNumber, fromNext);
    }

    public enum ActionType {
        ON_SHOW_IMAGE,
        ON_PREVIOUS_PICTURE,
        ON_SHOW_CHAPTER,
        ON_NEXT,
        ON_INIT
    }

    public void onResume(final MangaShowListener listener) {
        this.canHandle = true;
        this.listener = listener;
        while (!delayedActions.isEmpty()) {
            DelayedAction delayedAction = delayedActions.remove(0);
            delayedAction.run();
        }
    }

    public void onPause() {
        this.canHandle = false;
        this.listener = null;
    }

}
