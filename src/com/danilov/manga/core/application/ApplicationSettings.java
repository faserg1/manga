package com.danilov.manga.core.application;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class ApplicationSettings {

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
}
