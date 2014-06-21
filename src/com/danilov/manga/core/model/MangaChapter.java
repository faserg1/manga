package com.danilov.manga.core.model;

/**
 * Created by Semyon Danilov on 21.05.2014.
 */
public class MangaChapter {

    private String title;

    private String uri;

    private int number;

    public MangaChapter(final String title, final int number, final String uri) {
        this.title = title;
        this.uri = uri;
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }

    public int getNumber() {
        return number;
    }

}
