package com.danilov.supermanga.core.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.app.DialogFragment;

/**
 * Created by Semyon on 09.11.2014.
 */
public class CustomDialogFragment extends DialogFragment {

    private Dialog dialog;

    private boolean dismissOnDestroy;

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

    public boolean isDismissOnDestroy() {
        return dismissOnDestroy;
    }

    public void setDismissOnDestroy(final boolean dismissOnDestroy) {
        this.dismissOnDestroy = dismissOnDestroy;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        if (dismissOnDestroy) {
            dismiss();
        }
        super.onPause();
    }
}
