package com.danilov.manga.core.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.repository.RepositoryEngine;
import com.danilov.manga.core.util.ServiceContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon on 29.11.2014.
 */
public class MangaDAO {

    private final static String TAG = "MangaDAO";
    private static final String packageName = "com.danilov.manga";

    private static final int DAOVersion = 1;
    private static final String TABLE_NAME = "manga";
    private static final String DB_NAME = "manga.db";

    //COMMON
    private static final String ID = "id";
    private static final String CHAPTERS_QUANTITY = "chapters_quantity";
    private static final String MANGA_TITLE = "manga_title";
    private static final String MANGA_DESCRIPTION = "manga_description";
    private static final String MANGA_REPOSITORY = "manga_repository";
    private static final String MANGA_AUTHOR = "manga_author";
    private static final String IS_FAVORITE = "is_favorite";
    private static final String MANGA_INET_URI = "manga_inet_uri";
    private static final String MANGA_COVER_URI = "manga_cover_uri";
    private static final String IS_DOWNLOADED = "is_downloaded";

    //LOCAL
    private static final String LOCAL_URI = "local_uri";

    public DatabaseHelper databaseHelper = null;

    public MangaDAO() {
        File externalStorageDir = Environment.getExternalStorageDirectory();
        //{SD_PATH}/Android/data/com.danilov.manga/download
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

    public synchronized void addManga(final Manga manga) throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openWritable();
        ContentValues cv = new ContentValues();

        cv.put(MANGA_TITLE, manga.getTitle());
        cv.put(MANGA_COVER_URI, manga.getCoverUri());
        cv.put(MANGA_DESCRIPTION, manga.getDescription());
        cv.put(MANGA_AUTHOR, manga.getAuthor());
        cv.put(MANGA_REPOSITORY, manga.getRepository().toString());
        cv.put(MANGA_INET_URI, manga.getUri());
        cv.put(CHAPTERS_QUANTITY, manga.getChaptersQuantity());
        cv.put(IS_FAVORITE, manga.isFavorite() ? 1 : 0);

        if (manga.isDownloaded()) {
            //its local manga
            LocalManga localManga = (LocalManga) manga;
            cv.put(LOCAL_URI, localManga.getLocalUri());
            cv.put(IS_DOWNLOADED, 1);
        } else {
            cv.put(IS_DOWNLOADED, 0);
        }

        try {
            db.insertOrThrow(TABLE_NAME, null, cv);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            throw new DatabaseAccessException(e.getMessage());
        }
    }

    public synchronized List<Manga> getAllManga() throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openReadable();
        List<Manga> mangaList = new ArrayList<Manga>();
        try {
            Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (!cursor.moveToFirst()) {
                return new ArrayList<Manga>(0);
            }
            do {
                Manga manga = null;
                manga = resolve(cursor);
                mangaList.add(manga);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            throw new DatabaseAccessException(e.getMessage());
        }
        return mangaList;
    }

    public synchronized Manga getByLinkAndRepository(final String inetUri, final RepositoryEngine.Repository repository, final boolean downloaded) throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openReadable();
        String selection = MANGA_INET_URI + " = ? AND " + MANGA_REPOSITORY + " = ? AND " + IS_DOWNLOADED + " = ?";
        String[] selectionArgs = new String[] {inetUri, repository.toString(), downloaded ? "1" : "0"};
        Manga manga = null;
        try {
            Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
            if (!cursor.moveToFirst()) {
                return null;
            }
            manga = resolve(cursor);
        } catch (Exception e) {
            throw new DatabaseAccessException(e.getMessage());
        }
        return manga;
    }

    public synchronized List<LocalManga> getAllDownloaded() throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openReadable();
        String selection = IS_DOWNLOADED + " = 1";
        List<LocalManga> mangaList = new ArrayList<LocalManga>();
        try {
            Cursor cursor = db.query(TABLE_NAME, null, selection, null, null, null, null);
            if (!cursor.moveToFirst()) {
                return new ArrayList<LocalManga>(0);
            }
            do {
                Manga manga = null;
                manga = resolve(cursor);
                mangaList.add((LocalManga) manga);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            throw new DatabaseAccessException(e.getMessage());
        }
        return mangaList;
    }

    public synchronized List<Manga> getFavorite() throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openReadable();
        String selection = IS_FAVORITE + " = 1";
        List<Manga> mangaList = new ArrayList<Manga>();
        try {
            Cursor cursor = db.query(TABLE_NAME, null, selection, null, null, null, null);
            if (!cursor.moveToFirst()) {
                return new ArrayList<Manga>(0);
            }
            do {
                Manga manga = null;
                manga = resolve(cursor);
                mangaList.add(manga);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            throw new DatabaseAccessException(e.getMessage());
        }
        return mangaList;
    }

    public synchronized Manga getById(final int id) throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openReadable();
        String selection = ID + " = ?";
        String[] selectionArgs = new String[] {"" + id};
        Manga manga = null;
        try {
            Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
            if (!cursor.moveToFirst()) {
                return null;
            }
            manga = resolve(cursor);
        } catch (Exception e) {
            throw new DatabaseAccessException(e.getMessage());
        }
        return manga;
    }

    public synchronized void deleteManga(final Manga manga) throws DatabaseAccessException {
        SQLiteDatabase db = databaseHelper.openWritable();
        String selection = ID + " = ?";
        String[] selectionArgs = new String[] {"" + manga.getId()};
        try {
            db.delete(TABLE_NAME, selection, selectionArgs);
        } catch (Exception e){
            throw new DatabaseAccessException(e.getMessage());
        }
    }

    public synchronized Manga updateInfo(final Manga manga, final int chapters, final boolean downloaded) throws DatabaseAccessException {
        Manga _manga = null;
        _manga = getByLinkAndRepository(manga.getUri(), manga.getRepository(), downloaded);
        if (_manga != null) {
            SQLiteDatabase db = databaseHelper.openWritable();
            try {
                ContentValues cv = new ContentValues();
                cv.put(CHAPTERS_QUANTITY, chapters);
                String selection = ID + " = ?";
                String id = String.valueOf(_manga.getId());
                db.update(TABLE_NAME, cv, selection, new String[] {id});
            } catch (Exception e) {
                throw new DatabaseAccessException(e.getMessage());
            }
            return _manga;
        } else {
            addManga(manga);
        }
        return null;
    }

    private Manga resolve(final Cursor cursor) {
        int idIndex = cursor.getColumnIndex(ID);
        int chaptersQuantityIndex = cursor.getColumnIndex(CHAPTERS_QUANTITY);
        int titleIndex = cursor.getColumnIndex(MANGA_TITLE);
        int descriptionIndex = cursor.getColumnIndex(MANGA_DESCRIPTION);
        int repositoryIndex = cursor.getColumnIndex(MANGA_REPOSITORY);
        int authorIndex = cursor.getColumnIndex(MANGA_AUTHOR);
        int isFavoriteIndex = cursor.getColumnIndex(IS_FAVORITE);
        int inetUriIndex = cursor.getColumnIndex(MANGA_INET_URI);
        int coverUriIndex = cursor.getColumnIndex(MANGA_COVER_URI);

        int isDownloadedIndex = cursor.getColumnIndex(IS_DOWNLOADED);
        int localUriIndex = cursor.getColumnIndex(LOCAL_URI);

        boolean isDownloaded = cursor.getInt(isDownloadedIndex) == 1;
        boolean isFavorite = cursor.getInt(isFavoriteIndex) == 1;

        int id = cursor.getInt(idIndex);
        String title = cursor.getString(titleIndex);
        String author = cursor.getString(authorIndex);
        String description = cursor.getString(descriptionIndex);
        RepositoryEngine.Repository repository = RepositoryEngine.Repository.valueOf(cursor.getString(repositoryIndex));
        String uri = cursor.getString(inetUriIndex);
        String coverUri = cursor.getString(coverUriIndex);
        int chaptersQuantity = cursor.getInt(chaptersQuantityIndex);

        Manga manga = null;
        if (isDownloaded) {
            String localUri = cursor.getString(localUriIndex);
            LocalManga localManga = new LocalManga(title, uri, repository);
            manga = localManga;
            localManga.setLocalUri(localUri);
        } else {
            manga = new Manga(title, uri, repository);
        }
        manga.setId(id);
        manga.setUri(uri);
        manga.setDescription(description);
        manga.setChaptersQuantity(chaptersQuantity);
        manga.setAuthor(author);
        manga.setCoverUri(coverUri);
        manga.setFavorite(isFavorite);
        return manga;
    }

    private static class UpgradeHandler implements DatabaseHelper.DatabaseUpgradeHandler {

        private String constraint = "repo_uri";

        @Override
        public void onUpgrade(final SQLiteDatabase database) {
            DatabaseOptions.Builder builder = new DatabaseOptions.Builder();
            builder.setName(TABLE_NAME);
            builder.addColumn(ID, DatabaseOptions.Type.INT, true, true);
            builder.addColumn(CHAPTERS_QUANTITY, DatabaseOptions.Type.INT, false, false);
            builder.addColumn(MANGA_TITLE, DatabaseOptions.Type.TEXT, false, false);
            builder.addColumn(MANGA_DESCRIPTION, DatabaseOptions.Type.TEXT, false, false);
            builder.addColumn(MANGA_AUTHOR, DatabaseOptions.Type.TEXT, false, false);
            builder.addColumn(MANGA_COVER_URI, DatabaseOptions.Type.TEXT, false, false);
            builder.addColumn(LOCAL_URI, DatabaseOptions.Type.TEXT, false, false);
            builder.addColumn(IS_FAVORITE, DatabaseOptions.Type.INT, false, false);
            builder.addColumn(IS_DOWNLOADED, DatabaseOptions.Type.INT, false, false, constraint);
            builder.addColumn(MANGA_REPOSITORY, DatabaseOptions.Type.TEXT, false, false, constraint);
            builder.addColumn(MANGA_INET_URI, DatabaseOptions.Type.TEXT, false, false, constraint);

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
