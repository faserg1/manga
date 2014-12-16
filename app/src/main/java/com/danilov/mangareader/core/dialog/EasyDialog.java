package com.danilov.mangareader.core.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.danilov.mangareader.R;

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
    private static final String TITLE_KEY = "TTK";
    private static final String HAS_PROGRESS_KEY = "HPK";
    private static final String USER_CLOSABLE_KEY = "UCK";
    private static final String VIEW_ID_KEY = "VIK";

    private boolean userClosable = false;

    private String textData = "";

    private String title = "";

    private boolean hasProgress = false;

    private int viewId = -1;

    public EasyDialog() {
        super();
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        if(savedInstanceState != null){
            restoreSavedInstanceState(savedInstanceState);
        }
        CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
        if (userClosable) {
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    dismiss();
                }

            });
        }
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        if (hasProgress) {
            viewId = R.layout.dialog_progress;
        } else {
            if (viewId == -1) {
                viewId = R.layout.dialog_message;
            }
        }
        View contentView = layoutInflater.inflate(viewId, null);
        builder.setView(contentView);
        builder.setTitle(title);
        fillView(contentView);
        return builder.build();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!userClosable) {
            setCancelable(false);
        }
    }

    private void fillView(final View contentView) {
        TextView textView = (TextView) contentView.findViewById(R.id.text);
        if (textView != null) {
            textView.setText(textData);
        }
    }

    private void restoreSavedInstanceState(final Bundle savedInstanceState) {
        userClosable = savedInstanceState.getBoolean(USER_CLOSABLE_KEY, true);
        hasProgress = savedInstanceState.getBoolean(HAS_PROGRESS_KEY, true);
        viewId = savedInstanceState.getInt(VIEW_ID_KEY);
        textData = savedInstanceState.getString(TEXT_KEY);
        title = savedInstanceState.getString(TITLE_KEY);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TEXT_KEY, textData);
        outState.putString(TITLE_KEY, title);
        outState.putBoolean(USER_CLOSABLE_KEY, userClosable);
        outState.putBoolean(HAS_PROGRESS_KEY, hasProgress);
        outState.putInt(VIEW_ID_KEY, viewId);
    }

    private void setCustomView(final int viewId) {
        this.viewId = viewId;
    }

    public void setUserClosable(final boolean userClosable) {
        this.userClosable = userClosable;
    }

    public void setTextData(final String textData) {
        this.textData = textData;
    }

    public void setHasProgress(final boolean hasProgress) {
        this.hasProgress = hasProgress;
    }

    public void setTitle(final String title) {
        this.title = title;
    }


    public class EasyAlertDialog extends AlertDialog {

        protected EasyAlertDialog(final Context context) {
            super(context);
        }

        protected EasyAlertDialog(final Context context, final int theme) {
            super(context, theme);
        }

        protected EasyAlertDialog(final Context context, final boolean cancelable, final OnCancelListener cancelListener) {
            super(context, cancelable, cancelListener);
        }

    }

    public class EasyDialogBuilder extends AlertDialog.Builder {

        private int theme = 0;
        private Context context;

        private CharSequence mNeutralButtonText;
        private String title;
        private DialogInterface.OnClickListener mNeutralButtonListener;
        private View view;

        public EasyDialogBuilder(final Context context) {
            this(context, 0);
        }

        public EasyDialogBuilder(final Context context, final int theme) {
            super(context);
            this.theme = theme;
            this.context = context;
        }

        public EasyDialogBuilder setNeutralButton(final CharSequence text, final DialogInterface.OnClickListener listener) {
            this.mNeutralButtonText = text;
            this.mNeutralButtonListener = listener;
            return this;
        }

        @NonNull
        @Override
        public EasyDialogBuilder setView(final View view) {
            this.view = view;
            return this;
        }

        public void setTitle(final String title) {
            this.title = title;
        }

        @NonNull
        @Override
        public AlertDialog create() {
            EasyAlertDialog alertDialog = new EasyAlertDialog(context, theme);
            if (mNeutralButtonText != null) {
                alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, mNeutralButtonText,
                        mNeutralButtonListener);
            }
            alertDialog.setView(view);
            alertDialog.setTitle(title);
            return alertDialog;
        }
    }

}
