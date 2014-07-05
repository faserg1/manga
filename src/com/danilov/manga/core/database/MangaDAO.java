package com.danilov.manga.core.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;

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
    private static final String MANGA_AUTHOR = "manga_author";
    private static final String MANGA_URI = "manga_uri";

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
        databaseHelper = new DatabaseHelper(dbPath, DAOVersion, new FirstOpenHandler());
        try {
            SQLiteDatabase db = databaseHelper.openWritable();
            ContentValues cv = new ContentValues();
            cv.put(ID, 0);
            cv.put(CHAPTERS_QUANTITY, 25);
            cv.put(MANGA_TITLE, "123");
            cv.put(MANGA_DESCRIPTION, "zxczx");
            cv.put(MANGA_AUTHOR, "qwe");
            cv.put(MANGA_URI, "asd");
            db.insertOrThrow(TABLE_NAME, null, cv);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




    private static class FirstOpenHandler implements DatabaseHelper.DatabaseFirstOpenHandler {

        @Override
        public void onFirstOpen(final SQLiteDatabase database) {
            DatabaseOptions.Builder builder = new DatabaseOptions.Builder();
            builder.setName(TABLE_NAME);
            builder.addColumn(ID, DatabaseOptions.Type.INT, true, true);
            builder.addColumn(CHAPTERS_QUANTITY, DatabaseOptions.Type.INT, false, false);
            builder.addColumn(MANGA_TITLE, DatabaseOptions.Type.TEXT, false, false);
            builder.addColumn(MANGA_DESCRIPTION, DatabaseOptions.Type.TEXT, false, false);
            builder.addColumn(MANGA_AUTHOR, DatabaseOptions.Type.TEXT, false, false);
            builder.addColumn(MANGA_URI, DatabaseOptions.Type.TEXT, false, false);
            DatabaseOptions options = builder.build();
            String sqlStatement = options.toSQLStatement();
            try {
                database.execSQL(sqlStatement);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
