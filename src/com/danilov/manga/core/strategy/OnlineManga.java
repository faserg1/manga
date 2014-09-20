package com.danilov.manga.core.strategy;

import android.os.Environment;
import android.os.Handler;
import com.danilov.manga.core.interfaces.MangaShowObserver;
import com.danilov.manga.core.interfaces.MangaShowStrategy;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.model.MangaChapter;
import com.danilov.manga.core.repository.RepositoryEngine;
import com.danilov.manga.core.repository.RepositoryException;
import com.danilov.manga.core.service.DownloadManager;
import com.danilov.manga.core.view.InAndOutAnim;
import com.danilov.manga.core.view.MangaImageSwitcher;

import java.io.File;
import java.util.List;

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
    private Handler handler;

    private Manga manga;
    private RepositoryEngine engine;
    private int currentImageNumber;
    private int currentChapter;
    private List<String> uris = null;

    private MangaShowObserver observer;
    private MangaStrategyListener listener;

    public OnlineManga(final Manga manga, final MangaImageSwitcher mangaImageSwitcher, final InAndOutAnim nextImageAnim, final InAndOutAnim prevImageAnim) {
        this.manga = manga;
        this.engine = manga.getRepository().getEngine();
        this.mangaImageSwitcher = mangaImageSwitcher;
        this.nextImageAnim = nextImageAnim;
        this.prevImageAnim = prevImageAnim;
        this.handler = new Handler();
        this.downloadManager = new DownloadManager();
    }

    @Override
    public void showImage(final int i) {
        if (i == currentImageNumber || i >= uris.size() || i < 0) {
            return;
        }
        listener.onImageLoadStart(this);
        String uri = uris.get(i);
        String path = Environment.getExternalStorageDirectory() + "/cache/";
        File f = new File(path);
        f.mkdirs();
        path += "1.png";
        downloadManager.cancelAllDownloads();
        downloadManager.setListener(new DownloadListener(path, i));
        downloadManager.startDownload(uri, path);
    }

    private class DownloadListener implements DownloadManager.DownloadProgressListener {

        private String path;
        private int imgNum;

        public DownloadListener(final String path, final int imgNum) {
            this.path = path;
            this.imgNum = imgNum;
        }

        @Override
        public void onProgress(final DownloadManager.Download download, final int progress) {

        }

        @Override
        public void onPause(final DownloadManager.Download download) {

        }

        @Override
        public void onResume(final DownloadManager.Download download) {

        }

        @Override
        public void onComplete(final DownloadManager.Download download) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    File imageFile = new File(path);
                    if (imgNum < currentImageNumber) {
                        mangaImageSwitcher.setInAndOutAnim(prevImageAnim);
                        mangaImageSwitcher.setPreviousImageDrawable(imageFile.getPath());
                    } else if (imgNum > currentImageNumber) {
                        mangaImageSwitcher.setInAndOutAnim(nextImageAnim);
                        mangaImageSwitcher.setNextImageDrawable(imageFile.getPath());
                    }
                    currentImageNumber = imgNum;
                    updateObserver();
                }
            });
        }

        @Override
        public void onCancel(final DownloadManager.Download download) {

        }

        @Override
        public void onError(final DownloadManager.Download download, final String errorMsg) {

        }

    }

    private void updateObserver() {
        if (observer != null) {
            observer.onUpdate(this);
        }
    }

    @Override
    public void showChapter(final int i) throws ShowMangaException {
        this.currentChapter = i;
        this.currentImageNumber = -1;
        if (manga.getChaptersQuantity() <= 0) {
            throw new ShowMangaException("No chapters to show");
        }
        final MangaChapter chapter = manga.getChapterByListPos(i);
        if (chapter == null) {
            throw new ShowMangaException("No chapter ");
        }
        listener.onChapterInfoLoadStart(this);
        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    uris = engine.getChapterImages(chapter);
                } catch (Exception e) {
                    listener.onChapterInfoLoadEnd(OnlineManga.this, false, e.getMessage());
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onChapterInfoLoadEnd(OnlineManga.this, true, "");
                        if (uris != null && uris.size() > 0) {
                            showImage(0);
                        }
                        updateObserver();
                    }
                });
            }
        };
        thread.start();
    }

    @Override
    public void next() throws ShowMangaException {
        if (currentImageNumber + 1 >= uris.size()) {
            showChapter(currentChapter + 1);
            return;
        }
        showImage(currentImageNumber + 1);
    }

    @Override
    public void previous() throws ShowMangaException{
        showImage(currentImageNumber - 1);
    }

    @Override
    public void initStrategy() throws ShowMangaException {
        try {
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        engine.queryForChapters(manga);
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                listener.onInit(OnlineManga.this);
                            }

                        });
                    } catch (RepositoryException e) {
                        e.printStackTrace();
                    }
                }

            };
            t.start();
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
    public String getTotalChaptersNumber() {
        if (manga == null) {
            return "0";
        }
        return String.valueOf(manga.getChaptersQuantity());
    }

    @Override
    public void setObserver(final MangaShowObserver observer) {
        this.observer = observer;
    }

    @Override
    public void setOnStrategyListener(final MangaStrategyListener mangaStrategyListener) {
        this.listener = mangaStrategyListener;
    }

}
