package com.danilov.mangareader.core.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import com.danilov.mangareader.R;

/**
 * Created by semyon on 18.12.14.
 */
public class TriStateCheckbox extends CompoundButton {

    private int state;

    public TriStateCheckbox(final Context context) {
        this(context, null);
    }

    public TriStateCheckbox(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TriStateCheckbox(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setButtonDrawable(R.drawable.tri_state_checkbox_drawable);
    }


    public static interface onCheckChangedListener{
        void onCheckChanged(TriStateCheckbox view, int state);
    }

    public void onCheckChanged(TriStateCheckbox view, int state){
        this.state = state;
    }

}