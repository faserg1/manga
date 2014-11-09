package com.danilov.manga.core.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by Semyon on 09.11.2014.
 */
public class CustomDialogFragment extends DialogFragment {

    private Dialog dialog;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return dialog;
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void setDialog(final Dialog dialog) {
        this.dialog = dialog;
    }

}
