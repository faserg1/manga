package com.danilov.mangareader.core.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Semyon Danilov on 04.07.2014.
 */
public class DatabaseHelper {

    private String path;
    private SQLiteDatabase database;
    private int version;
    private DatabaseUpgradeHandler handler;

    private boolean withSpecificTable = false;
    private String tableName;

    public DatabaseHelper(final String path, final int version, final DatabaseUpgradeHandler handler) {
        this.path = path;
        this.version = version;
        this.handler = handler;
    }

    public DatabaseHelper(final String path, final int version, final DatabaseUpgradeHandler handler, final boolean withSpecificTable, final String tableName) {
        this(path, version, handler);
        this.withSpecificTable = withSpecificTable;
        this.tableName = tableName;
    }

    public synchronized SQLiteDatabase openWritable() throws DatabaseAccessException {
        return withSpecificTable ? internalOpenWritableWithSpecificTable() : internalOpenWritable();
    }

    private SQLiteDatabase internalOpenWritableWithSpecificTable() throws DatabaseAccessException {
        if (database != null) {
            if (!database.isOpen()) {
                database = null;
            } else if (!database.isReadOnly()) {
                return database;
            }
        }
        if (database != null) {
            throw new DatabaseAccessException("Can't open writable");
        }
        database = SQLiteDatabase.openOrCreateDatabase(path, null);
        int v = database.getVersion();
        boolean shouldUpgrade = false;
        if (v != version || !tableExists(tableName, database)) {
            shouldUpgrade = true;
        }
        if (shouldUpgrade) {
            database.beginTransaction();
            try {
                if (handler != null) {
                    handler.onUpgrade(database);
                }
                database.setVersion(version);
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
        return database;
    }

    private SQLiteDatabase internalOpenWritable() throws DatabaseAccessException {
        if (database != null) {
            if (!database.isOpen()) {
                database = null;
            } else if (!database.isReadOnly()) {
                return database;
            }
        }
        if (database != null) {
            throw new DatabaseAccessException("Can't open writable");
        }
        database = SQLiteDatabase.openOrCreateDatabase(path, null);
        int v = database.getVersion();
        boolean shouldUpgrade = false;
        if (v != version) {
            shouldUpgrade = true;
        }
        if (shouldUpgrade) {
            database.beginTransaction();
            try {
                if (handler != null) {
                    handler.onUpgrade(database);
                }
                database.setVersion(version);
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
        return database;
    }

    public synchronized SQLiteDatabase openReadable() throws DatabaseAccessException {
        return withSpecificTable ? internalOpenReadableWithSpecificTable() : internalOpenReadable();
    }

    private SQLiteDatabase internalOpenReadable() throws DatabaseAccessException {
        if (database != null) {
            if (!database.isOpen()) {
                database = null;
            } else {
                return database;
            }
        }
        database = SQLiteDatabase.openOrCreateDatabase(path, null);
        int v = database.getVersion();
        boolean shouldUpgrade = false;
        if (v != version) {
            shouldUpgrade = true;
        }
        if (shouldUpgrade) {
            database.beginTransaction();
            try {
                if (handler != null) {
                    handler.onUpgrade(database);
                }
                database.setVersion(version);
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
        return database;
    }

    private SQLiteDatabase internalOpenReadableWithSpecificTable() throws DatabaseAccessException {
        if (database != null) {
            if (!database.isOpen()) {
                database = null;
            } else {
                return database;
            }
        }
        database = SQLiteDatabase.openOrCreateDatabase(path, null);
        int v = database.getVersion();
        boolean shouldUpgrade = false;
        if (v != version || !tableExists(tableName, database)) {
            shouldUpgrade = true;
        }
        if (shouldUpgrade) {
            database.beginTransaction();
            try {
                if (handler != null) {
                    handler.onUpgrade(database);
                }
                database.setVersion(version);
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
        return database;
    }

    public static interface DatabaseUpgradeHandler {

        void onUpgrade(final SQLiteDatabase database);

    }

    private boolean tableExists(final String tableName, final SQLiteDatabase database) {
        Cursor cursor = database.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

}
