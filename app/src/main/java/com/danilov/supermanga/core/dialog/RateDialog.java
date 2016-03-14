package com.danilov.supermanga.core.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.util.Constants;

/**
 * Created by Semyon on 16.08.2015.
 */
public class RateDialog extends DialogFragment {

    public static final String TAG = "RateDialog";

    public RateDialog() {
        super();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
        builder.setPositiveButton(getString(R.string.please_rate_ok), (arg0, arg1) -> {
            Context context = MangaApplication.getContext();
            SharedPreferences preferencese = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor ed = preferencese.edit();
            ed.putBoolean(Constants.RATED, true);
            ed.apply();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setData(Uri.parse("market://details?id=com.danilov.supermanga"));
            try {
                context.startActivity(i);
            } catch (Exception e) {
                //маркет не установлен
            }
            dismiss();
        });
        builder.setNegativeButton(getString(R.string.please_rate_forget_about_it), (dialogInterface, i) -> {
            Context context = MangaApplication.getContext();
            SharedPreferences preferencese = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor ed = preferencese.edit();
            ed.putBoolean(Constants.RATED, true);
            ed.apply();
            dismiss();
        });
        builder.setNeutralButton(getString(R.string.please_rate_later), (dialogInterface, i) -> {
            dismiss();
        });
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View contentView = layoutInflater.inflate(R.layout.rate_dialog, null);
        builder.setView(contentView);
        builder.setTitle(getString(R.string.please_rate_title));
        return builder.build();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }


}
