package com.danilov.supermanga.core.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import com.danilov.supermanga.core.application.ApplicationSettings;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.UpdatesElement;
import com.danilov.supermanga.core.util.ServiceContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Semyon on 23.11.2014.
 */
public class UpdatesDAO {

    private final static String TAG = "UpdatesDAO";
    private static final String packageName = ApplicationSettings.PACKAGE_NAME;

    private static final int DAOVersion = 3;
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
        databaseHelper = new DatabaseHelper(dbPath, DAOVersion, new MangaDAO.SharedUpgradeHandler(), true, TABLE_NAME);
    }

    /**
     * @param manga
     * @return
     * @throws DatabaseAccessException
     */
    public UpdatesElement getUpdatesByManga(final Manga manga) throws DatabaseAccessException {
        Database db = databaseHelper.openWritable();
        String selection = MANGA_ID + " = ?";
        String[] selectionArgs = new String[] {"" + manga.getId()};
        UpdatesElement element = null;
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
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
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
        }
        return element;
    }

    public int getUpdatesQuantity() throws DatabaseAccessException {
        Database db = databaseHelper.openWritable();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select count(*) from " + TABLE_NAME, null);
            if (!cursor.moveToFirst()) {
                return 0;
            }
            return cursor.getInt(0);
        } catch (Exception e) {
            throw new DatabaseAccessException(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
        }
    }

    public List<UpdatesElement> getAllUpdates() throws DatabaseAccessException {
        Database db = databaseHelper.openWritable();
        MangaDAO mangaDAO = ServiceContainer.getService(MangaDAO.class);
        List<UpdatesElement> mangaList = null;
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
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
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
        }
        return mangaList;
    }

    public void deleteByManga(final Manga manga) throws DatabaseAccessException {
        Database db = databaseHelper.openWritable();
        String selection = MANGA_ID + " = ?";
        String[] selectionArgs = new String[] {"" + manga.getId()};
        try {
            db.delete(TABLE_NAME, selection, selectionArgs);
        } catch (Exception e){
            throw new DatabaseAccessException(e.getMessage());
        } finally {
            db.close();
        }
    }

    public void delete(final UpdatesElement updatesElement) throws DatabaseAccessException {
        Database db = databaseHelper.openWritable();
        String selection = ID + " = ?";
        String[] selectionArgs = new String[] {"" + updatesElement.getId()};
        try {
            db.delete(TABLE_NAME, selection, selectionArgs);
        } catch (Exception e){
            throw new DatabaseAccessException(e.getMessage());
        } finally {
            db.close();
        }
    }

    public long addUpdatesElement(final Manga manga, final int difference, final Date timestamp) throws DatabaseAccessException {
        Database db = databaseHelper.openWritable();
        ContentValues cv = new ContentValues();
        cv.put(TIMESTAMP, timestamp.getTime());
        cv.put(MANGA_ID, manga.getId());
        cv.put(DIFFERENCE, difference);
        try {
            return db.insertOrThrow(TABLE_NAME, null, cv);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            throw new DatabaseAccessException(e.getMessage());
        } finally {
            db.close();
        }
    }

    public UpdatesElement updateInfo(final Manga manga, final int difference, final Date timestamp) throws DatabaseAccessException{
        UpdatesElement updatesElement = getUpdatesByManga(manga);
        if (updatesElement != null) {
            Database db = databaseHelper.openWritable();
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
            } finally {
                db.close();
            }
            return updatesElement;
        } else {
            long id = addUpdatesElement(manga, difference, timestamp);
            return new UpdatesElement((int) id, manga, timestamp, difference);
        }
    }

    public static class UpgradeHandler implements DatabaseHelper.DatabaseUpgradeHandler {

        @Override
        public void onUpgrade(final Database database, final int currentVersion) {
            final List<String> sqls = new ArrayList<>();
            switch (currentVersion) {
                case 0:
                    onNewDatabase(database);
                    break;
                case 1:
                case 2:
                default:
                    break;
            }
        }
        private void onNewDatabase(final Database database) {
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