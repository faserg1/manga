package com.danilov.supermanga.core.model;

import java.util.Date;

/**
 * Created by Semyon on 24.11.2014.
 */
public class UpdatesElement {

    public UpdatesElement() {
    }

    public UpdatesElement(final int id, final Manga manga, final int difference) {
        this.id = id;
        this.manga = manga;
        this.difference = difference;
    }

    public UpdatesElement(final int id, final Manga manga, final Date timestamp, final int difference) {
        this.manga = manga;
        this.id = id;
        this.timestamp = timestamp;
        this.difference = difference;
    }

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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpdatesElement element = (UpdatesElement) o;

        if (id != element.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

}