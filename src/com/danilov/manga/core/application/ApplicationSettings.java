package com.danilov.manga.core.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class ApplicationSettings {

    private static final String TAG = "ApplicationSettings";

    private static final String DOWNLOAD_PATH_FIELD = "DPF";
    private static final String MANGA_DOWNLOAD_BASE_PATH_FIELD = "MDBPF";

    private static ApplicationSettings instance;

    private String downloadPath;

    private String mangaDownloadBasePath;

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(final String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public String getMangaDownloadBasePath() {
        return mangaDownloadBasePath;
    }

    public void setMangaDownloadBasePath(final String mangaDownloadBasePath) {
        this.mangaDownloadBasePath = mangaDownloadBasePath;
    }

    public static ApplicationSettings get(final Context context) {
        if (instance == null) {
            instance = new ApplicationSettings(context);
        }
        return instance;
    }

    private ApplicationSettings(final Context context) {
        load(context);
    }

    private void load(final Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        this.downloadPath = sharedPreferences.getString(DOWNLOAD_PATH_FIELD, "");
        this.mangaDownloadBasePath = sharedPreferences.getString(MANGA_DOWNLOAD_BASE_PATH_FIELD, "");
        if ("".equals(mangaDownloadBasePath)) {
            File sdPath = Environment.getExternalStorageDirectory();
            mangaDownloadBasePath = sdPath.getPath() + "/manga/download/";
            sdPath = new File(mangaDownloadBasePath);
            if (!sdPath.mkdirs()) {
                Log.d(TAG, "WTF??!");
            }
        }
    }

}
