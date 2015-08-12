package com.danilov.supermanga.core.view.library;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Semyon Danilov on 02.08.2014.
 */
public class SquareImageView extends LayoutSuppressingImageView {

    /**
     * @param context The {@link Context} to use
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public SquareImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMeasure(final int widthSpec, final int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        final int mSize = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(mSize, mSize);
    }

}