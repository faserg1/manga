package com.danilov.manga.core.database;

import android.os.Environment;

/**
 * Created by Semyon Danilov on 04.07.2014.
 */
public class MangaDAO {

    public static DatabaseHelper databaseHelper = null;

    static {
        String sdPath = Environment.getExternalStorageDirectory().getPath();
        String dbPath = sdPath + "/manga/db/manga.db";
        databaseHelper = new DatabaseHelper(dbPath);
    }



}
