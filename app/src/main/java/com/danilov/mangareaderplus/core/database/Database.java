package com.danilov.mangareaderplus.core.database;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteTransactionListener;
import android.os.Build;
import android.os.CancellationSignal;
import android.util.Pair;

import com.danilov.mangareaderplus.core.util.Logger;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Semyon on 09.08.2015.
 */
public class Database {

    private static final Logger LOGGER = new Logger(Database.class);

    private final SQLiteDatabase database;


    private Database(final SQLiteDatabase database) {
        this.database = database;
        LOGGER.d("Opening database");
    }

    public Cursor rawQuery(final String sql, final String[] selectionArgs) {
        return database.rawQuery(sql, selectionArgs);
    }

    public synchronized void close() {
        openCount--;
        LOGGER.d("Close issued, remaining: " + openCount);
        if (openCount == 0) {
            LOGGER.d("Closing db");
            database.close();
        }
    }

    public boolean isReadOnly() {
        return database.isReadOnly();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setMaxSqlCacheSize(final int cacheSize) {
        database.setMaxSqlCacheSize(cacheSize);
    }

    public Cursor rawQueryWithFactory(final SQLiteDatabase.CursorFactory cursorFactory, final String sql, final String[] selectionArgs, final String editTable) {
        return database.rawQueryWithFactory(cursorFactory, sql, selectionArgs, editTable);
    }

    public long insertWithOnConflict(final String table, final String nullColumnHack, final ContentValues initialValues, final int conflictAlgorithm) {
        return database.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm);
    }

    @Deprecated
    public void markTableSyncable(final String table, final String foreignKey, final String updateTable) {
        database.markTableSyncable(table, foreignKey, updateTable);
    }

    public SQLiteStatement compileStatement(final String sql) throws SQLException {
        return database.compileStatement(sql);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor rawQueryWithFactory(final SQLiteDatabase.CursorFactory cursorFactory, final String sql, final String[] selectionArgs, final String editTable, final CancellationSignal cancellationSignal) {
        return database.rawQueryWithFactory(cursorFactory, sql, selectionArgs, editTable, cancellationSignal);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void beginTransactionNonExclusive() {
        database.beginTransactionNonExclusive();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setForeignKeyConstraintsEnabled(final boolean enable) {
        database.setForeignKeyConstraintsEnabled(enable);
    }

    @Deprecated
    public void setLockingEnabled(final boolean lockingEnabled) {
        database.setLockingEnabled(lockingEnabled);
    }

    public void setVersion(final int version) {
        database.setVersion(version);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void disableWriteAheadLogging() {
        database.disableWriteAheadLogging();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean enableWriteAheadLogging() {
        return database.enableWriteAheadLogging();
    }

    public void endTransaction() {
        database.endTransaction();
    }

    public boolean yieldIfContendedSafely(final long sleepAfterYieldDelay) {
        return database.yieldIfContendedSafely(sleepAfterYieldDelay);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean isDatabaseIntegrityOk() {
        return database.isDatabaseIntegrityOk();
    }

    public boolean yieldIfContendedSafely() {
        return database.yieldIfContendedSafely();
    }

    public boolean needUpgrade(final int newVersion) {
        return database.needUpgrade(newVersion);
    }

    public int updateWithOnConflict(final String table, final ContentValues values, final String whereClause, final String[] whereArgs, final int conflictAlgorithm) {
        return database.updateWithOnConflict(table, values, whereClause, whereArgs, conflictAlgorithm);
    }

    @Deprecated
    public boolean isDbLockedByOtherThreads() {
        return database.isDbLockedByOtherThreads();
    }

    @Deprecated
    public void releaseReferenceFromContainer() {
        database.releaseReferenceFromContainer();
    }

    public int delete(final String table, final String whereClause, final String[] whereArgs) {
        return database.delete(table, whereClause, whereArgs);
    }

    public void setLocale(final Locale locale) {
        database.setLocale(locale);
    }

    public void releaseReference() {
        database.releaseReference();
    }

    public int getVersion() {
        return database.getVersion();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor query(final boolean distinct, final String table, final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit, final CancellationSignal cancellationSignal) {
        return database.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, cancellationSignal);
    }

    public boolean isDbLockedByCurrentThread() {
        return database.isDbLockedByCurrentThread();
    }

    public long replaceOrThrow(final String table, final String nullColumnHack, final ContentValues initialValues) throws SQLException {
        return database.replaceOrThrow(table, nullColumnHack, initialValues);
    }

    public Cursor query(final String table, final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public long getMaximumSize() {
        return database.getMaximumSize();
    }

    public Cursor queryWithFactory(final SQLiteDatabase.CursorFactory cursorFactory, final boolean distinct, final String table, final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return database.queryWithFactory(cursorFactory, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public int update(final String table, final ContentValues values, final String whereClause, final String[] whereArgs) {
        return database.update(table, values, whereClause, whereArgs);
    }

    public void beginTransactionWithListener(final SQLiteTransactionListener transactionListener) {
        database.beginTransactionWithListener(transactionListener);
    }

    public Cursor query(final String table, final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy) {
        return database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    public void execSQL(final String sql) throws SQLException {
        database.execSQL(sql);
    }

    public void acquireReference() {
        database.acquireReference();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public List<Pair<String, String>> getAttachedDbs() {
        return database.getAttachedDbs();
    }

    public void execSQL(final String sql, final Object[] bindArgs) throws SQLException {
        database.execSQL(sql, bindArgs);
    }

    @Deprecated
    public boolean yieldIfContended() {
        return database.yieldIfContended();
    }

    public Cursor query(final boolean distinct, final String table, final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return database.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean isWriteAheadLoggingEnabled() {
        return database.isWriteAheadLoggingEnabled();
    }

    public void setTransactionSuccessful() {
        database.setTransactionSuccessful();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor queryWithFactory(final SQLiteDatabase.CursorFactory cursorFactory, final boolean distinct, final String table, final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit, final CancellationSignal cancellationSignal) {
        return database.queryWithFactory(cursorFactory, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, cancellationSignal);
    }

    public void beginTransaction() {
        database.beginTransaction();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void beginTransactionWithListenerNonExclusive(final SQLiteTransactionListener transactionListener) {
        database.beginTransactionWithListenerNonExclusive(transactionListener);
    }

    public void setPageSize(final long numBytes) {
        database.setPageSize(numBytes);
    }

    public long insertOrThrow(final String table, final String nullColumnHack, final ContentValues values) throws SQLException {
        return database.insertOrThrow(table, nullColumnHack, values);
    }

    @Deprecated
    public Map<String, String> getSyncedTables() {
        return database.getSyncedTables();
    }

    public boolean inTransaction() {
        return database.inTransaction();
    }

    public boolean isOpen() {
        return database.isOpen();
    }

    public long replace(final String table, final String nullColumnHack, final ContentValues initialValues) {
        return database.replace(table, nullColumnHack, initialValues);
    }

    public long getPageSize() {
        return database.getPageSize();
    }

    @Deprecated
    public void markTableSyncable(final String table, final String deletedTable) {
        database.markTableSyncable(table, deletedTable);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Cursor rawQuery(final String sql, final String[] selectionArgs, final CancellationSignal cancellationSignal) {
        return database.rawQuery(sql, selectionArgs, cancellationSignal);
    }

    public long setMaximumSize(final long numBytes) {
        return database.setMaximumSize(numBytes);
    }

    public long insert(final String table, final String nullColumnHack, final ContentValues values) {
        return database.insert(table, nullColumnHack, values);
    }

    public String getPath() {
        return database.getPath();
    }



    private int openCount = 0;

    public synchronized void incOpen() {
        openCount++;
        LOGGER.d("Another one opening DB");
    }


    //static

    public static int releaseMemory() {
        return SQLiteDatabase.releaseMemory();
    }

    public static String findEditTable(final String tables) {
        return SQLiteDatabase.findEditTable(tables);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean deleteDatabase(final File file) {
        return SQLiteDatabase.deleteDatabase(file);
    }

    public static Database create(final SQLiteDatabase.CursorFactory factory) {
        Database database = new Database(SQLiteDatabase.create(factory));
        return database;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Database openOrCreateDatabase(final String path, final SQLiteDatabase.CursorFactory factory, final DatabaseErrorHandler errorHandler) {
        Database database = new Database(SQLiteDatabase.openOrCreateDatabase(path, factory, errorHandler));
        return database;
    }

    public static Database openDatabase(final String path, final SQLiteDatabase.CursorFactory factory, final int flags) {
        Database database = new Database(SQLiteDatabase.openDatabase(path, factory, flags));
        return database;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Database openDatabase(final String path, final SQLiteDatabase.CursorFactory factory, final int flags, final DatabaseErrorHandler errorHandler) {
        Database database = new Database(SQLiteDatabase.openDatabase(path, factory, flags, errorHandler));
        return database;
    }

    public static Database openOrCreateDatabase(final String path, final SQLiteDatabase.CursorFactory factory) {
        Database database = new Database(SQLiteDatabase.openOrCreateDatabase(path, factory));
        return database;
    }

    public static Database openOrCreateDatabase(final File file, final SQLiteDatabase.CursorFactory factory) {
        Database database = new Database(SQLiteDatabase.openOrCreateDatabase(file, factory));
        return database;
    }

}
