package com.danilov.mangareader.core.view.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Semyon Danilov on 02.08.2014.
 */
public class SquareView extends ViewGroup {

    /**
     * @param context The {@link Context} to use
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public SquareView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final View mChildren = getChildAt(0);
        mChildren.measure(widthMeasureSpec, widthMeasureSpec);
        final int mWidth = resolveSize(mChildren.getMeasuredWidth(), widthMeasureSpec);
        mChildren.measure(mWidth, mWidth);
        setMeasuredDimension(mWidth, mWidth);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onLayout(final boolean changed, final int l, final int u, final int r,
                            final int d) {
        getChildAt(0).layout(0, 0, r - l, d - u);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestLayout() {
        forceLayout();
    }

}