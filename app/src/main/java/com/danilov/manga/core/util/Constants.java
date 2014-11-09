package com.danilov.manga.core.util;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class Constants {

    public static final long FILE_CACHE_THRESHOLD = IoUtils.convertMbToBytes(25);
    public static final long FILE_CACHE_TRIM_AMOUNT = IoUtils.convertMbToBytes(15);

    public static final String USER_AGENT_STRING = "";

    public static final String MANGA_PARCEL_KEY = "MPK";
    public static final String SELECTED_CHAPTERS_KEY = "SCK";
    public static final String REPOSITORY_KEY = "RK";
    public static final String FROM_CHAPTER_KEY = "FCK";
    public static final String FROM_PAGE_KEY = "FPK";

}
