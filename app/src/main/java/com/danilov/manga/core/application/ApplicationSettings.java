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
    private static final String packageName = "com.danilov.manga";

    private static final String DOWNLOAD_PATH_FIELD = "DPF";
    private static final String MANGA_DOWNLOAD_BASE_PATH_FIELD = "MDBPF";
    private static final String TUTORIAL_VIEWER_PASSED_FIELD = "TVPF";

    private static ApplicationSettings instance;

    private String downloadPath;

    private String mangaDownloadBasePath;

    private boolean tutorialViewerPassed;

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

    public boolean isTutorialViewerPassed() {
        return tutorialViewerPassed;
    }

    public void setTutorialViewerPassed(final boolean tutorialViewerPassed) {
        this.tutorialViewerPassed = tutorialViewerPassed;
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
        this.tutorialViewerPassed = sharedPreferences.getBoolean(TUTORIAL_VIEWER_PASSED_FIELD, false);
        if ("".equals(mangaDownloadBasePath)) {
            loadMangaBasePath();
        }

    }

    private void loadMangaBasePath() {
        //TODO: checking state
        File externalStorageDir = Environment.getExternalStorageDirectory();
        //{SD_PATH}/Android/data/com.danilov.manga/download
        File extStorageAppCachePath = new File(externalStorageDir, "Android" + File.separator + "data" + File.separator + packageName + File.separator + "download");
        mangaDownloadBasePath = extStorageAppCachePath.getPath();
        File path = new File(mangaDownloadBasePath);
        if (!path.mkdirs() && !path.exists()) {
            Log.d(TAG, "Failure on creation of " + path.toString() + " path");
        }
    }

    public void update(final Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DOWNLOAD_PATH_FIELD, downloadPath);
        editor.putString(MANGA_DOWNLOAD_BASE_PATH_FIELD, mangaDownloadBasePath);
        editor.putBoolean(TUTORIAL_VIEWER_PASSED_FIELD, tutorialViewerPassed);
        editor.commit();
    }

}