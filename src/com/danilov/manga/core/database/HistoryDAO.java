package com.danilov.manga.core.database;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;

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
