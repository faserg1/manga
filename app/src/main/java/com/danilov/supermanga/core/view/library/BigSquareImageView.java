package com.danilov.supermanga.core.view.library;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Semyon Danilov on 02.08.2014.
 */
public class BigSquareImageView extends LayoutSuppressingImageView {

    /**
     * @param context The {@link android.content.Context} to use
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public BigSquareImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMeasure(final int widthSpec, final int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        final int mSize = Math.max(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(mSize, mSize);
    }

}