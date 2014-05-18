package com.danilov.manga.core.model;

/**
 * Created by Semyon Danilov on 17.05.2014.
 */
public class Manga {

    private String uri;

    private String title;

    private String author; //optional desu ka?

    private String coverUri;

    public Manga(final String title, final String uri) {
        this.title = title;
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }

    public String getCoverUri() {
        return coverUri;
    }

    public void setCoverUri(final String coverUri) {
        this.coverUri = coverUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }
}
