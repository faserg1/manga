package com.danilov.manga.core.util;

import android.content.Context;
import android.widget.Toast;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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

}
