package com.danilov.mangareader.core.model;

import java.util.Date;

/**
 * Created by Semyon Danilov on 02.10.2014.
 */
public class HistoryElement {

    private Manga manga;

    private int chapter;

    private int page;

    private int id;

    private Date date;

    //TODO: add isOnline

    public HistoryElement(final Manga manga, final int chapter, final int page) {
        this.manga = manga;
        this.chapter = chapter;
        this.page = page;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public Manga getManga() {
        return manga;
    }

    public void setManga(final Manga manga) {
        this.manga = manga;
    }

    public int getChapter() {
        return chapter;
    }

    public void setChapter(final int chapter) {
        this.chapter = chapter;
    }

    public int getPage() {
        return page;
    }

    public void setPage(final int page) {
        this.page = page;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

}
