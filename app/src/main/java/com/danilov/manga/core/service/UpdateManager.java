package com.danilov.manga.core.service;

import android.os.Handler;

import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.repository.RepositoryEngine;
import com.danilov.manga.core.repository.RepositoryException;
import com.danilov.manga.core.util.Pair;
import com.danilov.promise.Promise;
import com.danilov.promise.Promise.Action;

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
                        int quantity = manga.getChaptersQuantity();
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