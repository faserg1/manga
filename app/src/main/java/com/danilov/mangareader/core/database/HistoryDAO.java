package com.danilov.mangareader.core.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import com.danilov.mangareader.core.model.HistoryElement;
import com.danilov.mangareader.core.model.Manga;
import com.danilov.mangareader.core.util.ServiceContainer;

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
    private static final String TABLE_NAME = "historyManga";
    private static final String DB_NAME = "history.db";

    private static final String ID = "id";
    private static final String MANGA_ID = "manga_id";
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
    public HistoryElement getHistoryByManga(final Manga manga) throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openReadable();
        String selection = MANGA_ID + " = ?";
        String[] selectionArgs = new String[] {"" + manga.getId()};
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
        }
        return historyElement;
    }

    public List<HistoryElement> getMangaHistory() throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openReadable();
        MangaDAO mangaDAO = ServiceContainer.getService(MangaDAO.class);
        List<HistoryElement> mangaList = null;
        try {
            Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (!cursor.moveToFirst()) {
                return null;
            }
            mangaList = new ArrayList<HistoryElement>(cursor.getCount());
            int idIndex = cursor.getColumnIndex(ID);
            int localIdIndex = cursor.getColumnIndex(MANGA_ID);
            int chapterIndex = cursor.getColumnIndex(CHAPTER);
            int pageIndex = cursor.getColumnIndex(PAGE);
            do {
                int localId = cursor.getInt(localIdIndex);
                int id = cursor.getInt(idIndex);
                int chapter = cursor.getInt(chapterIndex);
                int page = cursor.getInt(pageIndex);
                Manga manga = mangaDAO.getById(localId);
                HistoryElement historyElement = new HistoryElement(manga, chapter, page);
                historyElement.setId(id);
                mangaList.add(historyElement);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            throw new DatabaseAccessException(e.getMessage());
        }
        return mangaList;
    }

    public void deleteManga(final Manga localManga) throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openWritable();
        String selection = MANGA_ID + " = ?";
        String[] selectionArgs = new String[] {"" + localManga.getId()};
        try {
            db.delete(TABLE_NAME, selection, selectionArgs);
        } catch (Exception e){
            throw new DatabaseAccessException(e.getMessage());
        }
    }

    public void addHistory(final Manga manga, final int chapter, final int page) throws DatabaseAccessException {

        MangaDAO mangaDAO = ServiceContainer.getService(MangaDAO.class);
        Manga _manga = mangaDAO.getByLinkAndRepository(manga.getUri(), manga.getRepository(), manga.isDownloaded());
        if (_manga == null) {
            mangaDAO.addManga(manga);
            _manga = mangaDAO.getByLinkAndRepository(manga.getUri(), manga.getRepository(), manga.isDownloaded());
        }
        SQLiteDatabase db = databaseHelper.openWritable();
        ContentValues cv = new ContentValues();
        cv.put(CHAPTER, chapter);
        cv.put(MANGA_ID, _manga.getId());
        cv.put(PAGE, page);
        try {
            db.insertOrThrow(TABLE_NAME, null, cv);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            throw new DatabaseAccessException(e.getMessage());
        }
    }

    public HistoryElement updateHistory(final Manga manga, final int chapter, final int page) throws DatabaseAccessException{
        HistoryElement historyElement = getHistoryByManga(manga);
        if (historyElement != null) {
            SQLiteDatabase db = databaseHelper.openWritable();
            try {
                ContentValues cv = new ContentValues();
                cv.put(PAGE, page);
                cv.put(CHAPTER, chapter);
                String selection = ID + " = ?";
                String id = String.valueOf(historyElement.getId());
                db.update(TABLE_NAME, cv, selection, new String[] {id});
                historyElement.setChapter(chapter);
                historyElement.setPage(page);
            } catch (Exception e) {
                throw new DatabaseAccessException(e.getMessage());
            }
            return historyElement;
        } else {
            addHistory(manga, chapter, page);
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
            builder.addColumn(PAGE, DatabaseOptions.Type.INT, false, false);
            builder.addColumn(CHAPTER, DatabaseOptions.Type.INT, false, false);
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
