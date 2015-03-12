package com.danilov.mangareaderplus.core.service;

import android.os.Handler;

import com.danilov.mangareaderplus.core.model.Manga;
import com.danilov.mangareaderplus.core.model.MangaChapter;
import com.danilov.mangareaderplus.core.repository.RepositoryEngine;
import com.danilov.mangareaderplus.core.repository.RepositoryException;
import com.danilov.mangareaderplus.core.util.Pair;
import com.danilov.mangareaderplus.core.util.Promise;
import com.danilov.mangareaderplus.core.util.Promise.Action;

import java.util.List;


/**
 * Created by Semyon on 23.11.2014.
 */
public class UpdateManager {

    private Handler handler = new Handler();

    private List<Manga> mangasToUpdate;

    public UpdateManager(final List<Manga> mangasToUpdate) {
        this.mangasToUpdate = mangasToUpdate;
    }

    public void checkUpdate(final Action<Pair, Void> successHandler,
                            final Action<Exception, Void> errorHandler) {
        if (mangasToUpdate == null) {
            return;
        }
        for (Manga manga : mangasToUpdate) {
            Promise<Pair> promise = updateInfo(manga);
            promise.then(successHandler);
            promise.catchException(errorHandler);
        }
    }

    private Promise<Pair> updateInfo(final Manga manga) {
        final RepositoryEngine engine = manga.getRepository().getEngine();
        final Promise<Pair> promise = Promise.run(new Promise.PromiseRunnable<Pair>() {
            @Override
            public void run(final Promise<Pair>.Resolver resolver) {
                try {
                    int oldQuantity = manga.getChaptersQuantity();
                    if (engine.queryForChapters(manga)) {
                        List<MangaChapter> chapters = manga.getChapters();
                        int quantity = 0;
                        if (chapters != null) {
                            quantity = chapters.size();
                        }
                        manga.setChaptersQuantity(quantity);
                        int diff = quantity - oldQuantity;
                        resolver.resolve(Pair.obtain(manga, diff));
                    }
                } catch (RepositoryException e) {
                    resolver.except(e);
                }
            }
        }, true);
        return promise;
    }

}