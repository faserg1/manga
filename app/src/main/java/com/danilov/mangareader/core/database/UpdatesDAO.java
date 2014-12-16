package com.danilov.mangareader.core.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.danilov.mangareader.core.model.Manga;
import com.danilov.mangareader.core.model.UpdatesElement;
import com.danilov.mangareader.core.util.ServiceContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Semyon on 23.11.2014.
 */
public class UpdatesDAO {

    private final static String TAG = "UpdatesDAO";
    private static final String packageName = "com.danilov.manga";

    private static final int DAOVersion = 1;
    private static final String TABLE_NAME = "updatesManga";
    private static final String DB_NAME = "manga.db";
    private static final String ID = "id";
    private static final String MANGA_ID = "manga_id";
    private static final String TIMESTAMP = "timestamp";
    private static final String DIFFERENCE = "difference";

    public DatabaseHelper databaseHelper = null;

    public UpdatesDAO() {
        File externalStorageDir = Environment.getExternalStorageDirectory();
        //{SD_PATH}/Android/data/com.danilov.manga/db/
        File dbPathFile = new File(externalStorageDir, "Android" + File.separator + "data" + File.separator + packageName + File.separator + "db");
        if (!dbPathFile.exists()) {
            boolean created = dbPathFile.mkdirs();
            if (!created) {
                Log.e(TAG, "Can't create file");
            }
        }
        String dbPath = dbPathFile + "/" + DB_NAME;
        databaseHelper = new DatabaseHelper(dbPath, DAOVersion, new UpgradeHandler(), true, TABLE_NAME);
    }

    /**
     * @param manga
     * @return
     * @throws DatabaseAccessException
     */
    public UpdatesElement getUpdatesByManga(final Manga manga) throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openReadable();
        String selection = MANGA_ID + " = ?";
        String[] selectionArgs = new String[] {"" + manga.getId()};
        UpdatesElement element = null;
        try {
            Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
            if (!cursor.moveToFirst()) {
                return null;
            }
            int idIndex = cursor.getColumnIndex(ID);
            int differenceIndex = cursor.getColumnIndex(DIFFERENCE);
            int timestampIndex = cursor.getColumnIndex(TIMESTAMP);

            int id = cursor.getInt(idIndex);
            long timestamp = cursor.getInt(timestampIndex);
            int difference = cursor.getInt(differenceIndex);
            element = new UpdatesElement();
            element.setId(id);
            element.setDifference(difference);
            element.setManga(manga);
            element.setTimestamp(new Date(timestamp));
        } catch (Exception e) {
            throw new DatabaseAccessException(e.getMessage());
        }
        return element;
    }

    public int getUpdatesQuantity() throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openReadable();
        try {
            Cursor cursor = db.rawQuery("select count(*) from " + TABLE_NAME, null);
            if (!cursor.moveToFirst()) {
                return 0;
            }
            return cursor.getInt(0);
        } catch (Exception e) {
            throw new DatabaseAccessException(e.getMessage());
        }
    }

    public List<UpdatesElement> getAllUpdates() throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openReadable();
        MangaDAO mangaDAO = ServiceContainer.getService(MangaDAO.class);
        List<UpdatesElement> mangaList = null;
        try {
            Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (!cursor.moveToFirst()) {
                return null;
            }
            mangaList = new ArrayList<UpdatesElement>(cursor.getCount());
            int idIndex = cursor.getColumnIndex(ID);
            int mangaIdIndex = cursor.getColumnIndex(MANGA_ID);
            int differenceIndex = cursor.getColumnIndex(DIFFERENCE);
            int timestampIndex = cursor.getColumnIndex(TIMESTAMP);
            do {
                int mangaId = cursor.getInt(mangaIdIndex);
                int id = cursor.getInt(idIndex);
                long timestamp = cursor.getInt(timestampIndex);
                int difference = cursor.getInt(differenceIndex);
                Manga manga = mangaDAO.getById(mangaId);
                UpdatesElement element = new UpdatesElement();
                element.setId(id);
                element.setDifference(difference);
                element.setManga(manga);
                element.setTimestamp(new Date(timestamp));
                mangaList.add(element);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            throw new DatabaseAccessException(e.getMessage());
        }
        return mangaList;
    }

    public void deleteByManga(final Manga manga) throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openWritable();
        String selection = MANGA_ID + " = ?";
        String[] selectionArgs = new String[] {"" + manga.getId()};
        try {
            db.delete(TABLE_NAME, selection, selectionArgs);
        } catch (Exception e){
            throw new DatabaseAccessException(e.getMessage());
        }
    }

    public void delete(final UpdatesElement updatesElement) throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openWritable();
        String selection = ID + " = ?";
        String[] selectionArgs = new String[] {"" + updatesElement.getId()};
        try {
            db.delete(TABLE_NAME, selection, selectionArgs);
        } catch (Exception e){
            throw new DatabaseAccessException(e.getMessage());
        }
    }

    public void addUpdatesElement(final Manga manga, final int difference, final Date timestamp) throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openWritable();
        ContentValues cv = new ContentValues();
        cv.put(TIMESTAMP, timestamp.getTime());
        cv.put(MANGA_ID, manga.getId());
        cv.put(DIFFERENCE, difference);
        try {
            db.insertOrThrow(TABLE_NAME, null, cv);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            throw new DatabaseAccessException(e.getMessage());
        }
    }

    public UpdatesElement updateInfo(final Manga manga, final int difference, final Date timestamp) throws DatabaseAccessException{
        UpdatesElement updatesElement = getUpdatesByManga(manga);
        if (updatesElement != null) {
            SQLiteDatabase db = databaseHelper.openWritable();
            try {
                ContentValues cv = new ContentValues();
                cv.put(DIFFERENCE, updatesElement.getDifference() + difference);
                cv.put(TIMESTAMP, timestamp.getTime());
                String selection = ID + " = ?";
                String id = String.valueOf(updatesElement.getId());
                db.update(TABLE_NAME, cv, selection, new String[] {id});
                updatesElement.setTimestamp(timestamp);
                updatesElement.setDifference(updatesElement.getDifference() + difference);
            } catch (Exception e) {
                throw new DatabaseAccessException(e.getMessage());
            }
            return updatesElement;
        } else {
            addUpdatesElement(manga, difference, timestamp);
        }
        return null;
    }

    private static class UpgradeHandler implements DatabaseHelper.DatabaseUpgradeHandler {

        @Override
        public void onUpgrade(final SQLiteDatabase database) {
            DatabaseOptions.Builder builder = new DatabaseOptions.Builder();
            builder.setName(TABLE_NAME);
            builder.addColumn(ID, DatabaseOptions.Type.INT, true, true);
            builder.addColumn(MANGA_ID, DatabaseOptions.Type.INT, false, false);
            builder.addColumn(TIMESTAMP, DatabaseOptions.Type.INT, false, false);
            builder.addColumn(DIFFERENCE, DatabaseOptions.Type.INT, false, false);
            DatabaseOptions options = builder.build();
            String sqlStatement = options.toSQLStatement();
            try {
                database.execSQL("drop table if exists " + TABLE_NAME + ";");
                database.execSQL(sqlStatement);
            } catch (Exception e) {
                Log.e(TAG, "UpgradeHandler onUpgrade failed: " + e.getMessage());
            }
        }

    }

}