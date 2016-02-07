package com.danilov.supermanga.core.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.danilov.supermanga.R;

/**
 * Created by Semyon on 07.02.2016.
 */
public class ToggleImageButton extends ImageButton {

    private boolean isChecked;

    private OnClickListener onClickListener;

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };

    public ToggleImageButton(final Context context) {
        super(context);
    }

    public ToggleImageButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ToggleImageButton(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ToggleImageButton(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr);
    }

    private void init(final AttributeSet attrs, final int defStyle) {
        super.setOnClickListener(new OnClickListener() {

            private boolean broadcasting = false;

            @Override
            public void onClick(final View v) {
                if (broadcasting) {//kill the recursion
                    return;
                }
                broadcasting = true;
                toggle();
                if (onClickListener != null) {
                    onClickListener.onClick(v);
                }
                if (onCheckedChangeListener != null) {
                    onCheckedChangeListener.onCheckedChanged(null, isChecked);
                }
                broadcasting = false;
            }
        });
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(final boolean isChecked) {
        this.isChecked = isChecked;
        refreshDrawableState();
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable drawable = getDrawable();
        if (drawable != null) {
            int[] myDrawableState = getDrawableState();

            // Set the state of the Drawable
            drawable.setState(myDrawableState);

            invalidate();
        }
    }

    private void toggle() {
        setIsChecked(!isChecked);
    }

    @Override
    public void setOnClickListener(final OnClickListener l) {
        onClickListener = l;
    }

    public void setOnCheckedChangeListener(final CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

}