package com.danilov.manga.core.model;

/**
 * Created by Semyon Danilov on 21.05.2014.
 */
public class MangaChapter {

    private String title;

    private String uri;

    public MangaChapter(final String title, final String uri) {
        this.title = title;
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }

}
