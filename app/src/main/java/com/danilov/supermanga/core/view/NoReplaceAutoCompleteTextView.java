package com.danilov.supermanga.core.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * Created by Semyon on 12.08.2015.
 */
public class NoReplaceAutoCompleteTextView extends AutoCompleteTextView {

    public NoReplaceAutoCompleteTextView(final Context context) {
        super(context);
    }

    public NoReplaceAutoCompleteTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public NoReplaceAutoCompleteTextView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NoReplaceAutoCompleteTextView(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void replaceText(final CharSequence text) {
    }
}
