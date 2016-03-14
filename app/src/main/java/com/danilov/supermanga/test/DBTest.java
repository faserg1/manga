package com.danilov.supermanga.test;

import android.os.Environment;
import android.util.Log;

import com.annimon.stream.function.BiFunction;
import com.annimon.stream.function.Function;
import com.danilov.supermanga.core.application.ApplicationSettings;
import com.danilov.supermanga.core.database.Database;
import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.DatabaseHelper;
import com.danilov.supermanga.core.database.DatabaseOptions;
import com.danilov.supermanga.core.util.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Semyon on 09.08.2015.
 */
public class DBTest {

    private static final Logger LOGGER = new Logger(DBTest.class);

    private final static String TAG = "HistoryDAO";
    private static final String packageName = ApplicationSettings.PACKAGE_NAME;

    private static final int DAOVersion = 1;
    private static final String TABLE_NAME = "testdb";
    private static final String DB_NAME = "test.db";

    private static final String ID = "id";
    private static final String MANGA_ID = "manga_id";
    private static final String CHAPTER = "chapter";
    private static final String PAGE = "page";
    private static final String DATE = "date";
    private static final String IS_ONLINE = "is_online";

    public DatabaseHelper databaseHelper = null;

    private final Executor executor = Executors.newSingleThreadExecutor();

    public static void test() {
        final DatabaseHelper databaseHelper = new DBTest().getDatabaseHelper();
        new Thread() {
            @Override
            public void run() {
                Database database = null;
                try {
                    database = databaseHelper.openWritable();
                } catch (DatabaseAccessException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (database != null) {
                    database.close();
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                Database database = null;
                try {
                    database = databaseHelper.openWritable();
                } catch (DatabaseAccessException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (database != null) {
                    database.close();
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                Database database = null;
                try {
                    database = databaseHelper.openWritable();
                } catch (DatabaseAccessException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(12000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (database != null) {
                    database.close();
                }
            }
        }.start();
    }

    public DBTest() {
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

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

}