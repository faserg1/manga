package com.danilov.manga.core.model;

import com.danilov.manga.core.repository.RepositoryEngine;

/**
 * Created by Semyon Danilov on 09.07.2014.
 */
public class LocalManga extends Manga {

    private int localId;

    public LocalManga(final String title, final String uri, final RepositoryEngine.Repository repository) {
        super(title, uri, repository);
    }

    public int getLocalId() {
        return localId;
    }

    public void setLocalId(final int localId) {
        this.localId = localId;
    }

}