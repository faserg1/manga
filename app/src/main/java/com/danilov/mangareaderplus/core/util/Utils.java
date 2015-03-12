package com.danilov.mangareaderplus.core.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.danilov.mangareaderplus.core.dialog.EasyDialog;
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


    /**
     * Create a color integer value with specified alpha.
     * This may be useful to change alpha value of background color.
     *
     * @param alpha     alpha value from 0.0f to 1.0f.
     * @param baseColor base color. alpha value will be ignored.
     * @return a color with alpha made from base color
     */
    public static int getColorWithAlpha(float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        return a + rgb;
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

    public static int getNavigationBarHeight(final Context context, final int orientation) {
        try {
            Resources resources = context.getResources();
            int id = resources.getIdentifier(
                    orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape",
                    "dimen", "android");
            if (id > 0) {
                return resources.getDimensionPixelSize(id);
            }
        } catch (NullPointerException | IllegalArgumentException | Resources.NotFoundException e) {
            return 0;
        }
        return 0;
    }

    public static RelativeLayout.LayoutParams getRightParam(final Context context, final Resources resources) {
        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        int margin = ((Number) (resources.getDisplayMetrics().density * 12)).intValue();
        lps.setMargins(margin, margin, margin, margin + getNavigationBarHeight(context, resources.getConfiguration().orientation));
        return lps;
    }

}
