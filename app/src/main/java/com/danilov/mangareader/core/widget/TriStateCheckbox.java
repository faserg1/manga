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

    private boolean isCrossed = false;
    private boolean isChecked = false;

    private static final int[] STATE_CHECKED_CROSS = {R.attr.state_checked_cross};
    private static final int[] STATE_CHECKED = {R.attr.state_checked};

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

    public void setState(final int state) {
        this.state = state;
        switch (state) {
            case UNCHECKED:
                setTriCrossed(false);
                setTriChecked(false);
                break;
            case CHECKED:
                setTriCrossed(false);
                setTriChecked(true);
                break;
            case CROSSED:
                setTriChecked(false);
                setTriCrossed(true);
                break;
        }
        refreshDrawableState();
    }

    public void setTriStateListener(final TriStateListener _listener) {
        this._listener = _listener;
    }

    @Override
    public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
        state++;
        if (state > 2) {
            state = 0;
        }
        switch (state) {
            case UNCHECKED:
                setTriCrossed(false);
                setTriChecked(false);
                break;
            case CHECKED:
                setTriCrossed(false);
                setTriChecked(true);
                break;
            case CROSSED:
                setTriCrossed(true);
                setTriChecked(false);
                break;
        }
        if (_listener != null) {
            _listener.onStateChanged(state);
        }
    }

    public void setTriChecked(final boolean isChecked) {
        this.isChecked = isChecked;
    }

    public boolean isTriChecked() {
        return isChecked;
    }

    public void onCheckChanged(TriStateCheckbox view, int state){
        this.state = state;
    }

    public boolean isTriCrossed() {
        return isCrossed;
    }

    public void setTriCrossed(final boolean isCheckedCross) {
        this.isCrossed = isCheckedCross;
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isCrossed) {
            mergeDrawableStates(drawableState, STATE_CHECKED_CROSS);
        }
        if (isChecked) {
            mergeDrawableStates(drawableState, STATE_CHECKED);
        }
        return drawableState;
    }

    public interface TriStateListener {
        public void onStateChanged(final int state);
    }

}