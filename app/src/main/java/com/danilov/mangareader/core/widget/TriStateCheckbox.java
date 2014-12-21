package com.danilov.mangareader.core.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import com.danilov.mangareader.R;

/**
 * Created by semyon on 18.12.14.
 */
public class TriStateCheckbox extends CompoundButton implements CompoundButton.OnCheckedChangeListener{

    public static final int UNCHECKED = 0;
    public static final int CHECKED = 1;
    public static final int CROSSED = 2;

    private int state = UNCHECKED;

    private boolean isCheckedCross = false;

    private static final int[] STATE_CHECKED_CROSS = {R.attr.state_checked_cross};

    private TriStateListener _listener;

    public TriStateCheckbox(final Context context) {
        this(context, null);
    }

    public TriStateCheckbox(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TriStateCheckbox(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        setButtonDrawable(R.drawable.tri_state_checkbox_drawable);
        setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
        state++;
        if (state > 2) {
            state = 0;
        }
        switch (state) {
            case UNCHECKED:
                setChecked(false);
                setCheckedCross(false);
                break;
            case CHECKED:
                setChecked(true);
                setCheckedCross(false);
                break;
            case CROSSED:
                setChecked(false);
                setCheckedCross(true);
                break;
        }
        if (_listener != null) {
            _listener.onStateChanged(state);
        }
    }

    public void onCheckChanged(TriStateCheckbox view, int state){
        this.state = state;
    }

    public boolean isCheckedCross() {
        return isCheckedCross;
    }

    public void setCheckedCross(final boolean isCheckedCross) {
        this.isCheckedCross = isCheckedCross;
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isCheckedCross) {
            mergeDrawableStates(drawableState, STATE_CHECKED_CROSS);
        }
        return drawableState;
    }

    private interface TriStateListener {
        public void onStateChanged(final int state);
    }

}