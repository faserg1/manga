package com.danilov.supermanga.core.database.crud;

import android.support.annotation.NonNull;

import java.util.Collection;

/**
 * Created by Semyon on 03.02.2016.
 */
public interface Crud<T extends Model> {

    T create(final T t);

    void delete(final T t);

    T update(final T t);

    @NonNull
    Collection<T> select(final Selector selector);

    interface Selector {

        String formatQuery();

    }

}
