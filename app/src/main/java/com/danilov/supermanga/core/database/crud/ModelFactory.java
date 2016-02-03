package com.danilov.supermanga.core.database.crud;

/**
 * Created by Semyon on 03.02.2016.
 */
public interface ModelFactory<T extends Model> {

    T create();

}
