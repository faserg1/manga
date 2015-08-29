package com.danilov.supermanga.core.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

/**
 * Created by Semyon on 28.08.2015.
 */
public class UnderToolbarScrollView extends ScrollView {

    private UnderToolbarScrollListener listener;

    public UnderToolbarScrollView(final Context context) {
        super(context);
    }

    public UnderToolbarScrollView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public UnderToolbarScrollView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UnderToolbarScrollView(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setListener(final UnderToolbarScrollListener listener) {
        this.listener = listener;
    }

    @Override
    public void scrollTo(final int x, final int y) {
        if (listener != null) {
            listener.onScroll(x, y);
        }
        super.scrollTo(x, y);
    }

//    @Override
//    protected boolean overScrollBy(final int deltaX, final int deltaY, final int scrollX, final int scrollY, final int scrollRangeX, final int scrollRangeY, final int maxOverScrollX, final int maxOverScrollY, final boolean isTouchEvent) {
//        int y = deltaY + scrollY;
//        int valueToScroll = listener == null ? y : listener.onScroll(scrollX, y);
//        int newDeltaY = valueToScroll - scrollY;
//        return super.overScrollBy(deltaX, newDeltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
//    }

    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        if (listener != null) {
            listener.onScroll(l, t);
        }
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public static interface UnderToolbarScrollListener{

        public int onScroll(final int x, final int y);

    }

}
