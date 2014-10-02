package com.danilov.manga.core.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import com.danilov.manga.core.model.HistoryElement;
import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.util.ServiceContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon Danilov on 02.10.2014.
 */
public class HistoryDAO {

    private final static String TAG = "HistoryDAO";
    private static final String packageName = "com.danilov.manga";

    private static final int DAOVersion = 1;
    private static final String TABLE_NAME = "downloadedManga";
    private static final String DB_NAME = "history.db";

    private static final String ID = "id";
    private static final String LOCAL_MANGA_ID = "local_manga_id";
    private static final String CHAPTER = "chapter";
    private static final String PAGE = "page";

    public DatabaseHelper databaseHelper = null;

    public HistoryDAO() {
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
        databaseHelper = new DatabaseHelper(dbPath, DAOVersion, new UpgradeHandler());
    }

    /**
     * For use in MangaViewerActivity
     * @param manga
     * @return
     * @throws DatabaseAccessException
     */
    public HistoryElement getLocalHistoryByManga(final LocalManga manga) throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openReadable();
        String selection = LOCAL_MANGA_ID + " = ?";
        String[] selectionArgs = new String[] {"" + manga.getLocalId()};
        HistoryElement historyElement = null;
        try {
            Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
            if (!cursor.moveToFirst()) {
                return null;
            }
            int idIndex = cursor.getColumnIndex(ID);
            int chapterIndex = cursor.getColumnIndex(CHAPTER);
            int pageIndex = cursor.getColumnIndex(PAGE);
            int id = cursor.getInt(idIndex);
            int chapter = cursor.getInt(chapterIndex);
            int page = cursor.getInt(pageIndex);
            historyElement = new HistoryElement(manga, chapter, page);
            historyElement.setId(id);
        } catch (Exception e) {
            throw new DatabaseAccessException(e.getMessage());
        } finally {
            db.close();
        }
        return historyElement;
    }

    public List<HistoryElement> getAllLocalMangaHistory() throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openReadable();
        DownloadedMangaDAO downloadedMangaDAO = ServiceContainer.getService(DownloadedMangaDAO.class);
        List<HistoryElement> mangaList = null;
        try {
            Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (!cursor.moveToFirst()) {
                return null;
            }
            mangaList = new ArrayList<HistoryElement>(cursor.getCount());
            int idIndex = cursor.getColumnIndex(ID);
            int localIdIndex = cursor.getColumnIndex(LOCAL_MANGA_ID);
            int chapterIndex = cursor.getColumnIndex(CHAPTER);
            int pageIndex = cursor.getColumnIndex(PAGE);
            do {
                int localId = cursor.getInt(localIdIndex);
                int id = cursor.getInt(idIndex);
                int chapter = cursor.getInt(chapterIndex);
                int page = cursor.getInt(pageIndex);
                LocalManga manga = downloadedMangaDAO.getById(localId);
                HistoryElement historyElement = new HistoryElement(manga, chapter, page);
                historyElement.setId(id);
                mangaList.add(historyElement);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            throw new DatabaseAccessException(e.getMessage());
        } finally {
            db.close();
        }
        return mangaList;
    }

    private static class UpgradeHandler implements DatabaseHelper.DatabaseUpgradeHandler {

        @Override
        public void onUpgrade(final SQLiteDatabase database) {
            DatabaseOptions.Builder builder = new DatabaseOptions.Builder();
            builder.setName(TABLE_NAME);
            builder.addColumn(ID, DatabaseOptions.Type.INT, true, true);
            builder.addColumn(LOCAL_MANGA_ID, DatabaseOptions.Type.INT, false, false);
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
