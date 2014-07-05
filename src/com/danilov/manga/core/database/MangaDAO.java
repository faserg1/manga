package com.danilov.manga.core.database;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

/**
 * Created by Semyon Danilov on 04.07.2014.
 */
public class MangaDAO {

    private static final int DAOVersion = 1;
    private static final String tableName = "manga";

    public static DatabaseHelper databaseHelper = null;

    static {
        String sdPath = Environment.getExternalStorageDirectory().getPath();
        String dbPath = sdPath + "/manga/db/manga.db";
        databaseHelper = new DatabaseHelper(dbPath, DAOVersion, new FirstOpenHandler());
    }


    private static class FirstOpenHandler implements DatabaseHelper.DatabaseFirstOpenHandler {

        @Override
        public void onFirstOpen(final SQLiteDatabase database) {
            DatabaseOptions.Builder builder = new DatabaseOptions.Builder();
            builder.setName(tableName);

        }

    }

}
