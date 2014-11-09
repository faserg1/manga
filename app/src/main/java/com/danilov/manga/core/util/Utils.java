package com.danilov.manga.core.util;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;
import com.danilov.manga.core.dialog.EasyDialog;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon Danilov on 18.05.2014.
 */
public class Utils {

    private static final String TAG = "Utils";

    public static Document toDocument(final String content) {
        return Jsoup.parse(content);
    }

    public static JSONObject toJSON(final String content) throws JSONException {
        return new JSONObject(content);
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

    public static DialogFragment easyDialogMessage(final FragmentManager fm, final boolean userClosable, final boolean hasProgress, final String title, final String message) {
        EasyDialog easyDialog = new EasyDialog();
        easyDialog.setHasProgress(hasProgress);
        easyDialog.setUserClosable(userClosable);
        easyDialog.setTextData(message);
        easyDialog.setTitle(title);
        easyDialog.show(fm, "message-dialog");
        return easyDialog;
    }

    public static DialogFragment easyDialogProgress(final FragmentManager fm, final String title, final String message) {
        EasyDialog easyDialog = new EasyDialog();
        easyDialog.setHasProgress(true);
        easyDialog.setUserClosable(false);
        easyDialog.setTextData(message);
        easyDialog.setTitle(title);
        easyDialog.show(fm, "progress-dialog");
        return easyDialog;
    }

    public static Integer stringToInt(final String s) {
        Integer integer = null;
        try {
            integer = Integer.parseInt(s);
        } catch (NumberFormatException e) {
        }
        return integer;
    }

    public static <T> ArrayList<T> listToArrayList(final List<? extends T> oldList) {
        if (oldList instanceof ArrayList) {
            return (ArrayList) oldList;
        } else {
            return new ArrayList<T>(oldList);
        }
    }

}
