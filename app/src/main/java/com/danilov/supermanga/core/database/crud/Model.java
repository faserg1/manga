package com.danilov.supermanga.core.database.crud;

/**
 * Created by Semyon on 03.02.2016.
 */
public abstract class Model {

    long id;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public abstract void load(final ResultSet resultSet);

}