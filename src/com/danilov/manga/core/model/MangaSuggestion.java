package com.danilov.manga.core.model;

/**
 * Created by Semyon Danilov on 27.07.2014.
 */
public class MangaSuggestion {

    private String url;

    private String title;

    public MangaSuggestion(final String title, final String url) {
        this.title = title;
        this.url = url;
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
}
