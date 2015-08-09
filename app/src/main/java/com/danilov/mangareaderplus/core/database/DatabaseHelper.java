package com.danilov.mangareaderplus.core.database;

import android.database.Cursor;

/**
 * Created by Semyon Danilov on 04.07.2014.
 */
public class DatabaseHelper {

    private String path;
    private Database database;
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

    public synchronized Database openWritable() throws DatabaseAccessException {
        Database database = withSpecificTable ? internalOpenWritableWithSpecificTable() : internalOpenWritable();
        database.incOpen();
        return database;
    }

    private Database internalOpenWritableWithSpecificTable() throws DatabaseAccessException {
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
        tryCreateDatabase();
        database = Database.openOrCreateDatabase(path, null);
        int v = database.getVersion();
        boolean shouldUpgrade = false;
        if (v != version || !tableExists(tableName, database)) {
            shouldUpgrade = true;
        }
        if (shouldUpgrade) {
            database.beginTransaction();
            try {
                if (handler != null) {
                    handler.onUpgrade(database, v);
                }
                database.setVersion(version);
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
        return database;
    }

    private Database internalOpenWritable() throws DatabaseAccessException {
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
        tryCreateDatabase();
        database = Database.openOrCreateDatabase(path, null);
        int v = database.getVersion();
        boolean shouldUpgrade = false;
        if (v != version) {
            shouldUpgrade = true;
        }
        if (shouldUpgrade) {
            database.beginTransaction();
            try {
                if (handler != null) {
                    handler.onUpgrade(database, v);
                }
                database.setVersion(version);
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
        return database;
    }

//    public synchronized Database openReadable() throws DatabaseAccessException {
//        return withSpecificTable ? internalOpenReadableWithSpecificTable() : internalOpenReadable();
//    }
//
//    private Database internalOpenReadable() throws DatabaseAccessException {
//        if (database != null) {
//            if (!database.isOpen()) {
//                database = null;
//            } else {
//                return database;
//            }
//        }
//        tryCreateDatabase();
//        database = Database.openOrCreateDatabase(path, null);
//        int v = database.getVersion();
//        boolean shouldUpgrade = false;
//        if (v != version) {
//            shouldUpgrade = true;
//        }
//        if (shouldUpgrade) {
//            database.beginTransaction();
//            try {
//                if (handler != null) {
//                    handler.onUpgrade(database, v);
//                }
//                database.setVersion(version);
//                database.setTransactionSuccessful();
//            } finally {
//                database.endTransaction();
//            }
//        }
//        return database;
//    }
//
//    private Database internalOpenReadableWithSpecificTable() throws DatabaseAccessException {
//        if (database != null) {
//            if (!database.isOpen()) {
//                database = null;
//            } else {
//                return database;
//            }
//        }
//        tryCreateDatabase();
//        database = Database.openOrCreateDatabase(path, null);
//        int v = database.getVersion();
//        boolean shouldUpgrade = false;
//        if (v != version || !tableExists(tableName, database)) {
//            shouldUpgrade = true;
//        }
//        if (shouldUpgrade) {
//            database.beginTransaction();
//            try {
//                if (handler != null) {
//                    handler.onUpgrade(database, v);
//                }
//                database.setVersion(version);
//                database.setTransactionSuccessful();
//            } finally {
//                database.endTransaction();
//            }
//        }
//        return database;
//    }

    public void tryCreateDatabase() throws DatabaseAccessException {
        //TODO: implementation
//        File f = new File(path);
//        if (!f.exists()) {
//            File parent = new File(f.getParent() + File.separator);
//            if (!parent.exists()) {
//                boolean result = parent.mkdirs();
//                if (!result) {
//                    throw new DatabaseAccessException("Can't create database folder");
//                }
//            }
//            try {
//                boolean result = f.createNewFile();
//                if (!result) {
//                    throw new DatabaseAccessException("Can't create database file");
//                }
//            } catch (IOException e) {
//                throw new DatabaseAccessException("Can't create database: " + e.getMessage());
//            }
//        }
    }

    public static interface DatabaseUpgradeHandler {

        void onUpgrade(final Database database, final int currentVersion);

    }

    private boolean tableExists(final String tableName, final Database database) {
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
