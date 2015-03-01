package com.danilov.mangareader.core.application;

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
    private static final String packageName = "com.danilov.mangareader";

    private static final String DOWNLOAD_PATH_FIELD = "DPF";
    private static final String MANGA_DOWNLOAD_BASE_PATH_FIELD = "MDBPF";
    private static final String TUTORIAL_VIEWER_PASSED_FIELD = "TVPF";
    private static final String VIEWER_FULLSCREEN_FIELD = "VFF";
    private static final String FIRST_LAUNCH = "FL";
    private static final String SHOW_ADVERTISEMENT = "SA";
    private static final String TUTORIAL_MENU_PASSED_FIELD = "TMP";

    private static ApplicationSettings instance;

    private String downloadPath;

    private String mangaDownloadBasePath;

    private boolean tutorialViewerPassed;

    private boolean tutorialMenuPassed;

    private boolean firstLaunch;

    private boolean showAdvertisement;

    private boolean viewerFullscreen;

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

    public boolean isShowAdvertisement() {
        return showAdvertisement;
    }

    public void setShowAdvertisement(final boolean showAdvertisement) {
        this.showAdvertisement = showAdvertisement;
    }

    public boolean isTutorialMenuPassed() {
        return tutorialMenuPassed;
    }

    public void setTutorialMenuPassed(final boolean tutorialMenuPassed) {
        this.tutorialMenuPassed = tutorialMenuPassed;
    }

    public boolean isFirstLaunch() {
        return firstLaunch;
    }

    public void setFirstLaunch(final boolean firstLaunch) {
        this.firstLaunch = firstLaunch;
    }

    public boolean isViewerFullscreen() {
        return viewerFullscreen;
    }

    public void setViewerFullscreen(final boolean viewerFullscreen) {
        this.viewerFullscreen = viewerFullscreen;
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
        this.tutorialMenuPassed = sharedPreferences.getBoolean(TUTORIAL_MENU_PASSED_FIELD, false);
        this.firstLaunch = sharedPreferences.getBoolean(FIRST_LAUNCH, true);
        this.showAdvertisement = sharedPreferences.getBoolean(SHOW_ADVERTISEMENT, true);
        this.viewerFullscreen = sharedPreferences.getBoolean(VIEWER_FULLSCREEN_FIELD, false);
        if ("".equals(mangaDownloadBasePath)) {
            loadMangaBasePath();
        }

    }

    private void loadMangaBasePath() {
        //TODO: checking state
        File externalStorageDir = Environment.getExternalStorageDirectory();
        //{SD_PATH}/Android/data/com.danilov.mangareader/download
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
        editor.putBoolean(TUTORIAL_MENU_PASSED_FIELD, tutorialMenuPassed);
        editor.putBoolean(VIEWER_FULLSCREEN_FIELD, viewerFullscreen);
        editor.putBoolean(FIRST_LAUNCH, firstLaunch);
        editor.putBoolean(SHOW_ADVERTISEMENT, showAdvertisement);
        editor.commit();
    }

}
