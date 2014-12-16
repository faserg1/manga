package com.danilov.mangareader.core.model;

import com.danilov.mangareader.core.repository.RepositoryEngine;

/**
 * Created by Semyon Danilov on 27.07.2014.
 */
public class MangaSuggestion {

    private String url;

    private String title;

    private RepositoryEngine.Repository repository;

    public MangaSuggestion(final String title, final String url, final RepositoryEngine.Repository repository) {
        this.title = title;
        this.url = url;
        this.repository = repository;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public RepositoryEngine.Repository getRepository() {
        return repository;
    }

    public void setRepository(final RepositoryEngine.Repository repository) {
        this.repository = repository;
    }
}
