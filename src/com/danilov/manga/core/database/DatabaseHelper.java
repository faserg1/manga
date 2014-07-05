package com.danilov.manga.core.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Semyon Danilov on 04.07.2014.
 */
public class DatabaseHelper {

    public String path;
    public SQLiteDatabase database;

    public DatabaseHelper(final String path) {
        this.path = path;
    }

    public synchronized SQLiteDatabase openWritable() throws DatabaseException {
        if (database != null) {
            if (!database.isOpen()) {
                database = null;
            } else if (!database.isReadOnly()) {
                return database;
            }
        }
        if (database != null) {
            throw new DatabaseException("Can't open writable");
        }
        database = SQLiteDatabase.openOrCreateDatabase(path, null);
        return database;
    }

    public synchronized SQLiteDatabase openReadable() throws DatabaseException {
        if (database != null) {
            if (!database.isOpen()) {
                database = null;
            } else {
                return database;
            }
        }
        database = SQLiteDatabase.openOrCreateDatabase(path, null);
        return database;
    }

    public static interface DatabaseFirstOpenHandler {

        void onFirstOpen(final SQLiteDatabase database);

    }

}
