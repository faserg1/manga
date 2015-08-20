package com.danilov.supermanga.core.util;

import android.app.AlarmManager;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class Constants {

    public static final boolean IS_MARKET_VERSION = true;

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

    public static class ImageRestrictions {

        //если превышает, то не грузим заранее
        public static final int MAX_SIDE_SIZE = 2500; //pxls

    }

}
