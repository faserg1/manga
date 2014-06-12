package com.danilov.manga.core.util;

import android.content.Context;
import android.widget.Toast;
import com.danilov.manga.core.application.ApplicationSettings;
import com.danilov.manga.core.model.Manga;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;

/**
 * Created by Semyon Danilov on 18.05.2014.
 */
public class Utils {

    public static final Document parseForDocument(final String content) {
        Document doc = Jsoup.parse(content);
        return doc;
    }

    public static void showToast(final Context context, final String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static String stringResource(final Context context, final int id) {
        return context.getResources().getString(id);
    }

    public static String errorMessage(final Context context, final String error, final int errorMessageId) {
        return stringResource(context, errorMessageId) + ": " + error;
    }

    public static String createPathForMangaChapter(final Manga manga, final int chapterNum, final Context context) {
        ApplicationSettings applicationSettings = ApplicationSettings.get(context);
        String downloadPath = applicationSettings.getMangaDownloadBasePath();
        File mangaFolder = new File(downloadPath + manga.getTitle() + "/" + chapterNum + "/");
        if (!mangaFolder.mkdirs()) {

        }
        return mangaFolder.getPath();
    }

}
