package com.danilov.supermanga.core.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.danilov.supermanga.core.application.ApplicationSettings;
import com.danilov.supermanga.core.model.HistoryElement;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.util.Logger;
import com.danilov.supermanga.core.util.ServiceContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Semyon Danilov on 02.10.2014.
 */
public class HistoryDAO {

    private static final Logger LOGGER = new Logger(HistoryDAO.class);

    private final static String TAG = "HistoryDAO";
    private static final String packageName = ApplicationSettings.PACKAGE_NAME;

    private static final int DAOVersion = 1;
    private static final String TABLE_NAME = "historyManga";
    private static final String DB_NAME = "history.db";

    private static final String ID = "id";
    private static final String MANGA_ID = "manga_id";
    private static final String CHAPTER = "chapter";
    private static final String PAGE = "page";
    private static final String DATE = "date";
    private static final String IS_ONLINE = "is_online";

    public DatabaseHelper databaseHelper = null;

    private final Executor executor = Executors.newSingleThreadExecutor();

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
    public HistoryElement getHistoryByManga(@NonNull final Manga manga, final boolean isOnline) throws DatabaseAccessException {
        Database db = databaseHelper.openWritable();
        String selection = MANGA_ID + " = ? AND " + IS_ONLINE + " = ?";
        String[] selectionArgs = new String[] {"" + manga.getId(), isOnline ? "1" : "0"};
        HistoryElement historyElement = null;
        try {
            Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
            if (!cursor.moveToFirst()) {
                return null;
            }
            int idIndex = cursor.getColumnIndex(ID);
            int chapterIndex = cursor.getColumnIndex(CHAPTER);
            int pageIndex = cursor.getColumnIndex(PAGE);
            int dateIndex = cursor.getColumnIndex(DATE);
            int id = cursor.getInt(idIndex);
            int chapter = cursor.getInt(chapterIndex);
            int page = cursor.getInt(pageIndex);
            long dateMillis = cursor.getLong(dateIndex);
            Date date = new Date(dateMillis);
            historyElement = new HistoryElement(manga, isOnline, chapter, page);
            historyElement.setId(id);
            historyElement.setDate(date);
        } catch (Exception e) {
            throw new DatabaseAccessException(e.getMessage());
        } finally {
            db.close();
        }
        return historyElement;
    }

    @NonNull
    public List<HistoryElement> getMangaHistory() throws DatabaseAccessException {
        Database db = databaseHelper.openWritable();
        MangaDAO mangaDAO = ServiceContainer.getService(MangaDAO.class);
        List<HistoryElement> mangaList = Collections.emptyList();

        final List<Integer> scheduledForDeletion = new LinkedList<>();

        try {
            Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (!cursor.moveToFirst()) {
                return mangaList;
            }
            mangaList = new ArrayList<>(cursor.getCount());
            int idIndex = cursor.getColumnIndex(ID);
            int localIdIndex = cursor.getColumnIndex(MANGA_ID);
            int chapterIndex = cursor.getColumnIndex(CHAPTER);
            int pageIndex = cursor.getColumnIndex(PAGE);
            int dateIndex = cursor.getColumnIndex(DATE);
            int isOnlineIndex = cursor.getColumnIndex(IS_ONLINE);

            do {
                int localId = cursor.getInt(localIdIndex);
                int id = cursor.getInt(idIndex);
                int chapter = cursor.getInt(chapterIndex);
                int page = cursor.getInt(pageIndex);
                long dateMillis = cursor.getLong(dateIndex);
                Manga manga = mangaDAO.getById(localId);

                if (manga == null) {
                    //нет манги, надо удалить историю
                    scheduledForDeletion.add(id);
                    continue;
                }

                boolean isOnline = cursor.getInt(isOnlineIndex) == 1;
                if (!isOnline && !manga.isDownloaded()) {
                    //это история для оффлайн, а её нет
                    scheduledForDeletion.add(id);
                    continue;
                }
                HistoryElement historyElement = new HistoryElement(manga, isOnline, chapter, page);
                Date date = new Date(dateMillis);
                historyElement.setId(id);
                historyElement.setDate(date);
                mangaList.add(historyElement);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            throw new DatabaseAccessException(e.getMessage());
        } finally {
            db.close();
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                for (Integer id : scheduledForDeletion) {
                    try {
                        deleteHistoryById(id);
                    } catch (DatabaseAccessException e) {
                        LOGGER.e("Failed to delete manga history: " + e.getMessage(), e); //shit shit shit
                    }
                }
            }
        });
        return mangaList;
    }

    /**
     * helper метод для удаления истории по ID, если вдруг осталась история без манги
     * @param id
     */
    public void deleteHistoryById(final int id) throws DatabaseAccessException {
        Database db = databaseHelper.openWritable();
        String selection = ID + " = ?";
        String[] selectionArgs = new String[] {"" + id};
        try {
            db.delete(TABLE_NAME, selection, selectionArgs);
        } catch (Exception e){
            throw new DatabaseAccessException(e.getMessage());
        } finally {
            db.close();
        }
    }

    public void deleteManga(final Manga manga, final boolean isOnline) throws DatabaseAccessException {
        Database db = databaseHelper.openWritable();
        String selection = MANGA_ID + " = ? AND " + IS_ONLINE + " = ?";
        String[] selectionArgs = new String[] {"" + manga.getId(), isOnline ? "1" : "0"};
        try {
            db.delete(TABLE_NAME, selection, selectionArgs);
        } catch (Exception e){
            throw new DatabaseAccessException(e.getMessage());
        } finally {
            db.close();
        }
    }

    public void addHistory(final Manga manga, final boolean isOnline, final int chapter, final int page) throws DatabaseAccessException {

        MangaDAO mangaDAO = ServiceContainer.getService(MangaDAO.class);
        Manga _manga = mangaDAO.getByLinkAndRepository(manga.getUri(), manga.getRepository());
        if (_manga == null) {
            mangaDAO.addManga(manga);
            _manga = mangaDAO.getByLinkAndRepository(manga.getUri(), manga.getRepository());
        }
        Database db = databaseHelper.openWritable();
        ContentValues cv = new ContentValues();
        cv.put(CHAPTER, chapter);
        cv.put(MANGA_ID, _manga.getId());
        cv.put(PAGE, page);
        cv.put(IS_ONLINE, isOnline ? 1 : 0);
        cv.put(DATE, new Date().getTime());
        try {
            db.insertOrThrow(TABLE_NAME, null, cv);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            throw new DatabaseAccessException(e.getMessage());
        } finally {
            db.close();
        }
    }

    public HistoryElement updateHistory(final Manga manga, final boolean isOnline, final int chapter, final int page) throws DatabaseAccessException{
        HistoryElement historyElement = getHistoryByManga(manga, isOnline);
        if (historyElement != null) {
            Database db = databaseHelper.openWritable();
            try {
                ContentValues cv = new ContentValues();
                cv.put(PAGE, page);
                cv.put(CHAPTER, chapter);
                cv.put(IS_ONLINE, isOnline ? 1 : 0);
                cv.put(DATE, new Date().getTime());
                String selection = ID + " = ?";
                String id = String.valueOf(historyElement.getId());
                db.update(TABLE_NAME, cv, selection, new String[] {id});
                historyElement.setChapter(chapter);
                historyElement.setPage(page);
            } catch (Exception e) {
                throw new DatabaseAccessException(e.getMessage());
            } finally {
                db.close();
            }
            return historyElement;
        } else {
            addHistory(manga, isOnline, chapter, page);
        }
        return null;
    }

    private static class UpgradeHandler implements DatabaseHelper.DatabaseUpgradeHandler {

        @Override
        public void onUpgrade(final Database database, final int currentVersion) {
            DatabaseOptions.Builder builder = new DatabaseOptions.Builder();
            builder.setName(TABLE_NAME);
            builder.addColumn(ID, DatabaseOptions.Type.INT, true, true);
            builder.addColumn(MANGA_ID, DatabaseOptions.Type.INT, false, false);
            builder.addColumn(PAGE, DatabaseOptions.Type.INT, false, false);
            builder.addColumn(CHAPTER, DatabaseOptions.Type.INT, false, false);
            builder.addColumn(DATE, DatabaseOptions.Type.INT, false, false);
            builder.addColumn(IS_ONLINE, DatabaseOptions.Type.INT, false, false);
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
