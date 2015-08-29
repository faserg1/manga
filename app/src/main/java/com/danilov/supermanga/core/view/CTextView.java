package com.danilov.supermanga.core.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.danilov.supermanga.core.util.Utils;

/**
 * Created by Semyon on 28.08.2015.
 */
public class CTextView extends TextView {

    public CTextView(final Context context) {
        super(context);
        init();
    }

    public CTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CTextView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CTextView(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        Typeface typeface = Utils.getTypeface("fonts/rmedium.ttf");
        if (typeface != null) {
            setTypeface(typeface);
        }
    }

}
