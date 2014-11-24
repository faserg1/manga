package com.danilov.manga.core.model;

import java.util.Date;

/**
 * Created by Semyon on 24.11.2014.
 */
public class UpdatesElement {

    private Manga manga;

    private int id;

    private Date timestamp;

    private int difference;

    public Manga getManga() {
        return manga;
    }

    public void setManga(final Manga manga) {
        this.manga = manga;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getDifference() {
        return difference;
    }

    public void setDifference(final int difference) {
        this.difference = difference;
    }

}