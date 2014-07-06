package com.danilov.manga.core.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.repository.RepositoryEngine.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon Danilov on 04.07.2014.
 */
public class MangaDAO {

    private final static String TAG = "MangaDAO";

    private static final int DAOVersion = 1;
    private static final String TABLE_NAME = "manga";

    private static final String ID = "id";
    private static final String CHAPTERS_QUANTITY = "chapters_quantity";
    private static final String MANGA_TITLE = "manga_title";
    private static final String MANGA_DESCRIPTION = "manga_description";
    private static final String MANGA_REPOSITORY = "manga_repository";
    private static final String MANGA_AUTHOR = "manga_author";
    private static final String MANGA_URI = "manga_uri";
    private static final String MANGA_INET_URI = "manga_inet_uri";

    public static DatabaseHelper databaseHelper = null;

    static {
        String sdPath = Environment.getExternalStorageDirectory().getPath();
        String dbFolder = sdPath + "/manga/db/";
        File file = new File(dbFolder);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (!created) {
                Log.e(TAG, "Can't create file");
            }
        }
        String dbPath = dbFolder + "/manga.db";
        databaseHelper = new DatabaseHelper(dbPath, DAOVersion, new UpgradeHandler());
        try {
            SQLiteDatabase db = databaseHelper.openWritable();
            db.execSQL("drop table if exists " + TABLE_NAME + ";");
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void addManga(final Manga manga, final String localUri) throws DatabaseException {
        SQLiteDatabase db = databaseHelper.openWritable();
        ContentValues cv = new ContentValues();
        cv.put(MANGA_TITLE, manga.getTitle());
        cv.put(MANGA_DESCRIPTION, manga.getDescription());
        cv.put(MANGA_AUTHOR, manga.getAuthor());
        cv.put(MANGA_REPOSITORY, manga.getRepository().toString());
        cv.put(MANGA_URI, localUri);
        cv.put(MANGA_INET_URI, manga.getUri());
        try {
            db.insertOrThrow(TABLE_NAME, null, cv);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            throw new DatabaseException(e.getMessage());
        } finally {
            db.close();
        }
    }

    public static List<Manga> getAllManga() throws DatabaseException{
        SQLiteDatabase db = databaseHelper.openReadable();
        List<Manga> mangaList = new ArrayList<Manga>();
        try {
            Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (!cursor.moveToFirst()) {
                return null;
            }
            int titleIndex = cursor.getColumnIndex(MANGA_TITLE);
            int descriptionIndex = cursor.getColumnIndex(MANGA_DESCRIPTION);
            int authorIndex = cursor.getColumnIndex(MANGA_AUTHOR);
            int repositoryIndex = cursor.getColumnIndex(MANGA_REPOSITORY);
            int uriIndex = cursor.getColumnIndex(MANGA_URI);
            int inetUriIndex = cursor.getColumnIndex(MANGA_INET_URI);
            do {
                String title = cursor.getString(titleIndex);
                String description = cursor.getString(descriptionIndex);
                String author = cursor.getString(authorIndex);
                Repository repository = Repository.valueOf(cursor.getString(repositoryIndex));
                String uri = cursor.getString(uriIndex);
                String inetUri = cursor.getString(inetUriIndex);
                Manga manga = new Manga(title, inetUri, repository);
                manga.setDescription(description);
                manga.setAuthor(author);
                manga.setLocalUri(uri);
                mangaList.add(manga);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
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
            builder.addColumn(CHAPTERS_QUANTITY, DatabaseOptions.Type.INT, false, false);
            builder.addColumn(MANGA_TITLE, DatabaseOptions.Type.TEXT, false, false);
            builder.addColumn(MANGA_DESCRIPTION, DatabaseOptions.Type.TEXT, false, false);
            builder.addColumn(MANGA_REPOSITORY, DatabaseOptions.Type.TEXT, false, false);
            builder.addColumn(MANGA_AUTHOR, DatabaseOptions.Type.TEXT, false, false);
            builder.addColumn(MANGA_URI, DatabaseOptions.Type.TEXT, false, false);
            builder.addColumn(MANGA_INET_URI, DatabaseOptions.Type.TEXT, false, false);
            DatabaseOptions options = builder.build();
            String sqlStatement = options.toSQLStatement();
            try {
                database.execSQL("drop table if exists " + TABLE_NAME + ";");
                database.execSQL(sqlStatement);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
