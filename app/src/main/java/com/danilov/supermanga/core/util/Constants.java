package com.danilov.supermanga.core.util;

import android.app.AlarmManager;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class Constants {

    public static final boolean IS_MARKET_VERSION = false;
    public static final boolean HAS_ADS = false;

    public static final long FILE_CACHE_THRESHOLD = IoUtils.convertMbToBytes(25);
    public static final long FILE_CACHE_TRIM_AMOUNT = IoUtils.convertMbToBytes(15);

    public static final String USER_AGENT_STRING = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 YaBrowser/15.4.2272.3716 Safari/537.36";

    public static final String MANGA_PARCEL_KEY = "MPK";
    public static final String MANGA_CHAPTERS_DIFFERENCE = "MCD";
    public static final String SELECTED_CHAPTERS_KEY = "SCK";
    public static final String REPOSITORY_KEY = "RK";
    public static final String FROM_CHAPTER_KEY = "FCK";
    public static final String FROM_PAGE_KEY = "FPK";
    public static final String FRAGMENTS_KEY = "FK";
    public static final String SHOW_ONLINE = "SO";

    public static final String LAST_UPDATE_TIME = "LAST_UPDATE_TIME";

    public static final long UPDATES_INTERVAL = AlarmManager.INTERVAL_HALF_DAY;

    public static final String RATED = "RATED";
    public static final long VIEWER_SAVE_PERIOD = 60_000;

    public static class Settings {

        public static final String ONLINE_SETTINGS_FILENAME = "ONLINE_SETTINGS_FILENAME";
        public static final String LAST_UPDATE_PROFILE_TIME = "LAST_UPDATE_PROFILE_TIME";
        public static final String GOOGLE_PROFILE_NAME = "GOOGLE_PROFILE_NAME";


        public static final String USER_NAME = "USER_NAME";
        public static final String EMAIL = "EMAIL";
        public static final String TIME_READ = "TIME_READ";
        public static final String MANGA_DOWNLOAD_PATH = "MANGA_DOWNLOAD_PATH";
        public static final String MANGA_FINISHED = "MANGA_FINISHED";
        public static final String BYTES_DOWNLOADED = "BYTES_DOWNLOADED";
        public static final String ALWAYS_SHOW_VIEWER_BUTTONS = "ALWAYS_SHOW_VIEWER_BUTTONS";
        public static final String TUTORIAL_VIEWER_PASSED = "TUTORIAL_VIEWER_PASSED";

        public static final String[] ALL_SETTINGS = {USER_NAME, EMAIL, TIME_READ, MANGA_DOWNLOAD_PATH, MANGA_FINISHED, BYTES_DOWNLOADED, ALWAYS_SHOW_VIEWER_BUTTONS, TUTORIAL_VIEWER_PASSED};

    }

    public static class ImageRestrictions {

        //если превышает, то не грузим заранее
        public static final int MAX_SIDE_SIZE = 2500; //pxls

    }

}
