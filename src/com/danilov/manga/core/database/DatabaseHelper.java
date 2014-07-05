package com.danilov.manga.core.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Semyon Danilov on 04.07.2014.
 */
public class DatabaseHelper {

    private String path;
    private SQLiteDatabase database;
    private int version;
    private DatabaseFirstOpenHandler handler;

    public DatabaseHelper(final String path, final int version, final DatabaseFirstOpenHandler handler) {
        this.path = path;
        this.version = version;
        this.handler = handler;
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
        int v = database.getVersion();
        if (v != version) {
            database.beginTransaction();
            try {
                if (database.getVersion() == 0) {
                    if (handler != null) {
                        handler.onFirstOpen(database);
                    }
                }
                database.setVersion(version);
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
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
        int v = database.getVersion();
        if (v != version) {
            database.beginTransaction();
            try {
                if (database.getVersion() == 0) {
                    if (handler != null) {
                        handler.onFirstOpen(database);
                    }
                }
                database.setVersion(version);
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
        return database;
    }

    public static interface DatabaseFirstOpenHandler {

        void onFirstOpen(final SQLiteDatabase database);

    }

}
