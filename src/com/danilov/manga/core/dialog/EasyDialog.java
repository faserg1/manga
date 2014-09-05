package com.danilov.manga.core.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Semyon Danilov on 05.09.2014.
 */

/**
 * Helper class for dialog instantiation
 * By default dialog is not progress and user cant close it
 * by touching the screen.
 *
 * Use {link} setCustomView to set the layout for dialog
 * By default it's just TextView with ProgressBar (determinant)
 *
 */
public class EasyDialog extends DialogFragment {

    private static final String TEXT_KEY = "TK";
    private static final String HAS_PROGRESS_KEY = "HPK";
    private static final String USER_CLOSABLE_KEY = "UCK";
    private static final String VIEW_ID_KEY = "VIK";

    private boolean userClosable = false;

    private String textData = "";

    private boolean hasProgress = false;

    private int viewId;

    public EasyDialog() {

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TEXT_KEY, textData);
        outState.putBoolean(USER_CLOSABLE_KEY, userClosable);
        outState.putBoolean(HAS_PROGRESS_KEY, hasProgress);
        outState.putInt(VIEW_ID_KEY, viewId);
    }

    private void setCustomView(final int viewId) {

    }

}
